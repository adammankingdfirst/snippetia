package com.snippetia.application.service

import com.snippetia.domain.entity.User
import com.snippetia.domain.entity.UserFollow
import com.snippetia.domain.entity.UserSupport
import com.snippetia.domain.entity.AccountStatus
import com.snippetia.domain.repository.UserRepository
import com.snippetia.application.dto.UserProfileResponse
import com.snippetia.application.dto.UserSummaryResponse
import com.snippetia.application.dto.UpdateProfileRequest
import com.snippetia.application.dto.FollowUserRequest
import com.snippetia.application.dto.SupportUserRequest
import com.snippetia.infrastructure.exception.ResourceNotFoundException
import com.snippetia.infrastructure.exception.BusinessException
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
    private val userRepository: UserRepository,
    private val notificationService: NotificationService,
    private val paymentService: PaymentService,
    private val reputationService: ReputationService
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        return userRepository.findByEmailOrUsername(username, username)
            .orElseThrow { UsernameNotFoundException("User not found: $username") }
    }

    fun getUserById(id: Long): User {
        return userRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("User not found with id: $id") }
    }

    fun getUserByUsername(username: String): User {
        return userRepository.findByUsername(username)
            .orElseThrow { ResourceNotFoundException("User not found with username: $username") }
    }

    fun getUserByEmail(email: String): User {
        return userRepository.findByEmail(email)
            .orElseThrow { ResourceNotFoundException("User not found with email: $email") }
    }

    fun getUserProfile(userId: Long): UserProfileResponse {
        val user = getUserById(userId)
        return user.toProfileResponse()
    }

    fun updateProfile(userId: Long, request: UpdateProfileRequest): UserProfileResponse {
        val user = getUserById(userId)
        
        val updatedUser = user.copy(
            firstName = request.firstName,
            lastName = request.lastName,
            bio = request.bio,
            location = request.location,
            company = request.company,
            jobTitle = request.jobTitle,
            websiteUrl = request.websiteUrl,
            githubUsername = request.githubUsername,
            twitterUsername = request.twitterUsername,
            linkedinUsername = request.linkedinUsername,
            skills = request.skills.toSet(),
            yearsOfExperience = request.yearsOfExperience,
            updatedAt = LocalDateTime.now()
        )
        
        val savedUser = userRepository.save(updatedUser)
        return savedUser.toProfileResponse()
    }

    fun followUser(followerId: Long, request: FollowUserRequest): Boolean {
        val follower = getUserById(followerId)
        val following = getUserById(request.userId)
        
        if (follower.id == following.id) {
            throw BusinessException("Cannot follow yourself")
        }
        
        if (follower.isFollowing(following.id)) {
            throw BusinessException("Already following this user")
        }
        
        val userFollow = UserFollow(
            follower = follower,
            following = following
        )
        
        // This would be handled by a UserFollowRepository
        // userFollowRepository.save(userFollow)
        
        userRepository.incrementFollowerCount(following.id)
        userRepository.incrementFollowingCount(follower.id)
        
        // Send notification
        notificationService.sendFollowNotification(following, follower)
        
        // Update reputation
        reputationService.updateReputationForFollow(following.id)
        
        return true
    }

    fun unfollowUser(followerId: Long, userId: Long): Boolean {
        val follower = getUserById(followerId)
        val following = getUserById(userId)
        
        if (!follower.isFollowing(following.id)) {
            throw BusinessException("Not following this user")
        }
        
        // This would be handled by a UserFollowRepository
        // userFollowRepository.deleteByFollowerAndFollowing(follower, following)
        
        userRepository.decrementFollowerCount(following.id)
        userRepository.decrementFollowingCount(follower.id)
        
        return true
    }

    fun supportUser(supporterId: Long, request: SupportUserRequest): UserSupport {
        val supporter = getUserById(supporterId)
        val recipient = getUserById(request.recipientId)
        
        if (supporter.id == recipient.id) {
            throw BusinessException("Cannot support yourself")
        }
        
        if (!recipient.canReceiveSupport()) {
            throw BusinessException("User cannot receive support at this time")
        }
        
        // Process payment
        val paymentResult = paymentService.processPayment(
            supporter,
            request.amount,
            request.currency,
            request.paymentMethod
        )
        
        val userSupport = UserSupport(
            supporter = supporter,
            recipient = recipient,
            amount = request.amount,
            currency = request.currency,
            message = request.message,
            paymentMethod = request.paymentMethod,
            paymentId = paymentResult.paymentId,
            isAnonymous = request.isAnonymous
        )
        
        // This would be handled by a UserSupportRepository
        // val savedSupport = userSupportRepository.save(userSupport)
        
        // Send notification
        notificationService.sendSupportReceivedNotification(recipient, supporter, request.amount, request.isAnonymous)
        
        // Update reputation
        reputationService.updateReputationForSupport(recipient.id, request.amount)
        
        return userSupport
    }

    fun searchUsers(query: String, pageable: Pageable): Page<UserSummaryResponse> {
        return userRepository.searchUsers(query, pageable)
            .map { it.toSummaryResponse() }
    }

    fun getPopularUsers(pageable: Pageable): Page<UserSummaryResponse> {
        return userRepository.findPopularUsers(100, pageable)
            .map { it.toSummaryResponse() }
    }

    fun getTopUsers(pageable: Pageable): Page<UserSummaryResponse> {
        return userRepository.findTopUsers(1000, pageable)
            .map { it.toSummaryResponse() }
    }

    fun updateLastActivity(userId: Long) {
        userRepository.updateLastActivity(userId, LocalDateTime.now())
    }

    fun deactivateUser(userId: Long, reason: String) {
        val user = getUserById(userId)
        val updatedUser = user.copy(
            isActive = false,
            accountStatus = AccountStatus.SUSPENDED,
            updatedAt = LocalDateTime.now()
        )
        userRepository.save(updatedUser)
        
        // Send notification
        notificationService.sendAccountStatusNotification(user, AccountStatus.SUSPENDED, reason)
    }

    fun reactivateUser(userId: Long) {
        val user = getUserById(userId)
        val updatedUser = user.copy(
            isActive = true,
            accountStatus = AccountStatus.ACTIVE,
            updatedAt = LocalDateTime.now()
        )
        userRepository.save(updatedUser)
        
        // Send notification
        notificationService.sendAccountStatusNotification(user, AccountStatus.ACTIVE, "Account reactivated")
    }

    fun verifyDeveloper(userId: Long) {
        val user = getUserById(userId)
        val updatedUser = user.copy(
            isVerifiedDeveloper = true,
            updatedAt = LocalDateTime.now()
        )
        userRepository.save(updatedUser)
        
        // Send notification
        notificationService.sendDeveloperVerificationNotification(user)
        
        // Update reputation
        reputationService.updateReputationForVerification(userId)
    }

    fun getUserStatistics(): Map<String, Any> {
        val totalUsers = userRepository.count()
        val activeUsers = userRepository.countActiveUsers(LocalDateTime.now().minusDays(30))
        val newUsers = userRepository.countNewUsers(LocalDateTime.now().minusDays(7))
        val premiumUsers = userRepository.countPremiumUsers()
        
        return mapOf(
            "totalUsers" to totalUsers,
            "activeUsers" to activeUsers,
            "newUsers" to newUsers,
            "premiumUsers" to premiumUsers
        )
    }
}

