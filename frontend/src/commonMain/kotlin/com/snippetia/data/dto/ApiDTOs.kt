package com.snippetia.data.dto

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class PagedResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)

@Serializable
data class CreateSnippetRequest(
    val title: String,
    val description: String? = null,
    val content: String,
    val language: String,
    val tags: List<String> = emptyList(),
    val isPublic: Boolean = true
)

@Serializable
data class UpdateSnippetRequest(
    val title: String? = null,
    val description: String? = null,
    val content: String? = null,
    val language: String? = null,
    val tags: List<String>? = null,
    val isPublic: Boolean? = null
)

@Serializable
data class LikeResponse(
    val isLiked: Boolean,
    val likeCount: Long
)

@Serializable
data class UserSummary(
    val id: Long,
    val username: String,
    val displayName: String,
    val avatarUrl: String? = null
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val displayName: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserResponse
)

@Serializable
data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val githubUsername: String? = null,
    val twitterUsername: String? = null,
    val websiteUrl: String? = null,
    val isEmailVerified: Boolean,
    val isTwoFactorEnabled: Boolean,
    val accountStatus: String,
    val createdAt: String // Using String for serialization, convert to LocalDateTime in domain
)

@Serializable
data class CommentRequest(
    val content: String
)

@Serializable
data class CommentResponse(
    val id: Long,
    val content: String,
    val author: UserSummary,
    val createdAt: String
)

@Serializable
data class NotificationResponse(
    val id: Long,
    val type: String,
    val title: String,
    val message: String,
    val isRead: Boolean,
    val actionUrl: String? = null,
    val priority: String,
    val createdAt: String
)

@Serializable
data class AnalyticsResponse(
    val totalSnippets: Long,
    val totalUsers: Long,
    val totalLanguages: Int,
    val todaySnippets: Long,
    val popularLanguages: List<LanguageStat>,
    val recentActivity: List<ActivityItem>
)

@Serializable
data class LanguageStat(
    val language: String,
    val count: Long,
    val percentage: Double
)

@Serializable
data class ActivityItem(
    val type: String,
    val description: String,
    val timestamp: String,
    val user: UserSummary? = null
)