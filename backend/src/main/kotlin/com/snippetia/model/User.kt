package com.snippetia.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener::class)
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true, nullable = false)
    var email: String,

    @Column(unique = true, nullable = false)
    var username: String,

    @JsonIgnore
    @Column(nullable = false)
    var password: String,

    @Column(name = "first_name")
    var firstName: String? = null,

    @Column(name = "last_name")
    var lastName: String? = null,

    @Column(name = "display_name")
    var displayName: String,

    @Column(name = "avatar_url")
    var avatarUrl: String? = null,

    @Column(name = "bio", columnDefinition = "TEXT")
    var bio: String? = null,

    @Column(name = "github_username")
    var githubUsername: String? = null,

    @Column(name = "twitter_username")
    var twitterUsername: String? = null,

    @Column(name = "website_url")
    var websiteUrl: String? = null,

    @Column(name = "is_email_verified")
    var isEmailVerified: Boolean = false,

    @Column(name = "is_two_factor_enabled")
    var isTwoFactorEnabled: Boolean = false,

    @Column(name = "account_status")
    var accountStatus: String = "ACTIVE",

    @Column(name = "is_suspended")
    var isSuspended: Boolean = false,

    @Column(name = "suspension_reason")
    var suspensionReason: String? = null,

    @Column(name = "suspended_until")
    var suspendedUntil: LocalDateTime? = null,

    @Column(name = "email_verification_token")
    var emailVerificationToken: String? = null,

    @Column(name = "password_reset_token")
    var passwordResetToken: String? = null,

    @Column(name = "password_reset_token_expiry")
    var passwordResetTokenExpiry: LocalDateTime? = null,

    @Column(name = "last_login_at")
    var lastLoginAt: LocalDateTime? = null,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    var roles: MutableSet<Role> = mutableSetOf(),

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)