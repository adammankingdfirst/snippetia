package com.snippetia.model

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "notifications")
@EntityListeners(AuditingEntityListener::class)
data class Notification(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: NotificationType,

    @Column(name = "title", nullable = false, length = 200)
    var title: String,

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    var message: String,

    @Column(name = "is_read")
    var isRead: Boolean = false,

    @Column(name = "action_url")
    var actionUrl: String? = null,

    @Column(name = "metadata", columnDefinition = "TEXT")
    var metadata: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    var priority: NotificationPriority = NotificationPriority.NORMAL,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)

enum class NotificationType {
    NEW_SNIPPET, SNIPPET_LIKED, SNIPPET_COMMENTED, SNIPPET_FORKED,
    NEW_FOLLOWER, SUBSCRIPTION_CREATED, SUBSCRIPTION_CANCELLED,
    EVENT_REMINDER, EVENT_CANCELLED, EVENT_UPDATED,
    CONTENT_MODERATED, ACCOUNT_SUSPENDED, SYSTEM_ANNOUNCEMENT,
    MODERATION_UPDATE, PAYMENT_RECEIVED, PAYMENT_FAILED
}

enum class NotificationPriority {
    LOW, NORMAL, HIGH, URGENT
}