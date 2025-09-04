package com.snippetia.repository

import com.snippetia.model.Channel
import com.snippetia.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ChannelRepository : JpaRepository<Channel, Long> {
    
    fun findByName(name: String): Channel?
    
    fun existsByName(name: String): Boolean
    
    fun findByOwner(owner: User): Channel?
    
    fun findByOwnerOrderByCreatedAtDesc(owner: User, pageable: Pageable): Page<Channel>
    
    fun findByDisplayNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
        displayName: String, 
        description: String, 
        pageable: Pageable
    ): Page<Channel>
    
    fun findByIsVerifiedTrueOrderBySubscriberCountDesc(pageable: Pageable): Page<Channel>
    
    @Query("""
        SELECT c FROM Channel c 
        WHERE c.isVerified = true 
        ORDER BY (c.subscriberCount * 0.4 + c.totalStars * 0.3 + c.snippetCount * 0.3) DESC
    """)
    fun findTrendingChannels(pageable: Pageable): Page<Channel>
}