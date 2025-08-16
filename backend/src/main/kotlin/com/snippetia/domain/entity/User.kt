package com.snippetia.domain.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener::class)
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    val email: String,

    @Column(unique = true, nullable = false)
    val username: String,

    @JsonIgnore
    @Column(nullable = false)
    private val password: String,

    @Column(name = "first_name")
    val firstName: String? = null,

    @Column(name = "last_name")
    val lastName: String? = null,

    @Column(name = "avatar_url")
    val avatarUrl: String? = null,

    @Column(name = "bio", columnDefinition = "TEXT")
    val bio: String? = null,

    @Column(name = "github_username")
    val githubUsername: String? = null,

    @Column(name = "twitter_username")
    val twitterUsername: String? = null,

    @Column(name = "linkedin_username")
    val linkedinUsername: String? = null,

    @Column(name = "website_url")
    val websiteUrl: String? = null,

    @Column(name = "location")
    val location: String? = null,

    @Column(name = "company")
    val company: String? = null,

    @Column(name = "job_title")
    val jobTitle: String? = null,

    @ElementCollection
    @CollectionTable(name = "user_skills", joinColumns = [JoinColumn(name = "user_id")])
    @Column(name = "skill")
    val skills: Set<String> = setOf(),

    @Column(name = "years_of_experience")
    val yearsOfExperience: Int? = null,

    @Column(name = "is_email_verified")
    val isEmailVerified: Boolean = false,

    @Column(name = "is_two_factor_enabled")
    val isTwoFactorEnabled: Boolean = false,

    @Column(name = "is_active")
    val isActive: Boolean = true,

    @Column(name = "is_verified_developer")
    val isVerifiedDeveloper: Boolean = false,

    @Column(name = "is_premium")
    val isPremium: Boolean = false,

    @Column(name = "premium_expires_at")
    val premiumExpiresAt: LocalDateTime? = null,

    @Column(name = "follower_count")
    val followerCount: Long = 0,

    @Column(name = "following_count")
    val followingCount: Long = 0,

    @Column(name = "snippet_count")
    val snippetCount: Long = 0,

    @Column(name = "total_likes_received")
    val totalLikesReceived: Long = 0,

    @Column(name = "total_forks_received")
    val totalForksReceived: Long = 0,

    @Column(name = "reputation_score")
    val reputationScore: Long = 0,

    @Column(name = "coffee_balance", precision = 10, scale = 2)
    val coffeeBalance: BigDecimal = BigDecimal.ZERO,

    @Column(name = "stripe_customer_id")
    val stripeCustomerId: String? = null,

    @Column(name = "paypal_customer_id")
    val paypalCustomerId: String? = null,

    @Column(name = "notification_preferences", columnDefinition = "TEXT")
    val notificationPreferences: String = "{}",

    @Column(name = "privacy_settings", columnDefinition = "TEXT")
    val privacySettings: String = "{}",

    @Column(name = "theme_preference")
    val themePreference: String = "SYSTEM",

    @Column(name = "language_preference")
    val languagePreference: String = "en",

    @Column(name = "timezone")
    val timezone: String = "UTC",

    @Column(name = "last_login_at")
    val lastLoginAt: LocalDateTime? = null,

    @Column(name = "last_activity_at")
    val lastActivityAt: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status")
    val accountStatus: AccountStatus = AccountStatus.ACTIVE,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    val roles: Set<Role> = setOf(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val snippets: List<CodeSnippet> = listOf(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val webAuthnCredentials: List<WebAuthnCredential> = listOf(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val oauthAccounts: List<OAuthAccount> = listOf(),

    @OneToMany(mappedBy = "follower", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val following: List<UserFollow> = listOf(),

    @OneToMany(mappedBy = "following", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val followers: List<UserFollow> = listOf(),

    @OneToMany(mappedBy = "recipient", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val receivedSupports: List<UserSupport> = listOf(),

    @OneToMany(mappedBy = "supporter", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val givenSupports: List<UserSupport> = listOf(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val notifications: List<Notification> = listOf(),

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return roles.flatMap { role ->
            role.permissions.map { permission ->
                SimpleGrantedAuthority("ROLE_${permission.name}")
            } + SimpleGrantedAuthority("ROLE_${role.name}")
        }
    }

    override fun getPassword(): String = password
    override fun getUsername(): String = username
    override fun isAccountNonExpired(): Boolean = accountStatus != AccountStatus.EXPIRED
    override fun isAccountNonLocked(): Boolean = accountStatus != AccountStatus.LOCKED
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = isActive && accountStatus == AccountStatus.ACTIVE

    fun getDisplayName(): String = "$firstName $lastName".trim().ifEmpty { username }
    
    fun getFullName(): String = "$firstName $lastName".trim()
    
    fun isFollowing(userId: Long): Boolean = following.any { it.following.id == userId }
    
    fun canReceiveSupport(): Boolean = isActive && accountStatus == AccountStatus.ACTIVE
}

enum class AccountStatus {
    ACTIVE, SUSPENDED, LOCKED, EXPIRED, PENDING_VERIFICATION, BANNED
}

@Entity
@Table(name = "user_follows")
@EntityListeners(AuditingEntityListener::class)
data class UserFollow(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    val follower: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", nullable = false)
    val following: User,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "user_supports")
@EntityListeners(AuditingEntityListener::class)
data class UserSupport(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supporter_id", nullable = false)
    val supporter: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    val recipient: User,

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    val amount: BigDecimal,

    @Column(name = "currency", nullable = false)
    val currency: String = "USD",

    @Column(name = "message", columnDefinition = "TEXT")
    val message: String? = null,

    @Column(name = "payment_method")
    val paymentMethod: String,

    @Column(name = "payment_id")
    val paymentId: String,

    @Column(name = "is_anonymous")
    val isAnonymous: Boolean = false,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    val status: SupportStatus = SupportStatus.PENDING,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class SupportStatus {
    PENDING, COMPLETED, FAILED, REFUNDED
}