package com.snippetia.service

import com.snippetia.model.*
import com.snippetia.repository.*
import com.snippetia.dto.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import kotlinx.coroutines.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class ChannelMonetizationService(
    private val channelRepository: ChannelRepository,
    private val channelSubscriptionRepository: ChannelSubscriptionRepository,
    private val channelFollowerRepository: ChannelFollowerRepository,
    private val channelContributorRepository: ChannelContributorRepository,
    private val revenueDistributionRepository: RevenueDistributionRepository,
    private val snippetOwnershipTransferRepository: SnippetOwnershipTransferRepository,
    private val userRepository: UserRepository,
    private val codeSnippetRepository: CodeSnippetRepository,
    private val notificationService: NotificationService,
    private val paymentService: PaymentService,
    private val eventPublisher: ApplicationEventPublisher
) {
    
    // Channel Subscription Management
    suspend fun subscribeToChannel(channelId: Long, subscriberId: Long, request: ChannelSubscriptionRequest): ChannelSubscriptionResponse {
        val channel = channelRepository.findById(channelId).orElseThrow { 
            IllegalArgumentException("Channel not found") 
        }
        val subscriber = userRepository.findById(subscriberId).orElseThrow { 
            IllegalArgumentException("User not found") 
        }
        
        require(channel.subscriptionEnabled) { "Channel monetization is not enabled" }
        
        // Check if already subscribed
        val existingSubscription = channelSubscriptionRepository.findByChannelAndSubscriberAndStatus(
            channel, subscriber, SubscriptionStatus.ACTIVE
        )
        
        if (existingSubscription != null) {
            throw IllegalStateException("User is already subscribed to this channel")
        }
        
        val amount = when (request.tier) {
            SubscriptionTier.BASIC -> channel.basicTierPrice ?: throw IllegalArgumentException("Basic tier not available")
            SubscriptionTier.PREMIUM -> channel.premiumTierPrice ?: throw IllegalArgumentException("Premium tier not available")
            SubscriptionTier.ENTERPRISE -> channel.enterpriseTierPrice ?: throw IllegalArgumentException("Enterprise tier not available")
            SubscriptionTier.CUSTOM -> request.customAmount ?: throw IllegalArgumentException("Custom amount required")
        }
        
        // Process payment through Stripe
        val paymentResult = paymentService.processChannelSubscription(
            subscriber = subscriber,
            channel = channel,
            amount = amount,
            tier = request.tier,
            paymentMethodId = request.paymentMethodId
        )
        
        if (!paymentResult.successful) {
            throw RuntimeException("Payment failed: ${paymentResult.errorMessage}")
        }
        
        // Create subscription
        val subscription = ChannelSubscription(
            channel = channel,
            subscriber = subscriber,
            tier = request.tier,
            amount = amount,
            currency = channel.currency ?: "USD",
            startDate = LocalDateTime.now(),
            endDate = LocalDateTime.now().plusMonths(1), // Monthly subscription
            stripeSubscriptionId = paymentResult.subscriptionId
        )
        
        val savedSubscription = channelSubscriptionRepository.save(subscription)
        
        // Update channel subscriber count
        channel.subscriberCount += 1
        channelRepository.save(channel)
        
        // Process revenue distribution (95% to creator, 5% to platform)
        processRevenueDistribution(savedSubscription, amount)
        
        // Send notifications
        notificationService.sendChannelSubscriptionNotification(channel, subscriber)
        
        // Publish event
        eventPublisher.publishEvent(ChannelSubscribedEvent(channelId, subscriberId, request.tier))
        
        return mapToChannelSubscriptionResponse(savedSubscription)
    }
    
    fun cancelSubscription(subscriptionId: Long, userId: Long): Boolean {
        val subscription = channelSubscriptionRepository.findById(subscriptionId).orElseThrow()
        
        require(subscription.subscriber.id == userId) { "User can only cancel their own subscriptions" }
        require(subscription.status == SubscriptionStatus.ACTIVE) { "Subscription is not active" }
        
        // Cancel Stripe subscription
        if (subscription.stripeSubscriptionId != null) {
            paymentService.cancelSubscription(subscription.stripeSubscriptionId!!)
        }
        
        subscription.status = SubscriptionStatus.CANCELLED
        subscription.cancelledAt = LocalDateTime.now()
        subscription.autoRenew = false
        
        channelSubscriptionRepository.save(subscription)
        
        // Update channel subscriber count
        val channel = subscription.channel
        channel.subscriberCount = maxOf(0, channel.subscriberCount - 1)
        channelRepository.save(channel)
        
        eventPublisher.publishEvent(ChannelUnsubscribedEvent(subscription.channel.id!!, userId))
        
        return true
    }
    
    // Channel Following
    fun followChannel(channelId: Long, followerId: Long, notificationTypes: Set<NotificationType> = setOf()): ChannelFollowerResponse {
        val channel = channelRepository.findById(channelId).orElseThrow()
        val follower = userRepository.findById(followerId).orElseThrow()
        
        // Check if already following
        val existingFollower = channelFollowerRepository.findByChannelAndFollower(channel, follower)
        if (existingFollower != null) {
            throw IllegalStateException("User is already following this channel")
        }
        
        val channelFollower = ChannelFollower(
            channel = channel,
            follower = follower,
            notificationTypes = notificationTypes.toMutableSet().ifEmpty { 
                mutableSetOf(NotificationType.NEW_SNIPPET, NotificationType.SNIPPET_UPDATE) 
            }
        )
        
        val savedFollower = channelFollowerRepository.save(channelFollower)
        
        // Update channel follower count
        channel.followerCount += 1
        channelRepository.save(channel)
        
        eventPublisher.publishEvent(ChannelFollowedEvent(channelId, followerId))
        
        return mapToChannelFollowerResponse(savedFollower)
    }
    
    fun unfollowChannel(channelId: Long, followerId: Long): Boolean {
        val channel = channelRepository.findById(channelId).orElseThrow()
        val follower = userRepository.findById(followerId).orElseThrow()
        val channelFollower = channelFollowerRepository.findByChannelAndFollower(channel, follower)
            ?: return false
        
        channelFollowerRepository.delete(channelFollower)
        
        // Update channel follower count
        channel.followerCount = maxOf(0, channel.followerCount - 1)
        channelRepository.save(channel)
        
        eventPublisher.publishEvent(ChannelUnfollowedEvent(channelId, followerId))
        
        return true
    }
    
    // Contributors Management
    fun inviteContributor(channelId: Long, inviterUserId: Long, request: InviteContributorRequest): ChannelContributorResponse {
        val channel = channelRepository.findById(channelId).orElseThrow()
        val inviterUser = userRepository.findById(inviterUserId).orElseThrow()
        val invitedUser = userRepository.findById(request.userId).orElseThrow()
        
        require(hasPermission(channel, inviterUser, Permission.MANAGE_CONTRIBUTORS)) {
            "User does not have permission to manage contributors"
        }
        
        // Check if user is already a contributor
        val existingContributor = channelContributorRepository.findByChannelAndUser(channel, invitedUser)
        if (existingContributor != null) {
            throw IllegalStateException("User is already a contributor to this channel")
        }
        
        val contributor = ChannelContributor(
            channel = channel,
            user = invitedUser,
            role = request.role,
            permissions = request.permissions.toMutableSet(),
            revenueSharePercentage = request.revenueSharePercentage,
            invitedBy = inviterUser,
            status = ContributorStatus.INVITED
        )
        
        val savedContributor = channelContributorRepository.save(contributor)
        
        // Send invitation notification
        notificationService.sendContributorInvitationNotification(channel, invitedUser, inviterUser, request.role)
        
        eventPublisher.publishEvent(ContributorInvitedEvent(channelId, invitedUser.id!!, request.role))
        
        return mapToChannelContributorResponse(savedContributor)
    }
    
    fun acceptContributorInvitation(contributorId: Long, userId: Long): ChannelContributorResponse {
        val contributor = channelContributorRepository.findById(contributorId).orElseThrow()
        
        require(contributor.user.id == userId) { "User can only accept their own invitations" }
        require(contributor.status == ContributorStatus.INVITED) { "Invitation is not pending" }
        
        contributor.status = ContributorStatus.ACTIVE
        
        val savedContributor = channelContributorRepository.save(contributor)
        
        eventPublisher.publishEvent(ContributorJoinedEvent(contributor.channel.id!!, userId, contributor.role))
        
        return mapToChannelContributorResponse(savedContributor)
    }
    
    fun updateContributorRole(channelId: Long, contributorId: Long, updaterUserId: Long, request: UpdateContributorRequest): ChannelContributorResponse {
        val channel = channelRepository.findById(channelId).orElseThrow()
        val updaterUser = userRepository.findById(updaterUserId).orElseThrow()
        val contributor = channelContributorRepository.findById(contributorId).orElseThrow()
        
        require(hasPermission(channel, updaterUser, Permission.MANAGE_CONTRIBUTORS)) {
            "User does not have permission to manage contributors"
        }
        
        require(contributor.channel.id == channelId) { "Contributor does not belong to this channel" }
        
        contributor.apply {
            role = request.role ?: role
            permissions = request.permissions?.toMutableSet() ?: permissions
            revenueSharePercentage = request.revenueSharePercentage ?: revenueSharePercentage
            updatedAt = LocalDateTime.now()
        }
        
        val savedContributor = channelContributorRepository.save(contributor)
        
        eventPublisher.publishEvent(ContributorUpdatedEvent(channelId, contributor.user.id!!, contributor.role))
        
        return mapToChannelContributorResponse(savedContributor)
    }
    
    // Snippet Ownership Transfer
    fun requestSnippetOwnershipTransfer(
        snippetId: Long, 
        fromUserId: Long, 
        request: OwnershipTransferRequest
    ): SnippetOwnershipTransferResponse {
        val snippet = codeSnippetRepository.findById(snippetId).orElseThrow()
        val fromUser = userRepository.findById(fromUserId).orElseThrow()
        val toUser = userRepository.findById(request.toUserId).orElseThrow()
        val toChannel = request.toChannelId?.let { channelRepository.findById(it).orElseThrow() }
        
        require(snippet.author.id == fromUserId) { "Only snippet owner can transfer ownership" }
        
        val transfer = SnippetOwnershipTransfer(
            snippet = snippet,
            fromUser = fromUser,
            toUser = toUser,
            fromChannel = snippet.channel,
            toChannel = toChannel,
            transferReason = request.reason
        )
        
        val savedTransfer = snippetOwnershipTransferRepository.save(transfer)
        
        // Send notification to recipient
        notificationService.sendOwnershipTransferNotification(snippet, fromUser, toUser)
        
        eventPublisher.publishEvent(OwnershipTransferRequestedEvent(snippetId, fromUserId, request.toUserId))
        
        return mapToOwnershipTransferResponse(savedTransfer)
    }
    
    fun acceptOwnershipTransfer(transferId: Long, userId: Long): SnippetOwnershipTransferResponse {
        val transfer = snippetOwnershipTransferRepository.findById(transferId).orElseThrow()
        
        require(transfer.toUser.id == userId) { "Only the recipient can accept the transfer" }
        require(transfer.status == TransferStatus.PENDING) { "Transfer is not pending" }
        
        transfer.status = TransferStatus.ACCEPTED
        transfer.acceptedAt = LocalDateTime.now()
        
        // Update snippet ownership
        val snippet = transfer.snippet
        snippet.author = transfer.toUser
        snippet.channel = transfer.toChannel
        codeSnippetRepository.save(snippet)
        
        val savedTransfer = snippetOwnershipTransferRepository.save(transfer)
        
        eventPublisher.publishEvent(OwnershipTransferAcceptedEvent(snippet.id!!, transfer.fromUser.id!!, userId))
        
        return mapToOwnershipTransferResponse(savedTransfer)
    }
    
    // Revenue Distribution
    private suspend fun processRevenueDistribution(subscription: ChannelSubscription, amount: BigDecimal) {
        val platformFeePercentage = BigDecimal("0.05") // 5% platform fee
        val platformFee = amount.multiply(platformFeePercentage)
        val creatorAmount = amount.subtract(platformFee)
        
        val distribution = RevenueDistribution(
            channelSubscription = subscription,
            channel = subscription.channel,
            totalAmount = amount,
            platformFee = platformFee,
            creatorAmount = creatorAmount,
            currency = subscription.currency,
            platformFeePercentage = platformFeePercentage.multiply(BigDecimal(100))
        )
        
        revenueDistributionRepository.save(distribution)
        
        // Update channel earnings
        val channel = subscription.channel
        channel.totalEarnings = channel.totalEarnings.add(creatorAmount)
        channel.monthlyEarnings = channel.monthlyEarnings.add(creatorAmount)
        channelRepository.save(channel)
        
        // Process payout to creator if enabled
        if (channel.payoutEnabled && channel.stripeAccountId != null) {
            try {
                val transferId = paymentService.transferToConnectAccount(
                    accountId = channel.stripeAccountId!!,
                    amount = creatorAmount,
                    currency = subscription.currency
                )
                
                distribution.stripeTransferId = transferId
                distribution.payoutStatus = PayoutStatus.COMPLETED
                distribution.payoutDate = LocalDateTime.now()
                
                revenueDistributionRepository.save(distribution)
            } catch (e: Exception) {
                distribution.payoutStatus = PayoutStatus.FAILED
                revenueDistributionRepository.save(distribution)
            }
        }
    }
    
    // Notifications for followers
    suspend fun notifyFollowers(
        channelId: Long, 
        notificationType: NotificationType, 
        title: String,
        message: String, 
        metadata: Map<String, Any> = emptyMap()
    ) {
        val channel = channelRepository.findById(channelId).orElseThrow()
        val followers = channelFollowerRepository.findByChannelAndNotificationEnabledAndNotificationTypesContaining(
            channel, true, notificationType
        )
        
        followers.forEach { follower ->
            notificationService.sendNotification(
                userId = follower.follower.id!!,
                type = notificationType.name,
                title = title,
                message = message,
                metadata = metadata + mapOf("channelId" to channelId)
            )
        }
    }
    
    // Analytics
    fun getChannelAnalytics(channelId: Long, userId: Long): ChannelAnalyticsResponse {
        val channel = channelRepository.findById(channelId).orElseThrow()
        val user = userRepository.findById(userId).orElseThrow()
        
        require(hasPermission(channel, user, Permission.VIEW_ANALYTICS)) {
            "User does not have permission to view analytics"
        }
        
        val subscriptions = channelSubscriptionRepository.findByChannelAndStatus(channel, SubscriptionStatus.ACTIVE)
        val revenueDistributions = revenueDistributionRepository.findByChannel(channel)
        
        return ChannelAnalyticsResponse(
            channelId = channelId,
            subscriberCount = channel.subscriberCount,
            followerCount = channel.followerCount,
            snippetCount = channel.snippetCount,
            totalEarnings = channel.totalEarnings,
            monthlyEarnings = channel.monthlyEarnings,
            subscriptionsByTier = subscriptions.groupBy { it.tier }.mapValues { it.value.size },
            revenueByMonth = revenueDistributions.groupBy { it.processedAt.month }.mapValues { 
                it.value.sumOf { dist -> dist.creatorAmount } 
            },
            topSubscribers = subscriptions.sortedByDescending { it.amount }.take(10).map {
                SubscriberSummary(it.subscriber.id!!, it.subscriber.username, it.amount, it.tier)
            }
        )
    }
    
    // Utility methods
    private fun hasPermission(channel: Channel, user: User, permission: Permission): Boolean {
        val contributor = channelContributorRepository.findByChannelAndUser(channel, user)
        return contributor?.permissions?.contains(permission) == true || 
               contributor?.role == ContributorRole.OWNER ||
               channel.owner.id == user.id
    }
    
    // Mapping functions
    private fun mapToChannelSubscriptionResponse(subscription: ChannelSubscription): ChannelSubscriptionResponse {
        return ChannelSubscriptionResponse(
            id = subscription.id!!,
            channelId = subscription.channel.id!!,
            channelName = subscription.channel.displayName,
            tier = subscription.tier,
            amount = subscription.amount,
            currency = subscription.currency,
            status = subscription.status,
            startDate = subscription.startDate,
            endDate = subscription.endDate,
            autoRenew = subscription.autoRenew,
            createdAt = subscription.createdAt
        )
    }
    
    private fun mapToChannelFollowerResponse(follower: ChannelFollower): ChannelFollowerResponse {
        return ChannelFollowerResponse(
            id = follower.id!!,
            channelId = follower.channel.id!!,
            channelName = follower.channel.displayName,
            notificationEnabled = follower.notificationEnabled,
            notificationTypes = follower.notificationTypes,
            followedAt = follower.followedAt
        )
    }
    
    private fun mapToChannelContributorResponse(contributor: ChannelContributor): ChannelContributorResponse {
        return ChannelContributorResponse(
            id = contributor.id!!,
            channelId = contributor.channel.id!!,
            userId = contributor.user.id!!,
            username = contributor.user.username,
            role = contributor.role,
            permissions = contributor.permissions,
            revenueSharePercentage = contributor.revenueSharePercentage,
            status = contributor.status,
            joinedAt = contributor.joinedAt
        )
    }
    
    private fun mapToOwnershipTransferResponse(transfer: SnippetOwnershipTransfer): SnippetOwnershipTransferResponse {
        return SnippetOwnershipTransferResponse(
            id = transfer.id!!,
            snippetId = transfer.snippet.id!!,
            snippetTitle = transfer.snippet.title,
            fromUserId = transfer.fromUser.id!!,
            fromUsername = transfer.fromUser.username,
            toUserId = transfer.toUser.id!!,
            toUsername = transfer.toUser.username,
            fromChannelId = transfer.fromChannel?.id,
            toChannelId = transfer.toChannel?.id,
            status = transfer.status,
            transferReason = transfer.transferReason,
            requestedAt = transfer.requestedAt,
            acceptedAt = transfer.acceptedAt,
            rejectedAt = transfer.rejectedAt
        )
    }
}

