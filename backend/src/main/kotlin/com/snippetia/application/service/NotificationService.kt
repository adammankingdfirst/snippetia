package com.snippetia.application.service

import com.snippetia.domain.entity.*
import com.snippetia.domain.repository.NotificationRepository
import com.snippetia.application.dto.NotificationResponse
import com.snippetia.application.dto.CreateNotificationRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
@Transactional
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val emailService: EmailService,
    private val pushNotificationService: PushNotificationService
) {

    fun createNotification(request: CreateNotificationRequest): NotificationResponse {
        val notification = Notification(
            user = request.user,
            title = request.title,
            message = request.message,
            type = request.type,
            referenceId = request.referenceId,
            referenceType = request.referenceType,
            actionUrl = request.actionUrl,
            priority = request.priority,
            expiresAt = request.expiresAt,
            metadata = request.metadata
        )
        
        val savedNotification = notificationRepository.save(notification)
        
        // Send to message queue for async processing
        kafkaTemplate.send("notifications", savedNotification.id.toString(), savedNotification)
        
        return savedNotification.toResponse()
    }

    fun getUserNotifications(userId: Long, pageable: Pageable): Page<NotificationResponse> {
        val user = User(id = userId, email = "", username = "", password = "")
        return notificationRepository.findByUser(user, pageable)
            .map { it.toResponse() }
    }

    fun getUnreadNotifications(userId: Long, pageable: Pageable): Page<NotificationResponse> {
        val user = User(id = userId, email = "", username = "", password = "")
        return notificationRepository.findUnreadNotificationsByUser(user, pageable)
            .map { it.toResponse() }
    }

    fun getUnreadCount(userId: Long): Long {
        val user = User(id = userId, email = "", username = "", password = "")
        return notificationRepository.countUnreadNotifications(user)
    }

    fun markAsRead(notificationId: Long) {
        notificationRepository.markAsRead(notificationId, LocalDateTime.now())
    }

    fun markAllAsRead(userId: Long) {
        val user = User(id = userId, email = "", username = "", password = "")
        notificationRepository.markAllAsRead(user, LocalDateTime.now())
    }

    // Specific notification methods
    fun sendSnippetLikedNotification(snippetOwner: User, liker: User, snippet: CodeSnippet) {
        val notification = CreateNotificationRequest(
            user = snippetOwner,
            title = "Your snippet was liked!",
            message = "${liker.getDisplayName()} liked your snippet \"${snippet.title}\"",
            type = NotificationType.SNIPPET_LIKED,
            referenceId = snippet.id,
            referenceType = "snippet",
            actionUrl = "/snippets/${snippet.id}",
            priority = NotificationPriority.NORMAL
        )
        createNotification(notification)
    }

    fun sendSnippetForkedNotification(snippetOwner: User, forker: User, snippet: CodeSnippet) {
        val notification = CreateNotificationRequest(
            user = snippetOwner,
            title = "Your snippet was forked!",
            message = "${forker.getDisplayName()} forked your snippet \"${snippet.title}\"",
            type = NotificationType.SNIPPET_FORKED,
            referenceId = snippet.id,
            referenceType = "snippet",
            actionUrl = "/snippets/${snippet.id}",
            priority = NotificationPriority.NORMAL
        )
        createNotification(notification)
    }

    fun sendSnippetCommentedNotification(snippetOwner: User, commenter: User, snippet: CodeSnippet, comment: Comment) {
        val notification = CreateNotificationRequest(
            user = snippetOwner,
            title = "New comment on your snippet",
            message = "${commenter.getDisplayName()} commented on your snippet \"${snippet.title}\"",
            type = NotificationType.SNIPPET_COMMENTED,
            referenceId = comment.id,
            referenceType = "comment",
            actionUrl = "/snippets/${snippet.id}#comment-${comment.id}",
            priority = NotificationPriority.NORMAL
        )
        createNotification(notification)
    }

    fun sendFollowNotification(followed: User, follower: User) {
        val notification = CreateNotificationRequest(
            user = followed,
            title = "New follower!",
            message = "${follower.getDisplayName()} started following you",
            type = NotificationType.USER_FOLLOWED,
            referenceId = follower.id,
            referenceType = "user",
            actionUrl = "/users/${follower.username}",
            priority = NotificationPriority.NORMAL
        )
        createNotification(notification)
    }

    fun sendSupportReceivedNotification(recipient: User, supporter: User, amount: BigDecimal, isAnonymous: Boolean) {
        val supporterName = if (isAnonymous) "Someone" else supporter.getDisplayName()
        val notification = CreateNotificationRequest(
            user = recipient,
            title = "You received support!",
            message = "$supporterName supported you with $${amount}",
            type = NotificationType.SUPPORT_RECEIVED,
            referenceId = supporter.id,
            referenceType = "support",
            actionUrl = "/dashboard/support",
            priority = NotificationPriority.HIGH
        )
        createNotification(notification)
    }

    fun sendCollaborationInviteNotification(invitee: User, inviter: User, snippet: CodeSnippet) {
        val notification = CreateNotificationRequest(
            user = invitee,
            title = "Collaboration invitation",
            message = "${inviter.getDisplayName()} invited you to collaborate on \"${snippet.title}\"",
            type = NotificationType.COLLABORATION_INVITE,
            referenceId = snippet.id,
            referenceType = "snippet",
            actionUrl = "/snippets/${snippet.id}/collaborate",
            priority = NotificationPriority.HIGH
        )
        createNotification(notification)
    }

    fun sendExecutionCompletedNotification(user: User, snippet: CodeSnippet, execution: SnippetExecution) {
        val notification = CreateNotificationRequest(
            user = user,
            title = "Code execution completed",
            message = "Your execution of \"${snippet.title}\" has completed",
            type = NotificationType.SNIPPET_EXECUTION_COMPLETED,
            referenceId = execution.id,
            referenceType = "execution",
            actionUrl = "/executions/${execution.id}",
            priority = NotificationPriority.NORMAL
        )
        createNotification(notification)
    }

    fun sendSecurityAlertNotification(user: User, alertMessage: String) {
        val notification = CreateNotificationRequest(
            user = user,
            title = "Security Alert",
            message = alertMessage,
            type = NotificationType.SECURITY_ALERT,
            actionUrl = "/security/alerts",
            priority = NotificationPriority.URGENT
        )
        createNotification(notification)
    }

    fun sendAccountStatusNotification(user: User, status: AccountStatus, reason: String) {
        val title = when (status) {
            AccountStatus.SUSPENDED -> "Account Suspended"
            AccountStatus.ACTIVE -> "Account Reactivated"
            AccountStatus.LOCKED -> "Account Locked"
            AccountStatus.BANNED -> "Account Banned"
            else -> "Account Status Changed"
        }
        
        val notification = CreateNotificationRequest(
            user = user,
            title = title,
            message = reason,
            type = NotificationType.SYSTEM_ANNOUNCEMENT,
            actionUrl = "/account/status",
            priority = NotificationPriority.URGENT
        )
        createNotification(notification)
    }

    fun sendDeveloperVerificationNotification(user: User) {
        val notification = CreateNotificationRequest(
            user = user,
            title = "Developer Verified!",
            message = "Congratulations! Your developer account has been verified.",
            type = NotificationType.ACCOUNT_VERIFIED,
            actionUrl = "/profile",
            priority = NotificationPriority.HIGH
        )
        createNotification(notification)
    }

    fun sendAISuggestionNotification(user: User, suggestion: String, snippetId: Long) {
        val notification = CreateNotificationRequest(
            user = user,
            title = "AI Code Suggestion",
            message = suggestion,
            type = NotificationType.AI_SUGGESTION,
            referenceId = snippetId,
            referenceType = "snippet",
            actionUrl = "/snippets/$snippetId",
            priority = NotificationPriority.NORMAL
        )
        createNotification(notification)
    }

    fun sendTrendingSnippetNotification(user: User, snippet: CodeSnippet) {
        val notification = CreateNotificationRequest(
            user = user,
            title = "Your snippet is trending!",
            message = "Your snippet \"${snippet.title}\" is trending in the ${snippet.programmingLanguage} category",
            type = NotificationType.TRENDING_SNIPPET,
            referenceId = snippet.id,
            referenceType = "snippet",
            actionUrl = "/snippets/${snippet.id}",
            priority = NotificationPriority.HIGH
        )
        createNotification(notification)
    }

    fun processNotificationQueue() {
        val unsentNotifications = notificationRepository.findUnsentNotifications(LocalDateTime.now())
        
        unsentNotifications.forEach { notification ->
            try {
                // Send email notification
                if (shouldSendEmail(notification)) {
                    emailService.sendNotificationEmail(notification)
                }
                
                // Send push notification
                if (shouldSendPush(notification)) {
                    pushNotificationService.sendPushNotification(notification)
                }
                
                // Mark as sent
                notificationRepository.markAsSent(notification.id, LocalDateTime.now())
                
            } catch (e: Exception) {
                // Log error and continue with next notification
                println("Failed to send notification ${notification.id}: ${e.message}")
            }
        }
    }

    fun cleanupExpiredNotifications() {
        notificationRepository.deleteExpiredNotifications(LocalDateTime.now())
    }

    private fun shouldSendEmail(notification: Notification): Boolean {
        // Check user preferences and notification type
        return when (notification.type) {
            NotificationType.SECURITY_ALERT,
            NotificationType.SYSTEM_ANNOUNCEMENT,
            NotificationType.ACCOUNT_VERIFIED -> true
            else -> false // Check user preferences
        }
    }

    private fun shouldSendPush(notification: Notification): Boolean {
        // Check user preferences and notification type
        return notification.priority == NotificationPriority.HIGH || 
               notification.priority == NotificationPriority.URGENT
    }
}

// Extension function for model to DTO conversion
private fun Notification.toResponse(): NotificationResponse {
    return NotificationResponse(
        id = id,
        title = title,
        message = message,
        type = type.name,
        referenceId = referenceId,
        referenceType = referenceType,
        actionUrl = actionUrl,
        isRead = isRead,
        readAt = readAt,
        priority = priority.name,
        createdAt = createdAt
    )
}