package com.snippetia.presentation.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.snippetia.domain.model.CodeSnippet
import com.snippetia.presentation.component.*
import com.snippetia.presentation.theme.getLanguageIndicatorColor
import com.snippetia.presentation.util.formatTimeAgo
import com.snippetia.presentation.util.formatCount

data class SnippetDetailScreen(
    val snippetId: Long
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val clipboardManager = LocalClipboardManager.current
        
        // Mock data - replace with actual data loading
        val snippet = remember { createMockSnippet() }
        
        var isLiked by remember { mutableStateOf(false) }
        var isBookmarked by remember { mutableStateOf(false) }
        var showComments by remember { mutableStateOf(false) }
        var showVersions by remember { mutableStateOf(false) }
        var isEditing by remember { mutableStateOf(false) }
        
        var codeEditorState by remember {
            mutableStateOf(
                CodeEditorState(
                    content = TextFieldValue(snippet.content),
                    language = snippet.language,
                    isReadOnly = true,
                    showLineNumbers = true,
                    syntaxHighlighting = true,
                    aiAssistance = false
                )
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = snippet.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "by ${snippet.user.displayName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        // Share
                        IconButton(onClick = { /* Share snippet */ }) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                        
                        // Bookmark
                        IconButton(onClick = { isBookmarked = !isBookmarked }) {
                            Icon(
                                if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // More options
                        var showMenu by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More")
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Edit") },
                                    onClick = {
                                        isEditing = true
                                        codeEditorState = codeEditorState.copy(
                                            isReadOnly = false,
                                            aiAssistance = true
                                        )
                                        showMenu = false
                                    },
                                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Fork") },
                                    onClick = {
                                        // Navigate to create screen with forked content
                                        showMenu = false
                                    },
                                    leadingIcon = { Icon(Icons.Default.ForkRight, contentDescription = null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Download") },
                                    onClick = {
                                        // Download snippet
                                        showMenu = false
                                    },
                                    leadingIcon = { Icon(Icons.Default.Download, contentDescription = null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Report") },
                                    onClick = {
                                        // Report snippet
                                        showMenu = false
                                    },
                                    leadingIcon = { Icon(Icons.Default.Report, contentDescription = null) }
                                )
                            }
                        }
                    }
                )
            },
            bottomBar = {
                if (isEditing) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 3.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    isEditing = false
                                    codeEditorState = codeEditorState.copy(
                                        isReadOnly = true,
                                        aiAssistance = false,
                                        content = TextFieldValue(snippet.content)
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel")
                            }
                            
                            Button(
                                onClick = {
                                    // Save changes
                                    isEditing = false
                                    codeEditorState = codeEditorState.copy(
                                        isReadOnly = true,
                                        aiAssistance = false
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Save Changes")
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Snippet Header
                item {
                    SnippetHeader(
                        snippet = snippet,
                        isLiked = isLiked,
                        onLikeClick = { isLiked = !isLiked },
                        onUserClick = { /* Navigate to user profile */ }
                    )
                }

                // Action Buttons
                item {
                    ActionButtonsRow(
                        onCopyClick = {
                            clipboardManager.setText(AnnotatedString(snippet.content))
                        },
                        onRunClick = { /* Run code */ },
                        onVersionsClick = { showVersions = !showVersions },
                        onCommentsClick = { showComments = !showComments }
                    )
                }

                // Code Editor
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isEditing) 600.dp else 400.dp)
                    ) {
                        CodeEditor(
                            state = codeEditorState,
                            onStateChange = { codeEditorState = it },
                            onContentChange = { /* Handle content change */ },
                            placeholder = "No code content",
                            onAiSuggestion = if (isEditing) { { /* Handle AI suggestions */ } } else null,
                            onFormatCode = if (isEditing) { { /* Format code */ } } else null,
                            onRunCode = { /* Run code */ },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Versions Section
                if (showVersions) {
                    item {
                        VersionsSection(
                            versions = listOf(), // Mock versions
                            onVersionClick = { /* Load version */ }
                        )
                    }
                }

                // Related Snippets
                item {
                    RelatedSnippetsSection(
                        snippets = listOf(), // Mock related snippets
                        onSnippetClick = { /* Navigate to snippet */ }
                    )
                }

                // Comments Section
                if (showComments) {
                    item {
                        CommentsSection(
                            comments = listOf(), // Mock comments
                            onAddComment = { /* Add comment */ }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SnippetHeader(
    snippet: CodeSnippet,
    isLiked: Boolean,
    onLikeClick: () -> Unit,
    onUserClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title and Description
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = snippet.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                if (snippet.description.isNotEmpty()) {
                    Text(
                        text = snippet.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Author and Metadata
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    UserAvatar(
                        avatarUrl = snippet.user.avatarUrl,
                        username = snippet.user.username,
                        size = 40.dp
                    )
                    
                    Column {
                        Text(
                            text = snippet.user.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = formatTimeAgo(snippet.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Language Badge
                Surface(
                    color = getLanguageIndicatorColor(snippet.language),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = snippet.language.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Tags
            if (snippet.tags.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(snippet.tags) { tag ->
                        AssistChip(
                            onClick = { /* Filter by tag */ },
                            label = {
                                Text(
                                    text = "#$tag",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        )
                    }
                }
            }

            // Stats and Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stats
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    StatItem(
                        icon = Icons.Default.Visibility,
                        count = snippet.viewCount,
                        label = "Views"
                    )
                    StatItem(
                        icon = if (isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        count = snippet.likeCount,
                        label = "Likes",
                        tint = if (isLiked) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = onLikeClick
                    )
                    StatItem(
                        icon = Icons.Default.ForkRight,
                        count = snippet.forkCount,
                        label = "Forks"
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButtonsRow(
    onCopyClick: () -> Unit,
    onRunClick: () -> Unit,
    onVersionsClick: () -> Unit,
    onCommentsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onCopyClick,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                Icons.Default.ContentCopy,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Copy")
        }
        
        OutlinedButton(
            onClick = onRunClick,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Run")
        }
        
        OutlinedButton(
            onClick = onVersionsClick,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                Icons.Default.History,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Versions")
        }
        
        OutlinedButton(
            onClick = onCommentsClick,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                Icons.Default.Comment,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Comments")
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Long,
    label: String,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: (() -> Unit)? = null
) {
    val modifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else {
        Modifier
    }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = tint
        )
        Column {
            Text(
                text = formatCount(count),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = tint
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = tint.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun VersionsSection(
    versions: List<Any>, // Replace with actual version type
    onVersionClick: (Any) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Version History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (versions.isEmpty()) {
                Text(
                    text = "No version history available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Version list would go here
            }
        }
    }
}

@Composable
private fun RelatedSnippetsSection(
    snippets: List<Any>, // Replace with actual snippet type
    onSnippetClick: (Any) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Related Snippets",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (snippets.isEmpty()) {
                Text(
                    text = "No related snippets found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Related snippets list would go here
            }
        }
    }
}

@Composable
private fun CommentsSection(
    comments: List<Any>, // Replace with actual comment type
    onAddComment: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Comments",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Add comment input
            var commentText by remember { mutableStateOf("") }
            
            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                placeholder = { Text("Add a comment...") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                onAddComment(commentText)
                                commentText = ""
                            }
                        },
                        enabled = commentText.isNotBlank()
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send")
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (comments.isEmpty()) {
                Text(
                    text = "No comments yet. Be the first to comment!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Comments list would go here
            }
        }
    }
}

// Mock data function - replace with actual data loading
private fun createMockSnippet(): CodeSnippet {
    return CodeSnippet(
        id = 1,
        title = "Kotlin Coroutines Example",
        description = "A simple example demonstrating how to use Kotlin coroutines for asynchronous programming",
        content = """
            import kotlinx.coroutines.*
            
            suspend fun fetchData(): String {
                delay(1000) // Simulate network call
                return "Hello, Coroutines!"
            }
            
            fun main() = runBlocking {
                println("Starting...")
                val result = fetchData()
                println(result)
                println("Done!")
            }
        """.trimIndent(),
        language = "kotlin",
        tags = listOf("coroutines", "async", "kotlin", "example"),
        isPublic = true,
        author = com.snippetia.domain.model.User(
            id = 1,
            username = "kotlindev",
            email = "dev@example.com",
            firstName = "John",
            lastName = "Doe",
            displayName = "John Doe",
            avatarUrl = null,
            bio = "Kotlin enthusiast",
            githubUsername = "kotlindev",
            twitterUsername = null,
            websiteUrl = null,
            isEmailVerified = true,
            isTwoFactorEnabled = false,
            accountStatus = "ACTIVE",
            roles = listOf("USER"),
            createdAt = kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()),
            lastLoginAt = null
        ),
        likeCount = 89,
        viewCount = 1234,
        forkCount = 23,
        forkedFrom = null,
        createdAt = kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()),
        updatedAt = kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
    )
}