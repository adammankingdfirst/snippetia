package com.snippetia.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.snippetia.data.model.CodeSnippet
import com.snippetia.ui.component.*
import com.snippetia.ui.viewmodel.HomeScreenModel
import kotlinx.coroutines.launch

class HomeScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = getScreenModel<HomeScreenModel>()
        val state by screenModel.state.collectAsState()
        val scope = rememberCoroutineScope()
        val listState = rememberLazyListState()

        LaunchedEffect(Unit) {
            screenModel.loadSnippets()
        }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top App Bar
            TopAppBar(
                title = {
                    Text(
                        text = "Snippetia",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = { navigator.push(SearchScreen()) }
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(
                        onClick = { navigator.push(ProfileScreen()) }
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                }
            )

            // Filter Chips
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.categories) { category ->
                    FilterChip(
                        onClick = { screenModel.filterByCategory(category.id) },
                        label = { Text(category.name) },
                        selected = state.selectedCategoryId == category.id,
                        leadingIcon = if (state.selectedCategoryId == category.id) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else null
                    )
                }
            }

            // Content
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading && state.snippets.isEmpty() -> {
                        LoadingIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    state.error != null -> {
                        ErrorMessage(
                            message = state.error,
                            onRetry = { screenModel.loadSnippets() },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    state.snippets.isEmpty() -> {
                        EmptyState(
                            title = "No snippets found",
                            description = "Be the first to share a code snippet!",
                            icon = Icons.Default.Code,
                            actionText = "Create Snippet",
                            onAction = { navigator.push(CreateSnippetScreen()) },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(
                                items = state.snippets,
                                key = { it.id }
                            ) { snippet ->
                                SnippetCard(
                                    snippet = snippet,
                                    onSnippetClick = { navigator.push(SnippetDetailScreen(snippet.id)) },
                                    onLikeClick = { screenModel.toggleLike(snippet.id) },
                                    onForkClick = { screenModel.forkSnippet(snippet.id) },
                                    onShareClick = { screenModel.shareSnippet(snippet) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            if (state.hasMore && !state.isLoading) {
                                item {
                                    LaunchedEffect(Unit) {
                                        screenModel.loadMoreSnippets()
                                    }
                                    LoadingIndicator(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Floating Action Button
                ExtendedFloatingActionButton(
                    onClick = { navigator.push(CreateSnippetScreen()) },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Create") },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                )

                // Loading overlay for pagination
                if (state.isLoading && state.snippets.isNotEmpty()) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                    )
                }
            }
        }

        // Handle infinite scroll
        LaunchedEffect(listState) {
            snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                .collect { lastVisibleIndex ->
                    if (lastVisibleIndex != null && 
                        lastVisibleIndex >= state.snippets.size - 3 && 
                        state.hasMore && 
                        !state.isLoading) {
                        screenModel.loadMoreSnippets()
                    }
                }
        }
    }
}