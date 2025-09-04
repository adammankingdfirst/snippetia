package com.snippetia.service

import com.snippetia.model.CodeSnippet
import com.snippetia.repository.CodeSnippetRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class SearchService(
    private val codeSnippetRepository: CodeSnippetRepository
) {

    fun searchSnippets(
        query: String,
        language: String? = null,
        category: String? = null,
        tags: List<String>? = null,
        pageable: Pageable
    ): Page<CodeSnippet> {
        // Basic database search implementation
        // In production, this would use Elasticsearch or similar
        
        return when {
            language != null && !tags.isNullOrEmpty() -> {
                codeSnippetRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseAndLanguageAndTagsIn(
                    query, query, language, tags, pageable
                )
            }
            language != null -> {
                codeSnippetRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseAndLanguage(
                    query, query, language, pageable
                )
            }
            !tags.isNullOrEmpty() -> {
                codeSnippetRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseAndTagsIn(
                    query, query, tags, pageable
                )
            }
            else -> {
                codeSnippetRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
                    query, query, pageable
                )
            }
        }
    }

    fun searchSnippetsByUser(
        query: String,
        userId: Long,
        pageable: Pageable
    ): Page<CodeSnippet> {
        return codeSnippetRepository.findByAuthorIdAndTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
            userId, query, query, pageable
        )
    }

    fun getPopularSnippets(pageable: Pageable): Page<CodeSnippet> {
        return codeSnippetRepository.findByIsPublicTrueOrderByLikeCountDescViewCountDesc(pageable)
    }

    fun getTrendingSnippets(pageable: Pageable): Page<CodeSnippet> {
        // Simple trending algorithm based on recent activity
        return codeSnippetRepository.findTrendingSnippets(pageable)
    }

    fun getSnippetsByLanguage(language: String, pageable: Pageable): Page<CodeSnippet> {
        return codeSnippetRepository.findByLanguageAndIsPublicTrueOrderByCreatedAtDesc(language, pageable)
    }

    fun getSnippetsByTags(tags: List<String>, pageable: Pageable): Page<CodeSnippet> {
        return codeSnippetRepository.findByTagsInAndIsPublicTrueOrderByCreatedAtDesc(tags, pageable)
    }

    fun indexSnippet(snippet: CodeSnippet) {
        // In production, this would index the snippet in Elasticsearch
        // For now, we'll just log it
        println("Indexing snippet: ${snippet.id} - ${snippet.title}")
    }

    fun updateSnippet(snippet: CodeSnippet) {
        // In production, this would update the snippet in Elasticsearch
        println("Updating snippet index: ${snippet.id} - ${snippet.title}")
    }

    fun deleteSnippet(snippetId: Long) {
        // In production, this would delete the snippet from Elasticsearch
        println("Deleting snippet from index: $snippetId")
    }

    fun getSuggestions(query: String, limit: Int = 10): List<String> {
        // Basic suggestion implementation
        // In production, this would use Elasticsearch completion suggester
        
        if (query.length < 2) return emptyList()
        
        val titleSuggestions = codeSnippetRepository.findTitleSuggestions(query, limit)
        val tagSuggestions = codeSnippetRepository.findTagSuggestions(query, limit)
        
        return (titleSuggestions + tagSuggestions).distinct().take(limit)
    }

    fun getLanguageStats(): Map<String, Long> {
        return codeSnippetRepository.getLanguageStats()
    }

    fun getTagStats(): Map<String, Long> {
        return codeSnippetRepository.getTagStats()
    }
}