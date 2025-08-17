package com.snippetia.domain.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class CodeSnippet(
    val id: Long,
    val title: String,
    val description: String,
    val content: String,
    val language: String,
    val tags: List<String> = emptyList(),
    val isPublic: Boolean = true,
    val author: User,
    val likeCount: Long = 0,
    val viewCount: Long = 0,
    val forkCount: Long = 0,
    val forkedFrom: CodeSnippet? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

@Serializable
data class User(
    val id: Long,
    val username: String,
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val displayName: String,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val githubUsername: String? = null,
    val twitterUsername: String? = null,
    val websiteUrl: String? = null,
    val isEmailVerified: Boolean = false,
    val isTwoFactorEnabled: Boolean = false,
    val accountStatus: String = "ACTIVE",
    val roles: List<String> = emptyList(),
    val createdAt: LocalDateTime,
    val lastLoginAt: LocalDateTime? = null
) {
    fun getDisplayName(): String = displayName.ifEmpty { username }
}

@Serializable
data class Category(
    val id: Long,
    val name: String,
    val description: String? = null,
    val iconUrl: String? = null,
    val colorCode: String? = null
)