package com.snippetia.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

// Moderation Request DTOs
data class ReportContentRequest(
    @field:NotNull(message = "Content type is required")
    val contentType: String,
    
    @field:NotNull(message = "Content ID is required")
    val contentId: Long,
    
    @field:NotBlank(message = "Reason is required")
    val reason: String,
    
    val description: String? = null
)

data class ReviewReportRequest(
    @field:NotBlank(message = "Decision is required")
    val decision: String,
    
    val notes: String? = null
)

data class SuspendUserRequest(
    @field:NotNull(message = "Duration is required")
    val duration: Int,
    
    @field:NotBlank(message = "Reason is required")
    val reason: String
)

data class HideContentRequest(
    @field:NotBlank(message = "Content type is required")
    val contentType: String,
    
    @field:NotNull(message = "Content ID is required")
    val contentId: Long,
    
    @field:NotBlank(message = "Reason is required")
    val reason: String
)

// Moderation Response DTOs
data class ReportResponse(
    val id: Long,
    val reporter: UserSummaryResponse,
    val moderator: UserSummaryResponse? = null,
    val contentType: String,
    val contentId: Long,
    val reason: String,
    val description: String? = null,
    val status: String,
    val decision: String? = null,
    val createdAt: LocalDateTime,
    val reviewedAt: LocalDateTime? = null
)

data class ModerationStatsResponse(
    val pendingReports: Long,
    val resolvedToday: Long,
    val totalReports: Long
)

data class ModerationActionResponse(
    val success: Boolean,
    val message: String
)