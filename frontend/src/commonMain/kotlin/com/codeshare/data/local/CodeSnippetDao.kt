package com.codeshare.data.local

import com.codeshare.data.model.CodeSnippet

// This would be implemented using SQLDelight or Room depending on platform
interface CodeSnippetDao {
    suspend fun insertSnippet(snippet: CodeSnippet)
    suspend fun insertSnippets(snippets: List<CodeSnippet>)
    suspend fun updateSnippet(snippet: CodeSnippet)
    suspend fun deleteSnippet(id: Long)
    suspend fun getSnippet(id: Long): CodeSnippet?
    suspend fun getAllSnippets(): List<CodeSnippet>
    suspend fun getSnippetsByLanguage(language: String): List<CodeSnippet>
    suspend fun searchSnippets(query: String): List<CodeSnippet>
    suspend fun clearAll()
}

// Mock implementation for demo
class MockCodeSnippetDao : CodeSnippetDao {
    private val snippets = mutableMapOf<Long, CodeSnippet>()
    
    override suspend fun insertSnippet(snippet: CodeSnippet) {
        snippets[snippet.id] = snippet
    }
    
    override suspend fun insertSnippets(snippets: List<CodeSnippet>) {
        snippets.forEach { insertSnippet(it) }
    }
    
    override suspend fun updateSnippet(snippet: CodeSnippet) {
        snippets[snippet.id] = snippet
    }
    
    override suspend fun deleteSnippet(id: Long) {
        snippets.remove(id)
    }
    
    override suspend fun getSnippet(id: Long): CodeSnippet? {
        return snippets[id]
    }
    
    override suspend fun getAllSnippets(): List<CodeSnippet> {
        return snippets.values.toList()
    }
    
    override suspend fun getSnippetsByLanguage(language: String): List<CodeSnippet> {
        return snippets.values.filter { 
            it.programmingLanguage.equals(language, ignoreCase = true) 
        }
    }
    
    override suspend fun searchSnippets(query: String): List<CodeSnippet> {
        return snippets.values.filter { snippet ->
            snippet.title.contains(query, ignoreCase = true) ||
            snippet.description.contains(query, ignoreCase = true) ||
            snippet.tags.any { it.contains(query, ignoreCase = true) }
        }
    }
    
    override suspend fun clearAll() {
        snippets.clear()
    }
}