// Extension functions for model to DTO conversion
private fun User.toProfileResponse(): UserProfileResponse {
    return UserProfileResponse(
        id = id,
        username = username,
        email = email,
        firstName = firstName,
        lastName = lastName,
        displayName = getDisplayName(),
        fullName = getFullName(),
        avatarUrl = avatarUrl,
        bio = bio,
        location = location,
        company = company,
        jobTitle = jobTitle,
        websiteUrl = websiteUrl,
        githubUsername = githubUsername,
        twitterUsername = twitterUsername,
        linkedinUsername = linkedinUsername,
        skills = skills.toList(),
        yearsOfExperience = yearsOfExperience,
        isEmailVerified = isEmailVerified,
        isTwoFactorEnabled = isTwoFactorEnabled,
        isVerifiedDeveloper = isVerifiedDeveloper,
        isPremium = isPremium,
        followerCount = followerCount,
        followingCount = followingCount,
        snippetCount = snippetCount,
        totalLikesReceived = totalLikesReceived,
        totalForksReceived = totalForksReceived,
        reputationScore = reputationScore,
        accountStatus = accountStatus.name,
        roles = roles.map { it.name },
        createdAt = createdAt,
        lastLoginAt = lastLoginAt,
        lastActivityAt = lastActivityAt
    )
}

private fun User.toSummaryResponse(): UserSummaryResponse {
    return UserSummaryResponse(
        id = id,
        username = username,
        displayName = getDisplayName(),
        avatarUrl = avatarUrl,
        bio = bio,
        location = location,
        company = company,
        isVerifiedDeveloper = isVerifiedDeveloper,
        followerCount = followerCount,
        snippetCount = snippetCount,
        reputationScore = reputationScore
    )
}