package com.snippetia.controller

import com.snippetia.dto.*
import com.snippetia.service.AuthService
import com.snippetia.service.WebAuthnService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
class AuthController(
    private val authService: AuthService,
    private val webAuthnService: WebAuthnService
) {

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> {
        val response = authService.register(request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        val response = authService.login(request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    fun refreshToken(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<AuthResponse> {
        val response = authService.refreshToken(request.refreshToken)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user")
    fun logout(request: HttpServletRequest): ResponseEntity<ApiResponse> {
        val token = request.getHeader("Authorization")?.removePrefix("Bearer ")
        if (token != null) {
            authService.logout(token)
        }
        return ResponseEntity.ok(ApiResponse(success = true, message = "Logged out successfully"))
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email address")
    fun verifyEmail(@Valid @RequestBody request: VerifyEmailRequest): ResponseEntity<ApiResponse> {
        authService.verifyEmail(request.token)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Email verified successfully"))
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset")
    fun forgotPassword(@Valid @RequestBody request: ForgotPasswordRequest): ResponseEntity<ApiResponse> {
        authService.forgotPassword(request.email)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Password reset email sent"))
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password")
    fun resetPassword(@Valid @RequestBody request: ResetPasswordRequest): ResponseEntity<ApiResponse> {
        authService.resetPassword(request.token, request.newPassword)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Password reset successfully"))
    }

    // WebAuthn endpoints
    @PostMapping("/webauthn/register/begin")
    @Operation(summary = "Begin WebAuthn registration")
    fun beginWebAuthnRegistration(request: HttpServletRequest): ResponseEntity<WebAuthnRegistrationBeginResponse> {
        val userId = authService.getCurrentUserId(request)
        val response = webAuthnService.beginRegistration(userId)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/webauthn/register/finish")
    @Operation(summary = "Finish WebAuthn registration")
    fun finishWebAuthnRegistration(
        @Valid @RequestBody request: WebAuthnRegistrationFinishRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ApiResponse> {
        val userId = authService.getCurrentUserId(httpRequest)
        webAuthnService.finishRegistration(userId, request)
        return ResponseEntity.ok(ApiResponse(success = true, message = "WebAuthn credential registered successfully"))
    }

    @PostMapping("/webauthn/authenticate/begin")
    @Operation(summary = "Begin WebAuthn authentication")
    fun beginWebAuthnAuthentication(@Valid @RequestBody request: WebAuthnAuthenticationBeginRequest): ResponseEntity<WebAuthnAuthenticationBeginResponse> {
        val response = webAuthnService.beginAuthentication(request.username)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/webauthn/authenticate/finish")
    @Operation(summary = "Finish WebAuthn authentication")
    fun finishWebAuthnAuthentication(@Valid @RequestBody request: WebAuthnAuthenticationFinishRequest): ResponseEntity<AuthResponse> {
        val response = webAuthnService.finishAuthentication(request)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/oauth2/success")
    @Operation(summary = "OAuth2 success callback")
    fun oauth2Success(@RequestParam token: String): ResponseEntity<AuthResponse> {
        val response = authService.handleOAuth2Success(token)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    fun getCurrentUser(request: HttpServletRequest): ResponseEntity<UserProfileResponse> {
        val userId = authService.getCurrentUserId(request)
        val profile = authService.getUserProfile(userId)
        return ResponseEntity.ok(profile)
    }
}