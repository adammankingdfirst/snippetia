package com.snippetia.controller

import com.snippetia.dto.*
import com.snippetia.service.AiBotService
import com.snippetia.security.CurrentUser
import com.snippetia.security.UserPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import jakarta.validation.Valid

@RestController
@RequestMapping("/api/ai")
class AiBotController(
    private val aiBotService: AiBotService
) {

    @PostMapping("/chat")
    @PreAuthorize("hasRole('USER')")
    fun chatWithBot(
        @Valid @RequestBody request: BotQueryRequest,
        @CurrentUser userPrincipal: UserPrincipal
    ): Mono<ResponseEntity<BotResponse>> {
        val context = request.context?.let { ctx ->
            BotContext(
                userId = userPrincipal.id,
                snippetId = ctx.snippetId,
                description = ctx.description
            )
        }
        
        return aiBotService.processUserQuery(request.query, context)
            .map { ResponseEntity.ok(it) }
    }

    @PostMapping("/analyze")
    @PreAuthorize("hasRole('USER')")
    fun analyzeCode(
        @Valid @RequestBody request: CodeAnalysisRequest
    ): Mono<ResponseEntity<CodeAnalysisResponse>> {
        return aiBotService.analyzeCode(request.code, request.language)
            .map { ResponseEntity.ok(it) }
    }

    @PostMapping("/complete")
    @PreAuthorize("hasRole('USER')")
    fun completeCode(
        @Valid @RequestBody request: CodeCompletionRequest
    ): Mono<ResponseEntity<CodeCompletionResponse>> {
        return aiBotService.generateCodeCompletion(
            request.code, 
            request.language, 
            request.cursorPosition
        ).map { ResponseEntity.ok(it) }
    }

    @PostMapping("/explain")
    @PreAuthorize("hasRole('USER')")
    fun explainCode(
        @Valid @RequestBody request: CodeExplanationRequest
    ): Mono<ResponseEntity<CodeExplanationResponse>> {
        return aiBotService.explainCode(request.code, request.language)
            .map { ResponseEntity.ok(it) }
    }

    @PostMapping("/document")
    @PreAuthorize("hasRole('USER')")
    fun generateDocumentation(
        @Valid @RequestBody request: DocumentationRequest
    ): Mono<ResponseEntity<DocumentationResponse>> {
        return aiBotService.generateDocumentation(request.code, request.language)
            .map { ResponseEntity.ok(it) }
    }

    @PostMapping("/refactor")
    @PreAuthorize("hasRole('USER')")
    fun suggestRefactoring(
        @Valid @RequestBody request: RefactoringRequest
    ): Mono<ResponseEntity<RefactoringResponse>> {
        return aiBotService.suggestRefactoring(request.code, request.language)
            .map { ResponseEntity.ok(it) }
    }
}