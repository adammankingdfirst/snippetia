package com.snippetia.repository

import com.snippetia.model.CodeSnippet
import com.snippetia.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface CodeSnippetRepository : JpaRepository<CodeSnippet, Long> {
    
    fun findByAuthorId(authorId: Long, pageable: Pageable): Page<CodeSnippet>
    
    fun findByAuthor(author: User): List<CodeSnippet>
    
    fun findByAuthorOrderByStarCountDesc(author: User, pageable: Pageable): Page<CodeSnippet>
    
    fun countByAuthor(author: User): Long
    
    fun countByAuthorAndCreatedAtAfter(author: User, date: LocalDateTime): Long
    
    fun countByIsPublicTrue(): Long
    
    fun countByCreatedAtAfter(date: LocalDateTime): Long
    
    @Query("""
        SELECT s FROM CodeSnippet s 
        WHERE s.isPublic = true 
        AND (:language IS NULL OR s.language = :language)
        AND (:category IS NULL OR :category = '')
        AND (:tags IS NULL OR EXISTS (SELECT t FROM s.tags t WHERE t IN :tags))
    """)
    fun findPublicSnippets(
        @Param("language") language: String?,
        @Param("category") category: String?,
        @Param("tags") tags: List<String>?,
        pageable: Pageable
    ): Page<CodeSnippet>
    
    @Query("""
        SELECT s FROM CodeSnippet s 
        WHERE s.isPublic = true 
        ORDER BY s.likeCount DESC, s.viewCount DESC
    """)
    fun findFeaturedSnippets(pageable: Pageable): Page<CodeSnippet>
    
    @Query("""
        SELECT s FROM CodeSnippet s 
        WHERE s.isPublic = true 
        AND s.createdAt >= CURRENT_DATE - 7
        ORDER BY (s.likeCount + s.viewCount + s.forkCount) DESC
    """)
    fun findTrendingSnippets(pageable: Pageable): Page<CodeSnippet>
    
    // Analytics queries
    @Query("""
        SELECT s.language, COUNT(s) FROM CodeSnippet s 
        WHERE s.author = :author 
        GROUP BY s.language 
        ORDER BY COUNT(s) DESC
    """)
    fun findLanguageDistributionByAuthor(@Param("author") author: User): List<Array<Any>>
    
    @Query("""
        SELECT s.language, COUNT(s) FROM CodeSnippet s 
        WHERE s.isPublic = true 
        GROUP BY s.language 
        ORDER BY COUNT(s) DESC
    """)
    fun findTopLanguages(pageable: Pageable): List<Array<Any>>
    
    @Query("""
        SELECT s FROM CodeSnippet s 
        WHERE s.createdAt >= :since 
        ORDER BY (s.starCount + s.viewCount) DESC
    """)
    fun findTrendingSnippets(@Param("since") since: LocalDateTime, pageable: Pageable): List<CodeSnippet>
    
    @Query("""
        SELECT s.language, COUNT(s) FROM CodeSnippet s 
        WHERE s.createdAt >= :since 
        GROUP BY s.language 
        ORDER BY COUNT(s) DESC
    """)
    fun findTrendingLanguages(@Param("since") since: LocalDateTime, pageable: Pageable): List<Array<Any>>
    
    @Query("""
        SELECT t, COUNT(s) FROM CodeSnippet s JOIN s.tags t 
        WHERE s.createdAt >= :since 
        GROUP BY t 
        ORDER BY COUNT(s) DESC
    """)
    fun findTrendingTags(@Param("since") since: LocalDateTime, pageable: Pageable): List<Array<Any>>
    
    // Search methods
    fun findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
        title: String, 
        content: String, 
        pageable: Pageable
    ): Page<CodeSnippet>
    
    fun findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseAndLanguage(
        title: String, 
        content: String, 
        language: String, 
        pageable: Pageable
    ): Page<CodeSnippet>
    
    fun findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseAndTagsIn(
        title: String, 
        content: String, 
        tags: List<String>, 
        pageable: Pageable
    ): Page<CodeSnippet>
    
    fun findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseAndLanguageAndTagsIn(
        title: String, 
        content: String, 
        language: String, 
        tags: List<String>, 
        pageable: Pageable
    ): Page<CodeSnippet>
    
    fun findByAuthorIdAndTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
        authorId: Long, 
        title: String, 
        content: String, 
        pageable: Pageable
    ): Page<CodeSnippet>
    
    fun findByIsPublicTrueOrderByLikeCountDescViewCountDesc(pageable: Pageable): Page<CodeSnippet>
    
    fun findByLanguageAndIsPublicTrueOrderByCreatedAtDesc(language: String, pageable: Pageable): Page<CodeSnippet>
    
    fun findByTagsInAndIsPublicTrueOrderByCreatedAtDesc(tags: List<String>, pageable: Pageable): Page<CodeSnippet>
    
    @Query("""
        SELECT DISTINCT s.title FROM CodeSnippet s 
        WHERE s.title LIKE %:query% 
        AND s.isPublic = true 
        ORDER BY s.title 
        LIMIT :limit
    """)
    fun findTitleSuggestions(@Param("query") query: String, @Param("limit") limit: Int): List<String>
    
    @Query("""
        SELECT DISTINCT t FROM CodeSnippet s JOIN s.tags t 
        WHERE t LIKE %:query% 
        ORDER BY t 
        LIMIT :limit
    """)
    fun findTagSuggestions(@Param("query") query: String, @Param("limit") limit: Int): List<String>
    
    @Query("""
        SELECT s.language, COUNT(s) FROM CodeSnippet s 
        WHERE s.isPublic = true 
        GROUP BY s.language
    """)
    fun getLanguageStats(): Map<String, Long>
    
    @Query("""
        SELECT t, COUNT(s) FROM CodeSnippet s JOIN s.tags t 
        WHERE s.isPublic = true 
        GROUP BY t
    """)
    fun getTagStats(): Map<String, Long>
}