// DTOs for Channel Monetization
data class ChannelSubscriptionRequest(
    val tier: SubscriptionTier,
    val paymentMethodId: String,
    val customAmount: BigDecimal? = null
)

data class ChannelSubscriptionResponse(
    val id: Long,
    val channelId: Long,
    val channelName: String,
    val tier: SubscriptionTier,
    val amount: BigDecimal,
    val currency: String,
    val status: SubscriptionStatus,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime?,
    val autoRenew: Boolean,
    val createdAt: LocalDateTime
)

data class ChannelFollowerResponse(
    val id: Long,
    val channelId: Long,
    val channelName: String,
    val notificationEnabled: Boolean,
    val notificationTypes: Set<NotificationType>,
    val followedAt: LocalDateTime
)

data class InviteContributorRequest(
    val userId: Long,
    val role: ContributorRole,
    val permissions: Set<Permission>,
    val revenueSharePercentage: BigDecimal? = null
)

data class UpdateContributorRequest(
    val role: ContributorRole? = null,
    val permissions: Set<Permission>? = null,
    val revenueSharePercentage: BigDecimal? = null
)

data class ChannelContributorResponse(
    val id: Long,
    val channelId: Long,
    val userId: Long,
    val username: String,
    val role: ContributorRole,
    val permissions: Set<Permission>,
    val revenueSharePercentage: BigDecimal?,
    val status: ContributorStatus,
    val joinedAt: LocalDateTime
)

