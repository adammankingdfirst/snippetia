package com.snippetia.domain.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "code_snippets")
@EntityListeners(AuditingEntityListener::class)
data class CodeSnippet(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val title: String,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "code_content", columnDefinition = "TEXT", nullable = false)
    val codeContent: String,

    @Column(name = "programming_language", nullable = false)
    val programmingLanguage: String,

    @Column(name = "framework_version")
    val frameworkVersion: String? = null,

    @ElementCollection
    @CollectionTable(name = "snippet_tags", joinColumns = [JoinColumn(name = "snippet_id")])
    @Column(name = "tag")
    val tags: Set<String> = setOf(),

    @Column(name = "is_public")
    val isPublic: Boolean = true,

    @Column(name = "is_featured")
    val isFeatured: Boolean = false,

    @Column(name = "is_executable")
    val isExecutable: Boolean = false,

    @Column(name = "is_testable")
    val isTestable: Boolean = false,

    @Column(name = "execution_environment")
    val executionEnvironment: String? = null,

    @Column(name = "dependencies", columnDefinition = "TEXT")
    val dependencies: String? = null,

    @Column(name = "build_command")
    val buildCommand: String? = null,

    @Column(name = "run_command")
    val runCommand: String? = null,

    @Column(name = "test_command")
    val testCommand: String? = null,

    @Column(name = "view_count")
    val viewCount: Long = 0,

    @Column(name = "like_count")
    val likeCount: Long = 0,

    @Column(name = "fork_count")
    val forkCount: Long = 0,

    @Column(name = "download_count")
    val downloadCount: Long = 0,

    @Column(name = "run_count")
    val runCount: Long = 0,

    @Column(name = "file_size")
    val fileSize: Long = 0,

    @Column(name = "checksum")
    val checksum: String? = null,

    @Column(name = "virus_scan_status")
    @Enumerated(EnumType.STRING)
    val virusScanStatus: VirusScanStatus = VirusScanStatus.PENDING,

    @Column(name = "virus_scan_result", columnDefinition = "TEXT")
    val virusScanResult: String? = null,

    @Column(name = "security_scan_status")
    @Enumerated(EnumType.STRING)
    val securityScanStatus: SecurityScanStatus = SecurityScanStatus.PENDING,

    @Column(name = "security_scan_result", columnDefinition = "TEXT")
    val securityScanResult: String? = null,

    @Column(name = "ai_analysis_result", columnDefinition = "TEXT")
    val aiAnalysisResult: String? = null,

    @Column(name = "complexity_score")
    val complexityScore: Int? = null,

    @Column(name = "quality_score")
    val qualityScore: Int? = null,

    @Column(name = "license_type")
    val licenseType: String? = null,

    @Column(name = "original_snippet_id")
    val originalSnippetId: Long? = null,

    @Column(name = "version_number")
    val versionNumber: String = "1.0.0",

    @Column(name = "git_repository_url")
    val gitRepositoryUrl: String? = null,

    @Column(name = "git_branch")
    val gitBranch: String? = null,

    @Column(name = "git_commit_hash")
    val gitCommitHash: String? = null,

    @Column(name = "git_file_path")
    val gitFilePath: String? = null,

    @Column(name = "sync_status")
    @Enumerated(EnumType.STRING)
    val syncStatus: SyncStatus = SyncStatus.NOT_SYNCED,

    @Column(name = "last_sync_at")
    val lastSyncAt: LocalDateTime? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    val category: Category? = null,

    @OneToMany(mappedBy = "snippet", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val versions: List<SnippetVersion> = listOf(),

    @OneToMany(mappedBy = "snippet", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val comments: List<Comment> = listOf(),

    @OneToMany(mappedBy = "snippet", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val likes: List<SnippetLike> = listOf(),

    @OneToMany(mappedBy = "snippet", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val executions: List<SnippetExecution> = listOf(),

    @OneToMany(mappedBy = "snippet", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val collaborators: List<SnippetCollaborator> = listOf(),

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class VirusScanStatus {
    PENDING, SCANNING, CLEAN, INFECTED, ERROR, SKIPPED
}

enum class SecurityScanStatus {
    PENDING, SCANNING, SAFE, VULNERABLE, ERROR, SKIPPED
}

enum class SyncStatus {
    NOT_SYNCED, SYNCING, SYNCED, SYNC_ERROR, CONFLICT
}

@Entity
@Table(name = "snippet_versions")
@EntityListeners(AuditingEntityListener::class)
data class SnippetVersion(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "version_number", nullable = false)
    val versionNumber: String,

    @Column(name = "code_content", columnDefinition = "TEXT", nullable = false)
    val codeContent: String,

    @Column(name = "change_description", columnDefinition = "TEXT")
    val changeDescription: String? = null,

    @Column(name = "file_size")
    val fileSize: Long = 0,

    @Column(name = "checksum")
    val checksum: String? = null,

    @Column(name = "git_commit_hash")
    val gitCommitHash: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "snippet_id", nullable = false)
    val snippet: CodeSnippet,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "snippet_executions")
@EntityListeners(AuditingEntityListener::class)
data class SnippetExecution(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "snippet_id", nullable = false)
    val snippet: CodeSnippet,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(name = "execution_environment", nullable = false)
    val executionEnvironment: String,

    @Column(name = "input_data", columnDefinition = "TEXT")
    val inputData: String? = null,

    @Column(name = "output_data", columnDefinition = "TEXT")
    val outputData: String? = null,

    @Column(name = "error_output", columnDefinition = "TEXT")
    val errorOutput: String? = null,

    @Column(name = "execution_time_ms")
    val executionTimeMs: Long? = null,

    @Column(name = "memory_usage_mb")
    val memoryUsageMb: Long? = null,

    @Column(name = "cpu_usage_percent")
    val cpuUsagePercent: Double? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    val status: ExecutionStatus,

    @Column(name = "exit_code")
    val exitCode: Int? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class ExecutionStatus {
    PENDING, RUNNING, COMPLETED, FAILED, TIMEOUT, CANCELLED
}

@Entity
@Table(name = "snippet_collaborators")
@EntityListeners(AuditingEntityListener::class)
data class SnippetCollaborator(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "snippet_id", nullable = false)
    val snippet: CodeSnippet,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Enumerated(EnumType.STRING)
    @Column(name = "permission_level")
    val permissionLevel: CollaboratorPermission,

    @Column(name = "invited_by_user_id")
    val invitedByUserId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    val status: CollaboratorStatus = CollaboratorStatus.PENDING,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class CollaboratorPermission {
    READ, WRITE, ADMIN
}

enum class CollaboratorStatus {
    PENDING, ACCEPTED, DECLINED, REMOVED
}