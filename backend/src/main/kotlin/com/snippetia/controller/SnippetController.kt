package com.snippetia.controller

import com.snippetia.dto.*
import com.snippetia.service.SnippetService
import com.snippetia.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/snippets")
@Tag(name = "Code Snippets", description = "Code snippet management endpoints")
class SnippetController(
    private val snippetService: SnippetService,
    private val authService: AuthService
) {

    @GetMapping
    @Operation(summary = "Get all public snippets")
    fun getAllSnippets(
        @RequestParam(required = false) language: String?,
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) tags: List<String>?,
        @RequestParam(required = false) search: String?,
        pageable: Pageable
    ): ResponseEntity<Page<SnippetResponse>> {
        val snippets = snippetService.getAllPublicSnippets(language, category, tags, search, pageable)
        return ResponseEntity.ok(snippets)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get snippet by ID")
    fun getSnippet(@PathVariable id: Long): ResponseEntity<SnippetDetailResponse> {
        val snippet = snippetService.getSnippetById(id)
        return ResponseEntity.ok(snippet)
    }

    @PostMapping
    @Operation(summary = "Create a new snippet")
    @PreAuthorize("hasRole('USER')")
    fun createSnippet(
        @Valid @RequestBody request: CreateSnippetRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<SnippetResponse> {
        val userId = authService.getCurrentUserId(httpRequest)
        val snippet = snippetService.createSnippet(userId, request)
        return ResponseEntity.ok(snippet)
    }

    @PostMapping("/upload")
    @Operation(summary = "Upload snippet from file")
    @PreAuthorize("hasRole('USER')")
    fun uploadSnippet(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("title") title: String,
        @RequestParam("description", required = false) description: String?,
        @RequestParam("language") language: String,
        @RequestParam("tags", required = false) tags: List<String>?,
        @RequestParam("isPublic", defaultValue = "true") isPublic: Boolean,
        httpRequest: HttpServletRequest
    ): ResponseEntity<SnippetResponse> {
        val userId = authService.getCurrentUserId(httpRequest)
        val request = UploadSnippetRequest(file, title, description, language, tags ?: emptyList(), isPublic)
        val snippet = snippetService.uploadSnippet(userId, request)
        return ResponseEntity.ok(snippet)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update snippet")
    @PreAuthorize("hasRole('USER')")
    fun updateSnippet(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateSnippetRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<SnippetResponse> {
        val userId = authService.getCurrentUserId(httpRequest)
        val snippet = snippetService.updateSnippet(userId, id, request)
        return ResponseEntity.ok(snippet)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete snippet")
    @PreAuthorize("hasRole('USER')")
    fun deleteSnippet(
        @PathVariable id: Long,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ApiResponse> {
        val userId = authService.getCurrentUserId(httpRequest)
        snippetService.deleteSnippet(userId, id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Snippet deleted successfully"))
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "Like/unlike snippet")
    @PreAuthorize("hasRole('USER')")
    fun toggleLike(
        @PathVariable id: Long,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ApiResponse> {
        val userId = authService.getCurrentUserId(httpRequest)
        val liked = snippetService.toggleLike(userId, id)
        val message = if (liked) "Snippet liked" else "Snippet unliked"
        return ResponseEntity.ok(ApiResponse(success = true, message = message))
    }

    @PostMapping("/{id}/fork")
    @Operation(summary = "Fork snippet")
    @PreAuthorize("hasRole('USER')")
    fun forkSnippet(
        @PathVariable id: Long,
        httpRequest: HttpServletRequest
    ): ResponseEntity<SnippetResponse> {
        val userId = authService.getCurrentUserId(httpRequest)
        val snippet = snippetService.forkSnippet(userId, id)
        return ResponseEntity.ok(snippet)
    }

    @GetMapping("/{id}/versions")
    @Operation(summary = "Get snippet versions")
    fun getSnippetVersions(@PathVariable id: Long): ResponseEntity<List<SnippetVersionResponse>> {
        val versions = snippetService.getSnippetVersions(id)
        return ResponseEntity.ok(versions)
    }

    @PostMapping("/{id}/versions")
    @Operation(summary = "Create new version")
    @PreAuthorize("hasRole('USER')")
    fun createVersion(
        @PathVariable id: Long,
        @Valid @RequestBody request: CreateVersionRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<SnippetVersionResponse> {
        val userId = authService.getCurrentUserId(httpRequest)
        val version = snippetService.createVersion(userId, id, request)
        return ResponseEntity.ok(version)
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user's snippets")
    @PreAuthorize("hasRole('USER')")
    fun getMySnippets(
        httpRequest: HttpServletRequest,
        pageable: Pageable
    ): ResponseEntity<Page<SnippetResponse>> {
        val userId = authService.getCurrentUserId(httpRequest)
        val snippets = snippetService.getUserSnippets(userId, pageable)
        return ResponseEntity.ok(snippets)
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured snippets")
    fun getFeaturedSnippets(pageable: Pageable): ResponseEntity<Page<SnippetResponse>> {
        val snippets = snippetService.getFeaturedSnippets(pageable)
        return ResponseEntity.ok(snippets)
    }

    @GetMapping("/trending")
    @Operation(summary = "Get trending snippets")
    fun getTrendingSnippets(pageable: Pageable): ResponseEntity<Page<SnippetResponse>> {
        val snippets = snippetService.getTrendingSnippets(pageable)
        return ResponseEntity.ok(snippets)
    }

    @PostMapping("/{id}/comments")
    @Operation(summary = "Add comment to snippet")
    @PreAuthorize("hasRole('USER')")
    fun addComment(
        @PathVariable id: Long,
        @Valid @RequestBody request: CreateCommentRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<CommentResponse> {
        val userId = authService.getCurrentUserId(httpRequest)
        val comment = snippetService.addComment(userId, id, request)
        return ResponseEntity.ok(comment)
    }

    @GetMapping("/{id}/comments")
    @Operation(summary = "Get snippet comments")
    fun getComments(
        @PathVariable id: Long,
        pageable: Pageable
    ): ResponseEntity<Page<CommentResponse>> {
        val comments = snippetService.getComments(id, pageable)
        return ResponseEntity.ok(comments)
    }
}