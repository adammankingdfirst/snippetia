package com.snippetia.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
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

    @Column(name = "website_url")
    val websiteUrl: String? = null,

    @Column(name = "is_email_verified")
    val isEmailVerified: Boolean = false,

    @Column(name = "is_two_factor_enabled")
    val isTwoFactorEnabled: Boolean = false,

    @Column(name = "is_active")
    val isActive: Boolean = true,

    @Column(name = "last_login_at")
    val lastLoginAt: LocalDateTime? = null,

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
}

enum class AccountStatus {
    ACTIVE, SUSPENDED, LOCKED, EXPIRED, PENDING_VERIFICATION
}