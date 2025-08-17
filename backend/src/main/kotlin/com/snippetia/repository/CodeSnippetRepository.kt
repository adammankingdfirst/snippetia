package com.snippetia.repository

import com.snippetia.model.CodeSnippet
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CodeSnippetRepository : JpaRepository<CodeSnippet, Long> {
    
    fun findByAuthorId(authorId: Long, pageable: Pageable): Page<CodeSnippet>
    
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
}