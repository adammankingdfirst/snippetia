package com.snippetia.service

import com.snippetia.dto.*
import com.snippetia.model.*
import com.snippetia.repository.*
import com.snippetia.exception.ResourceNotFoundException
import com.snippetia.exception.BusinessException
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
@Transactional
class SubscriptionService(
    private val subscriptionRepository: SubscriptionRepository,
    private val userRepository: UserRepository,
    private val channelRepository: ChannelRepository,
    private val paymentService: PaymentService,
    private val notificationService: NotificationService
) {

    @Value("\${app.platform.fee-percentage:0.15}")
    private val platformFeePercentage: Double = 0.15

    fun createSubscription(userId: Long, request: CreateSubscriptionRequest): SubscriptionResponse {
        val subscriber = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        val channelOwner = userRepository.findById(request.channelOwnerId)
            .orElseThrow { ResourceNotFoundException("Channel owner not found") }

        val channel = channelRepository.findByOwner(channelOwner)
            ?: throw ResourceNotFoundException("Channel not found")

        if (!channel.subscriptionEnabled) {
            throw BusinessException("Subscriptions are not enabled for this channel")
        }

        // Check if user already has an active subscription
        val existingSubscription = subscriptionRepository.findBySubscriberAndChannelOwnerAndStatus(
            subscriber, channelOwner, SubscriptionStatus.ACTIVE
        )
        if (existingSubscription != null) {
            throw BusinessException("You already have an active subscription to this channel")
        }

        val tier = SubscriptionTier.valueOf(request.tier.uppercase())
        val amount = getSubscriptionAmount(channel, tier)
        val platformFee = amount.multiply(BigDecimal(platformFeePercentage))
        val creatorAmount = amount.subtract(platformFee)

        // Process payment
        val paymentResult = paymentService.processSubscriptionPayment(
            userId = userId,
            amount = amount,
            paymentMethodId = request.paymentMethodId,
            description = "Subscription to ${channel.displayName}"
        )

        if (!paymentResult.successful) {
            throw BusinessException("Payment failed: ${paymentResult.errorMessage}")
        }

        val subscription = Subscription(
            subscriber = subscriber,
            channelOwner = channelOwner,
            tier = tier,
            amount = amount,
            platformFee = platformFee,
            creatorAmount = creatorAmount,
            stripeSubscriptionId = paymentResult.subscriptionId,
            currentPeriodStart = LocalDateTime.now(),
            currentPeriodEnd = LocalDateTime.now().plusMonths(1)
        )

        val savedSubscription = subscriptionRepository.save(subscription)

        // Update channel subscriber count
        channel.subscriberCount++
        channelRepository.save(channel)

        // Send notifications
        notificationService.createNotification(
            userId = channelOwner.id!!,
            type = NotificationType.SUBSCRIPTION_CREATED,
            title = "New Subscriber!",
            message = "${subscriber.displayName} subscribed to your channel",
            actionUrl = "/channels/${channel.name}"
        )

        return mapToSubscriptionResponse(savedSubscription)
    }

    fun cancelSubscription(userId: Long, subscriptionId: Long): SubscriptionResponse {
        val subscription = subscriptionRepository.findById(subscriptionId)
            .orElseThrow { ResourceNotFoundException("Subscription not found") }

        if (subscription.subscriber.id != userId) {
            throw BusinessException("Not authorized to cancel this subscription")
        }

        if (subscription.status != SubscriptionStatus.ACTIVE) {
            throw BusinessException("Subscription is not active")
        }

        // Cancel with payment provider
        subscription.stripeSubscriptionId?.let { stripeId ->
            paymentService.cancelSubscription(stripeId)
        }

        subscription.status = SubscriptionStatus.CANCELLED
        subscription.autoRenew = false
        val savedSubscription = subscriptionRepository.save(subscription)

        // Update channel subscriber count
        val channel = channelRepository.findByOwner(subscription.channelOwner)
        channel?.let {
            it.subscriberCount = maxOf(0, it.subscriberCount - 1)
            channelRepository.save(it)
        }

        // Send notification
        notificationService.createNotification(
            userId = subscription.channelOwner.id!!,
            type = NotificationType.SUBSCRIPTION_CANCELLED,
            title = "Subscription Cancelled",
            message = "${subscription.subscriber.displayName} cancelled their subscription",
            actionUrl = "/dashboard/subscriptions"
        )

        return mapToSubscriptionResponse(savedSubscription)
    }

    fun getUserSubscriptions(userId: Long, pageable: Pageable): Page<SubscriptionResponse> {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        return subscriptionRepository.findBySubscriberOrderByCreatedAtDesc(user, pageable)
            .map { mapToSubscriptionResponse(it) }
    }

    fun getChannelSubscriptions(userId: Long, pageable: Pageable): Page<SubscriptionResponse> {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        return subscriptionRepository.findByChannelOwnerOrderByCreatedAtDesc(user, pageable)
            .map { mapToSubscriptionResponse(it) }
    }

    fun updateSubscriptionTier(userId: Long, subscriptionId: Long, newTier: String): SubscriptionResponse {
        val subscription = subscriptionRepository.findById(subscriptionId)
            .orElseThrow { ResourceNotFoundException("Subscription not found") }

        if (subscription.subscriber.id != userId) {
            throw BusinessException("Not authorized to update this subscription")
        }

        val tier = SubscriptionTier.valueOf(newTier.uppercase())
        val channel = channelRepository.findByOwner(subscription.channelOwner)
            ?: throw ResourceNotFoundException("Channel not found")

        val newAmount = getSubscriptionAmount(channel, tier)
        val platformFee = newAmount.multiply(BigDecimal(platformFeePercentage))
        val creatorAmount = newAmount.subtract(platformFee)

        // Update payment with provider
        subscription.stripeSubscriptionId?.let { stripeId ->
            paymentService.updateSubscriptionAmount(stripeId, newAmount)
        }

        subscription.tier = tier
        subscription.amount = newAmount
        subscription.platformFee = platformFee
        subscription.creatorAmount = creatorAmount

        val savedSubscription = subscriptionRepository.save(subscription)
        return mapToSubscriptionResponse(savedSubscription)
    }

    fun getSubscriptionStats(userId: Long): SubscriptionStatsResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        val totalSubscribers = subscriptionRepository.countByChannelOwnerAndStatus(user, SubscriptionStatus.ACTIVE)
        val totalRevenue = subscriptionRepository.getTotalRevenueForChannelOwner(user)
        val monthlyRevenue = subscriptionRepository.getMonthlyRevenueForChannelOwner(user, LocalDateTime.now().minusMonths(1))

        return SubscriptionStatsResponse(
            totalSubscribers = totalSubscribers,
            totalRevenue = totalRevenue.toString(),
            monthlyRevenue = monthlyRevenue.toString()
        )
    }

    private fun getSubscriptionAmount(channel: Channel, tier: SubscriptionTier): BigDecimal {
        return when (tier) {
            SubscriptionTier.BASIC -> channel.basicTierPrice
                ?: throw BusinessException("Basic tier not available")
            SubscriptionTier.PREMIUM -> channel.premiumTierPrice
                ?: throw BusinessException("Premium tier not available")
            SubscriptionTier.ENTERPRISE -> channel.enterpriseTierPrice
                ?: throw BusinessException("Enterprise tier not available")
        }
    }

    private fun mapToSubscriptionResponse(subscription: Subscription): SubscriptionResponse {
        return SubscriptionResponse(
            id = subscription.id!!,
            channelOwner = UserSummaryResponse(
                id = subscription.channelOwner.id!!,
                username = subscription.channelOwner.username,
                displayName = subscription.channelOwner.displayName,
                avatarUrl = subscription.channelOwner.avatarUrl
            ),
            tier = subscription.tier.name,
            amount = subscription.amount.toString(),
            status = subscription.status.name,
            currentPeriodStart = subscription.currentPeriodStart,
            currentPeriodEnd = subscription.currentPeriodEnd,
            autoRenew = subscription.autoRenew,
            createdAt = subscription.createdAt
        )
    }
}

data class SubscriptionStatsResponse(
    val totalSubscribers: Long,
    val totalRevenue: String,
    val monthlyRevenue: String
)

data class PaymentResult(
    val successful: Boolean,
    val subscriptionId: String? = null,
    val errorMessage: String? = null
)