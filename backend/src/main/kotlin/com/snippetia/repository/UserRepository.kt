package com.snippetia.repository

import com.snippetia.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

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
}