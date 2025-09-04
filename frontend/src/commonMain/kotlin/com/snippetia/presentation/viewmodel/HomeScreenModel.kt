package com.snippetia.presentation.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.snippetia.domain.model.CodeSnippet
import com.snippetia.domain.model.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class HomeUiState(
    val isLoading: Boolean = false,
    val snippets: List<CodeSnippet> = emptyList(),
    val allSnippets: List<CodeSnippet> = emptyList(),
    val featuredSnippets: List<CodeSnippet> = emptyList(),
    val trendingSnippets: List<CodeSnippet> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategory: Category? = null,
    val likedSnippets: Set<Long> = emptySet(),
    val hasMore: Boolean = true,
    val totalSnippets: Int = 0,
    val unreadNotificationCount: Int = 0,
    val stats: Map<String, Any>? = null,
    val error: String? = null
)

class HomeScreenModel(
    private val getSnippetsUseCase: GetSnippetsUseCase,
    private val getFeaturedSnippetsUseCase: GetFeaturedSnippetsUseCase,
    private val getTrendingSnippetsUseCase: GetTrendingSnippetsUseCase,
    private val likeSnippetUseCase: LikeSnippetUseCase,
    private val forkSnippetUseCase: ForkSnippetUseCase,
    private val searchSnippetsUseCase: SearchSnippetsUseCase
) : ScreenModel {
    
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()
    
    private var currentPage = 0
    private val pageSize = 20

    init {
        loadInitialData()
    }
    
    fun loadInitialData() {
        screenModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            try {
                // Load snippets, featured, and trending in parallel
                loadSnippets()
                loadFeaturedSnippets()
                loadTrendingSnippets()
                loadCategories()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    private suspend fun loadSnippets() {
        getSnippetsUseCase(page = currentPage, size = pageSize).collect { result ->
            result.onSuccess { pagedResult ->
                _state.value = _state.value.copy(
                    snippets = pagedResult.items,
                    allSnippets = pagedResult.items,
                    hasMore = pagedResult.hasNext,
                    totalSnippets = pagedResult.totalElements.toInt(),
                    isLoading = false,
                    error = null
                )
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = error.message
                )
            }
        }
    }
    
    private suspend fun loadFeaturedSnippets() {
        getFeaturedSnippetsUseCase(page = 0, size = 5).collect { result ->
            result.onSuccess { pagedResult ->
                _state.value = _state.value.copy(
                    featuredSnippets = pagedResult.items
                )
            }.onFailure { error ->
                // Don't update error state for featured snippets failure
                println("Failed to load featured snippets: ${error.message}")
            }
        }
    }
    
    private suspend fun loadTrendingSnippets() {
        getTrendingSnippetsUseCase(page = 0, size = 5).collect { result ->
            result.onSuccess { pagedResult ->
                _state.value = _state.value.copy(
                    trendingSnippets = pagedResult.items
                )
            }.onFailure { error ->
                // Don't update error state for trending snippets failure
                println("Failed to load trending snippets: ${error.message}")
            }
        }
    }
    
    private fun loadCategories() {
        // For now, use mock categories since we don't have a categories API yet
        _state.value = _state.value.copy(
            categories = generateMockCategories()
        )
    }
    
    private fun loadMockData() {
        // Generate mock snippets
        val mockSnippets = generateMockSnippets(20)
        
        _state.value = _state.value.copy(
            isLoading = false,
            snippets = mockSnippets,
            allSnippets = mockSnippets,
            featuredSnippets = mockSnippets.take(5),
            trendingSnippets = mockSnippets.drop(5).take(5),
            categories = generateMockCategories(),
            stats = mapOf(
                "totalSnippets" to 1234,
                "totalUsers" to 567,
                "totalLanguages" to 25,
                "todaySnippets" to 12
            )
        )
    }
    
    private fun generateMockCategories(): List<Category> {
        return listOf(
            Category(1, "Web Development", "Frontend and backend web technologies", "#3B82F6"),
            Category(2, "Mobile", "iOS and Android development", "#10B981"),
            Category(3, "Data Science", "Machine learning and analytics", "#8B5CF6"),
            Category(4, "DevOps", "Infrastructure and deployment", "#F59E0B"),
            Category(5, "Algorithms", "Data structures and algorithms", "#EF4444")
        )
    }
    
    fun selectCategory(category: Category?) {
        _state.value = _state.value.copy(selectedCategory = category)
        filterSnippetsByCategory(category)
    }
    
    private fun filterSnippetsByCategory(category: Category?) {
        val allSnippets = _state.value.allSnippets
        val filteredSnippets = if (category != null) {
            allSnippets.filter { snippet ->
                // Assuming snippets have categories or tags that match
                snippet.tags.contains(category.name.lowercase()) ||
                snippet.language.equals(category.name, ignoreCase = true)
            }
        } else {
            allSnippets
        }
        _state.value = _state.value.copy(snippets = filteredSnippets)
    }
    
    fun toggleLike(snippetId: Long) {
        screenModelScope.launch {
            // Optimistically update UI
            val currentLiked = _state.value.likedSnippets
            val newLiked = if (currentLiked.contains(snippetId)) {
                currentLiked - snippetId
            } else {
                currentLiked + snippetId
            }
            _state.value = _state.value.copy(likedSnippets = newLiked)
            
            // Make API call
            likeSnippetUseCase(snippetId).collect { result ->
                result.onSuccess { likeResult ->
                    // Update with actual server response
                    val updatedSnippets = _state.value.snippets.map { snippet ->
                        if (snippet.id == snippetId) {
                            snippet.copy(likeCount = likeResult.likeCount)
                        } else {
                            snippet
                        }
                    }
                    
                    val updatedLiked = if (likeResult.isLiked) {
                        _state.value.likedSnippets + snippetId
                    } else {
                        _state.value.likedSnippets - snippetId
                    }
                    
                    _state.value = _state.value.copy(
                        snippets = updatedSnippets,
                        likedSnippets = updatedLiked
                    )
                }.onFailure { error ->
                    // Revert optimistic update on failure
                    _state.value = _state.value.copy(likedSnippets = currentLiked)
                    // Show error message
                    _state.value = _state.value.copy(error = "Failed to like snippet: ${error.message}")
                }
            }
        }
    }
    
    fun forkSnippet(snippetId: Long) {
        // Update fork count in the UI
        val updatedSnippets = _state.value.snippets.map { snippet ->
            if (snippet.id == snippetId) {
                snippet.copy(forkCount = snippet.forkCount + 1)
            } else {
                snippet
            }
        }
        _state.value = _state.value.copy(snippets = updatedSnippets)
        
        // In a real app, make API call and navigate to editor
        // apiService.forkSnippet(snippetId)
        // navigator.navigate(CreateSnippetScreen(forkedFromId = snippetId))
    }
    
    fun shareSnippet(snippet: CodeSnippet) {
        // Create share text
        val shareText = buildString {
            appendLine("Check out this code snippet: ${snippet.title}")
            appendLine("Language: ${snippet.language}")
            if (snippet.description?.isNotBlank() == true) {
                appendLine("Description: ${snippet.description}")
            }
            appendLine("https://snippetia.com/snippets/${snippet.id}")
        }
        
        // In a real app, use platform-specific sharing
        // On Android: Intent.ACTION_SEND
        // On iOS: UIActivityViewController
        // For now, just copy to clipboard (if available)
        println("Share text: $shareText")
    }
    
    fun loadMoreSnippets() {
        if (_state.value.isLoading) return
        
        _state.value = _state.value.copy(isLoading = true)
        
        // Simulate loading more snippets
        screenModelScope.launch {
            try {
                delay(1000) // Simulate network delay
                
                // In a real app, load next page from API
                val newSnippets = generateMockSnippets(10) // Load 10 more
                val currentSnippets = _state.value.snippets
                val allSnippets = _state.value.allSnippets + newSnippets
                
                _state.value = _state.value.copy(
                    snippets = currentSnippets + newSnippets,
                    allSnippets = allSnippets,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to load more snippets: ${e.message}"
                )
            }
        }
    }
    
    private fun generateMockSnippets(count: Int): List<CodeSnippet> {
        return (1..count).map { index ->
            val languages = listOf("kotlin", "java", "javascript", "python", "go", "rust")
            val titles = listOf(
                "Advanced Algorithm Implementation",
                "Database Connection Helper",
                "UI Component Library",
                "API Client Wrapper",
                "Utility Functions Collection",
                "Performance Optimization Tricks"
            )
            
            CodeSnippet(
                id = System.currentTimeMillis() + index,
                title = "${titles.random()} ${index + _state.value.allSnippets.size}",
                description = "A useful code snippet for developers",
                content = "// Sample code content\nfun example() {\n    println(\"Hello World\")\n}",
                language = languages.random(),
                tags = listOf("utility", "helper", "example"),
                isPublic = true,
                viewCount = (100..1000).random().toLong(),
                likeCount = (10..100).random().toLong(),
                forkCount = (1..20).random().toLong(),
                author = com.snippetia.domain.model.User(
                    id = (1..100).random().toLong(),
                    username = "user${(1..100).random()}",
                    displayName = "Developer ${(1..100).random()}",
                    avatarUrl = null
                ),
                createdAt = java.time.LocalDateTime.now().minusDays((1..30).random().toLong()),
                updatedAt = java.time.LocalDateTime.now()
            )
        }
    }
}