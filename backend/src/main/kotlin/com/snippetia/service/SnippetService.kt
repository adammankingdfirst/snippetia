package com.snippetia.service

import com.snippetia.dto.*
import com.snippetia.model.CodeSnippet
import com.snippetia.model.User
import com.snippetia.model.Comment
import com.snippetia.model.Like
import com.snippetia.model.SnippetVersion
import com.snippetia.repository.*
import com.snippetia.exception.ResourceNotFoundException
import com.snippetia.exception.UnauthorizedException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@Service
@Transactional
class SnippetService(
    private val snippetRepository: CodeSnippetRepository,
    private val userRepository: UserRepository,
    private val commentRepository: CommentRepository,
    private val likeRepository: LikeRepository,
    private val versionRepository: SnippetVersionRepository,
    private val securityScanService: SecurityScanService,
    private val fileStorageService: FileStorageService,
    private val searchService: SearchService
) {

    fun getAllPublicSnippets(
        language: String?,
        category: String?,
        tags: List<String>?,
        search: String?,
        pageable: Pageable
    ): Page<SnippetResponse> {
        val snippets = when {
            search != null -> searchService.searchSnippets(search, language, category, tags, pageable)
            else -> snippetRepository.findPublicSnippets(language, category, tags, pageable)
        }
        
        return snippets.map { mapToSnippetResponse(it) }
    }

    fun getSnippetById(id: Long): SnippetDetailResponse {
        val snippet = snippetRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Snippet not found") }
        
        if (!snippet.isPublic) {
            throw UnauthorizedException("Snippet is private")
        }

        // Increment view count
        snippet.viewCount++
        snippetRepository.save(snippet)

        return mapToSnippetDetailResponse(snippet)
    }

    fun createSnippet(userId: Long, request: CreateSnippetRequest): SnippetResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        val snippet = CodeSnippet(
            title = request.title,
            description = request.description,
            content = request.content,
            language = request.language,
            tags = request.tags.toMutableSet(),
            isPublic = request.isPublic,
            author = user,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        // Perform security scan
        securityScanService.scanSnippet(snippet)

        val savedSnippet = snippetRepository.save(snippet)
        
        // Index for search
        searchService.indexSnippet(savedSnippet)

        return mapToSnippetResponse(savedSnippet)
    }

    fun uploadSnippet(userId: Long, request: UploadSnippetRequest): SnippetResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        // Read file content
        val content = String(request.file.bytes)
        
        // Detect language if not provided
        val detectedLanguage = request.language.ifEmpty { 
            fileStorageService.detectLanguage(request.file.originalFilename ?: "")
        }

        val snippet = CodeSnippet(
            title = request.title,
            description = request.description,
            content = content,
            language = detectedLanguage,
            tags = request.tags.toMutableSet(),
            isPublic = request.isPublic,
            author = user,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        // Perform security scan
        securityScanService.scanSnippet(snippet)

        val savedSnippet = snippetRepository.save(snippet)
        
        // Index for search
        searchService.indexSnippet(savedSnippet)

        return mapToSnippetResponse(savedSnippet)
    }

    fun updateSnippet(userId: Long, snippetId: Long, request: UpdateSnippetRequest): SnippetResponse {
        val snippet = snippetRepository.findById(snippetId)
            .orElseThrow { ResourceNotFoundException("Snippet not found") }

        if (snippet.author.id != userId) {
            throw UnauthorizedException("You can only update your own snippets")
        }

        // Create new version if content changed
        if (request.content != null && request.content != snippet.content) {
            createSnippetVersion(snippet, snippet.content)
        }

        // Update snippet
        request.title?.let { snippet.title = it }
        request.description?.let { snippet.description = it }
        request.content?.let { snippet.content = it }
        request.language?.let { snippet.language = it }
        request.tags?.let { snippet.tags = it.toMutableSet() }
        request.isPublic?.let { snippet.isPublic = it }
        snippet.updatedAt = LocalDateTime.now()

        // Re-scan if content changed
        if (request.content != null) {
            securityScanService.scanSnippet(snippet)
        }

        val savedSnippet = snippetRepository.save(snippet)
        
        // Update search index
        searchService.updateSnippet(savedSnippet)

        return mapToSnippetResponse(savedSnippet)
    }

    fun deleteSnippet(userId: Long, snippetId: Long) {
        val snippet = snippetRepository.findById(snippetId)
            .orElseThrow { ResourceNotFoundException("Snippet not found") }

        if (snippet.author.id != userId) {
            throw UnauthorizedException("You can only delete your own snippets")
        }

        // Remove from search index
        searchService.deleteSnippet(snippetId)
        
        snippetRepository.delete(snippet)
    }

    fun toggleLike(userId: Long, snippetId: Long): Boolean {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }
        
        val snippet = snippetRepository.findById(snippetId)
            .orElseThrow { ResourceNotFoundException("Snippet not found") }

        val existingLike = likeRepository.findByUserAndSnippet(user, snippet)
        
        return if (existingLike != null) {
            likeRepository.delete(existingLike)
            snippet.likeCount--
            snippetRepository.save(snippet)
            false
        } else {
            val like = Like(user = user, snippet = snippet, createdAt = LocalDateTime.now())
            likeRepository.save(like)
            snippet.likeCount++
            snippetRepository.save(snippet)
            true
        }
    }

    fun forkSnippet(userId: Long, snippetId: Long): SnippetResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }
        
        val originalSnippet = snippetRepository.findById(snippetId)
            .orElseThrow { ResourceNotFoundException("Snippet not found") }

        val forkedSnippet = CodeSnippet(
            title = "Fork of ${originalSnippet.title}",
            description = originalSnippet.description,
            content = originalSnippet.content,
            language = originalSnippet.language,
            tags = originalSnippet.tags.toMutableSet(),
            isPublic = true,
            author = user,
            forkedFrom = originalSnippet,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        originalSnippet.forkCount++
        snippetRepository.save(originalSnippet)

        val savedSnippet = snippetRepository.save(forkedSnippet)
        
        // Index for search
        searchService.indexSnippet(savedSnippet)

        return mapToSnippetResponse(savedSnippet)
    }

    fun getSnippetVersions(snippetId: Long): List<SnippetVersionResponse> {
        val snippet = snippetRepository.findById(snippetId)
            .orElseThrow { ResourceNotFoundException("Snippet not found") }

        val versions = versionRepository.findBySnippetOrderByCreatedAtDesc(snippet)
        return versions.map { mapToVersionResponse(it) }
    }

    fun createVersion(userId: Long, snippetId: Long, request: CreateVersionRequest): SnippetVersionResponse {
        val snippet = snippetRepository.findById(snippetId)
            .orElseThrow { ResourceNotFoundException("Snippet not found") }

        if (snippet.author.id != userId) {
            throw UnauthorizedException("You can only create versions for your own snippets")
        }

        val version = createSnippetVersion(snippet, request.content, request.description)
        
        // Update snippet with new content
        snippet.content = request.content
        snippet.updatedAt = LocalDateTime.now()
        snippetRepository.save(snippet)

        return mapToVersionResponse(version)
    }

    fun getUserSnippets(userId: Long, pageable: Pageable): Page<SnippetResponse> {
        val snippets = snippetRepository.findByAuthorId(userId, pageable)
        return snippets.map { mapToSnippetResponse(it) }
    }

    fun getFeaturedSnippets(pageable: Pageable): Page<SnippetResponse> {
        val snippets = snippetRepository.findFeaturedSnippets(pageable)
        return snippets.map { mapToSnippetResponse(it) }
    }

    fun getTrendingSnippets(pageable: Pageable): Page<SnippetResponse> {
        val snippets = snippetRepository.findTrendingSnippets(pageable)
        return snippets.map { mapToSnippetResponse(it) }
    }

    fun addComment(userId: Long, snippetId: Long, request: CreateCommentRequest): CommentResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }
        
        val snippet = snippetRepository.findById(snippetId)
            .orElseThrow { ResourceNotFoundException("Snippet not found") }

        val comment = Comment(
            content = request.content,
            author = user,
            snippet = snippet,
            createdAt = LocalDateTime.now()
        )

        val savedComment = commentRepository.save(comment)
        return mapToCommentResponse(savedComment)
    }

    fun getComments(snippetId: Long, pageable: Pageable): Page<CommentResponse> {
        val snippet = snippetRepository.findById(snippetId)
            .orElseThrow { ResourceNotFoundException("Snippet not found") }

        val comments = commentRepository.findBySnippetOrderByCreatedAtDesc(snippet, pageable)
        return comments.map { mapToCommentResponse(it) }
    }

    private fun createSnippetVersion(snippet: CodeSnippet, content: String, description: String? = null): SnippetVersion {
        val version = SnippetVersion(
            snippet = snippet,
            content = content,
            description = description,
            versionNumber = (versionRepository.countBySnippet(snippet) + 1).toInt(),
            createdAt = LocalDateTime.now()
        )
        return versionRepository.save(version)
    }

    private fun mapToSnippetResponse(snippet: CodeSnippet): SnippetResponse {
        return SnippetResponse(
            id = snippet.id!!,
            title = snippet.title,
            description = snippet.description,
            language = snippet.language,
            tags = snippet.tags.toList(),
            isPublic = snippet.isPublic,
            author = UserSummaryResponse(
                id = snippet.author.id!!,
                username = snippet.author.username,
                displayName = snippet.author.displayName,
                avatarUrl = snippet.author.avatarUrl
            ),
            likeCount = snippet.likeCount,
            viewCount = snippet.viewCount,
            forkCount = snippet.forkCount,
            createdAt = snippet.createdAt,
            updatedAt = snippet.updatedAt
        )
    }

    private fun mapToSnippetDetailResponse(snippet: CodeSnippet): SnippetDetailResponse {
        return SnippetDetailResponse(
            id = snippet.id!!,
            title = snippet.title,
            description = snippet.description,
            content = snippet.content,
            language = snippet.language,
            tags = snippet.tags.toList(),
            isPublic = snippet.isPublic,
            author = UserSummaryResponse(
                id = snippet.author.id!!,
                username = snippet.author.username,
                displayName = snippet.author.displayName,
                avatarUrl = snippet.author.avatarUrl
            ),
            likeCount = snippet.likeCount,
            viewCount = snippet.viewCount,
            forkCount = snippet.forkCount,
            forkedFrom = snippet.forkedFrom?.let {
                SnippetSummaryResponse(
                    id = it.id!!,
                    title = it.title,
                    author = UserSummaryResponse(
                        id = it.author.id!!,
                        username = it.author.username,
                        displayName = it.author.displayName,
                        avatarUrl = it.author.avatarUrl
                    )
                )
            },
            createdAt = snippet.createdAt,
            updatedAt = snippet.updatedAt
        )
    }

    private fun mapToVersionResponse(version: SnippetVersion): SnippetVersionResponse {
        return SnippetVersionResponse(
            id = version.id!!,
            versionNumber = version.versionNumber,
            description = version.description,
            createdAt = version.createdAt
        )
    }

    private fun mapToCommentResponse(comment: Comment): CommentResponse {
        return CommentResponse(
            id = comment.id!!,
            content = comment.content,
            author = UserSummaryResponse(
                id = comment.author.id!!,
                username = comment.author.username,
                displayName = comment.author.displayName,
                avatarUrl = comment.author.avatarUrl
            ),
            createdAt = comment.createdAt
        )
    }
}