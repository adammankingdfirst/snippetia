package com.snippetia.repository

import com.snippetia.model.Repository
import com.snippetia.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository as SpringRepository

@SpringRepository
interface RepositoryRepository : JpaRepository<Repository, Long> {
    
    fun findByFullName(fullName: String): Repository?
    
    fun findByOwnerOrderByUpdatedAtDesc(owner: User, pageable: Pageable): Page<Repository>
    
    fun findByOwnerAndIsPrivateFalseOrderByStarCountDesc(owner: User, pageable: Pageable): Page<Repository>
    
    @Query("""
        SELECT r FROM Repository r 
        WHERE r.isPrivate = false 
        AND (:language IS NULL OR r.primaryLanguage = :language)
        AND (:topic IS NULL OR :topic MEMBER OF r.topics)
        ORDER BY r.starCount DESC
    """)
    fun findPublicRepositories(
        @Param("language") language: String?,
        @Param("topic") topic: String?,
        pageable: Pageable
    ): Page<Repository>
    
    @Query("""
        SELECT r FROM Repository r 
        WHERE r.isPrivate = false 
        AND (LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%')) 
             OR LOWER(r.description) LIKE LOWER(CONCAT('%', :query, '%')))
        ORDER BY r.starCount DESC
    """)
    fun searchPublicRepositories(@Param("query") query: String, pageable: Pageable): Page<Repository>
    
    fun findTop10ByIsPrivateFalseOrderByStarCountDesc(): List<Repository>
    
    fun findByOwnerAndArchivedFalse(owner: User): List<Repository>
    
    fun countByOwner(owner: User): Long
    
    fun countByOwnerAndIsPrivateFalse(owner: User): Long
    
    @Query("""
        SELECT r FROM Repository r 
        WHERE r.lastPushAt >= :since 
        ORDER BY (r.starCount + r.forkCount * 2) DESC
    """)
    fun findHotRepositories(@Param("since") since: java.time.LocalDateTime, pageable: Pageable): List<Repository>
}