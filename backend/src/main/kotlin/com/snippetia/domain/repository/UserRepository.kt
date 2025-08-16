package com.snippetia.domain.repository

import com.snippetia.domain.entity.User
import com.snippetia.domain.entity.AccountStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, Long> {
    
    fun findByEmail(email: String): Optional<User>
    
    fun findByUsername(username: String): Optional<User>
    
    fun findByEmailOrUsername(email: String, username: String): Optional<User>
    
    fun existsByEmail(email: String): Boolean
    
    fun existsByUsername(username: String): Boolean
    
    fun findByAccountStatus(status: AccountStatus, pageable: Pageable): Page<User>
    
    fun findByIsActiveTrue(pageable: Pageable): Page<User>
    
    fun findByIsPremiumTrue(pageable: Pageable): Page<User>
    
    fun findByIsVerifiedDeveloperTrue(pageable: Pageable): Page<User>
    
    @Query("SELECT u FROM User u WHERE u.createdAt >= :since")
    fun findRecentUsers(@Param("since") since: LocalDateTime, pageable: Pageable): Page<User>
    
    @Query("SELECT u FROM User u WHERE u.lastActivityAt >= :since")
    fun findActiveUsers(@Param("since") since: LocalDateTime, pageable: Pageable): Page<User>
    
    @Query("SELECT u FROM User u WHERE u.reputationScore >= :minScore ORDER BY u.reputationScore DESC")
    fun findTopUsers(@Param("minScore") minScore: Long, pageable: Pageable): Page<User>
    
    @Query("SELECT u FROM User u WHERE u.followerCount >= :minFollowers ORDER BY u.followerCount DESC")
    fun findPopularUsers(@Param("minFollowers") minFollowers: Long, pageable: Pageable): Page<User>
    
    @Query("""
        SELECT u FROM User u 
        WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))
        OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%'))
        OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%'))
        OR LOWER(u.bio) LIKE LOWER(CONCAT('%', :query, '%'))
    """)
    fun searchUsers(@Param("query") query: String, pageable: Pageable): Page<User>
    
    @Modifying
    @Query("UPDATE User u SET u.followerCount = u.followerCount + 1 WHERE u.id = :userId")
    fun incrementFollowerCount(@Param("userId") userId: Long)
    
    @Modifying
    @Query("UPDATE User u SET u.followerCount = u.followerCount - 1 WHERE u.id = :userId AND u.followerCount > 0")
    fun decrementFollowerCount(@Param("userId") userId: Long)
    
    @Modifying
    @Query("UPDATE User u SET u.followingCount = u.followingCount + 1 WHERE u.id = :userId")
    fun incrementFollowingCount(@Param("userId") userId: Long)
    
    @Modifying
    @Query("UPDATE User u SET u.followingCount = u.followingCount - 1 WHERE u.id = :userId AND u.followingCount > 0")
    fun decrementFollowingCount(@Param("userId") userId: Long)
    
    @Modifying
    @Query("UPDATE User u SET u.snippetCount = u.snippetCount + 1 WHERE u.id = :userId")
    fun incrementSnippetCount(@Param("userId") userId: Long)
    
    @Modifying
    @Query("UPDATE User u SET u.snippetCount = u.snippetCount - 1 WHERE u.id = :userId AND u.snippetCount > 0")
    fun decrementSnippetCount(@Param("userId") userId: Long)
    
    @Modifying
    @Query("UPDATE User u SET u.totalLikesReceived = u.totalLikesReceived + 1 WHERE u.id = :userId")
    fun incrementTotalLikesReceived(@Param("userId") userId: Long)
    
    @Modifying
    @Query("UPDATE User u SET u.totalLikesReceived = u.totalLikesReceived - 1 WHERE u.id = :userId AND u.totalLikesReceived > 0")
    fun decrementTotalLikesReceived(@Param("userId") userId: Long)
    
    @Modifying
    @Query("UPDATE User u SET u.reputationScore = u.reputationScore + :points WHERE u.id = :userId")
    fun updateReputationScore(@Param("userId") userId: Long, @Param("points") points: Long)
    
    @Modifying
    @Query("UPDATE User u SET u.lastActivityAt = :timestamp WHERE u.id = :userId")
    fun updateLastActivity(@Param("userId") userId: Long, @Param("timestamp") timestamp: LocalDateTime)
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :since")
    fun countNewUsers(@Param("since") since: LocalDateTime): Long
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.lastActivityAt >= :since")
    fun countActiveUsers(@Param("since") since: LocalDateTime): Long
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.isPremium = true")
    fun countPremiumUsers(): Long
}