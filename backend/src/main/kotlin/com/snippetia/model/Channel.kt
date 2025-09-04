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

    @Column(name = "website_url")
    var websiteUrl: String? = null,

    @Column(name = "github_url")
    var githubUrl: String? = null,

    @Column(name = "twitter_url")
    var twitterUrl: String? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)