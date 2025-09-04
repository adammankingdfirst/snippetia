package com.snippetia.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

// Follow DTOs
data class FollowRequest(
    val userId: Long,
    val notificationEnabled: Boolean = true
)

data class FollowResponse(
    val isFollowing: Boolean,
    val followerCount: Long,
    val followingCount: Long
)

data class FollowStatsResponse(
    val followerCount: Long,
    val followingCount: Long
)

// Star DTOs
data class StarRequest(
    val snippetId: Long
)

data class StarResponse(
    val isStarred: Boolean,
    val starCount: Long
)

// Notification DTOs
data class NotificationResponse(
    val id: Long,
    val type: String,
    val title: String,
    val message: String,
    val isRead: Boolean,
    val actionUrl: String?,
    val priority: String,
    val createdAt: LocalDateTime
)

data class NotificationSummaryResponse(
    val unreadCount: Long,
    val recentNotifications: List<NotificationResponse>
)

data class MarkNotificationsReadRequest(
    val notificationIds: List<Long>
)

// Subscription DTOs
data class CreateSubscriptionRequest(
    val channelOwnerId: Long,
    val tier: String,
    val paymentMethodId: String
)

data class SubscriptionResponse(
    val id: Long,
    val channelOwner: UserSummaryResponse,
    val tier: String,
    val amount: String,
    val status: String,
    val currentPeriodStart: LocalDateTime,
    val currentPeriodEnd: LocalDateTime,
    val autoRenew: Boolean,
    val createdAt: LocalDateTime
)

// Channel DTOs
data class CreateChannelRequest(
    @field:NotBlank(message = "Channel name is required")
    @field:Size(min = 3, max = 50, message = "Channel name must be between 3 and 50 characters")
    val name: String,

    @field:NotBlank(message = "Display name is required")
    @field:Size(max = 100, message = "Display name must not exceed 100 characters")
    val displayName: String,

    @field:Size(max = 1000, message = "Description must not exceed 1000 characters")
    val description: String? = null,

    val tags: List<String> = emptyList(),
    val subscriptionEnabled: Boolean = false,
    val basicTierPrice: String? = null,
    val premiumTierPrice: String? = null,
    val enterpriseTierPrice: String? = null
)

data class UpdateChannelRequest(
    @field:Size(max = 100, message = "Display name must not exceed 100 characters")
    val displayName: String? = null,

    @field:Size(max = 1000, message = "Description must not exceed 1000 characters")
    val description: String? = null,

    val tags: List<String>? = null,
    val subscriptionEnabled: Boolean? = null,
    val basicTierPrice: String? = null,
    val premiumTierPrice: String? = null,
    val enterpriseTierPrice: String? = null,
    val websiteUrl: String? = null,
    val githubUrl: String? = null,
    val twitterUrl: String? = null
)

