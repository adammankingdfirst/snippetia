package com.snippetia.controller

import com.snippetia.dto.*
import com.snippetia.model.*
import com.snippetia.service.ModerationService
import com.snippetia.security.CurrentUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/moderation")
@Tag(name = "Moderation", description = "Content moderation and reporting endpoints")
class ModerationController(
    private val moderationService: ModerationService
) {

    @PostMapping("/reports")
    @Operation(summary = "Report content", description = "Report inappropriate content")
    fun reportContent(
        @CurrentUser userId: Long,
        @Valid @RequestBody request: ReportContentRequest
    ): ResponseEntity<ReportResponse> {
        val report = moderationService.reportContent(userId, request)
        return ResponseEntity.ok(report)
    }

    @GetMapping("/reports")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    @Operation(summary = "Get pending reports", description = "Get all pending moderation reports")
    fun getPendingReports(
        pageable: Pageable
    ): ResponseEntity<Page<ReportResponse>> {
        val reports = moderationService.getPendingReports(pageable)
        return ResponseEntity.ok(reports)
    }

    @PostMapping("/reports/{reportId}/review")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    @Operation(summary = "Review report", description = "Review and make decision on a report")
    fun reviewReport(
        @CurrentUser moderatorId: Long,
        @PathVariable reportId: Long,
        @Valid @RequestBody request: ReviewReportRequest
    ): ResponseEntity<ReportResponse> {
        val decision = ModerationDecision.valueOf(request.decision.uppercase())
        val report = moderationService.reviewReport(moderatorId, reportId, decision)
        return ResponseEntity.ok(report)
    }

    @PostMapping("/content/hide")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    @Operation(summary = "Hide content", description = "Hide inappropriate content")
    fun hideContent(
        @CurrentUser moderatorId: Long,
        @Valid @RequestBody request: HideContentRequest
    ): ResponseEntity<Map<String, Any>> {
        val contentType = ContentType.valueOf(request.contentType.uppercase())
        val success = moderationService.hideContent(
            moderatorId, 
            contentType, 
            request.contentId, 
            request.reason
        )
        
        return ResponseEntity.ok(mapOf(
            "success" to success,
            "message" to if (success) "Content hidden successfully" else "Failed to hide content"
        ))
    }

    @PostMapping("/users/{userId}/suspend")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    @Operation(summary = "Suspend user", description = "Suspend a user account")
    fun suspendUser(
        @CurrentUser moderatorId: Long,
        @PathVariable userId: Long,
        @Valid @RequestBody request: SuspendUserRequest
    ): ResponseEntity<Map<String, Any>> {
        val success = moderationService.suspendUser(
            moderatorId, 
            userId, 
            request.duration, 
            request.reason
        )
        
        return ResponseEntity.ok(mapOf(
            "success" to success,
            "message" to if (success) "User suspended successfully" else "Failed to suspend user"
        ))
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    @Operation(summary = "Get moderation stats", description = "Get moderation statistics")
    fun getModerationStats(): ResponseEntity<ModerationStatsResponse> {
        val stats = moderationService.getModerationStats()
        return ResponseEntity.ok(stats)
    }

    @GetMapping("/my-reports")
    @Operation(summary = "Get user reports", description = "Get reports submitted by current user")
    fun getUserReports(
        @CurrentUser userId: Long,
        pageable: Pageable
    ): ResponseEntity<Page<ReportResponse>> {
        val reports = moderationService.getUserReports(userId, pageable)
        return ResponseEntity.ok(reports)
    }

    @GetMapping("/content-types")
    @Operation(summary = "Get content types", description = "Get available content types for reporting")
    fun getContentTypes(): ResponseEntity<List<String>> {
        val contentTypes = ContentType.values().map { it.name.lowercase() }
        return ResponseEntity.ok(contentTypes)
    }

    @GetMapping("/reasons")
    @Operation(summary = "Get moderation reasons", description = "Get available moderation reasons")
    fun getModerationReasons(): ResponseEntity<List<String>> {
        val reasons = ModerationReason.values().map { it.name.lowercase() }
        return ResponseEntity.ok(reasons)
    }

    @GetMapping("/decisions")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    @Operation(summary = "Get moderation decisions", description = "Get available moderation decisions")
    fun getModerationDecisions(): ResponseEntity<List<String>> {
        val decisions = ModerationDecision.values().map { it.name.lowercase() }
        return ResponseEntity.ok(decisions)
    }
}