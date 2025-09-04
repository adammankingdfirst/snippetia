package com.snippetia.model

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "subscriptions")
@EntityListeners(AuditingEntityListener::class)
data class Subscription(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_id", nullable = false)
    var subscriber: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_owner_id", nullable = false)
    var channelOwner: User,

    @Enumerated(EnumType.STRING)
    @Column(name = "tier", nullable = false)
    var tier: SubscriptionTier,

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    var amount: BigDecimal,

    @Column(name = "platform_fee", precision = 10, scale = 2, nullable = false)
    var platformFee: BigDecimal,

    @Column(name = "creator_amount", precision = 10, scale = 2, nullable = false)
    var creatorAmount: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: SubscriptionStatus = SubscriptionStatus.ACTIVE,

    @Column(name = "stripe_subscription_id")
    var stripeSubscriptionId: String? = null,

    @Column(name = "current_period_start", nullable = false)
    var currentPeriodStart: LocalDateTime,

    @Column(name = "current_period_end", nullable = false)
    var currentPeriodEnd: LocalDateTime,

    @Column(name = "auto_renew")
    var autoRenew: Boolean = true,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)

enum class SubscriptionTier {
    BASIC, PREMIUM, ENTERPRISE
}

enum class SubscriptionStatus {
    ACTIVE, CANCELLED, EXPIRED, SUSPENDED
}