package com.snippetia.presentation.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.snippetia.domain.model.CodeSnippet
import com.snippetia.domain.model.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val snippets: List<CodeSnippet> = emptyList(),
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

class HomeScreenModel : ScreenModel {
    
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()
    
    fun loadInitialData() {
        screenModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            try {
                // TODO: Load data from repository
                // For now, we'll use mock data
                loadMockData()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    private fun loadMockData() {
        // Mock data for development
        _state.value = _state.value.copy(
            isLoading = false,
            snippets = emptyList(),
            featuredSnippets = emptyList(),
            trendingSnippets = emptyList(),
            categories = emptyList(),
            stats = mapOf(
                "totalSnippets" to 1234,
                "totalUsers" to 567,
                "totalLanguages" to 25,
                "todaySnippets" to 12
            )
        )
    }
    
    fun selectCategory(category: Category?) {
        _state.value = _state.value.copy(selectedCategory = category)
        // TODO: Filter snippets by category
    }
    
    fun toggleLike(snippetId: Long) {
        val currentLiked = _state.value.likedSnippets
        val newLiked = if (currentLiked.contains(snippetId)) {
            currentLiked - snippetId
        } else {
            currentLiked + snippetId
        }
        _state.value = _state.value.copy(likedSnippets = newLiked)
        // TODO: Implement API call
    }
    
    fun forkSnippet(snippetId: Long) {
        // TODO: Implement fork functionality
    }
    
    fun shareSnippet(snippet: CodeSnippet) {
        // TODO: Implement share functionality
    }
    
    fun loadMoreSnippets() {
        // TODO: Implement pagination
    }
}