data class OwnershipTransferRequest(
    val toUserId: Long,
    val toChannelId: Long? = null,
    val reason: String? = null
)

data class SnippetOwnershipTransferResponse(
    val id: Long,
    val snippetId: Long,
    val snippetTitle: String,
    val fromUserId: Long,
    val fromUsername: String,
    val toUserId: Long,
    val toUsername: String,
    val fromChannelId: Long?,
    val toChannelId: Long?,
    val status: TransferStatus,
    val transferReason: String?,
    val requestedAt: LocalDateTime,
    val acceptedAt: LocalDateTime?,
    val rejectedAt: LocalDateTime?
)

data class ChannelAnalyticsResponse(
    val channelId: Long,
    val subscriberCount: Long,
    val followerCount: Long,
    val snippetCount: Long,
    val totalEarnings: BigDecimal,
    val monthlyEarnings: BigDecimal,
    val subscriptionsByTier: Map<SubscriptionTier, Int>,
    val revenueByMonth: Map<java.time.Month, BigDecimal>,
    val topSubscribers: List<SubscriberSummary>
)

data class SubscriberSummary(
    val userId: Long,
    val username: String,
    val amount: BigDecimal,
    val tier: SubscriptionTier
)

// Events
data class ChannelSubscribedEvent(val channelId: Long, val subscriberId: Long, val tier: SubscriptionTier)
data class ChannelUnsubscribedEvent(val channelId: Long, val subscriberId: Long)
data class ChannelFollowedEvent(val channelId: Long, val followerId: Long)
data class ChannelUnfollowedEvent(val channelId: Long, val followerId: Long)
data class ContributorInvitedEvent(val channelId: Long, val userId: Long, val role: ContributorRole)
data class ContributorJoinedEvent(val channelId: Long, val userId: Long, val role: ContributorRole)
data class ContributorUpdatedEvent(val channelId: Long, val userId: Long, val role: ContributorRole)
data class OwnershipTransferRequestedEvent(val snippetId: Long, val fromUserId: Long, val toUserId: Long)
data class OwnershipTransferAcceptedEvent(val snippetId: Long, val fromUserId: Long, val toUserId: Long)