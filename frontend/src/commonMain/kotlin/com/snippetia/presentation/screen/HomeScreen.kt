package com.snippetia.presentation.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.snippetia.domain.model.CodeSnippet
import com.snippetia.domain.model.Category
import com.snippetia.presentation.component.*
import com.snippetia.presentation.viewmodel.HomeScreenModel
import com.snippetia.presentation.viewmodel.HomeUiState
import kotlinx.coroutines.launch

class HomeScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = getScreenModel<HomeScreenModel>()
        val state by screenModel.state.collectAsState()
        val scope = rememberCoroutineScope()
        val listState = rememberLazyListState()
        val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

        LaunchedEffect(Unit) {
            screenModel.loadInitialData()
        }

        Scaffold(
            modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
            topBar = {
                HomeTopAppBar(
                    scrollBehavior = topAppBarScrollBehavior,
                    onSearchClick = { navigator.push(SearchScreen()) },
                    onNotificationClick = { navigator.push(NotificationScreen()) },
                    onProfileClick = { navigator.push(ProfileScreen()) },
                    unreadNotificationCount = state.unreadNotificationCount
                )
            },
            floatingActionButton = {
                AnimatedVisibility(
                    visible = !state.isLoading,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    ExtendedFloatingActionButton(
                        onClick = { navigator.push(CreateSnippetScreen()) },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("Create") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    state.isLoading && state.snippets.isEmpty() -> {
                        LoadingContent()
                    }
                    state.error != null -> {
                        ErrorContent(
                            error = state.error,
                            onRetry = { screenModel.loadInitialData() }
                        )
                    }
                    else -> {
                        HomeContent(
                            state = state,
                            listState = listState,
                            onCategorySelected = { screenModel.selectCategory(it) },
                            onSnippetClick = { navigator.push(SnippetDetailScreen(it.id)) },
                            onLikeClick = { screenModel.toggleLike(it.id) },
                            onForkClick = { screenModel.forkSnippet(it.id) },
                            onShareClick = { screenModel.shareSnippet(it) },
                            onUserClick = { navigator.push(UserProfileScreen(it.id)) },
                            onLoadMore = { screenModel.loadMoreSnippets() }
                        )
                    }
                }

                // Loading overlay for pagination
                AnimatedVisibility(
                    visible = state.isLoading && state.snippets.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    unreadNotificationCount: Int
) {
    LargeTopAppBar(
        title = {
            Column {
                Text(
                    text = "Snippetia",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Discover amazing code snippets",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            BadgedBox(
                badge = {
                    if (unreadNotificationCount > 0) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.error
                        ) {
                            Text(
                                text = if (unreadNotificationCount > 99) "99+" else unreadNotificationCount.toString(),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            ) {
                IconButton(onClick = onNotificationClick) {
                    Icon(
                        if (unreadNotificationCount > 0) Icons.Default.Notifications else Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            IconButton(onClick = onProfileClick) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        )
    )
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    listState: LazyListState,
    onCategorySelected: (Category?) -> Unit,
    onSnippetClick: (CodeSnippet) -> Unit,
    onLikeClick: (CodeSnippet) -> Unit,
    onForkClick: (CodeSnippet) -> Unit,
    onShareClick: (CodeSnippet) -> Unit,
    onUserClick: (Long) -> Unit,
    onLoadMore: () -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 88.dp), // Space for FAB
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Featured Section
        if (state.featuredSnippets.isNotEmpty()) {
            item {
                FeaturedSection(
                    snippets = state.featuredSnippets,
                    onSnippetClick = onSnippetClick,
                    onUserClick = onUserClick
                )
            }
        }

        // Categories Filter
        item {
            CategoriesFilter(
                categories = state.categories,
                selectedCategory = state.selectedCategory,
                onCategorySelected = onCategorySelected
            )
        }

        // Quick Stats
        if (state.stats != null) {
            item {
                QuickStatsCard(stats = state.stats)
            }
        }

        // Trending Section
        if (state.trendingSnippets.isNotEmpty()) {
            item {
                TrendingSection(
                    snippets = state.trendingSnippets,
                    onSnippetClick = onSnippetClick,
                    onUserClick = onUserClick
                )
            }
        }

        // Main Content Header
        item {
            SectionHeader(
                title = when {
                    state.selectedCategory != null -> "Snippets in ${state.selectedCategory.name}"
                    else -> "Latest Snippets"
                },
                subtitle = "${state.totalSnippets} snippets available"
            )
        }

        // Snippets List
        if (state.snippets.isEmpty() && !state.isLoading) {
            item {
                EmptyState(
                    title = "No snippets found",
                    description = "Be the first to share a code snippet in this category!",
                    icon = Icons.Default.Code,
                    actionText = "Create Snippet",
                    onAction = { /* Navigate to create */ }
                )
            }
        } else {
            items(
                items = state.snippets,
                key = { it.id }
            ) { snippet ->
                SnippetCard(
                    snippet = snippet,
                    onSnippetClick = { onSnippetClick(snippet) },
                    onLikeClick = { onLikeClick(snippet) },
                    onForkClick = { onForkClick(snippet) },
                    onShareClick = { onShareClick(snippet) },
                    onUserClick = { onUserClick(snippet.user.id) },
                    isLiked = state.likedSnippets.contains(snippet.id),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .animateItemPlacement()
                )
            }

            // Load more indicator
            if (state.hasMore && !state.isLoading) {
                item {
                    LaunchedEffect(Unit) {
                        onLoadMore()
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedSection(
    snippets: List<CodeSnippet>,
    onSnippetClick: (CodeSnippet) -> Unit,
    onUserClick: (Long) -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        SectionHeader(
            title = "Featured",
            subtitle = "Hand-picked quality snippets",
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(snippets) { snippet ->
                FeaturedSnippetCard(
                    snippet = snippet,
                    onSnippetClick = { onSnippetClick(snippet) },
                    onUserClick = { onUserClick(snippet.user.id) },
                    modifier = Modifier.width(280.dp)
                )
            }
        }
    }
}

@Composable
private fun TrendingSection(
    snippets: List<CodeSnippet>,
    onSnippetClick: (CodeSnippet) -> Unit,
    onUserClick: (Long) -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        SectionHeader(
            title = "Trending",
            subtitle = "Popular snippets this week",
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(snippets) { snippet ->
                TrendingSnippetCard(
                    snippet = snippet,
                    onSnippetClick = { onSnippetClick(snippet) },
                    onUserClick = { onUserClick(snippet.user.id) },
                    modifier = Modifier.width(240.dp)
                )
            }
        }
    }
}

@Composable
private fun CategoriesFilter(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category?) -> Unit
) {
    LazyRow(
        modifier = Modifier.padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                onClick = { onCategorySelected(null) },
                label = { Text("All") },
                selected = selectedCategory == null,
                leadingIcon = if (selectedCategory == null) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else null
            )
        }
        
        items(categories) { category ->
            FilterChip(
                onClick = { onCategorySelected(category) },
                label = { Text(category.name) },
                selected = selectedCategory?.id == category.id,
                leadingIcon = if (selectedCategory?.id == category.id) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else null
            )
        }
    }
}

@Composable
private fun QuickStatsCard(
    stats: Map<String, Any>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                icon = Icons.Default.Code,
                value = stats["totalSnippets"]?.toString() ?: "0",
                label = "Snippets"
            )
            StatItem(
                icon = Icons.Default.People,
                value = stats["totalUsers"]?.toString() ?: "0",
                label = "Developers"
            )
            StatItem(
                icon = Icons.Default.Language,
                value = stats["totalLanguages"]?.toString() ?: "0",
                label = "Languages"
            )
            StatItem(
                icon = Icons.Default.TrendingUp,
                value = stats["todaySnippets"]?.toString() ?: "0",
                label = "Today"
            )
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp
            )
            Text(
                text = "Loading amazing snippets...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = "Oops! Something went wrong",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onRetry,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Try Again")
            }
        }
    }
}