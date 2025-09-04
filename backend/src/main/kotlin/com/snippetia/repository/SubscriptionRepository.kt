package com.snippetia.repository

import com.snippetia.model.Subscription
import com.snippetia.model.SubscriptionStatus
import com.snippetia.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDateTime

@Repository
interface SubscriptionRepository : JpaRepository<Subscription, Long> {
    
    fun findBySubscriberAndChannelOwnerAndStatus(
        subscriber: User, 
        channelOwner: User, 
        status: SubscriptionStatus
    ): Subscription?
    
    fun findBySubscriberOrderByCreatedAtDesc(subscriber: User, pageable: Pageable): Page<Subscription>
    
    fun findByChannelOwnerOrderByCreatedAtDesc(channelOwner: User, pageable: Pageable): Page<Subscription>
    
    fun countByChannelOwnerAndStatus(channelOwner: User, status: SubscriptionStatus): Long
    
    @Query("""
        SELECT COALESCE(SUM(s.creatorAmount), 0) 
        FROM Subscription s 
        WHERE s.channelOwner = :channelOwner 
        AND s.status = 'ACTIVE'
    """)
    fun getTotalRevenueForChannelOwner(@Param("channelOwner") channelOwner: User): BigDecimal
    
    @Query("""
        SELECT COALESCE(SUM(s.creatorAmount), 0) 
        FROM Subscription s 
        WHERE s.channelOwner = :channelOwner 
        AND s.status = 'ACTIVE'
        AND s.createdAt >= :since
    """)
    fun getMonthlyRevenueForChannelOwner(
        @Param("channelOwner") channelOwner: User, 
        @Param("since") since: LocalDateTime
    ): BigDecimal
}