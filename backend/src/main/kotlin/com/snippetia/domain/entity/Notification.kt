package com.snippetia.domain.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "notifications")
@EntityListeners(AuditingEntityListener::class)
data class Notification(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(name = "title", nullable = false)
    val title: String,

    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    val message: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    val type: NotificationType,

    @Column(name = "reference_id")
    val referenceId: Long? = null,

    @Column(name = "reference_type")
    val referenceType: String? = null,

    @Column(name = "action_url")
    val actionUrl: String? = null,

    @Column(name = "is_read")
    val isRead: Boolean = false,

    @Column(name = "read_at")
    val readAt: LocalDateTime? = null,

    @Column(name = "is_sent")
    val isSent: Boolean = false,

    @Column(name = "sent_at")
    val sentAt: LocalDateTime? = null,

    @Column(name = "delivery_method")
    val deliveryMethod: String? = null,

    @Column(name = "metadata", columnDefinition = "TEXT")
    val metadata: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    val priority: NotificationPriority = NotificationPriority.NORMAL,

    @Column(name = "expires_at")
    val expiresAt: LocalDateTime? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class NotificationType {
    SNIPPET_LIKED,
    SNIPPET_FORKED,
    SNIPPET_COMMENTED,
    SNIPPET_SHARED,
    USER_FOLLOWED,
    USER_UNFOLLOWED,
    SUPPORT_RECEIVED,
    COLLABORATION_INVITE,
    COLLABORATION_ACCEPTED,
    COLLABORATION_DECLINED,
    SNIPPET_EXECUTION_COMPLETED,
    SNIPPET_EXECUTION_FAILED,
    SECURITY_ALERT,
    SYSTEM_ANNOUNCEMENT,
    PAYMENT_RECEIVED,
    PAYMENT_FAILED,
    SUBSCRIPTION_EXPIRED,
    ACCOUNT_VERIFIED,
    PASSWORD_CHANGED,
    LOGIN_ALERT,
    AI_SUGGESTION,
    TRENDING_SNIPPET,
    WEEKLY_DIGEST
}

enum class NotificationPriority {
    LOW, NORMAL, HIGH, URGENT
}

@Entity
@Table(name = "notification_preferences")
@EntityListeners(AuditingEntityListener::class)
data class NotificationPreference(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    val notificationType: NotificationType,

    @Column(name = "email_enabled")
    val emailEnabled: Boolean = true,

    @Column(name = "push_enabled")
    val pushEnabled: Boolean = true,

    @Column(name = "in_app_enabled")
    val inAppEnabled: Boolean = true,

    @Column(name = "sms_enabled")
    val smsEnabled: Boolean = false,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)