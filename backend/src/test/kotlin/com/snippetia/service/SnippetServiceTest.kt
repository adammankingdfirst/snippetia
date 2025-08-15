package com.snippetia.service

import com.snippetia.dto.CreateSnippetRequest
import com.snippetia.exception.ResourceNotFoundException
import com.snippetia.exception.UnauthorizedException
import com.snippetia.model.*
import com.snippetia.repository.*
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExtendWith(MockKExtension::class)
class SnippetServiceTest {

    @MockK
    private lateinit var snippetRepository: CodeSnippetRepository

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var categoryRepository: CategoryRepository

    @MockK
    private lateinit var snippetVersionRepository: SnippetVersionRepository

    @MockK
    private lateinit var commentRepository: CommentRepository

    @MockK
    private lateinit var snippetLikeRepository: SnippetLikeRepository

    @MockK
    private lateinit var securityScanService: SecurityScanService

    @MockK
    private lateinit var virusScanService: VirusScanService

    @MockK
    private lateinit var gitIntegrationService: GitIntegrationService

    @MockK
    private lateinit var searchService: SearchService

    private lateinit var snippetService: SnippetService

    private lateinit var testUser: User
    private lateinit var testSnippet: CodeSnippet

    @BeforeEach
    fun setUp() {
        snippetService = SnippetService(
            snippetRepository,
            userRepository,
            categoryRepository,
            snippetVersionRepository,
            commentRepository,
            snippetLikeRepository,
            securityScanService,
            virusScanService,
            gitIntegrationService,
            searchService
        )

        testUser = User(
            id = 1L,
            email = "test@example.com",
            username = "testuser",
            password = "hashedpassword",
            firstName = "Test",
            lastName = "User"
        )

        testSnippet = CodeSnippet(
            id = 1L,
            title = "Test Snippet",
            description = "A test code snippet",
            codeContent = "println(\"Hello, World!\")",
            programmingLanguage = "kotlin",
            tags = setOf("test", "kotlin"),
            user = testUser,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    @Test
    fun `createSnippet should create and return snippet when valid request`() {
        // Given
        val request = CreateSnippetRequest(
            title = "Test Snippet",
            description = "A test snippet",
            codeContent = "println(\"Hello, World!\")",
            programmingLanguage = "kotlin",
            tags = listOf("test", "kotlin"),
            isPublic = true
        )

        every { userRepository.findById(1L) } returns Optional.of(testUser)
        every { snippetRepository.save(any<CodeSnippet>()) } returns testSnippet
        every { securityScanService.scanSnippet(any()) } just Runs
        every { virusScanService.scanSnippet(any()) } just Runs

        // When
        val result = snippetService.createSnippet(1L, request)

        // Then
        assertNotNull(result)
        assertEquals("Test Snippet", result.title)
        assertEquals("kotlin", result.programmingLanguage)
        verify { snippetRepository.save(any<CodeSnippet>()) }
        verify { securityScanService.scanSnippet(any()) }
        verify { virusScanService.scanSnippet(any()) }
    }

    @Test
    fun `createSnippet should throw ResourceNotFoundException when user not found`() {
        // Given
        val request = CreateSnippetRequest(
            title = "Test Snippet",
            codeContent = "println(\"Hello, World!\")",
            programmingLanguage = "kotlin"
        )

        every { userRepository.findById(1L) } returns Optional.empty()

        // When & Then
        assertThrows<ResourceNotFoundException> {
            snippetService.createSnippet(1L, request)
        }
    }

    @Test
    fun `getSnippetById should return snippet and increment view count`() {
        // Given
        every { snippetRepository.findById(1L) } returns Optional.of(testSnippet)
        every { snippetRepository.incrementViewCount(1L) } just Runs

        // When
        val result = snippetService.getSnippetById(1L)

        // Then
        assertNotNull(result)
        assertEquals("Test Snippet", result.title)
        verify { snippetRepository.incrementViewCount(1L) }
    }

    @Test
    fun `getSnippetById should throw ResourceNotFoundException when snippet not found`() {
        // Given
        every { snippetRepository.findById(1L) } returns Optional.empty()

        // When & Then
        assertThrows<ResourceNotFoundException> {
            snippetService.getSnippetById(1L)
        }
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
    fun `deleteSnippet should throw UnauthorizedException when user is not owner`() {
        // Given
        val otherUser = testUser.copy(id = 2L)
        val snippet = testSnippet.copy(user = otherUser)
        every { snippetRepository.findById(1L) } returns Optional.of(snippet)

        // When & Then
        assertThrows<UnauthorizedException> {
            snippetService.deleteSnippet(1L, 1L)
        }
    }

    @Test
    fun `toggleLike should add like when not already liked`() {
        // Given
        every { snippetRepository.findById(1L) } returns Optional.of(testSnippet)
        every { userRepository.findById(1L) } returns Optional.of(testUser)
        every { snippetLikeRepository.findByUserAndSnippet(testUser, testSnippet) } returns null
        every { snippetLikeRepository.save(any<SnippetLike>()) } returns mockk()
        every { snippetRepository.incrementLikeCount(1L) } just Runs

        // When
        val result = snippetService.toggleLike(1L, 1L)

        // Then
        assertEquals(true, result)
        verify { snippetLikeRepository.save(any<SnippetLike>()) }
        verify { snippetRepository.incrementLikeCount(1L) }
    }

    @Test
    fun `toggleLike should remove like when already liked`() {
        // Given
        val existingLike = SnippetLike(id = 1L, user = testUser, snippet = testSnippet)
        every { snippetRepository.findById(1L) } returns Optional.of(testSnippet)
        every { userRepository.findById(1L) } returns Optional.of(testUser)
        every { snippetLikeRepository.findByUserAndSnippet(testUser, testSnippet) } returns existingLike
        every { snippetLikeRepository.delete(existingLike) } just Runs
        every { snippetRepository.decrementLikeCount(1L) } just Runs

        // When
        val result = snippetService.toggleLike(1L, 1L)

        // Then
        assertEquals(false, result)
        verify { snippetLikeRepository.delete(existingLike) }
        verify { snippetRepository.decrementLikeCount(1L) }
    }

    @Test
    fun `getAllPublicSnippets should return paginated snippets`() {
        // Given
        val pageable = PageRequest.of(0, 10)
        val snippets = listOf(testSnippet)
        val page = PageImpl(snippets, pageable, 1)

        every { 
            snippetRepository.findPublicSnippets(null, null, null, pageable) 
        } returns page

        // When
        val result = snippetService.getAllPublicSnippets(null, null, null, null, pageable)

        // Then
        assertEquals(1, result.totalElements)
        assertEquals(1, result.content.size)
        assertEquals("Test Snippet", result.content[0].title)
    }

    @Test
    fun `forkSnippet should create forked snippet and increment fork count`() {
        // Given
        val forkedSnippet = testSnippet.copy(
            id = 2L,
            title = "Test Snippet (Fork)",
            originalSnippetId = 1L
        )

        every { snippetRepository.findById(1L) } returns Optional.of(testSnippet)
        every { userRepository.findById(1L) } returns Optional.of(testUser)
        every { snippetRepository.save(any<CodeSnippet>()) } returns forkedSnippet
        every { snippetRepository.incrementForkCount(1L) } just Runs

        // When
        val result = snippetService.forkSnippet(1L, 1L)

        // Then
        assertNotNull(result)
        assertEquals("Test Snippet (Fork)", result.title)
        assertEquals(1L, result.originalSnippetId)
        verify { snippetRepository.incrementForkCount(1L) }
    }
}