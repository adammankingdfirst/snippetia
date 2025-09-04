package com.snippetia.domain.usecase

import com.snippetia.data.repository.PagedResult
import com.snippetia.data.repository.SnippetRepository
import com.snippetia.domain.model.CodeSnippet
import kotlinx.coroutines.flow.Flow

class GetSnippetsUseCase(
    private val repository: SnippetRepository
) {
    suspend operator fun invoke(
        page: Int = 0,
        size: Int = 20,
        language: String? = null,
        tags: List<String>? = null
    ): Flow<Result<PagedResult<CodeSnippet>>> {
        return repository.getSnippets(page, size, language, tags)
    }
}

class GetFeaturedSnippetsUseCase(
    private val repository: SnippetRepository
) {
    suspend operator fun invoke(
        page: Int = 0,
        size: Int = 10
    ): Flow<Result<PagedResult<CodeSnippet>>> {
        return repository.getFeaturedSnippets(page, size)
    }
}

class GetTrendingSnippetsUseCase(
    private val repository: SnippetRepository
) {
    suspend operator fun invoke(
        page: Int = 0,
        size: Int = 10
    ): Flow<Result<PagedResult<CodeSnippet>>> {
        return repository.getTrendingSnippets(page, size)
    }
}

class SearchSnippetsUseCase(
    private val repository: SnippetRepository
) {
    suspend operator fun invoke(
        query: String,
        page: Int = 0,
        size: Int = 20
    ): Flow<Result<PagedResult<CodeSnippet>>> {
        return repository.searchSnippets(query, page, size)
    }
}

class GetSnippetUseCase(
    private val repository: SnippetRepository
) {
    suspend operator fun invoke(id: Long): Flow<Result<CodeSnippet>> {
        return repository.getSnippet(id)
    }
}

class LikeSnippetUseCase(
    private val repository: SnippetRepository
) {
    suspend operator fun invoke(id: Long): Flow<Result<com.snippetia.data.repository.LikeResult>> {
        return repository.likeSnippet(id)
    }
}

class ForkSnippetUseCase(
    private val repository: SnippetRepository
) {
    suspend operator fun invoke(id: Long): Flow<Result<CodeSnippet>> {
        return repository.forkSnippet(id)
    }
}