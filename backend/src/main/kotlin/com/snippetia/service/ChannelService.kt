package com.snippetia.service

import com.snippetia.dto.*
import com.snippetia.model.Channel
import com.snippetia.model.User
import com.snippetia.repository.*
import com.snippetia.exception.ResourceNotFoundException
import com.snippetia.exception.BusinessException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
@Transactional
class ChannelService(
    private val channelRepository: ChannelRepository,
    private val userRepository: UserRepository,
    private val subscriptionRepository: SubscriptionRepository
) {

    fun createChannel(userId: Long, request: CreateChannelRequest): ChannelResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        // Check if channel name is already taken
        if (channelRepository.existsByName(request.name)) {
            throw BusinessException("Channel name already exists")
        }

        val channel = Channel(
            owner = user,
            name = request.name,
            displayName = request.displayName,
            description = request.description,
            tags = request.tags.toMutableSet(),
            subscriptionEnabled = request.subscriptionEnabled,
            basicTierPrice = request.basicTierPrice?.let { BigDecimal(it) },
            premiumTierPrice = request.premiumTierPrice?.let { BigDecimal(it) },
            enterpriseTierPrice = request.enterpriseTierPrice?.let { BigDecimal(it) }
        )

        val savedChannel = channelRepository.save(channel)
        return mapToChannelResponse(savedChannel)
    }

    fun updateChannel(userId: Long, channelId: Long, request: UpdateChannelRequest): ChannelResponse {
        val channel = channelRepository.findById(channelId)
            .orElseThrow { ResourceNotFoundException("Channel not found") }

        if (channel.owner.id != userId) {
            throw BusinessException("Not authorized to update this channel")
        }

        request.displayName?.let { channel.displayName = it }
        request.description?.let { channel.description = it }
        request.tags?.let { channel.tags = it.toMutableSet() }
        request.subscriptionEnabled?.let { channel.subscriptionEnabled = it }
        request.basicTierPrice?.let { channel.basicTierPrice = BigDecimal(it) }
        request.premiumTierPrice?.let { channel.premiumTierPrice = BigDecimal(it) }
        request.enterpriseTierPrice?.let { channel.enterpriseTierPrice = BigDecimal(it) }
        request.websiteUrl?.let { channel.websiteUrl = it }
        request.githubUrl?.let { channel.githubUrl = it }
        request.twitterUrl?.let { channel.twitterUrl = it }

        val savedChannel = channelRepository.save(channel)
        return mapToChannelResponse(savedChannel)
    }

    fun getChannel(channelId: Long): ChannelResponse {
        val channel = channelRepository.findById(channelId)
            .orElseThrow { ResourceNotFoundException("Channel not found") }
        return mapToChannelResponse(channel)
    }

    fun getChannelByName(name: String): ChannelResponse {
        val channel = channelRepository.findByName(name)
            ?: throw ResourceNotFoundException("Channel not found")
        return mapToChannelResponse(channel)
    }

    fun getUserChannels(userId: Long, pageable: Pageable): Page<ChannelResponse> {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        return channelRepository.findByOwnerOrderByCreatedAtDesc(user, pageable)
            .map { mapToChannelResponse(it) }
    }

    fun searchChannels(query: String, pageable: Pageable): Page<ChannelResponse> {
        return channelRepository.findByDisplayNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            query, query, pageable
        ).map { mapToChannelResponse(it) }
    }

    fun getFeaturedChannels(pageable: Pageable): Page<ChannelResponse> {
        return channelRepository.findByIsVerifiedTrueOrderBySubscriberCountDesc(pageable)
            .map { mapToChannelResponse(it) }
    }

    fun getTrendingChannels(pageable: Pageable): Page<ChannelResponse> {
        return channelRepository.findTrendingChannels(pageable)
            .map { mapToChannelResponse(it) }
    }

    fun deleteChannel(userId: Long, channelId: Long) {
        val channel = channelRepository.findById(channelId)
            .orElseThrow { ResourceNotFoundException("Channel not found") }

        if (channel.owner.id != userId) {
            throw BusinessException("Not authorized to delete this channel")
        }

        // Check if channel has active subscriptions
        val activeSubscriptions = subscriptionRepository.countByChannelOwnerAndStatus(
            channel.owner, com.snippetia.model.SubscriptionStatus.ACTIVE
        )
        
        if (activeSubscriptions > 0) {
            throw BusinessException("Cannot delete channel with active subscriptions")
        }

        channelRepository.delete(channel)
    }

    private fun mapToChannelResponse(channel: Channel): ChannelResponse {
        val subscriptionTiers = mutableListOf<SubscriptionTierResponse>()
        
        if (channel.subscriptionEnabled) {
            channel.basicTierPrice?.let {
                subscriptionTiers.add(
                    SubscriptionTierResponse(
                        tier = "BASIC",
                        price = it.toString(),
                        benefits = listOf("Access to basic content", "Community support")
                    )
                )
            }
            channel.premiumTierPrice?.let {
                subscriptionTiers.add(
                    SubscriptionTierResponse(
                        tier = "PREMIUM",
                        price = it.toString(),
                        benefits = listOf("Access to premium content", "Priority support", "Exclusive tutorials")
                    )
                )
            }
            channel.enterpriseTierPrice?.let {
                subscriptionTiers.add(
                    SubscriptionTierResponse(
                        tier = "ENTERPRISE",
                        price = it.toString(),
                        benefits = listOf("All premium benefits", "1-on-1 mentoring", "Custom content requests")
                    )
                )
            }
        }

        return ChannelResponse(
            id = channel.id!!,
            name = channel.name,
            displayName = channel.displayName,
            description = channel.description,
            avatarUrl = channel.avatarUrl,
            bannerUrl = channel.bannerUrl,
            tags = channel.tags.toList(),
            isVerified = channel.isVerified,
            subscriberCount = channel.subscriberCount,
            snippetCount = channel.snippetCount,
            totalStars = channel.totalStars,
            owner = UserSummaryResponse(
                id = channel.owner.id!!,
                username = channel.owner.username,
                displayName = channel.owner.displayName,
                avatarUrl = channel.owner.avatarUrl
            ),
            subscriptionEnabled = channel.subscriptionEnabled,
            subscriptionTiers = subscriptionTiers,
            websiteUrl = channel.websiteUrl,
            githubUrl = channel.githubUrl,
            twitterUrl = channel.twitterUrl,
            createdAt = channel.createdAt,
            updatedAt = channel.updatedAt
        )
    }
}