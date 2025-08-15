package com.snippetia.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

// Request DTOs
data class RegisterRequest(
    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    val username: String,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email must be valid")
    val email: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    val password: String,

    val firstName: String? = null,
    val lastName: String? = null
)

data class LoginRequest(
    @field:NotBlank(message = "Username or email is required")
    val usernameOrEmail: String,

    @field:NotBlank(message = "Password is required")
    val password: String,

    val rememberMe: Boolean = false
)

data class RefreshTokenRequest(
    @field:NotBlank(message = "Refresh token is required")
    val refreshToken: String
)

data class VerifyEmailRequest(
    @field:NotBlank(message = "Verification token is required")
    val token: String
)

data class ForgotPasswordRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email must be valid")
    val email: String
)

data class ResetPasswordRequest(
    @field:NotBlank(message = "Reset token is required")
    val token: String,

    @field:NotBlank(message = "New password is required")
    @field:Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    val newPassword: String
)

// WebAuthn DTOs
data class WebAuthnRegistrationBeginRequest(
    val credentialName: String? = null
)

data class WebAuthnRegistrationBeginResponse(
    val challenge: String,
    val user: WebAuthnUserResponse,
    val rp: WebAuthnRelyingPartyResponse,
    val pubKeyCredParams: List<WebAuthnPubKeyCredParamResponse>,
    val timeout: Long,
    val excludeCredentials: List<WebAuthnCredentialDescriptorResponse>
)

data class WebAuthnRegistrationFinishRequest(
    val credentialId: String,
    val clientDataJSON: String,
    val attestationObject: String,
    val credentialName: String? = null
)

data class WebAuthnAuthenticationBeginRequest(
    @field:NotBlank(message = "Username is required")
    val username: String
)

data class WebAuthnAuthenticationBeginResponse(
    val challenge: String,
    val timeout: Long,
    val rpId: String,
    val allowCredentials: List<WebAuthnCredentialDescriptorResponse>
)

data class WebAuthnAuthenticationFinishRequest(
    val credentialId: String,
    val clientDataJSON: String,
    val authenticatorData: String,
    val signature: String,
    val userHandle: String?
)

data class WebAuthnUserResponse(
    val id: String,
    val name: String,
    val displayName: String
)

data class WebAuthnRelyingPartyResponse(
    val id: String,
    val name: String
)

data class WebAuthnPubKeyCredParamResponse(
    val type: String,
    val alg: Int
)

data class WebAuthnCredentialDescriptorResponse(
    val type: String,
    val id: String,
    val transports: List<String>
)

// Response DTOs
data class AuthResponse(
    val success: Boolean,
    val message: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val user: UserProfileResponse
)

data class UserProfileResponse(
    val id: Long,
    val username: String,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val displayName: String,
    val avatarUrl: String?,
    val bio: String?,
    val githubUsername: String?,
    val twitterUsername: String?,
    val websiteUrl: String?,
    val isEmailVerified: Boolean,
    val isTwoFactorEnabled: Boolean,
    val accountStatus: String,
    val roles: List<String>,
    val createdAt: LocalDateTime,
    val lastLoginAt: LocalDateTime?
)

data class UserSummaryResponse(
    val id: Long,
    val username: String,
    val displayName: String,
    val avatarUrl: String?
)

data class ApiResponse(
    val success: Boolean,
    val message: String,
    val data: Any? = null
)