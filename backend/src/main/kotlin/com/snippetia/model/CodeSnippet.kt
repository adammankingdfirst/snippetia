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
    var id: Long? = null,

    @Column(nullable = false)
    var title: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    var content: String,

    @Column(name = "language", nullable = false)
    var language: String,

    @ElementCollection
    @CollectionTable(name = "snippet_tags", joinColumns = [JoinColumn(name = "snippet_id")])
    @Column(name = "tag")
    var tags: MutableSet<String> = mutableSetOf(),

    @Column(name = "is_public")
    var isPublic: Boolean = true,

    @Column(name = "view_count")
    var viewCount: Long = 0,

    @Column(name = "like_count")
    var likeCount: Long = 0,

    @Column(name = "fork_count")
    var forkCount: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    var author: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "forked_from_id")
    var forkedFrom: CodeSnippet? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
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