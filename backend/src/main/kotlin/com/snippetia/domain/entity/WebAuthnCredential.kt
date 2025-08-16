package com.snippetia.domain.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "webauthn_credentials")
@EntityListeners(AuditingEntityListener::class)
data class WebAuthnCredential(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "credential_id", nullable = false, unique = true)
    val credentialId: String,

    @Column(name = "public_key", columnDefinition = "TEXT", nullable = false)
    val publicKey: String,

    @Column(name = "signature_count", nullable = false)
    val signatureCount: Long = 0,

    @Column(name = "aaguid")
    val aaguid: String? = null,

    @Column(name = "credential_name")
    val credentialName: String? = null,

    @Column(name = "device_type")
    val deviceType: String? = null,

    @Column(name = "is_backup_eligible")
    val isBackupEligible: Boolean = false,

    @Column(name = "is_backup_state")
    val isBackupState: Boolean = false,

    @Column(name = "last_used_at")
    val lastUsedAt: LocalDateTime? = null,

    @Column(name = "is_active")
    val isActive: Boolean = true,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "oauth_accounts")
@EntityListeners(AuditingEntityListener::class)
data class OAuthAccount(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "provider", nullable = false)
    val provider: String,

    @Column(name = "provider_user_id", nullable = false)
    val providerUserId: String,

    @Column(name = "provider_username")
    val providerUsername: String? = null,

    @Column(name = "provider_email")
    val providerEmail: String? = null,

    @Column(name = "provider_avatar_url")
    val providerAvatarUrl: String? = null,

    @Column(name = "access_token", columnDefinition = "TEXT")
    val accessToken: String? = null,

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    val refreshToken: String? = null,

    @Column(name = "token_expires_at")
    val tokenExpiresAt: LocalDateTime? = null,

    @Column(name = "scope")
    val scope: String? = null,

    @Column(name = "is_primary")
    val isPrimary: Boolean = false,

    @Column(name = "is_active")
    val isActive: Boolean = true,

    @Column(name = "last_sync_at")
    val lastSyncAt: LocalDateTime? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "user_sessions")
@EntityListeners(AuditingEntityListener::class)
data class UserSession(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "session_id", nullable = false, unique = true)
    val sessionId: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(name = "ip_address")
    val ipAddress: String? = null,

    @Column(name = "user_agent", columnDefinition = "TEXT")
    val userAgent: String? = null,

    @Column(name = "device_type")
    val deviceType: String? = null,

    @Column(name = "browser")
    val browser: String? = null,

    @Column(name = "operating_system")
    val operatingSystem: String? = null,

    @Column(name = "location")
    val location: String? = null,

    @Column(name = "is_active")
    val isActive: Boolean = true,

    @Column(name = "expires_at", nullable = false)
    val expiresAt: LocalDateTime,

    @Column(name = "last_activity_at")
    val lastActivityAt: LocalDateTime? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)