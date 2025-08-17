package com.snippetia.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.snippetia.presentation.component.CodeEditor
import com.snippetia.presentation.component.CodeEditorState

data class CreateSnippetScreen(
    val snippetId: Long? = null // null for create, non-null for edit
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var selectedTags by remember { mutableStateOf<Set<String>>(emptySet()) }
        var newTag by remember { mutableStateOf("") }
        var isPublic by remember { mutableStateOf(true) }
        var isLoading by remember { mutableStateOf(false) }
        
        var codeEditorState by remember {
            mutableStateOf(
                CodeEditorState(
                    content = TextFieldValue("// Start coding your snippet here...\n"),
                    language = "kotlin",
                    showLineNumbers = true,
                    syntaxHighlighting = true,
                    aiAssistance = true
                )
            )
        }

        val scrollState = rememberScrollState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (snippetId == null) "Create Snippet" else "Edit Snippet",
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        // Save Draft
                        TextButton(
                            onClick = { /* Save as draft */ },
                            enabled = !isLoading
                        ) {
                            Text("Save Draft")
                        }
                        
                        // Publish
                        Button(
                            onClick = {
                                isLoading = true
                                // TODO: Save snippet
                            },
                            enabled = !isLoading && title.isNotBlank() && codeEditorState.content.text.isNotBlank(),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text(if (snippetId == null) "Publish" else "Update")
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Basic Information Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Snippet Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        // Title
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Title") },
                            placeholder = { Text("Enter a descriptive title for your snippet") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Title, contentDescription = null)
                            }
                        )

                        // Description
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description (Optional)") },
                            placeholder = { Text("Describe what your snippet does...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5,
                            leadingIcon = {
                                Icon(Icons.Default.Description, contentDescription = null)
                            }
                        )

                        // Tags Section
                        Column {
                            Text(
                                text = "Tags",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Add Tag Input
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newTag,
                                    onValueChange = { newTag = it },
                                    placeholder = { Text("Add tag...") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                
                                IconButton(
                                    onClick = {
                                        if (newTag.isNotBlank() && newTag !in selectedTags) {
                                            selectedTags = selectedTags + newTag.trim()
                                            newTag = ""
                                        }
                                    },
                                    enabled = newTag.isNotBlank()
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add Tag")
                                }
                            }
                            
                            // Selected Tags
                            if (selectedTags.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(selectedTags.toList()) { tag ->
                                        InputChip(
                                            onClick = { },
                                            label = { Text(tag) },
                                            selected = true,
                                            trailingIcon = {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Remove",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            },
                                            modifier = Modifier.clickable {
                                                selectedTags = selectedTags - tag
                                            }
                                        )
                                    }
                                }
                            }
                            
                            // Suggested Tags
                            val suggestedTags = getSuggestedTags(codeEditorState.language)
                            if (suggestedTags.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Suggested:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(suggestedTags.filter { it !in selectedTags }) { tag ->
                                        SuggestionChip(
                                            onClick = {
                                                selectedTags = selectedTags + tag
                                            },
                                            label = { Text(tag) }
                                        )
                                    }
                                }
                            }
                        }

                        // Visibility Settings
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Public Snippet",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (isPublic) "Anyone can view and fork" else "Only you can view",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Switch(
                                checked = isPublic,
                                onCheckedChange = { isPublic = it }
                            )
                        }
                    }
                }

                // Code Editor Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(600.dp)
                ) {
                    Column {
                        // Editor Header
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Code,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Code Editor",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        
                        // Code Editor
                        CodeEditor(
                            state = codeEditorState,
                            onStateChange = { codeEditorState = it },
                            onContentChange = { /* Handle content change */ },
                            placeholder = "Write your code here...\n\n// Tip: Use the AI assistant for suggestions!",
                            onAiSuggestion = { code ->
                                // Handle AI suggestions
                            },
                            onFormatCode = {
                                // Format the code
                            },
                            onRunCode = {
                                // Run the code (if supported)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Preview Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Preview,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text = "Preview",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Snippet Preview (simplified)
                        SnippetPreview(
                            title = title.ifBlank { "Untitled Snippet" },
                            description = description,
                            language = codeEditorState.language,
                            tags = selectedTags.toList(),
                            isPublic = isPublic,
                            codePreview = codeEditorState.content.text.take(200)
                        )
                    }
                }

                // Bottom spacing for FAB
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun SnippetPreview(
    title: String,
    description: String,
    language: String,
    tags: List<String>,
    isPublic: Boolean,
    codePreview: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Language Badge
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = language.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    
                    // Visibility Icon
                    Icon(
                        if (isPublic) Icons.Default.Public else Icons.Default.Lock,
                        contentDescription = if (isPublic) "Public" else "Private",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Description
            if (description.isNotBlank()) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Code Preview
            if (codePreview.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = codePreview + if (codePreview.length >= 200) "..." else "",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        ),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            
            // Tags
            if (tags.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(tags) { tag ->
                        AssistChip(
                            onClick = { },
                            label = {
                                Text(
                                    text = "#$tag",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun getSuggestedTags(language: String): List<String> {
    return when (language.lowercase()) {
        "kotlin" -> listOf("android", "multiplatform", "coroutines", "jetpack-compose")
        "java" -> listOf("spring", "maven", "gradle", "junit")
        "javascript" -> listOf("react", "node", "npm", "frontend")
        "typescript" -> listOf("angular", "react", "node", "types")
        "python" -> listOf("django", "flask", "pandas", "machine-learning")
        "go" -> listOf("backend", "microservices", "api", "performance")
        "rust" -> listOf("systems", "performance", "memory-safe", "cargo")
        "swift" -> listOf("ios", "macos", "swiftui", "mobile")
        "dart" -> listOf("flutter", "mobile", "cross-platform", "ui")
        else -> listOf("algorithm", "utility", "helper", "example")
    }
}