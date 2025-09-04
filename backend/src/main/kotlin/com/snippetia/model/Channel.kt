package com.snippetia.model

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "channels")
@EntityListeners(AuditingEntityListener::class)
data class Channel(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    var owner: User,

    @Column(name = "name", unique = true, nullable = false, length = 50)
    var name: String,

    @Column(name = "display_name", nullable = false, length = 100)
    var displayName: String,

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "avatar_url")
    var avatarUrl: String? = null,

    @Column(name = "banner_url")
    var bannerUrl: String? = null,

    @ElementCollection
    @CollectionTable(name = "channel_tags", joinColumns = [JoinColumn(name = "channel_id")])
    @Column(name = "tag")
    var tags: MutableSet<String> = mutableSetOf(),

    @Column(name = "is_verified")
    var isVerified: Boolean = false,

    @Column(name = "subscriber_count")
    var subscriberCount: Long = 0,

    @Column(name = "follower_count")
    var followerCount: Long = 0,

    @Column(name = "snippet_count")
    var snippetCount: Long = 0,

    @Column(name = "total_stars")
    var totalStars: Long = 0,

    @Column(name = "subscription_enabled")
    var subscriptionEnabled: Boolean = false,

    @Column(name = "basic_tier_price", precision = 10, scale = 2)
    var basicTierPrice: BigDecimal? = null,

    @Column(name = "premium_tier_price", precision = 10, scale = 2)
    var premiumTierPrice: BigDecimal? = null,

    @Column(name = "enterprise_tier_price", precision = 10, scale = 2)
    var enterpriseTierPrice: BigDecimal? = null,

    @Column(name = "total_earnings", precision = 15, scale = 2)
    var totalEarnings: BigDecimal = BigDecimal.ZERO,

    @Column(name = "monthly_earnings", precision = 15, scale = 2)
    var monthlyEarnings: BigDecimal = BigDecimal.ZERO,

    @Column(name = "currency", length = 3)
    var currency: String = "USD",

    @Column(name = "stripe_account_id")
    var stripeAccountId: String? = null,

    @Column(name = "payout_enabled")
    var payoutEnabled: Boolean = false,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: ChannelStatus = ChannelStatus.ACTIVE,

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility")
    var visibility: ChannelVisibility = ChannelVisibility.PUBLIC,

    @Column(name = "website_url")
    var websiteUrl: String? = null,

    @Column(name = "github_url")
    var githubUrl: String? = null,

    @Column(name = "twitter_url")
    var twitterUrl: String? = null,

    @Column(name = "linkedin_url")
    var linkedinUrl: String? = null,

    @Column(name = "discord_url")
    var discordUrl: String? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class ChannelStatus {
    ACTIVE, SUSPENDED, ARCHIVED, PENDING_REVIEW, MONETIZATION_DISABLED
}

enum class ChannelVisibility {
    PUBLIC, PRIVATE, UNLISTED
}

@Entity
@Table(name = "channel_subscriptions")
@EntityListeners(AuditingEntityListener::class)
data class ChannelSubscription(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    var channel: Channel,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_id", nullable = false)
    var subscriber: User,

    @Enumerated(EnumType.STRING)
    @Column(name = "tier", nullable = false)
    var tier: SubscriptionTier,

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    var amount: BigDecimal,

    @Column(name = "currency", length = 3, nullable = false)
    var currency: String = "USD",

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: SubscriptionStatus = SubscriptionStatus.ACTIVE,

    @Column(name = "start_date", nullable = false)
    var startDate: LocalDateTime,

    @Column(name = "end_date")
    var endDate: LocalDateTime? = null,

    @Column(name = "auto_renew", nullable = false)
    var autoRenew: Boolean = true,

    @Column(name = "stripe_subscription_id")
    var stripeSubscriptionId: String? = null,

    @Column(name = "cancelled_at")
    var cancelledAt: LocalDateTime? = null,

    @Column(name = "cancellation_reason")
    var cancellationReason: String? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class SubscriptionTier {
    BASIC, PREMIUM, ENTERPRISE, CUSTOM
}

enum class SubscriptionStatus {
    ACTIVE, CANCELLED, EXPIRED, PAUSED, PENDING, PAST_DUE
}

@Entity
@Table(name = "channel_followers")
@EntityListeners(AuditingEntityListener::class)
data class ChannelFollower(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    var channel: Channel,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    var follower: User,

    @Column(name = "notification_enabled", nullable = false)
    var notificationEnabled: Boolean = true,

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "follower_notification_types", joinColumns = [JoinColumn(name = "follower_id")])
    @Column(name = "notification_type")
    var notificationTypes: MutableSet<NotificationType> = mutableSetOf(
        NotificationType.NEW_SNIPPET,
        NotificationType.SNIPPET_UPDATE
    ),

    @CreatedDate
    @Column(name = "followed_at", nullable = false, updatable = false)
    var followedAt: LocalDateTime = LocalDateTime.now()
)

