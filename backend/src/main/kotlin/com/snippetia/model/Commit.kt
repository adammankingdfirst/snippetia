package com.snippetia.model

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "commits")
@EntityListeners(AuditingEntityListener::class)
data class Commit(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "sha", unique = true, nullable = false, length = 40)
    var sha: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    var repository: Repository,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    var author: User,

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    var message: String,

    @Column(name = "tree_sha", length = 40)
    var treeSha: String? = null,

    @Column(name = "branch", nullable = false)
    var branch: String,

    @Column(name = "additions")
    var additions: Int = 0,

    @Column(name = "deletions")
    var deletions: Int = 0,

    @Column(name = "changed_files")
    var changedFiles: Int = 0,

    @Column(name = "verified")
    var verified: Boolean = false,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)