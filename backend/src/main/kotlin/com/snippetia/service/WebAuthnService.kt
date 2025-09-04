package com.snippetia.service

import com.snippetia.dto.*
import com.snippetia.model.*
import com.snippetia.repository.*
import com.snippetia.exception.ResourceNotFoundException
import com.snippetia.exception.BusinessException
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class WebAuthnService(
    private val userRepository: UserRepository,
    private val webAuthnCredentialRepository: WebAuthnCredentialRepository
) {

    fun beginRegistration(userId: Long): WebAuthnRegistrationBeginResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        // Generate challenge and other registration options
        val challenge = generateChallenge()
        val credentialId = generateCredentialId()
        
        // Store challenge temporarily (in production, use Redis or similar)
        // For now, we'll create a basic response
        
        return WebAuthnRegistrationBeginResponse(
            challenge = challenge,
            rp = RelyingParty(
                name = "Snippetia",
                id = "snippetia.com"
            ),
            user = WebAuthnUser(
                id = user.id.toString(),
                name = user.username,
                displayName = user.displayName
            ),
            pubKeyCredParams = listOf(
                PubKeyCredParam(type = "public-key", alg = -7), // ES256
                PubKeyCredParam(type = "public-key", alg = -257) // RS256
            ),
            timeout = 60000,
            attestation = "none",
            authenticatorSelection = AuthenticatorSelection(
                authenticatorAttachment = "platform",
                userVerification = "required"
            )
        )
    }

    fun finishRegistration(userId: Long, request: WebAuthnRegistrationFinishRequest) {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        // In a real implementation, you would:
        // 1. Verify the attestation response
        // 2. Validate the challenge
        // 3. Extract and store the public key
        
        // For now, we'll create a basic credential record
        val credential = WebAuthnCredential(
            user = user,
            credentialId = request.credentialId,
            publicKey = request.publicKey,
            signCount = 0,
            name = request.name ?: "WebAuthn Key"
        )
        
        webAuthnCredentialRepository.save(credential)
    }

    fun beginAuthentication(username: String): WebAuthnAuthenticationBeginResponse {
        val user = userRepository.findByUsername(username)
            ?: throw ResourceNotFoundException("User not found")

        val credentials = webAuthnCredentialRepository.findByUser(user)
        val challenge = generateChallenge()
        
        return WebAuthnAuthenticationBeginResponse(
            challenge = challenge,
            timeout = 60000,
            rpId = "snippetia.com",
            allowCredentials = credentials.map { 
                AllowedCredential(
                    type = "public-key",
                    id = it.credentialId
                )
            },
            userVerification = "required"
        )
    }

    fun finishAuthentication(request: WebAuthnAuthenticationFinishRequest): AuthResponse {
        // In a real implementation, you would:
        // 1. Verify the assertion response
        // 2. Validate the challenge
        // 3. Verify the signature using the stored public key
        // 4. Update the sign count
        
        val credential = webAuthnCredentialRepository.findByCredentialId(request.credentialId)
            ?: throw BusinessException("Invalid credential")
        
        // Update sign count
        credential.signCount++
        credential.lastUsedAt = LocalDateTime.now()
        webAuthnCredentialRepository.save(credential)
        
        // Generate JWT token (simplified)
        val token = "jwt_token_placeholder" // In real implementation, generate actual JWT
        
        return AuthResponse(
            token = token,
            user = UserResponse(
                id = credential.user.id!!,
                username = credential.user.username,
                email = credential.user.email,
                displayName = credential.user.displayName,
                avatarUrl = credential.user.avatarUrl
            )
        )
    }

    private fun generateChallenge(): String {
        // Generate a cryptographically secure random challenge
        val bytes = ByteArray(32)
        java.security.SecureRandom().nextBytes(bytes)
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun generateCredentialId(): String {
        val bytes = ByteArray(16)
        java.security.SecureRandom().nextBytes(bytes)
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}

// WebAuthn DTOs
data class WebAuthnRegistrationBeginResponse(
    val challenge: String,
    val rp: RelyingParty,
    val user: WebAuthnUser,
    val pubKeyCredParams: List<PubKeyCredParam>,
    val timeout: Long,
    val attestation: String,
    val authenticatorSelection: AuthenticatorSelection
)

data class RelyingParty(
    val name: String,
    val id: String
)

data class WebAuthnUser(
    val id: String,
    val name: String,
    val displayName: String
)

data class PubKeyCredParam(
    val type: String,
    val alg: Int
)

data class AuthenticatorSelection(
    val authenticatorAttachment: String,
    val userVerification: String
)

data class WebAuthnRegistrationFinishRequest(
    val credentialId: String,
    val publicKey: String,
    val name: String? = null
)

data class WebAuthnAuthenticationBeginResponse(
    val challenge: String,
    val timeout: Long,
    val rpId: String,
    val allowCredentials: List<AllowedCredential>,
    val userVerification: String
)

data class AllowedCredential(
    val type: String,
    val id: String
)

data class WebAuthnAuthenticationFinishRequest(
    val credentialId: String,
    val signature: String,
    val authenticatorData: String,
    val clientDataJSON: String
)