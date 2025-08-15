package com.snippetia.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.snippetia.dto.CreateSnippetRequest
import com.snippetia.dto.SnippetResponse
import com.snippetia.dto.UserSummaryResponse
import com.snippetia.service.AuthService
import com.snippetia.service.SnippetService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime

@WebMvcTest(SnippetController::class)
class SnippetControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var snippetService: SnippetService

    @MockBean
    private lateinit var authService: AuthService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `getAllSnippets should return paginated snippets`() {
        // Given
        val snippets = listOf(
            SnippetResponse(
                id = 1L,
                title = "Test Snippet",
                description = "A test snippet",
                programmingLanguage = "kotlin",
                frameworkVersion = null,
                tags = listOf("test"),
                isPublic = true,
                viewCount = 10,
                likeCount = 5,
                forkCount = 2,
                downloadCount = 8,
                versionNumber = "1.0.0",
                author = UserSummaryResponse(1L, "testuser", "Test User", null),
                category = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )
        val page = PageImpl(snippets, PageRequest.of(0, 10), 1)

        every { 
            snippetService.getAllPublicSnippets(null, null, null, null, any()) 
        } returns page

        // When & Then
        mockMvc.perform(get("/api/v1/snippets"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content[0].title").value("Test Snippet"))
            .andExpect(jsonPath("$.totalElements").value(1))
    }

    @Test
    fun `getSnippet should return snippet details`() {
        // Given
        val snippetDetail = mockk<com.snippetia.dto.SnippetDetailResponse>()
        every { snippetDetail.id } returns 1L
        every { snippetDetail.title } returns "Test Snippet"
        every { snippetDetail.programmingLanguage } returns "kotlin"

        every { snippetService.getSnippetById(1L) } returns snippetDetail

        // When & Then
        mockMvc.perform(get("/api/v1/snippets/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("Test Snippet"))
    }

    @Test
    @WithMockUser(roles = ["USER"])
    fun `createSnippet should create new snippet when authenticated`() {
        // Given
        val request = CreateSnippetRequest(
            title = "New Snippet",
            description = "A new snippet",
            codeContent = "println(\"Hello\")",
            programmingLanguage = "kotlin",
            tags = listOf("test"),
            isPublic = true
        )

        val response = SnippetResponse(
            id = 1L,
            title = "New Snippet",
            description = "A new snippet",
            programmingLanguage = "kotlin",
            frameworkVersion = null,
            tags = listOf("test"),
            isPublic = true,
            viewCount = 0,
            likeCount = 0,
            forkCount = 0,
            downloadCount = 0,
            versionNumber = "1.0.0",
            author = UserSummaryResponse(1L, "testuser", "Test User", null),
            category = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        every { authService.getCurrentUserId(any()) } returns 1L
        every { snippetService.createSnippet(1L, request) } returns response

        // When & Then
        mockMvc.perform(
            post("/api/v1/snippets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value("New Snippet"))
            .andExpect(jsonPath("$.programmingLanguage").value("kotlin"))

        verify { snippetService.createSnippet(1L, request) }
    }

    @Test
    fun `createSnippet should return unauthorized when not authenticated`() {
        // Given
        val request = CreateSnippetRequest(
            title = "New Snippet",
            codeContent = "println(\"Hello\")",
            programmingLanguage = "kotlin"
        )

        // When & Then
        mockMvc.perform(
            post("/api/v1/snippets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(roles = ["USER"])
    fun `deleteSnippet should delete snippet when authenticated`() {
        // Given
        every { authService.getCurrentUserId(any()) } returns 1L
        every { snippetService.deleteSnippet(1L, 1L) } returns Unit

        // When & Then
        mockMvc.perform(delete("/api/v1/snippets/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Snippet deleted successfully"))

        verify { snippetService.deleteSnippet(1L, 1L) }
    }

    @Test
    @WithMockUser(roles = ["USER"])
    fun `toggleLike should toggle like status`() {
        // Given
        every { authService.getCurrentUserId(any()) } returns 1L
        every { snippetService.toggleLike(1L, 1L) } returns true

        // When & Then
        mockMvc.perform(post("/api/v1/snippets/1/like"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Snippet liked"))

        verify { snippetService.toggleLike(1L, 1L) }
    }

    @Test
    @WithMockUser(roles = ["USER"])
    fun `forkSnippet should create forked snippet`() {
        // Given
        val forkedSnippet = SnippetResponse(
            id = 2L,
            title = "Test Snippet (Fork)",
            description = "A forked snippet",
            programmingLanguage = "kotlin",
            frameworkVersion = null,
            tags = listOf("test"),
            isPublic = true,
            viewCount = 0,
            likeCount = 0,
            forkCount = 0,
            downloadCount = 0,
            versionNumber = "1.0.0",
            author = UserSummaryResponse(1L, "testuser", "Test User", null),
            category = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        every { authService.getCurrentUserId(any()) } returns 1L
        every { snippetService.forkSnippet(1L, 1L) } returns forkedSnippet

        // When & Then
        mockMvc.perform(post("/api/v1/snippets/1/fork"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value("Test Snippet (Fork)"))

        verify { snippetService.forkSnippet(1L, 1L) }
    }

    @Test
    fun `getAllSnippets should support filtering by language`() {
        // Given
        val snippets = listOf<SnippetResponse>()
        val page = PageImpl(snippets, PageRequest.of(0, 10), 0)

        every { 
            snippetService.getAllPublicSnippets("kotlin", null, null, null, any()) 
        } returns page

        // When & Then
        mockMvc.perform(get("/api/v1/snippets?language=kotlin"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.totalElements").value(0))

        verify { snippetService.getAllPublicSnippets("kotlin", null, null, null, any()) }
    }

    @Test
    fun `getAllSnippets should support search functionality`() {
        // Given
        val snippets = listOf<SnippetResponse>()
        val page = PageImpl(snippets, PageRequest.of(0, 10), 0)

        every { 
            snippetService.getAllPublicSnippets(null, null, null, "test query", any()) 
        } returns page

        // When & Then
        mockMvc.perform(get("/api/v1/snippets?search=test query"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)

        verify { snippetService.getAllPublicSnippets(null, null, null, "test query", any()) }
    }
}