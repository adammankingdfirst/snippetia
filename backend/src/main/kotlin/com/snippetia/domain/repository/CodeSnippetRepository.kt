package com.snippetia.domain.repository

import com.snippetia.domain.entity.CodeSnippet
import com.snippetia.domain.entity.User
import com.snippetia.domain.entity.Category
import com.snippetia.domain.entity.VirusScanStatus
import com.snippetia.domain.entity.SecurityScanStatus
import com.snippetia.domain.entity.SyncStatus
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
interface CodeSnippetRepository : JpaRepository<CodeSnippet, Long> {
    
    fun findByUser(user: User, pageable: Pageable): Page<CodeSnippet>
    
    fun findByUserId(userId: Long, pageable: Pageable): Page<CodeSnippet>
    
    fun findByIsPublicTrue(pageable: Pageable): Page<CodeSnippet>
    
    fun findByIsFeaturedTrueAndIsPublicTrue(pageable: Pageable): Page<CodeSnippet>
    
    fun findByCategory(category: Category, pageable: Pageable): Page<CodeSnippet>
    
    fun findByProgrammingLanguage(language: String, pageable: Pageable): Page<CodeSnippet>
    
    fun findByProgrammingLanguageAndIsPublicTrue(language: String, pageable: Pageable): Page<CodeSnippet>
    
    @Query("SELECT s FROM CodeSnippet s WHERE s.isPublic = true AND :tag MEMBER OF s.tags")
    fun findByTagAndIsPublicTrue(@Param("tag") tag: String, pageable: Pageable): Page<CodeSnippet>
    
    @Query("""
        SELECT s FROM CodeSnippet s 
        WHERE s.isPublic = true 
        AND (:language IS NULL OR s.programmingLanguage = :language)
        AND (:categoryId IS NULL OR s.category.id = :categoryId)
        AND (:tag IS NULL OR :tag MEMBER OF s.tags)
        ORDER BY s.createdAt DESC
    """)
    fun findPublicSnippets(
        @Param("language") language: String?,
        @Param("categoryId") categoryId: Long?,
        @Param("tag") tag: String?,
        pageable: Pageable
    ): Page<CodeSnippet>
    
    @Query("""
        SELECT s FROM CodeSnippet s 
        WHERE s.isPublic = true 
        AND s.createdAt >= :since
        ORDER BY (s.viewCount + s.likeCount * 2 + s.forkCount * 3) DESC
    """)
    fun findTrendingSnippets(@Param("since") since: LocalDateTime, pageable: Pageable): Page<CodeSnippet>
    
    @Query("""
        SELECT s FROM CodeSnippet s 
        WHERE s.isPublic = true 
        ORDER BY (s.viewCount + s.likeCount * 2 + s.forkCount * 3) DESC
    """)
    fun findPopularSnippets(pageable: Pageable): Page<CodeSnippet>
    
    @Query("""
        SELECT s FROM CodeSnippet s 
        WHERE s.isPublic = true 
        AND (LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%'))
        OR LOWER(s.description) LIKE LOWER(CONCAT('%', :query, '%'))
        OR LOWER(s.codeContent) LIKE LOWER(CONCAT('%', :query, '%')))
    """)
    fun searchSnippets(@Param("query") query: String, pageable: Pageable): Page<CodeSnippet>
    
    @Query("""
        SELECT s FROM CodeSnippet s 
        WHERE s.user.id IN (
            SELECT uf.following.id FROM UserFollow uf WHERE uf.follower.id = :userId
        )
        AND s.isPublic = true
        ORDER BY s.createdAt DESC
    """)
    fun findFollowingSnippets(@Param("userId") userId: Long, pageable: Pageable): Page<CodeSnippet>
    
    fun findByOriginalSnippetId(originalSnippetId: Long, pageable: Pageable): Page<CodeSnippet>
    
    fun findByVirusScanStatus(status: VirusScanStatus, pageable: Pageable): Page<CodeSnippet>
    
    fun findBySecurityScanStatus(status: SecurityScanStatus, pageable: Pageable): Page<CodeSnippet>
    
    fun findBySyncStatus(status: SyncStatus, pageable: Pageable): Page<CodeSnippet>
    
    fun findByGitRepositoryUrlIsNotNull(pageable: Pageable): Page<CodeSnippet>
    
    @Query("SELECT s FROM CodeSnippet s WHERE s.isExecutable = true AND s.isPublic = true")
    fun findExecutableSnippets(pageable: Pageable): Page<CodeSnippet>
    
    @Modifying
    @Query("UPDATE CodeSnippet s SET s.viewCount = s.viewCount + 1 WHERE s.id = :snippetId")
    fun incrementViewCount(@Param("snippetId") snippetId: Long)
    
    @Modifying
    @Query("UPDATE CodeSnippet s SET s.likeCount = s.likeCount + 1 WHERE s.id = :snippetId")
    fun incrementLikeCount(@Param("snippetId") snippetId: Long)
    
    @Modifying
    @Query("UPDATE CodeSnippet s SET s.likeCount = s.likeCount - 1 WHERE s.id = :snippetId AND s.likeCount > 0")
    fun decrementLikeCount(@Param("snippetId") snippetId: Long)
    
    @Modifying
    @Query("UPDATE CodeSnippet s SET s.forkCount = s.forkCount + 1 WHERE s.id = :snippetId")
    fun incrementForkCount(@Param("snippetId") snippetId: Long)
    
    @Modifying
    @Query("UPDATE CodeSnippet s SET s.forkCount = s.forkCount - 1 WHERE s.id = :snippetId AND s.forkCount > 0")
    fun decrementForkCount(@Param("snippetId") snippetId: Long)
    
    @Modifying
    @Query("UPDATE CodeSnippet s SET s.downloadCount = s.downloadCount + 1 WHERE s.id = :snippetId")
    fun incrementDownloadCount(@Param("snippetId") snippetId: Long)
    
    @Modifying
    @Query("UPDATE CodeSnippet s SET s.runCount = s.runCount + 1 WHERE s.id = :snippetId")
    fun incrementRunCount(@Param("snippetId") snippetId: Long)
    
    @Query("SELECT COUNT(s) FROM CodeSnippet s WHERE s.createdAt >= :since")
    fun countNewSnippets(@Param("since") since: LocalDateTime): Long
    
    @Query("SELECT COUNT(s) FROM CodeSnippet s WHERE s.isPublic = true")
    fun countPublicSnippets(): Long
    
    @Query("SELECT s.programmingLanguage, COUNT(s) FROM CodeSnippet s WHERE s.isPublic = true GROUP BY s.programmingLanguage ORDER BY COUNT(s) DESC")
    fun getLanguageStatistics(): List<Array<Any>>
    
    @Query("SELECT c.name, COUNT(s) FROM CodeSnippet s JOIN s.category c WHERE s.isPublic = true GROUP BY c.name ORDER BY COUNT(s) DESC")
    fun getCategoryStatistics(): List<Array<Any>>
    
    @Query("""
        SELECT DATE(s.createdAt), COUNT(s) 
        FROM CodeSnippet s 
        WHERE s.createdAt >= :since 
        GROUP BY DATE(s.createdAt) 
        ORDER BY DATE(s.createdAt)
    """)
    fun getCreationStatistics(@Param("since") since: LocalDateTime): List<Array<Any>>
}