enum class NotificationType {
    NEW_SNIPPET, SNIPPET_UPDATE, CHANNEL_UPDATE, LIVE_STREAM, ANNOUNCEMENT
}

@Entity
@Table(name = "channel_contributors")
@EntityListeners(AuditingEntityListener::class)
data class ChannelContributor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    var channel: Channel,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    var role: ContributorRole,

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "contributor_permissions", joinColumns = [JoinColumn(name = "contributor_id")])
    @Column(name = "permission")
    var permissions: MutableSet<Permission> = mutableSetOf(),

    @Column(name = "revenue_share_percentage", precision = 5, scale = 2)
    var revenueSharePercentage: BigDecimal? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by")
    var invitedBy: User? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: ContributorStatus = ContributorStatus.ACTIVE,

    @CreatedDate
    @Column(name = "joined_at", nullable = false, updatable = false)
    var joinedAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class ContributorRole {
    OWNER, ADMIN, MODERATOR, CONTRIBUTOR, VIEWER
}

enum class Permission {
    CREATE_SNIPPET, EDIT_SNIPPET, DELETE_SNIPPET, MANAGE_CONTRIBUTORS, 
    MANAGE_SETTINGS, VIEW_ANALYTICS, MANAGE_MONETIZATION, MODERATE_COMMENTS,
    TRANSFER_OWNERSHIP, MANAGE_PAYOUTS
}

enum class ContributorStatus {
    ACTIVE, PENDING, SUSPENDED, LEFT, INVITED
}

@Entity
@Table(name = "revenue_distributions")
@EntityListeners(AuditingEntityListener::class)
data class RevenueDistribution(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_subscription_id", nullable = false)
    var channelSubscription: ChannelSubscription,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    var channel: Channel,

    @Column(name = "total_amount", precision = 15, scale = 2, nullable = false)
    var totalAmount: BigDecimal,

    @Column(name = "platform_fee", precision = 15, scale = 2, nullable = false)
    var platformFee: BigDecimal,

    @Column(name = "creator_amount", precision = 15, scale = 2, nullable = false)
    var creatorAmount: BigDecimal,

    @Column(name = "currency", length = 3, nullable = false)
    var currency: String,

    @Column(name = "platform_fee_percentage", precision = 5, scale = 2, nullable = false)
    var platformFeePercentage: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(name = "payout_status", nullable = false)
    var payoutStatus: PayoutStatus = PayoutStatus.PENDING,

    @Column(name = "stripe_transfer_id")
    var stripeTransferId: String? = null,

    @Column(name = "payout_date")
    var payoutDate: LocalDateTime? = null,

    @CreatedDate
    @Column(name = "processed_at", nullable = false, updatable = false)
    var processedAt: LocalDateTime = LocalDateTime.now()
)

enum class PayoutStatus {
    PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED, ON_HOLD
}

@Entity
@Table(name = "snippet_ownership_transfers")
@EntityListeners(AuditingEntityListener::class)
data class SnippetOwnershipTransfer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "snippet_id", nullable = false)
    var snippet: CodeSnippet,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", nullable = false)
    var fromUser: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", nullable = false)
    var toUser: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_channel_id")
    var fromChannel: Channel? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_channel_id")
    var toChannel: Channel? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: TransferStatus = TransferStatus.PENDING,

    @Column(name = "transfer_reason")
    var transferReason: String? = null,

    @Column(name = "accepted_at")
    var acceptedAt: LocalDateTime? = null,

    @Column(name = "rejected_at")
    var rejectedAt: LocalDateTime? = null,

    @Column(name = "rejection_reason")
    var rejectionReason: String? = null,

    @CreatedDate
    @Column(name = "requested_at", nullable = false, updatable = false)
    var requestedAt: LocalDateTime = LocalDateTime.now()
)

enum class TransferStatus {
    PENDING, ACCEPTED, REJECTED, CANCELLED, EXPIRED
}