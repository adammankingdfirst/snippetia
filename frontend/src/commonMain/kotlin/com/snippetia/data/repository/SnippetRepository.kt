package com.snippetia.data.repository

import com.snippetia.data.api.ApiResponse
import com.snippetia.data.api.SnippetApiService
import com.snippetia.data.dto.*
import com.snippetia.domain.model.CodeSnippet
import com.snippetia.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

interface SnippetRepository {
    suspend fun getSnippets(
        page: Int = 0,
        size: Int = 20,
        language: String? = null,
        tags: List<String>? = null
    ): Flow<Result<PagedResult<CodeSnippet>>>
    
    suspend fun getSnippet(id: Long): Flow<Result<CodeSnippet>>
    suspend fun createSnippet(snippet: CreateSnippetData): Flow<Result<CodeSnippet>>
    suspend fun updateSnippet(id: Long, snippet: UpdateSnippetData): Flow<Result<CodeSnippet>>
    suspend fun deleteSnippet(id: Long): Flow<Result<Unit>>
    suspend fun likeSnippet(id: Long): Flow<Result<LikeResult>>
    suspend fun forkSnippet(id: Long): Flow<Result<CodeSnippet>>
    suspend fun getFeaturedSnippets(page: Int = 0, size: Int = 10): Flow<Result<PagedResult<CodeSnippet>>>
    suspend fun getTrendingSnippets(page: Int = 0, size: Int = 10): Flow<Result<PagedResult<CodeSnippet>>>
    suspend fun searchSnippets(query: String, page: Int = 0, size: Int = 20): Flow<Result<PagedResult<CodeSnippet>>>
}

