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
        // Implementation for OAuth2 success handling
        // This would typically involve extracting user info from the OAuth2 token
        // and creating or updating the user in the database
        throw NotImplementedError("OAuth2 success handling not implemented yet")
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