package com.snippetia.service

import com.snippetia.dto.CreateSnippetRequest
import com.snippetia.dto.UpdateSnippetRequest
import com.snippetia.exception.ResourceNotFoundException
import com.snippetia.model.CodeSnippet
import com.snippetia.model.User
import com.snippetia.repository.CodeSnippetRepository
import com.snippetia.repository.LikeRepository
import com.snippetia.repository.UserRepository
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SnippetServiceTest {

    private val snippetRepository = mockk<CodeSnippetRepository>()
    private val userRepository = mockk<UserRepository>()
    private val likeRepository = mockk<LikeRepository>()
    private val searchService = mockk<SearchService>()
    private val securityScanService = mockk<SecurityScanService>()
    private val notificationService = mockk<NotificationService>()

    private lateinit var snippetService: SnippetService

    private val testUser = User(
        id = 1L,
        username = "testuser",
        email = "test@example.com",
        firstName = "Test",
        lastName = "User",
        displayName = "Test User"
    )

    private val testSnippet = CodeSnippet(
        id = 1L,
        title = "Test Snippet",
        description = "A test snippet",
        content = "fun main() { println(\"Hello World\") }",
        language = "kotlin",
        tags = listOf("test", "kotlin"),
        author = testUser,
        isPublic = true,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )

    @BeforeEach
    fun setup() {
        snippetService = SnippetService(
            snippetRepository,
            userRepository,
            likeRepository,
            searchService,
            securityScanService,
            notificationService
        )
    }

    @Test
    fun `createSnippet should create and return snippet`() {
        // Given
        val request = CreateSnippetRequest(
            title = "Test Snippet",
            description = "A test snippet",
            content = "fun main() { println(\"Hello World\") }",
            language = "kotlin",
            tags = listOf("test", "kotlin"),
            isPublic = true
        )

        every { userRepository.findById(1L) } returns Optional.of(testUser)
        every { securityScanService.scanCode(any()) } returns emptyList()
        every { snippetRepository.save(any<CodeSnippet>()) } returns testSnippet

        // When
        val result = snippetService.createSnippet(1L, request)

        // Then
        assertNotNull(result)
        assertEquals("Test Snippet", result.title)
        assertEquals("kotlin", result.language)
        verify { snippetRepository.save(any<CodeSnippet>()) }
    }

    @Test
    fun `createSnippet should throw exception when user not found`() {
        // Given
        val request = CreateSnippetRequest(
            title = "Test Snippet",
            content = "test code",
            language = "kotlin"
        )

        every { userRepository.findById(1L) } returns Optional.empty()

        // When & Then
        assertThrows<ResourceNotFoundException> {
            snippetService.createSnippet(1L, request)
        }
    }

    @Test
    fun `getSnippet should return snippet when found`() {
        // Given
        every { snippetRepository.findById(1L) } returns Optional.of(testSnippet)

        // When
        val result = snippetService.getSnippet(1L)

        // Then
        assertNotNull(result)
        assertEquals(1L, result.id)
        assertEquals("Test Snippet", result.title)
    }

    @Test
    fun `getSnippet should throw exception when not found`() {
        // Given
        every { snippetRepository.findById(1L) } returns Optional.empty()

        // When & Then
        assertThrows<ResourceNotFoundException> {
            snippetService.getSnippet(1L)
        }
    }

    @Test
    fun `updateSnippet should update and return snippet`() {
        // Given
        val request = UpdateSnippetRequest(
            title = "Updated Snippet",
            description = "Updated description",
            content = "fun updated() { println(\"Updated\") }",
            language = "kotlin",
            tags = listOf("updated", "kotlin"),
            isPublic = false
        )

        val updatedSnippet = testSnippet.copy(
            title = "Updated Snippet",
            description = "Updated description",
            content = "fun updated() { println(\"Updated\") }",
            isPublic = false
        )

        every { snippetRepository.findById(1L) } returns Optional.of(testSnippet)
        every { securityScanService.scanCode(any()) } returns emptyList()
        every { snippetRepository.save(any<CodeSnippet>()) } returns updatedSnippet

        // When
        val result = snippetService.updateSnippet(1L, 1L, request)

        // Then
        assertNotNull(result)
        assertEquals("Updated Snippet", result.title)
        assertEquals("Updated description", result.description)
        verify { snippetRepository.save(any<CodeSnippet>()) }
    }

    @Test
    fun `deleteSnippet should delete snippet when user is owner`() {
        // Given
        every { snippetRepository.findById(1L) } returns Optional.of(testSnippet)
        every { snippetRepository.delete(testSnippet) } just Runs

        // When
        snippetService.deleteSnippet(1L, 1L)

        // Then
        verify { snippetRepository.delete(testSnippet) }
    }

    @Test
    fun `getPublicSnippets should return paginated snippets`() {
        // Given
        val pageable = PageRequest.of(0, 10)
        val snippets = listOf(testSnippet)
        val page = PageImpl(snippets, pageable, 1)

        every { snippetRepository.findByIsPublicTrueOrderByCreatedAtDesc(pageable) } returns page

        // When
        val result = snippetService.getPublicSnippets(pageable)

        // Then
        assertNotNull(result)
        assertEquals(1, result.content.size)
        assertEquals("Test Snippet", result.content[0].title)
    }

    @Test
    fun `likeSnippet should toggle like status`() {
        // Given
        every { snippetRepository.findById(1L) } returns Optional.of(testSnippet)
        every { userRepository.findById(1L) } returns Optional.of(testUser)
        every { likeRepository.findByUserAndSnippet(testUser, testSnippet) } returns null
        every { likeRepository.save(any()) } returns mockk()
        every { snippetRepository.save(any<CodeSnippet>()) } returns testSnippet.copy(likeCount = 1)

        // When
        val result = snippetService.likeSnippet(1L, 1L)

        // Then
        assertTrue(result.isLiked)
        assertEquals(1, result.likeCount)
        verify { likeRepository.save(any()) }
    }

    @Test
    fun `forkSnippet should create a copy of the snippet`() {
        // Given
        val forkedSnippet = testSnippet.copy(
            id = 2L,
            title = "Fork of Test Snippet",
            author = testUser,
            forkCount = 0,
            parentSnippetId = 1L
        )

        every { snippetRepository.findById(1L) } returns Optional.of(testSnippet)
        every { userRepository.findById(1L) } returns Optional.of(testUser)
        every { snippetRepository.save(any<CodeSnippet>()) } returnsMany listOf(
            testSnippet.copy(forkCount = 1),
            forkedSnippet
        )

        // When
        val result = snippetService.forkSnippet(1L, 1L)

        // Then
        assertNotNull(result)
        assertEquals("Fork of Test Snippet", result.title)
        assertEquals(1L, result.parentSnippetId)
        verify(exactly = 2) { snippetRepository.save(any<CodeSnippet>()) }
    }
}