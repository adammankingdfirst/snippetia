package com.snippetia.service

import com.snippetia.dto.*
import com.snippetia.exception.ResourceNotFoundException
import com.snippetia.exception.UnauthorizedException
import com.snippetia.model.*
import com.snippetia.repository.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class SnippetService(
    private val snippetRepository: CodeSnippetRepository,
    private val userRepository: UserRepository,
    private val categoryRepository: CategoryRepository,
    private val snippetVersionRepository: SnippetVersionRepository,
    private val commentRepository: CommentRepository,
    private val snippetLikeRepository: SnippetLikeRepository,
    private val securityScanService: SecurityScanService,
    private val virusScanService: VirusScanService,
    private val gitIntegrationService: GitIntegrationService,
    private val searchService: SearchService
) {

    fun getAllPublicSnippets(
        language: String?,
        category: String?,
        tags: List<String>?,
        search: String?,
        pageable: Pageable
    ): Page<SnippetResponse> {
        return if (search != null) {
            searchService.searchSnippets(search, language, category, tags, pageable)
        } else {
            snippetRepository.findPublicSnippets(language, category, tags, pageable)
                .map { it.toResponse() }
        }
    }

    fun getSnippetById(id: Long): SnippetDetailResponse {
        val snippet = snippetRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Snippet not found") }
        
        // Increment view count
        snippetRepository.incrementViewCount(id)
        
        return snippet.toDetailResponse()
    }

    fun createSnippet(userId: Long, request: CreateSnippetRequest): SnippetResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }
        
        val category = request.categoryId?.let { categoryId ->
            categoryRepository.findById(categoryId)
                .orElseThrow { ResourceNotFoundException("Category not found") }
        }

        val snippet = CodeSnippet(
            title = request.title,
            description = request.description,
            codeContent = request.codeContent,
            programmingLanguage = request.programmingLanguage,
            frameworkVersion = request.frameworkVersion,
            tags = request.tags.toSet(),
            isPublic = request.isPublic,
            licenseType = request.licenseType,
            user = user,
            category = category,
            fileSize = request.codeContent.length.toLong(),
            checksum = generateChecksum(request.codeContent)
        )

        val savedSnippet = snippetRepository.save(snippet)
        
        // Trigger security scans asynchronously
        securityScanService.scanSnippet(savedSnippet.id)
        virusScanService.scanSnippet(savedSnippet.id)
        
        return savedSnippet.toResponse()
    }

    fun uploadSnippet(userId: Long, request: UploadSnippetRequest): SnippetResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        val content = String(request.file.bytes)
        
        val snippet = CodeSnippet(
            title = request.title,
            description = request.description,
            codeContent = content,
            programmingLanguage = request.language,
            tags = request.tags.toSet(),
            isPublic = request.isPublic,
            user = user,
            fileSize = request.file.size,
            checksum = generateChecksum(content)
        )

        val savedSnippet = snippetRepository.save(snippet)
        
        // Trigger security scans asynchronously
        securityScanService.scanSnippet(savedSnippet.id)
        virusScanService.scanSnippet(savedSnippet.id)
        
        return savedSnippet.toResponse()
    }

    fun updateSnippet(userId: Long, snippetId: Long, request: UpdateSnippetRequest): SnippetResponse {
        val snippet = snippetRepository.findById(snippetId)
            .orElseThrow { ResourceNotFoundException("Snippet not found") }
        
        if (snippet.user.id != userId) {
            throw UnauthorizedException("You can only update your own snippets")
        }

        val category = request.categoryId?.let { categoryId ->
            categoryRepository.findById(categoryId)
                .orElseThrow { ResourceNotFoundException("Category not found") }
        }

        val updatedSnippet = snippet.copy(
            title = request.title,
            description = request.description,
            codeContent = request.codeContent,
            programmingLanguage = request.programmingLanguage,
            frameworkVersion = request.frameworkVersion,
            tags = request.tags.toSet(),
            isPublic = request.isPublic,
            licenseType = request.licenseType,
            category = category,
            fileSize = request.codeContent.length.toLong(),
            checksum = generateChecksum(request.codeContent),
            updatedAt = LocalDateTime.now()
        )

        val savedSnippet = snippetRepository.save(updatedSnippet)
        
        // Trigger security scans for updated content
        securityScanService.scanSnippet(savedSnippet.id)
        virusScanService.scanSnippet(savedSnippet.id)
        
        return savedSnippet.toResponse()
    }

    fun deleteSnippet(userId: Long, snippetId: Long) {
        val snippet = snippetRepository.findById(snippetId)
            .orElseThrow { ResourceNotFoundException("Snippet not found") }
        
        if (snippet.user.id != userId) {
            throw UnauthorizedException("You can only delete your own snippets")
        }

        snippetRepository.delete(snippet)
    }

    fun toggleLike(userId: Long, snippetId: Long): Boolean {
        val snippet = snippetRepository.findById(snippetId)
            .orElseThrow { ResourceNotFoundException("Snippet not found") }
        
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        val existingLike = snippetLikeRepository.findByUserAndSnippet(user, snippet)
        
        return if (existingLike != null) {
            snippetLikeRepository.delete(existingLike)
            snippetRepository.decrementLikeCount(snippetId)
            false
        } else {
            val like = SnippetLike(user = user, snippet = snippet)
            snippetLikeRepository.save(like)
            snippetRepository.incrementLikeCount(snippetId)
            true
        }
    }

    fun forkSnippet(userId: Long, snippetId: Long): SnippetResponse {
        val originalSnippet = snippetRepository.findById(snippetId)
            .orElseThrow { ResourceNotFoundException("Snippet not found") }
        
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        val forkedSnippet = originalSnippet.copy(
            id = 0,
            title = "${originalSnippet.title} (Fork)",
            user = user,
            originalSnippetId = originalSnippet.id,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            viewCount = 0,
            likeCount = 0,
            forkCount = 0,
            downloadCount = 0
        )

        val savedSnippet = snippetRepository.save(forkedSnippet)
        snippetRepository.incrementForkCount(snippetId)
        
        return savedSnippet.toResponse()
    }

    fun getUserSnippets(userId: Long, pageable: Pageable): Page<SnippetResponse> {
        return snippetRepository.findByUserId(userId, pageable)
            .map { it.toResponse() }
    }

    fun getFeaturedSnippets(pageable: Pageable): Page<SnippetResponse> {
        return snippetRepository.findByIsFeaturedTrueAndIsPublicTrue(pageable)
            .map { it.toResponse() }
    }

    fun getTrendingSnippets(pageable: Pageable): Page<SnippetResponse> {
        return snippetRepository.findTrendingSnippets(pageable)
            .map { it.toResponse() }
    }

    fun getSnippetVersions(snippetId: Long): List<SnippetVersionResponse> {
        val snippet = snippetRepository.findById(snippetId)
            .orElseThrow { ResourceNotFoundException("Snippet not found") }
        
        return snippetVersionRepository.findBySnippetOrderByCreatedAtDesc(snippet)
            .map { it.toResponse() }
    }

    fun createVersion(userId: Long, snippetId: Long, request: CreateVersionRequest): SnippetVersionResponse {
        val snippet = snippetRepository.findById(snippetId)
            .orElseThrow { ResourceNotFoundException("Snippet not found") }
        
        if (snippet.user.id != userId) {
            throw UnauthorizedException("You can only create versions for your own snippets")
        }

        val version = SnippetVersion(
            versionNumber = request.versionNumber,
            codeContent = request.codeContent,
            changeDescription = request.changeDescription,
            snippet = snippet,
            fileSize = request.codeContent.length.toLong(),
            checksum = generateChecksum(request.codeContent)
        )

        val savedVersion = snippetVersionRepository.save(version)
        
        // Update main snippet with new version
        val updatedSnippet = snippet.copy(
            codeContent = request.codeContent,
            versionNumber = request.versionNumber,
            fileSize = request.codeContent.length.toLong(),
            checksum = generateChecksum(request.codeContent),
            updatedAt = LocalDateTime.now()
        )
        snippetRepository.save(updatedSnippet)
        
        return savedVersion.toResponse()
    }

    fun addComment(userId: Long, snippetId: Long, request: CreateCommentRequest): CommentResponse {
        val snippet = snippetRepository.findById(snippetId)
            .orElseThrow { ResourceNotFoundException("Snippet not found") }
        
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        val comment = Comment(
            content = request.content,
            user = user,
            snippet = snippet
        )

        val savedComment = commentRepository.save(comment)
        return savedComment.toResponse()
    }

    fun getComments(snippetId: Long, pageable: Pageable): Page<CommentResponse> {
        val snippet = snippetRepository.findById(snippetId)
            .orElseThrow { ResourceNotFoundException("Snippet not found") }
        
        return commentRepository.findBySnippetOrderByCreatedAtDesc(snippet, pageable)
            .map { it.toResponse() }
    }

    private fun generateChecksum(content: String): String {
        return java.security.MessageDigest.getInstance("SHA-256")
            .digest(content.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}

// Extension functions for model to DTO conversion
private fun CodeSnippet.toResponse(): SnippetResponse {
    return SnippetResponse(
        id = id,
        title = title,
        description = description,
        programmingLanguage = programmingLanguage,
        frameworkVersion = frameworkVersion,
        tags = tags.toList(),
        isPublic = isPublic,
        viewCount = viewCount,
        likeCount = likeCount,
        forkCount = forkCount,
        downloadCount = downloadCount,
        versionNumber = versionNumber,
        author = UserSummaryResponse(
            id = user.id,
            username = user.username,
            displayName = user.getDisplayName(),
            avatarUrl = user.avatarUrl
        ),
        category = category?.let { CategoryResponse(it.id, it.name, it.description, it.iconUrl, it.colorCode) },
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

private fun CodeSnippet.toDetailResponse(): SnippetDetailResponse {
    return SnippetDetailResponse(
        id = id,
        title = title,
        description = description,
        codeContent = codeContent,
        programmingLanguage = programmingLanguage,
        frameworkVersion = frameworkVersion,
        tags = tags.toList(),
        isPublic = isPublic,
        viewCount = viewCount,
        likeCount = likeCount,
        forkCount = forkCount,
        downloadCount = downloadCount,
        versionNumber = versionNumber,
        licenseType = licenseType,
        fileSize = fileSize,
        virusScanStatus = virusScanStatus.name,
        securityScanStatus = securityScanStatus.name,
        author = UserSummaryResponse(
            id = user.id,
            username = user.username,
            displayName = user.getDisplayName(),
            avatarUrl = user.avatarUrl
        ),
        category = category?.let { CategoryResponse(it.id, it.name, it.description, it.iconUrl, it.colorCode) },
        originalSnippetId = originalSnippetId,
        gitRepositoryUrl = gitRepositoryUrl,
        gitBranch = gitBranch,
        gitCommitHash = gitCommitHash,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}