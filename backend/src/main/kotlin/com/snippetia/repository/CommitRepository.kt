package com.snippetia.repository

import com.snippetia.model.Commit
import com.snippetia.model.Repository
import com.snippetia.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository as SpringRepository
import java.time.LocalDateTime

@SpringRepository
interface CommitRepository : JpaRepository<Commit, Long> {
    
    fun findBySha(sha: String): Commit?
    
    fun findByRepositoryOrderByCreatedAtDesc(repository: Repository, pageable: Pageable): Page<Commit>
    
    fun findByAuthorOrderByCreatedAtDesc(author: User, pageable: Pageable): Page<Commit>
    
    fun countByRepository(repository: Repository): Long
    
    fun countByAuthor(author: User): Long
    
    @Query("""
        SELECT c FROM Commit c 
        WHERE c.repository = :repository 
        AND c.createdAt >= :since 
        ORDER BY c.createdAt DESC
    """)
    fun findRecentCommits(
        @Param("repository") repository: Repository,
        @Param("since") since: LocalDateTime,
        pageable: Pageable
    ): Page<Commit>
    
    @Query("""
        SELECT c FROM Commit c 
        WHERE c.author = :author 
        AND c.createdAt >= :since 
        ORDER BY c.createdAt DESC
    """)
    fun findUserRecentCommits(
        @Param("author") author: User,
        @Param("since") since: LocalDateTime,
        pageable: Pageable
    ): Page<Commit>
    
    @Query("""
        SELECT c FROM Commit c 
        WHERE c.repository.owner = :owner 
        ORDER BY c.createdAt DESC
    """)
    fun findByRepositoryOwner(@Param("owner") owner: User, pageable: Pageable): Page<Commit>
    
    fun findByBranch(branch: String, pageable: Pageable): Page<Commit>
    
    @Query("""
        SELECT COUNT(c) FROM Commit c 
        WHERE c.author = :author 
        AND c.createdAt >= :since
    """)
    fun countUserCommitsSince(@Param("author") author: User, @Param("since") since: LocalDateTime): Long
}