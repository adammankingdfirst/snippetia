package com.snippetia.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

// Repository DTOs
data class CreateRepositoryRequest(
    @field:NotBlank(message = "Repository name is required")
    @field:Size(min = 1, max = 100, message = "Repository name must be between 1 and 100 characters")
    val name: String,
    
    @field:Size(max = 500, message = "Description must not exceed 500 characters")
    val description: String? = null,
    
    val isPrivate: Boolean = false,
    val defaultBranch: String = "main",
    val topics: List<String> = emptyList(),
    val license: String? = null,
    val homepageUrl: String? = null,
    val hasIssues: Boolean = true,
    val hasProjects: Boolean = true,
    val hasWiki: Boolean = true
)

data class RepositoryResponse(
    val id: Long,
    val name: String,
    val fullName: String,
    val description: String?,
    val owner: UserSummaryResponse,
    val isPrivate: Boolean,
    val defaultBranch: String,
    val cloneUrl: String,
    val sshUrl: String,
    val sizeKb: Long,
    val starCount: Long,
    val forkCount: Long,
    val watchCount: Long,
    val issueCount: Long,
    val pullRequestCount: Long,
    val forkedFrom: RepositorySummaryResponse?,
    val topics: List<String>,
    val primaryLanguage: String?,
    val license: String?,
    val homepageUrl: String?,
    val hasIssues: Boolean,
    val hasProjects: Boolean,
    val hasWiki: Boolean,
    val archived: Boolean,
    val disabled: Boolean,
    val lastPushAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class RepositorySummaryResponse(
    val id: Long,
    val name: String,
    val fullName: String,
    val owner: UserSummaryResponse
)

// Commit DTOs
data class CreateCommitRequest(
    val repositoryId: Long,
    val message: String,
    val branch: String,
    val files: List<CommitFileChange>
)

data class CommitFileChange(
    val path: String,
    val content: String?,
    val operation: String // ADD, MODIFY, DELETE
)

data class CommitResponse(
    val id: Long,
    val sha: String,
    val repository: RepositorySummaryResponse,
    val author: UserSummaryResponse,
    val committer: UserSummaryResponse?,
    val message: String,
    val treeSha: String,
    val parentShas: List<String>,
    val branch: String,
    val additions: Int,
    val deletions: Int,
    val changedFiles: Int,
    val verified: Boolean,
    val signature: String?,
    val createdAt: LocalDateTime
)

// Branch DTOs
data class CreateBranchRequest(
    val name: String,
    val fromBranch: String = "main"
)

data class BranchResponse(
    val name: String,
    val sha: String,
    val protected: Boolean,
    val ahead: Int,
    val behind: Int
)

// Pull Request DTOs
data class CreatePullRequestRequest(
    val title: String,
    val description: String?,
    val headBranch: String,
    val baseBranch: String,
    val draft: Boolean = false
)

data class PullRequestResponse(
    val id: Long,
    val number: Int,
    val title: String,
    val description: String?,
    val author: UserSummaryResponse,
    val headBranch: String,
    val baseBranch: String,
    val state: String, // OPEN, CLOSED, MERGED
    val draft: Boolean,
    val mergeable: Boolean,
    val additions: Int,
    val deletions: Int,
    val changedFiles: Int,
    val commits: Int,
    val reviewers: List<UserSummaryResponse>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val mergedAt: LocalDateTime?
)

// File DTOs
data class FileContentResponse(
    val path: String,
    val content: String,
    val encoding: String,
    val size: Long,
    val sha: String,
    val lastModified: LocalDateTime
)

data class DirectoryResponse(
    val path: String,
    val entries: List<FileEntryResponse>
)

data class FileEntryResponse(
    val name: String,
    val path: String,
    val type: String, // FILE, DIRECTORY
    val size: Long?,
    val sha: String?,
    val lastModified: LocalDateTime?
)

// Diff DTOs
data class DiffResponse(
    val files: List<FileDiffResponse>,
    val additions: Int,
    val deletions: Int,
    val changedFiles: Int
)

data class FileDiffResponse(
    val path: String,
    val oldPath: String?,
    val status: String, // ADDED, MODIFIED, DELETED, RENAMED
    val additions: Int,
    val deletions: Int,
    val patch: String?
)