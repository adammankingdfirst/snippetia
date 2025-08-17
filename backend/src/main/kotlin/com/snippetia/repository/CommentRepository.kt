package com.snippetia.repository

import com.snippetia.model.Comment
import com.snippetia.model.CodeSnippet
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CommentRepository : JpaRepository<Comment, Long> {
    
    fun findBySnippetOrderByCreatedAtDesc(snippet: CodeSnippet, pageable: Pageable): Page<Comment>
}