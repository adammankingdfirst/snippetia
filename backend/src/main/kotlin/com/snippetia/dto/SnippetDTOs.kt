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
    val codeContent: String,

    @field:NotBlank(message = "Programming language is required")
    val programmingLanguage: String,

    val frameworkVersion: String? = null,
    val tags: List<String> = emptyList(),
    val isPublic: Boolean = true,
    val licenseType: String? = null,
    val categoryId: Long? = null
)

data class UpdateSnippetRequest(
    @field:NotBlank(message = "Title is required")
    @field:Size(max = 200, message = "Title must not exceed 200 characters")
    val title: String,

    @field:Size(max = 1000, message = "Description must not exceed 1000 characters")
    val description: String? = null,

    @field:NotBlank(message = "Code content is required")
    val codeContent: String,

    @field:NotBlank(message = "Programming language is required")
    val programmingLanguage: String,

    val frameworkVersion: String? = null,
    val tags: List<String> = emptyList(),
    val isPublic: Boolean = true,
    val licenseType: String? = null,
    val categoryId: Long? = null
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
    @field:NotBlank(message = "Version number is required")
    val versionNumber: String,

    @field:NotBlank(message = "Code content is required")
    val codeContent: String,

    val changeDescription: String? = null
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
    val programmingLanguage: String,
    val frameworkVersion: String?,
    val tags: List<String>,
    val isPublic: Boolean,
    val viewCount: Long,
    val likeCount: Long,
    val forkCount: Long,
    val downloadCount: Long,
    val versionNumber: String,
    val author: UserSummaryResponse,
    val category: CategoryResponse?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class SnippetDetailResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val codeContent: String,
    val programmingLanguage: String,
    val frameworkVersion: String?,
    val tags: List<String>,
    val isPublic: Boolean,
    val viewCount: Long,
    val likeCount: Long,
    val forkCount: Long,
    val downloadCount: Long,
    val versionNumber: String,
    val licenseType: String?,
    val fileSize: Long,
    val virusScanStatus: String,
    val securityScanStatus: String,
    val author: UserSummaryResponse,
    val category: CategoryResponse?,
    val originalSnippetId: Long?,
    val gitRepositoryUrl: String?,
    val gitBranch: String?,
    val gitCommitHash: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class SnippetVersionResponse(
    val id: Long,
    val versionNumber: String,
    val codeContent: String,
    val changeDescription: String?,
    val fileSize: Long,
    val createdAt: LocalDateTime
)

data class CommentResponse(
    val id: Long,
    val content: String,
    val author: UserSummaryResponse,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
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