class SnippetRepositoryImpl(
    private val apiService: SnippetApiService
) : SnippetRepository {
    
    override suspend fun getSnippets(
        page: Int,
        size: Int,
        language: String?,
        tags: List<String>?
    ): Flow<Result<PagedResult<CodeSnippet>>> = flow {
        when (val response = apiService.getSnippets(page, size, language, tags)) {
            is ApiResponse.Success -> {
                val pagedResult = PagedResult(
                    items = response.data.content.map { it.toDomainModel() },
                    page = response.data.page,
                    size = response.data.size,
                    totalElements = response.data.totalElements,
                    totalPages = response.data.totalPages,
                    hasNext = response.data.hasNext,
                    hasPrevious = response.data.hasPrevious
                )
                emit(Result.success(pagedResult))
            }
            is ApiResponse.Error -> {
                emit(Result.failure(Exception("${response.code}: ${response.message}")))
            }
        }
    }
    
    override suspend fun getSnippet(id: Long): Flow<Result<CodeSnippet>> = flow {
        when (val response = apiService.getSnippet(id)) {
            is ApiResponse.Success -> emit(Result.success(response.data.toDomainModel()))
            is ApiResponse.Error -> emit(Result.failure(Exception("${response.code}: ${response.message}")))
        }
    }
    
    override suspend fun createSnippet(snippet: CreateSnippetData): Flow<Result<CodeSnippet>> = flow {
        val request = CreateSnippetRequest(
            title = snippet.title,
            description = snippet.description,
            content = snippet.content,
            language = snippet.language,
            tags = snippet.tags,
            isPublic = snippet.isPublic
        )
        
        when (val response = apiService.createSnippet(request)) {
            is ApiResponse.Success -> emit(Result.success(response.data.toDomainModel()))
            is ApiResponse.Error -> emit(Result.failure(Exception("${response.code}: ${response.message}")))
        }
    }
    
    override suspend fun updateSnippet(id: Long, snippet: UpdateSnippetData): Flow<Result<CodeSnippet>> = flow {
        val request = UpdateSnippetRequest(
            title = snippet.title,
            description = snippet.description,
            content = snippet.content,
            language = snippet.language,
            tags = snippet.tags,
            isPublic = snippet.isPublic
        )
        
        when (val response = apiService.updateSnippet(id, request)) {
            is ApiResponse.Success -> emit(Result.success(response.data.toDomainModel()))
            is ApiResponse.Error -> emit(Result.failure(Exception("${response.code}: ${response.message}")))
        }
    }
    
    override suspend fun deleteSnippet(id: Long): Flow<Result<Unit>> = flow {
        when (val response = apiService.deleteSnippet(id)) {
            is ApiResponse.Success -> emit(Result.success(Unit))
            is ApiResponse.Error -> emit(Result.failure(Exception("${response.code}: ${response.message}")))
        }
    }
    
    override suspend fun likeSnippet(id: Long): Flow<Result<LikeResult>> = flow {
        when (val response = apiService.likeSnippet(id)) {
            is ApiResponse.Success -> {
                val result = LikeResult(
                    isLiked = response.data.isLiked,
                    likeCount = response.data.likeCount
                )
                emit(Result.success(result))
            }
            is ApiResponse.Error -> emit(Result.failure(Exception("${response.code}: ${response.message}")))
        }
    }
    
    override suspend fun forkSnippet(id: Long): Flow<Result<CodeSnippet>> = flow {
        when (val response = apiService.forkSnippet(id)) {
            is ApiResponse.Success -> emit(Result.success(response.data.toDomainModel()))
            is ApiResponse.Error -> emit(Result.failure(Exception("${response.code}: ${response.message}")))
        }
    }
    
    override suspend fun getFeaturedSnippets(page: Int, size: Int): Flow<Result<PagedResult<CodeSnippet>>> = flow {
        when (val response = apiService.getFeaturedSnippets(page, size)) {
            is ApiResponse.Success -> {
                val pagedResult = PagedResult(
                    items = response.data.content.map { it.toDomainModel() },
                    page = response.data.page,
                    size = response.data.size,
                    totalElements = response.data.totalElements,
                    totalPages = response.data.totalPages,
                    hasNext = response.data.hasNext,
                    hasPrevious = response.data.hasPrevious
                )
                emit(Result.success(pagedResult))
            }
            is ApiResponse.Error -> emit(Result.failure(Exception("${response.code}: ${response.message}")))
        }
    }
    
    override suspend fun getTrendingSnippets(page: Int, size: Int): Flow<Result<PagedResult<CodeSnippet>>> = flow {
        when (val response = apiService.getTrendingSnippets(page, size)) {
            is ApiResponse.Success -> {
                val pagedResult = PagedResult(
                    items = response.data.content.map { it.toDomainModel() },
                    page = response.data.page,
                    size = response.data.size,
                    totalElements = response.data.totalElements,
                    totalPages = response.data.totalPages,
                    hasNext = response.data.hasNext,
                    hasPrevious = response.data.hasPrevious
                )
                emit(Result.success(pagedResult))
            }
            is ApiResponse.Error -> emit(Result.failure(Exception("${response.code}: ${response.message}")))
        }
    }
    
    override suspend fun searchSnippets(query: String, page: Int, size: Int): Flow<Result<PagedResult<CodeSnippet>>> = flow {
        when (val response = apiService.searchSnippets(query, page, size)) {
            is ApiResponse.Success -> {
                val pagedResult = PagedResult(
                    items = response.data.content.map { it.toDomainModel() },
                    page = response.data.page,
                    size = response.data.size,
                    totalElements = response.data.totalElements,
                    totalPages = response.data.totalPages,
                    hasNext = response.data.hasNext,
                    hasPrevious = response.data.hasPrevious
                )
                emit(Result.success(pagedResult))
            }
            is ApiResponse.Error -> emit(Result.failure(Exception("${response.code}: ${response.message}")))
        }
    }
}

// Extension function to convert API model to domain model
private fun CodeSnippet.toDomainModel(): CodeSnippet {
    // This assumes the API returns the same structure as domain model
    // In a real app, you might need to map between different structures
    return this
}

// Data classes for repository layer
data class PagedResult<T>(
    val items: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)

data class CreateSnippetData(
    val title: String,
    val description: String?,
    val content: String,
    val language: String,
    val tags: List<String>,
    val isPublic: Boolean
)

data class UpdateSnippetData(
    val title: String?,
    val description: String?,
    val content: String?,
    val language: String?,
    val tags: List<String>?,
    val isPublic: Boolean?
)

data class LikeResult(
    val isLiked: Boolean,
    val likeCount: Long
)