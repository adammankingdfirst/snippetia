package com.snippetia.domain.usecase

import com.snippetia.data.repository.CreateSnippetData
import com.snippetia.data.repository.SnippetRepository
import com.snippetia.data.repository.UpdateSnippetData
import com.snippetia.domain.model.CodeSnippet
import kotlinx.coroutines.flow.Flow

class CreateSnippetUseCase(
    private val repository: SnippetRepository
) {
    suspend operator fun invoke(
        title: String,
        description: String?,
        content: String,
        language: String,
        tags: List<String> = emptyList(),
        isPublic: Boolean = true
    ): Flow<Result<CodeSnippet>> {
        val snippetData = CreateSnippetData(
            title = title,
            description = description,
            content = content,
            language = language,
            tags = tags,
            isPublic = isPublic
        )
        return repository.createSnippet(snippetData)
    }
}

class UpdateSnippetUseCase(
    private val repository: SnippetRepository
) {
    suspend operator fun invoke(
        id: Long,
        title: String? = null,
        description: String? = null,
        content: String? = null,
        language: String? = null,
        tags: List<String>? = null,
        isPublic: Boolean? = null
    ): Flow<Result<CodeSnippet>> {
        val snippetData = UpdateSnippetData(
            title = title,
            description = description,
            content = content,
            language = language,
            tags = tags,
            isPublic = isPublic
        )
        return repository.updateSnippet(id, snippetData)
    }
}

class DeleteSnippetUseCase(
    private val repository: SnippetRepository
) {
    suspend operator fun invoke(id: Long): Flow<Result<Unit>> {
        return repository.deleteSnippet(id)
    }
}