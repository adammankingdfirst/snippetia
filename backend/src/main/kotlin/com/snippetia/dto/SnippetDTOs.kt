package com.snippetia.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

// Request DTOs
data class CreateSnippetRequest(
    @field:NotBlank(message = "Title is required")
    @field:Size(max = 200, message = "Title must not exceed 200 characters")
    val title: String,

    @field:Size(max = 1000, message = "Description must not exceed 1000 characters")
    val description: String? = null,

    @field:NotBlank(message = "Code content is required")
    val content: String,

    @field:NotBlank(message = "Programming language is required")
    val language: String,

    val tags: List<String> = emptyList(),
    val isPublic: Boolean = true
)

data class UpdateSnippetRequest(
    @field:Size(max = 200, message = "Title must not exceed 200 characters")
    val title: String? = null,

    @field:Size(max = 1000, message = "Description must not exceed 1000 characters")
    val description: String? = null,

    val content: String? = null,

    val language: String? = null,

    val tags: List<String>? = null,
    val isPublic: Boolean? = null
)

data class UploadSnippetRequest(
    val file: MultipartFile,
    val title: String,
    val description: String? = null,
    val language: String,
    val tags: List<String> = emptyList(),
    val isPublic: Boolean = true
)

data class CreateVersionRequest(
    @field:NotBlank(message = "Code content is required")
    val content: String,

    val description: String? = null
)

data class CreateCommentRequest(
    @field:NotBlank(message = "Comment content is required")
    @field:Size(max = 2000, message = "Comment must not exceed 2000 characters")
    val content: String
)

// Response DTOs
data class SnippetResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val language: String,
    val tags: List<String>,
    val isPublic: Boolean,
    val author: UserSummaryResponse,
    val likeCount: Long,
    val viewCount: Long,
    val forkCount: Long,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class SnippetDetailResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val content: String,
    val language: String,
    val tags: List<String>,
    val isPublic: Boolean,
    val author: UserSummaryResponse,
    val likeCount: Long,
    val viewCount: Long,
    val forkCount: Long,
    val forkedFrom: SnippetSummaryResponse?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class SnippetSummaryResponse(
    val id: Long,
    val title: String,
    val author: UserSummaryResponse
)

data class SnippetVersionResponse(
    val id: Long,
    val versionNumber: Int,
    val description: String?,
    val createdAt: LocalDateTime
)

data class CommentResponse(
    val id: Long,
    val content: String,
    val author: UserSummaryResponse,
    val createdAt: LocalDateTime
)

data class CategoryResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val iconUrl: String?,
    val colorCode: String?
)

data class SearchResponse(
    val snippets: List<SnippetResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int,
    val facets: SearchFacetsResponse
)

data class SearchFacetsResponse(
    val languages: Map<String, Long>,
    val categories: Map<String, Long>,
    val tags: Map<String, Long>
)