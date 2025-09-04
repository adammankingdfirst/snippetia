package com.snippetia.model

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "repositories")
@EntityListeners(AuditingEntityListener::class)
data class Repository(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "name", nullable = false, length = 100)
    var name: String,

    @Column(name = "full_name", unique = true, nullable = false, length = 200)
    var fullName: String,

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    var owner: User,

    @Column(name = "is_private")
    var isPrivate: Boolean = false,

    @Column(name = "default_branch", nullable = false)
    var defaultBranch: String = "main",

    @Column(name = "clone_url")
    var cloneUrl: String? = null,

    @Column(name = "ssh_url")
    var sshUrl: String? = null,

    @Column(name = "size_kb")
    var sizeKb: Long = 0,

    @Column(name = "star_count")
    var starCount: Long = 0,

    @Column(name = "fork_count")
    var forkCount: Long = 0,

    @Column(name = "watch_count")
    var watchCount: Long = 0,

    @Column(name = "issue_count")
    var issueCount: Long = 0,

    @Column(name = "pull_request_count")
    var pullRequestCount: Long = 0,

    @Column(name = "primary_language")
    var primaryLanguage: String? = null,

    @Column(name = "license")
    var license: String? = null,

    @Column(name = "homepage_url")
    var homepageUrl: String? = null,

    @ElementCollection
    @CollectionTable(name = "repository_topics", joinColumns = [JoinColumn(name = "repository_id")])
    @Column(name = "topic")
    var topics: MutableSet<String> = mutableSetOf(),

    @Column(name = "has_issues")
    var hasIssues: Boolean = true,

    @Column(name = "has_projects")
    var hasProjects: Boolean = true,

    @Column(name = "has_wiki")
    var hasWiki: Boolean = true,

    @Column(name = "archived")
    var archived: Boolean = false,

    @Column(name = "disabled")
    var disabled: Boolean = false,

    @Column(name = "last_push_at")
    var lastPushAt: LocalDateTime? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)