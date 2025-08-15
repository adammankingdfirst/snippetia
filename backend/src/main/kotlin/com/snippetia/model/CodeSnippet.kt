package com.snippetia.model

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

    @Column(name = "view_count")
    val viewCount: Long = 0,

    @Column(name = "like_count")
    val likeCount: Long = 0,

    @Column(name = "fork_count")
    val forkCount: Long = 0,

    @Column(name = "download_count")
    val downloadCount: Long = 0,

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

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class VirusScanStatus {
    PENDING, SCANNING, CLEAN, INFECTED, ERROR
}

enum class SecurityScanStatus {
    PENDING, SCANNING, SAFE, VULNERABLE, ERROR
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "snippet_id", nullable = false)
    val snippet: CodeSnippet,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "categories")
@EntityListeners(AuditingEntityListener::class)
data class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    val name: String,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "icon_url")
    val iconUrl: String? = null,

    @Column(name = "color_code")
    val colorCode: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    val parent: Category? = null,

    @OneToMany(mappedBy = "parent", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val children: List<Category> = listOf(),

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)