package com.snippetia.data.api

import com.snippetia.domain.model.CodeSnippet
import com.snippetia.data.dto.*

interface SnippetApiService {
    suspend fun getSnippets(
        page: Int = 0,
        size: Int = 20,
        language: String? = null,
        tags: List<String>? = null
    ): ApiResponse<PagedResponse<CodeSnippet>>
    
    suspend fun getSnippet(id: Long): ApiResponse<CodeSnippet>
    
    suspend fun createSnippet(request: CreateSnippetRequest): ApiResponse<CodeSnippet>
    
    suspend fun updateSnippet(id: Long, request: UpdateSnippetRequest): ApiResponse<CodeSnippet>
    
    suspend fun deleteSnippet(id: Long): ApiResponse<Unit>
    
    suspend fun likeSnippet(id: Long): ApiResponse<LikeResponse>
    
    suspend fun forkSnippet(id: Long): ApiResponse<CodeSnippet>
    
    suspend fun getFeaturedSnippets(page: Int = 0, size: Int = 10): ApiResponse<PagedResponse<CodeSnippet>>
    
    suspend fun getTrendingSnippets(page: Int = 0, size: Int = 10): ApiResponse<PagedResponse<CodeSnippet>>
    
    suspend fun searchSnippets(
        query: String,
        page: Int = 0,
        size: Int = 20
    ): ApiResponse<PagedResponse<CodeSnippet>>
}

class SnippetApiServiceImpl(
    private val apiClient: ApiClient
) : SnippetApiService {
    
    override suspend fun getSnippets(
        page: Int,
        size: Int,
        language: String?,
        tags: List<String>?
    ): ApiResponse<PagedResponse<CodeSnippet>> {
        val params = mutableMapOf<String, Any>(
            "page" to page,
            "size" to size
        )
        
        language?.let { params["language"] = it }
        tags?.let { params["tags"] = it.joinToString(",") }
        
        return apiClient.get("/snippets", params)
    }
    
    override suspend fun getSnippet(id: Long): ApiResponse<CodeSnippet> {
        return apiClient.get("/snippets/$id")
    }
    
    override suspend fun createSnippet(request: CreateSnippetRequest): ApiResponse<CodeSnippet> {
        return apiClient.post("/snippets", request)
    }
    
    override suspend fun updateSnippet(id: Long, request: UpdateSnippetRequest): ApiResponse<CodeSnippet> {
        return apiClient.put("/snippets/$id", request)
    }
    
    override suspend fun deleteSnippet(id: Long): ApiResponse<Unit> {
        return apiClient.delete("/snippets/$id")
    }
    
    override suspend fun likeSnippet(id: Long): ApiResponse<LikeResponse> {
        return apiClient.post("/snippets/$id/like")
    }
    
    override suspend fun forkSnippet(id: Long): ApiResponse<CodeSnippet> {
        return apiClient.post("/snippets/$id/fork")
    }
    
    override suspend fun getFeaturedSnippets(page: Int, size: Int): ApiResponse<PagedResponse<CodeSnippet>> {
        return apiClient.get("/snippets/featured", mapOf("page" to page, "size" to size))
    }
    
    override suspend fun getTrendingSnippets(page: Int, size: Int): ApiResponse<PagedResponse<CodeSnippet>> {
        return apiClient.get("/snippets/trending", mapOf("page" to page, "size" to size))
    }
    
    override suspend fun searchSnippets(
        query: String,
        page: Int,
        size: Int
    ): ApiResponse<PagedResponse<CodeSnippet>> {
        return apiClient.get("/snippets/search", mapOf(
            "q" to query,
            "page" to page,
            "size" to size
        ))
    }
}