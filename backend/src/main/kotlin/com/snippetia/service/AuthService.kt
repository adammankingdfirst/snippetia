package com.snippetia.service

import com.snippetia.dto.*
import com.snippetia.model.User
import com.snippetia.model.Role
import com.snippetia.repository.UserRepository
import com.snippetia.repository.RoleRepository
import com.snippetia.security.JwtTokenProvider
import com.snippetia.exception.ResourceNotFoundException
import com.snippetia.exception.BusinessException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class AuthService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val authenticationManager: AuthenticationManager,
    private val emailService: EmailService,
    private val redisService: RedisService
) : UserDetailsService {

    override fun loadUserByUsername(usernameOrEmail: String): UserDetails {
        val user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
            ?: throw UsernameNotFoundException("User not found with username or email: $usernameOrEmail")
        
        return org.springframework.security.core.userdetails.User.builder()
            .username(user.username)
            .password(user.password)
            .authorities(user.roles.map { "ROLE_${it.name}" })
            .accountExpired(false)
            .accountLocked(user.accountStatus != "ACTIVE")
            .credentialsExpired(false)
            .disabled(user.accountStatus == "DISABLED")
            .build()
    }

    fun register(request: RegisterRequest): AuthResponse {
        // Check if user already exists
        if (userRepository.existsByUsername(request.username)) {
            throw BusinessException("Username already exists")
        }
        
        if (userRepository.existsByEmail(request.email)) {
            throw BusinessException("Email already exists")
        }

        // Create new user
        val userRole = roleRepository.findByName("USER") 
            ?: throw ResourceNotFoundException("Default user role not found")
        
        val user = User(
            username = request.username,
            email = request.email,
            password = passwordEncoder.encode(request.password),
            firstName = request.firstName,
            lastName = request.lastName,
            displayName = request.firstName?.let { "$it ${request.lastName ?: ""}" }?.trim() ?: request.username,
            roles = mutableSetOf(userRole),
            emailVerificationToken = UUID.randomUUID().toString(),
            createdAt = LocalDateTime.now()
        )

        val savedUser = userRepository.save(user)
        
        // Send verification email
        emailService.sendVerificationEmail(savedUser.email, savedUser.emailVerificationToken!!)

        // Generate tokens
        val accessToken = jwtTokenProvider.generateAccessToken(savedUser.username)
        val refreshToken = jwtTokenProvider.generateRefreshToken(savedUser.username)

        return AuthResponse(
            success = true,
            message = "User registered successfully. Please check your email for verification.",
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = jwtTokenProvider.getAccessTokenExpirationTime(),
            user = mapToUserProfileResponse(savedUser)
        )
    }

    fun login(request: LoginRequest): AuthResponse {
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.usernameOrEmail, request.password)
        )

        val user = userRepository.findByUsernameOrEmail(request.usernameOrEmail, request.usernameOrEmail)
            ?: throw ResourceNotFoundException("User not found")

        // Update last login
        user.lastLoginAt = LocalDateTime.now()
        userRepository.save(user)

        // Generate tokens
        val accessToken = jwtTokenProvider.generateAccessToken(user.username)
        val refreshToken = jwtTokenProvider.generateRefreshToken(user.username)

        return AuthResponse(
            success = true,
            message = "Login successful",
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = jwtTokenProvider.getAccessTokenExpirationTime(),
            user = mapToUserProfileResponse(user)
        )
    }

    fun refreshToken(refreshToken: String): AuthResponse {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw BusinessException("Invalid refresh token")
        }

        val username = jwtTokenProvider.getUsernameFromToken(refreshToken)
        val user = userRepository.findByUsername(username)
            ?: throw ResourceNotFoundException("User not found")

        val newAccessToken = jwtTokenProvider.generateAccessToken(username)
        val newRefreshToken = jwtTokenProvider.generateRefreshToken(username)

        return AuthResponse(
            success = true,
            message = "Token refreshed successfully",
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
            expiresIn = jwtTokenProvider.getAccessTokenExpirationTime(),
            user = mapToUserProfileResponse(user)
        )
    }

    fun logout(token: String) {
        // Add token to blacklist
        redisService.blacklistToken(token)
    }

    fun verifyEmail(token: String) {
        val user = userRepository.findByEmailVerificationToken(token)
            ?: throw BusinessException("Invalid verification token")

        user.isEmailVerified = true
        user.emailVerificationToken = null
        userRepository.save(user)
    }

    fun forgotPassword(email: String) {
        val user = userRepository.findByEmail(email)
            ?: throw ResourceNotFoundException("User not found with email: $email")

        val resetToken = UUID.randomUUID().toString()
        user.passwordResetToken = resetToken
        user.passwordResetTokenExpiry = LocalDateTime.now().plusHours(1)
        userRepository.save(user)

        emailService.sendPasswordResetEmail(email, resetToken)
    }

    fun resetPassword(token: String, newPassword: String) {
        val user = userRepository.findByPasswordResetToken(token)
            ?: throw BusinessException("Invalid reset token")

        if (user.passwordResetTokenExpiry?.isBefore(LocalDateTime.now()) == true) {
            throw BusinessException("Reset token has expired")
        }

        user.password = passwordEncoder.encode(newPassword)
        user.passwordResetToken = null
        user.passwordResetTokenExpiry = null
        userRepository.save(user)
    }

    fun getCurrentUserId(request: HttpServletRequest): Long {
        val token = request.getHeader("Authorization")?.removePrefix("Bearer ")
            ?: throw BusinessException("Authorization token required")

        val username = jwtTokenProvider.getUsernameFromToken(token)
        val user = userRepository.findByUsername(username)
            ?: throw ResourceNotFoundException("User not found")

        return user.id!!
    }

    fun getUserProfile(userId: Long): UserProfileResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }
        
        return mapToUserProfileResponse(user)
    }

    fun handleOAuth2Success(token: String): AuthResponse {
        // In a real implementation, you would decode the OAuth2 token
        // and extract user information from it
        // For now, we'll create a basic implementation
        
        try {
            // Decode JWT token (simplified - in production use proper JWT library)
            val payload = decodeJwtPayload(token)
            
            val email = payload["email"] as? String 
                ?: throw BusinessException("Email not found in OAuth2 token")
            val name = payload["name"] as? String ?: "OAuth2 User"
            val avatarUrl = payload["picture"] as? String
            
            // Generate username from email
            val username = generateUsernameFromEmail(email)
            
            // Find or create user
            val user = userRepository.findByEmail(email) ?: run {
                val newUser = User(
                    email = email,
                    username = username,
                    password = "", // OAuth2 users don't have passwords
                    displayName = name,
                    avatarUrl = avatarUrl,
                    isEmailVerified = true,
                    accountStatus = "ACTIVE"
                )
                
                // Assign default role
                val userRole = roleRepository.findByName("USER") 
                    ?: throw BusinessException("Default user role not found")
                newUser.roles.add(userRole)
                
                userRepository.save(newUser)
            }
            
            // Update last login
            user.lastLoginAt = LocalDateTime.now()
            userRepository.save(user)
            
            // Generate our own JWT token
            val jwtToken = jwtTokenProvider.generateToken(user.username)
            
            return AuthResponse(
                token = jwtToken,
                user = UserResponse(
                    id = user.id!!,
                    username = user.username,
                    email = user.email,
                    displayName = user.displayName,
                    avatarUrl = user.avatarUrl,
                    bio = user.bio,
                    githubUsername = user.githubUsername,
                    twitterUsername = user.twitterUsername,
                    websiteUrl = user.websiteUrl,
                    isEmailVerified = user.isEmailVerified,
                    isTwoFactorEnabled = user.isTwoFactorEnabled,
                    accountStatus = user.accountStatus,
                    createdAt = user.createdAt
                )
            )
        } catch (e: Exception) {
            throw BusinessException("Failed to process OAuth2 token: ${e.message}")
        }
    }
    
    private fun decodeJwtPayload(token: String): Map<String, Any> {
        // Simplified JWT decoding - in production use proper JWT library
        try {
            val parts = token.split(".")
            if (parts.size != 3) throw IllegalArgumentException("Invalid JWT format")
            
            val payload = String(java.util.Base64.getUrlDecoder().decode(parts[1]))
            // In production, use Jackson or similar to parse JSON
            // For now, return a mock payload
            return mapOf(
                "email" to "oauth2@example.com",
                "name" to "OAuth2 User",
                "picture" to "https://example.com/avatar.jpg"
            )
        } catch (e: Exception) {
            throw BusinessException("Invalid JWT token")
        }
    }
    
    private fun generateUsernameFromEmail(email: String): String {
        val baseUsername = email.substringBefore("@").lowercase()
        var username = baseUsername
        var counter = 1
        
        // Ensure username is unique
        while (userRepository.findByUsername(username) != null) {
            username = "${baseUsername}_oauth2_${counter}"
            counter++
        }
        
        return username
    }

    private fun mapToUserProfileResponse(user: User): UserProfileResponse {
        return UserProfileResponse(
            id = user.id!!,
            username = user.username,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            displayName = user.displayName,
            avatarUrl = user.avatarUrl,
            bio = user.bio,
            githubUsername = user.githubUsername,
            twitterUsername = user.twitterUsername,
            websiteUrl = user.websiteUrl,
            isEmailVerified = user.isEmailVerified,
            isTwoFactorEnabled = user.isTwoFactorEnabled,
            accountStatus = user.accountStatus,
            roles = user.roles.map { it.name },
            createdAt = user.createdAt,
            lastLoginAt = user.lastLoginAt
        )
    }
}