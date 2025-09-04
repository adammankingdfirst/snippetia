package com.snippetia.model

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "moderation_actions")
@EntityListeners(AuditingEntityListener::class)
data class ModerationAction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    var reporter: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderator_id")
    var moderator: User? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false)
    var contentType: ContentType,

    @Column(name = "content_id", nullable = false)
    var contentId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false)
    var reason: ModerationReason,

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: ModerationStatus,

    @Enumerated(EnumType.STRING)
    @Column(name = "decision")
    var decision: ModerationDecision? = null,

    @Column(name = "reviewed_at")
    var reviewedAt: LocalDateTime? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)

enum class ContentType {
    SNIPPET, COMMENT, USER, CHANNEL, SHOWCASE
}

enum class ModerationReason {
    SPAM, INAPPROPRIATE_CONTENT, HARASSMENT, COPYRIGHT_VIOLATION, 
    POLICY_VIOLATION, MISINFORMATION, OTHER
}

enum class ModerationStatus {
    PENDING, RESOLVED, DISMISSED, ACTION_TAKEN
}

enum class ModerationDecision {
    APPROVED, REJECTED, REQUIRES_ACTION
}