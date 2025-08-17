package com.snippetia.service

import com.snippetia.dto.UserProfileResponse
import com.snippetia.dto.UserSummaryResponse
import com.snippetia.model.User
import com.snippetia.repository.UserRepository
import com.snippetia.exception.ResourceNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsernameOrEmail(username, username)
            ?: throw UsernameNotFoundException("User not found: $username")
        
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

    fun getUserById(id: Long): User {
        return userRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("User not found with id: $id") }
    }

    fun getUserByUsername(username: String): User {
        return userRepository.findByUsername(username)
            ?: throw ResourceNotFoundException("User not found with username: $username")
    }

    fun getUserByEmail(email: String): User {
        return userRepository.findByEmail(email)
            ?: throw ResourceNotFoundException("User not found with email: $email")
    }

    fun getUserProfile(userId: Long): UserProfileResponse {
        val user = getUserById(userId)
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

    fun getAllUsers(pageable: Pageable): Page<User> {
        return userRepository.findAll(pageable)
    }

    fun updateUser(user: User): User {
        return userRepository.save(user)
    }

    fun deleteUser(id: Long) {
        val user = getUserById(id)
        userRepository.delete(user)
    }

    fun existsByUsername(username: String): Boolean {
        return userRepository.existsByUsername(username)
    }

    fun existsByEmail(email: String): Boolean {
        return userRepository.existsByEmail(email)
    }

    fun updateLastActivity(userId: Long) {
        val user = getUserById(userId)
        user.lastLoginAt = LocalDateTime.now()
        userRepository.save(user)
    }
}