data class ChannelResponse(
    val id: Long,
    val name: String,
    val displayName: String,
    val description: String?,
    val avatarUrl: String?,
    val bannerUrl: String?,
    val tags: List<String>,
    val isVerified: Boolean,
    val subscriberCount: Long,
    val snippetCount: Long,
    val totalStars: Long,
    val owner: UserSummaryResponse,
    val subscriptionEnabled: Boolean,
    val subscriptionTiers: List<SubscriptionTierResponse>,
    val websiteUrl: String?,
    val githubUrl: String?,
    val twitterUrl: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class SubscriptionTierResponse(
    val tier: String,
    val price: String,
    val benefits: List<String>
)

// Event DTOs
data class CreateEventRequest(
    @field:NotBlank(message = "Event title is required")
    val title: String,
    
    @field:NotBlank(message = "Event description is required")
    val description: String,
    
    val channelId: Long? = null,
    val type: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val timezone: String = "UTC",
    val location: String? = null,
    val virtualLink: String? = null,
    val maxAttendees: Int? = null,
    val registrationFee: String? = null,
    val requiresApproval: Boolean = false,
    val tags: List<String> = emptyList()
)

data class EventResponse(
    val id: Long,
    val title: String,
    val description: String,
    val organizer: UserSummaryResponse?,
    val channel: ChannelSummaryResponse?,
    val type: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val timezone: String,
    val location: String?,
    val virtualLink: String?,
    val maxAttendees: Int?,
    val currentAttendees: Int,
    val registrationFee: String?,
    val isFree: Boolean,
    val requiresApproval: Boolean,
    val bannerUrl: String?,
    val tags: List<String>,
    val status: String,
    val featured: Boolean,
    val createdAt: LocalDateTime
)

data class ChannelSummaryResponse(
    val id: Long,
    val name: String,
    val displayName: String,
    val avatarUrl: String?
)

// Developer Showcase DTOs
data class CreateShowcaseRequest(
    @field:NotBlank(message = "Title is required")
    val title: String,
    
    @field:NotBlank(message = "Description is required")
    val description: String,
    
    @field:NotBlank(message = "App name is required")
    val appName: String,
    
    val appUrl: String? = null,
    val githubUrl: String? = null,
    val demoUrl: String? = null,
    val videoUrl: String? = null,
    val screenshots: List<String> = emptyList(),
    val technologies: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val contactEmail: String,
    val hourlyRate: String? = null,
    val availableForHire: Boolean = false,
    val contractTypes: List<String> = emptyList(),
    val skills: List<String> = emptyList(),
    val experienceYears: Int? = null
)

data class ShowcaseResponse(
    val id: Long,
    val developer: UserSummaryResponse,
    val title: String,
    val description: String,
    val appName: String,
    val appUrl: String?,
    val githubUrl: String?,
    val demoUrl: String?,
    val videoUrl: String?,
    val screenshots: List<String>,
    val technologies: List<String>,
    val categories: List<String>,
    val status: String,
    val featured: Boolean,
    val viewCount: Long,
    val likeCount: Long,
    val contactEmail: String,
    val hourlyRate: String?,
    val availableForHire: Boolean,
    val contractTypes: List<String>,
    val skills: List<String>,
    val experienceYears: Int?,
    val createdAt: LocalDateTime
)Url: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class SubscriptionTierResponse(
    val tier: String,
    val price: String,
    val features: List<String>
)

data class ChannelSummaryResponse(
    val id: Long,
    val name: String,
    val displayName: String,
    val avatarUrl: String?
)

// Event DTOs
data class CreateEventRequest(
    @field:NotBlank(message = "Event title is required")
    @field:Size(max = 200, message = "Title must not exceed 200 characters")
    val title: String,

    @field:NotBlank(message = "Event description is required")
    @field:Size(max = 2000, message = "Description must not exceed 2000 characters")
    val description: String,

    val type: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val timezone: String = "UTC",
    val location: String? = null,
    val virtualLink: String? = null,
    val maxAttendees: Int? = null,
    val registrationFee: String? = null,
    val isFree: Boolean = true,
    val requiresApproval: Boolean = false,
    val tags: List<String> = emptyList()
)

data class EventResponse(
    val id: Long,
    val title: String,
    val description: String,
    val organizer: UserSummaryResponse?,
    val channel: ChannelSummaryResponse?,
    val type: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val timezone: String,
    val location: String?,
    val virtualLink: String?,
    val maxAttendees: Int?,
    val currentAttendees: Int,
    val registrationFee: String?,
    val isFree: Boolean,
    val requiresApproval: Boolean,
    val bannerUrl: String?,
    val tags: List<String>,
    val status: String,
    val featured: Boolean,
    val createdAt: LocalDateTime
)

// Developer Showcase DTOs
data class CreateShowcaseRequest(
    @field:NotBlank(message = "Title is required")
    @field:Size(max = 200, message = "Title must not exceed 200 characters")
    val title: String,

    @field:NotBlank(message = "Description is required")
    @field:Size(max = 2000, message = "Description must not exceed 2000 characters")
    val description: String,

    @field:NotBlank(message = "App name is required")
    val appName: String,

    val appUrl: String? = null,
    val githubUrl: String? = null,
    val demoUrl: String? = null,
    val videoUrl: String? = null,
    val screenshots: List<String> = emptyList(),
    val technologies: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val contactEmail: String,
    val hourlyRate: String? = null,
    val availableForHire: Boolean = false,
    val contractTypes: List<String> = emptyList(),
    val skills: List<String> = emptyList(),
    val experienceYears: Int? = null
)

data class ShowcaseResponse(
    val id: Long,
    val developer: UserSummaryResponse,
    val title: String,
    val description: String,
    val appName: String,
    val appUrl: String?,
    val githubUrl: String?,
    val demoUrl: String?,
    val videoUrl: String?,
    val screenshots: List<String>,
    val technologies: List<String>,
    val categories: List<String>,
    val status: String,
    val featured: Boolean,
    val viewCount: Long,
    val likeCount: Long,
    val contactEmail: String,
    val hourlyRate: String?,
    val availableForHire: Boolean,
    val contractTypes: List<String>,
    val skills: List<String>,
    val experienceYears: Int?,
    val createdAt: LocalDateTime
)

// AI Bot DTOs
data class BotQueryRequest(
    val query: String,
    val context: BotContextRequest? = null
)

data class BotContextRequest(
    val snippetId: Long? = null,
    val description: String? = null
)

data class CodeAnalysisRequest(
    val code: String,
    val language: String
)

data class CodeCompletionRequest(
    val code: String,
    val language: String,
    val cursorPosition: Int
)

data class CodeExplanationRequest(
    val code: String,
    val language: String
)

data class DocumentationRequest(
    val code: String,
    val language: String
)

data class RefactoringRequest(
    val code: String,
    val language: String
)