package com.snippetia.service

import com.snippetia.dto.*
import com.snippetia.model.*
import com.snippetia.repository.*
import com.snippetia.exception.ResourceNotFoundException
import com.snippetia.exception.BusinessException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class ModerationService(
    private val codeSnippetRepository: CodeSnippetRepository,
    private val userRepository: UserRepository,
    private val commentRepository: CommentRepository,
    private val moderationActionRepository: ModerationActionRepository,
    private val notificationService: NotificationService
) {

    fun reportContent(userId: Long, request: ReportContentRequest): ReportResponse {
        val reporter = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        val moderationAction = ModerationAction(
            reporter = reporter,
            contentType = ContentType.valueOf(request.contentType.uppercase()),
            contentId = request.contentId,
            reason = ModerationReason.valueOf(request.reason.uppercase()),
            description = request.description,
            status = ModerationStatus.PENDING
        )

        val savedAction = moderationActionRepository.save(moderationAction)
        return mapToReportResponse(savedAction)
    }

    fun getPendingReports(pageable: Pageable): Page<ReportResponse> {
        return moderationActionRepository.findByStatusOrderByCreatedAtDesc(ModerationStatus.PENDING, pageable)
            .map { mapToReportResponse(it) }
    }

    fun reviewReport(moderatorId: Long, reportId: Long, decision: ModerationDecision): ReportResponse {
        val moderator = userRepository.findById(moderatorId)
            .orElseThrow { ResourceNotFoundException("Moderator not found") }

        val report = moderationActionRepository.findById(reportId)
            .orElseThrow { ResourceNotFoundException("Report not found") }

        report.moderator = moderator
        report.decision = decision
        report.status = when (decision) {
            ModerationDecision.APPROVED -> ModerationStatus.RESOLVED
            ModerationDecision.REJECTED -> ModerationStatus.DISMISSED
            ModerationDecision.REQUIRES_ACTION -> ModerationStatus.ACTION_TAKEN
        }
        report.reviewedAt = LocalDateTime.now()

        // Take action based on decision
        when (decision) {
            ModerationDecision.REQUIRES_ACTION -> {
                takeContentAction(report)
            }
            ModerationDecision.REJECTED -> {
                // Content is fine, notify reporter
                notificationService.createNotification(
                    userId = report.reporter.id!!,
                    type = NotificationType.MODERATION_UPDATE,
                    title = "Report Reviewed",
                    message = "Your report has been reviewed and no action was taken",
                    actionUrl = "/reports/${report.id}"
                )
            }
            ModerationDecision.APPROVED -> {
                // Report was valid but no action needed
                notificationService.createNotification(
                    userId = report.reporter.id!!,
                    type = NotificationType.MODERATION_UPDATE,
                    title = "Report Acknowledged",
                    message = "Thank you for your report. We've taken note of the issue",
                    actionUrl = "/reports/${report.id}"
                )
            }
        }

        val savedReport = moderationActionRepository.save(report)
        return mapToReportResponse(savedReport)
    }

    fun hideContent(moderatorId: Long, contentType: ContentType, contentId: Long, reason: String): Boolean {
        val moderator = userRepository.findById(moderatorId)
            .orElseThrow { ResourceNotFoundException("Moderator not found") }

        return when (contentType) {
            ContentType.SNIPPET -> {
                val snippet = codeSnippetRepository.findById(contentId)
                    .orElseThrow { ResourceNotFoundException("Snippet not found") }
                
                snippet.isHidden = true
                snippet.moderationReason = reason
                codeSnippetRepository.save(snippet)

                // Notify author
                notificationService.createNotification(
                    userId = snippet.author.id!!,
                    type = NotificationType.CONTENT_MODERATED,
                    title = "Content Hidden",
                    message = "Your snippet has been hidden due to: $reason",
                    actionUrl = "/snippets/${snippet.id}"
                )
                true
            }
            ContentType.COMMENT -> {
                val comment = commentRepository.findById(contentId)
                    .orElseThrow { ResourceNotFoundException("Comment not found") }
                
                comment.isHidden = true
                comment.moderationReason = reason
                commentRepository.save(comment)

                // Notify author
                notificationService.createNotification(
                    userId = comment.author.id!!,
                    type = NotificationType.CONTENT_MODERATED,
                    title = "Comment Hidden",
                    message = "Your comment has been hidden due to: $reason",
                    actionUrl = "/snippets/${comment.snippet.id}"
                )
                true
            }
            else -> false
        }
    }

    fun suspendUser(moderatorId: Long, userId: Long, duration: Int, reason: String): Boolean {
        val moderator = userRepository.findById(moderatorId)
            .orElseThrow { ResourceNotFoundException("Moderator not found") }

        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        user.isSuspended = true
        user.suspensionReason = reason
        user.suspendedUntil = LocalDateTime.now().plusDays(duration.toLong())
        userRepository.save(user)

        // Create moderation record
        val moderationAction = ModerationAction(
            moderator = moderator,
            contentType = ContentType.USER,
            contentId = userId,
            reason = ModerationReason.POLICY_VIOLATION,
            description = reason,
            status = ModerationStatus.ACTION_TAKEN,
            decision = ModerationDecision.REQUIRES_ACTION
        )
        moderationActionRepository.save(moderationAction)

        // Notify user
        notificationService.createNotification(
            userId = userId,
            type = NotificationType.ACCOUNT_SUSPENDED,
            title = "Account Suspended",
            message = "Your account has been suspended for $duration days. Reason: $reason",
            priority = NotificationPriority.HIGH
        )

        return true
    }

    fun getModerationStats(): ModerationStatsResponse {
        val pendingReports = moderationActionRepository.countByStatus(ModerationStatus.PENDING)
        val resolvedToday = moderationActionRepository.countResolvedToday(LocalDateTime.now().toLocalDate())
        val totalReports = moderationActionRepository.count()

        return ModerationStatsResponse(
            pendingReports = pendingReports,
            resolvedToday = resolvedToday,
            totalReports = totalReports
        )
    }

    fun getUserReports(userId: Long, pageable: Pageable): Page<ReportResponse> {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        return moderationActionRepository.findByReporterOrderByCreatedAtDesc(user, pageable)
            .map { mapToReportResponse(it) }
    }

    private fun takeContentAction(report: ModerationAction) {
        when (report.reason) {
            ModerationReason.SPAM -> hideContent(
                report.moderator?.id ?: 1L,
                report.contentType,
                report.contentId,
                "Content marked as spam"
            )
            ModerationReason.INAPPROPRIATE_CONTENT -> hideContent(
                report.moderator?.id ?: 1L,
                report.contentType,
                report.contentId,
                "Inappropriate content"
            )
            ModerationReason.HARASSMENT -> {
                hideContent(
                    report.moderator?.id ?: 1L,
                    report.contentType,
                    report.contentId,
                    "Content removed for harassment"
                )
                // Could also suspend user for repeated harassment
            }
            ModerationReason.COPYRIGHT_VIOLATION -> hideContent(
                report.moderator?.id ?: 1L,
                report.contentType,
                report.contentId,
                "Copyright violation"
            )
            ModerationReason.POLICY_VIOLATION -> hideContent(
                report.moderator?.id ?: 1L,
                report.contentType,
                report.contentId,
                "Policy violation"
            )
            else -> {
                // Default action
                hideContent(
                    report.moderator?.id ?: 1L,
                    report.contentType,
                    report.contentId,
                    "Content moderated"
                )
            }
        }
    }

    private fun mapToReportResponse(moderationAction: ModerationAction): ReportResponse {
        return ReportResponse(
            id = moderationAction.id!!,
            reporter = UserSummaryResponse(
                id = moderationAction.reporter.id!!,
                username = moderationAction.reporter.username,
                displayName = moderationAction.reporter.displayName,
                avatarUrl = moderationAction.reporter.avatarUrl
            ),
            moderator = moderationAction.moderator?.let {
                UserSummaryResponse(
                    id = it.id!!,
                    username = it.username,
                    displayName = it.displayName,
                    avatarUrl = it.avatarUrl
                )
            },
            contentType = moderationAction.contentType.name,
            contentId = moderationAction.contentId,
            reason = moderationAction.reason.name,
            description = moderationAction.description,
            status = moderationAction.status.name,
            decision = moderationAction.decision?.name,
            createdAt = moderationAction.createdAt,
            reviewedAt = moderationAction.reviewedAt
        )
    }
}