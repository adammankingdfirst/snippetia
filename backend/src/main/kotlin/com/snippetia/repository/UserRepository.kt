package com.snippetia.repository

import com.snippetia.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface UserRepository : JpaRepository<User, Long> {
    
    fun findByUsername(username: String): User?
    
    fun findByEmail(email: String): User?
    
    @Query("SELECT u FROM User u WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
    fun findByUsernameOrEmail(@Param("usernameOrEmail") usernameOrEmail: String, @Param("usernameOrEmail") usernameOrEmail2: String): User?
    
    fun existsByUsername(username: String): Boolean
    
    fun existsByEmail(email: String): Boolean
    
    fun findByEmailVerificationToken(token: String): User?
    
    fun findByPasswordResetToken(token: String): User?
    
    // Analytics queries
    fun countByCreatedAtAfter(date: LocalDateTime): Long
    
    @Query("""
        SELECT COUNT(DISTINCT u) FROM User u 
        WHERE u.lastLoginAt >= :since
    """)
    fun countActiveUsersSince(@Param("since") since: LocalDateTime): Long
    
    @Query("""
        SELECT u FROM User u 
        WHERE u.createdAt >= :since 
        ORDER BY u.followerCount DESC
    """)
    fun findTrendingUsers(@Param("since") since: LocalDateTime, pageable: Pageable): List<User>
    
    @Query("""
        SELECT u FROM User u 
        WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) 
        OR LOWER(u.displayName) LIKE LOWER(CONCAT('%', :query, '%'))
        ORDER BY u.followerCount DESC
    """)
    fun searchUsers(@Param("query") query: String, pageable: Pageable): Page<User>
    
    fun findTop10ByOrderByFollowerCountDesc(): List<User>
}