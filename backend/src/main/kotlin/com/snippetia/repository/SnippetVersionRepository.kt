package com.snippetia.repository

import com.snippetia.model.SnippetVersion
import com.snippetia.model.CodeSnippet
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SnippetVersionRepository : JpaRepository<SnippetVersion, Long> {
    
    fun findBySnippetOrderByCreatedAtDesc(snippet: CodeSnippet): List<SnippetVersion>
    
    fun countBySnippet(snippet: CodeSnippet): Long
}