package com.snippetia.presentation.component

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

data class CodeEditorState(
    val content: TextFieldValue = TextFieldValue(""),
    val language: String = "kotlin",
    val isReadOnly: Boolean = false,
    val showLineNumbers: Boolean = true,
    val fontSize: Int = 14,
    val tabSize: Int = 4,
    val wordWrap: Boolean = false,
    val autoComplete: Boolean = true,
    val syntaxHighlighting: Boolean = true,
    val aiAssistance: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeEditor(
    state: CodeEditorState,
    onStateChange: (CodeEditorState) -> Unit,
    onContentChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Start coding...",
    onAiSuggestion: ((String) -> Unit)? = null,
    onFormatCode: (() -> Unit)? = null,
    onRunCode: (() -> Unit)? = null
) {
    var showSettings by remember { mutableStateOf(false) }
    var showAiPanel by remember { mutableStateOf(false) }
    var aiSuggestions by remember { mutableStateOf<List<AiSuggestion>>(emptyList()) }
    var isAnalyzing by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(12.dp)
            )
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            )
    ) {
        // Toolbar
        CodeEditorToolbar(
            language = state.language,
            onLanguageChange = { newLang ->
                onStateChange(state.copy(language = newLang))
            },
            onSettingsClick = { showSettings = !showSettings },
            onAiClick = { showAiPanel = !showAiPanel },
            onFormatClick = onFormatCode,
            onRunClick = onRunCode,
            aiEnabled = state.aiAssistance
        )

        // Settings Panel
        AnimatedVisibility(
            visible = showSettings,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            CodeEditorSettings(
                state = state,
                onStateChange = onStateChange
            )
        }

        Row(modifier = Modifier.fillMaxSize()) {
            // Main Editor
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                CodeEditorContent(
                    state = state,
                    onContentChange = { newContent ->
                        onStateChange(state.copy(content = newContent))
                        onContentChange(newContent.text)
                        
                        // Trigger AI analysis if enabled
                        if (state.aiAssistance && newContent.text.isNotBlank()) {
                            // Debounced AI analysis would go here
                        }
                    },
                    placeholder = placeholder,
                    focusRequester = focusRequester
                )

                // AI Suggestions Overlay
                if (aiSuggestions.isNotEmpty()) {
                    AiSuggestionsOverlay(
                        suggestions = aiSuggestions,
                        onApplySuggestion = { suggestion ->
                            // Apply AI suggestion to code
                            val newContent = applySuggestion(state.content, suggestion)
                            onStateChange(state.copy(content = newContent))
                            onContentChange(newContent.text)
                            aiSuggestions = emptyList()
                        },
                        onDismiss = { aiSuggestions = emptyList() }
                    )
                }
            }

            // AI Assistant Panel
            AnimatedVisibility(
                visible = showAiPanel && state.aiAssistance,
                enter = slideInHorizontally { it } + fadeIn(),
                exit = slideOutHorizontally { it } + fadeOut()
            ) {
                AiAssistantPanel(
                    code = state.content.text,
                    language = state.language,
                    onSuggestion = { suggestion ->
                        aiSuggestions = listOf(suggestion)
                    },
                    onAnalyze = {
                        isAnalyzing = true
                        // Trigger code analysis
                        onAiSuggestion?.invoke(state.content.text)
                    },
                    isAnalyzing = isAnalyzing,
                    modifier = Modifier.width(300.dp)
                )
            }
        }
    }
}

@Composable
private fun CodeEditorToolbar(
    language: String,
    onLanguageChange: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onAiClick: () -> Unit,
    onFormatClick: (() -> Unit)?,
    onRunClick: (() -> Unit)?,
    aiEnabled: Boolean
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Language Selector
                LanguageSelector(
                    selectedLanguage = language,
                    onLanguageSelected = onLanguageChange
                )

                // Format Button
                if (onFormatClick != null) {
                    IconButton(onClick = onFormatClick) {
                        Icon(
                            Icons.Default.FormatAlignLeft,
                            contentDescription = "Format Code",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Run Button
                if (onRunClick != null) {
                    IconButton(onClick = onRunClick) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Run Code",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // AI Assistant Button
                if (aiEnabled) {
                    IconButton(onClick = onAiClick) {
                        Icon(
                            Icons.Default.Psychology,
                            contentDescription = "AI Assistant",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                // Settings Button
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Editor Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageSelector(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    val supportedLanguages = listOf(
        "kotlin", "java", "javascript", "typescript", "python", "go", "rust",
        "cpp", "c", "csharp", "php", "ruby", "swift", "scala", "dart",
        "html", "css", "scss", "json", "xml", "yaml", "markdown",
        "sql", "shell", "dockerfile", "gradle", "maven"
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedLanguage.uppercase(),
            onValueChange = { },
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .width(120.dp),
            textStyle = MaterialTheme.typography.labelMedium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            supportedLanguages.forEach { language ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LanguageIcon(language)
                            Text(language.uppercase())
                        }
                    },
                    onClick = {
                        onLanguageSelected(language)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun LanguageIcon(language: String) {
    val color = getLanguageColor(language)
    Box(
        modifier = Modifier
            .size(12.dp)
            .background(color, RoundedCornerShape(2.dp))
    )
}

@Composable
private fun CodeEditorSettings(
    state: CodeEditorState,
    onStateChange: (CodeEditorState) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Editor Settings",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Show Line Numbers")
                Switch(
                    checked = state.showLineNumbers,
                    onCheckedChange = { onStateChange(state.copy(showLineNumbers = it)) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Word Wrap")
                Switch(
                    checked = state.wordWrap,
                    onCheckedChange = { onStateChange(state.copy(wordWrap = it)) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Syntax Highlighting")
                Switch(
                    checked = state.syntaxHighlighting,
                    onCheckedChange = { onStateChange(state.copy(syntaxHighlighting = it)) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("AI Assistance")
                Switch(
                    checked = state.aiAssistance,
                    onCheckedChange = { onStateChange(state.copy(aiAssistance = it)) }
                )
            }

            // Font Size Slider
            Column {
                Text("Font Size: ${state.fontSize}px")
                Slider(
                    value = state.fontSize.toFloat(),
                    onValueChange = { onStateChange(state.copy(fontSize = it.toInt())) },
                    valueRange = 10f..24f,
                    steps = 13
                )
            }

            // Tab Size Slider
            Column {
                Text("Tab Size: ${state.tabSize}")
                Slider(
                    value = state.tabSize.toFloat(),
                    onValueChange = { onStateChange(state.copy(tabSize = it.toInt())) },
                    valueRange = 2f..8f,
                    steps = 5
                )
            }
        }
    }
}

@Composable
private fun CodeEditorContent(
    state: CodeEditorState,
    onContentChange: (TextFieldValue) -> Unit,
    placeholder: String,
    focusRequester: FocusRequester
) {
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        // Line Numbers
        if (state.showLineNumbers) {
            LineNumbers(
                content = state.content.text,
                fontSize = state.fontSize
            )
        }

        // Code Input
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(8.dp)
        ) {
            if (state.syntaxHighlighting) {
                SyntaxHighlightedEditor(
                    state = state,
                    onContentChange = onContentChange,
                    placeholder = placeholder,
                    focusRequester = focusRequester
                )
            } else {
                PlainTextEditor(
                    state = state,
                    onContentChange = onContentChange,
                    placeholder = placeholder,
                    focusRequester = focusRequester
                )
            }
        }
    }
}

@Composable
private fun LineNumbers(
    content: String,
    fontSize: Int
) {
    val lineCount = content.count { it == '\n' } + 1
    
    LazyColumn(
        modifier = Modifier
            .width(48.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        items(lineCount) { index ->
            Text(
                text = "${index + 1}",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = fontSize.sp,
                    fontFamily = FontFamily.Monospace
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SyntaxHighlightedEditor(
    state: CodeEditorState,
    onContentChange: (TextFieldValue) -> Unit,
    placeholder: String,
    focusRequester: FocusRequester
) {
    val highlightedText = remember(state.content.text, state.language) {
        applySyntaxHighlighting(state.content.text, state.language)
    }

    BasicTextField(
        value = state.content,
        onValueChange = onContentChange,
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester),
        textStyle = TextStyle(
            fontSize = state.fontSize.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = (state.fontSize * 1.4).sp
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        readOnly = state.isReadOnly,
        decorationBox = { innerTextField ->
            Box {
                if (state.content.text.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = TextStyle(
                            fontSize = state.fontSize.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun PlainTextEditor(
    state: CodeEditorState,
    onContentChange: (TextFieldValue) -> Unit,
    placeholder: String,
    focusRequester: FocusRequester
) {
    BasicTextField(
        value = state.content,
        onValueChange = onContentChange,
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester),
        textStyle = TextStyle(
            fontSize = state.fontSize.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = (state.fontSize * 1.4).sp
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        readOnly = state.isReadOnly,
        decorationBox = { innerTextField ->
            Box {
                if (state.content.text.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = TextStyle(
                            fontSize = state.fontSize.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                }
                innerTextField()
            }
        }
    )
}

// Helper functions for syntax highlighting
private fun applySyntaxHighlighting(code: String, language: String): AnnotatedString {
    return buildAnnotatedString {
        append(code)
        
        // Apply syntax highlighting based on language
        when (language.lowercase()) {
            "kotlin" -> applyKotlinHighlighting(this, code)
            "java" -> applyJavaHighlighting(this, code)
            "javascript", "typescript" -> applyJavaScriptHighlighting(this, code)
            "python" -> applyPythonHighlighting(this, code)
            // Add more languages as needed
            else -> applyGenericHighlighting(this, code)
        }
    }
}

private fun applyKotlinHighlighting(builder: AnnotatedString.Builder, code: String) {
    val keywords = listOf(
        "class", "fun", "val", "var", "if", "else", "when", "for", "while",
        "return", "import", "package", "private", "public", "internal", "protected",
        "override", "open", "abstract", "final", "companion", "object", "data",
        "sealed", "enum", "interface", "suspend", "inline", "reified"
    )
    
    // Apply keyword highlighting
    keywords.forEach { keyword ->
        highlightKeyword(builder, code, keyword, Color(0xFF9C27B0)) // Purple
    }
    
    // Highlight strings
    highlightStrings(builder, code, Color(0xFF4CAF50)) // Green
    
    // Highlight comments
    highlightComments(builder, code, Color(0xFF757575)) // Gray
    
    // Highlight numbers
    highlightNumbers(builder, code, Color(0xFF2196F3)) // Blue
}

private fun applyJavaHighlighting(builder: AnnotatedString.Builder, code: String) {
    val keywords = listOf(
        "class", "public", "private", "protected", "static", "final", "abstract",
        "interface", "extends", "implements", "import", "package", "if", "else",
        "for", "while", "do", "switch", "case", "default", "return", "void",
        "int", "String", "boolean", "double", "float", "long", "char", "byte"
    )
    
    keywords.forEach { keyword ->
        highlightKeyword(builder, code, keyword, Color(0xFF9C27B0))
    }
    
    highlightStrings(builder, code, Color(0xFF4CAF50))
    highlightComments(builder, code, Color(0xFF757575))
    highlightNumbers(builder, code, Color(0xFF2196F3))
}

private fun applyJavaScriptHighlighting(builder: AnnotatedString.Builder, code: String) {
    val keywords = listOf(
        "function", "var", "let", "const", "if", "else", "for", "while", "do",
        "switch", "case", "default", "return", "class", "extends", "import",
        "export", "from", "async", "await", "try", "catch", "finally", "throw"
    )
    
    keywords.forEach { keyword ->
        highlightKeyword(builder, code, keyword, Color(0xFF9C27B0))
    }
    
    highlightStrings(builder, code, Color(0xFF4CAF50))
    highlightComments(builder, code, Color(0xFF757575))
    highlightNumbers(builder, code, Color(0xFF2196F3))
}

private fun applyPythonHighlighting(builder: AnnotatedString.Builder, code: String) {
    val keywords = listOf(
        "def", "class", "if", "elif", "else", "for", "while", "in", "not",
        "and", "or", "return", "import", "from", "as", "try", "except",
        "finally", "with", "lambda", "yield", "global", "nonlocal", "pass",
        "break", "continue", "True", "False", "None"
    )
    
    keywords.forEach { keyword ->
        highlightKeyword(builder, code, keyword, Color(0xFF9C27B0))
    }
    
    highlightStrings(builder, code, Color(0xFF4CAF50))
    highlightComments(builder, code, Color(0xFF757575))
    highlightNumbers(builder, code, Color(0xFF2196F3))
}

private fun applyGenericHighlighting(builder: AnnotatedString.Builder, code: String) {
    // Basic highlighting for unknown languages
    highlightStrings(builder, code, Color(0xFF4CAF50))
    highlightComments(builder, code, Color(0xFF757575))
    highlightNumbers(builder, code, Color(0xFF2196F3))
}

private fun highlightKeyword(builder: AnnotatedString.Builder, code: String, keyword: String, color: Color) {
    val regex = "\\b$keyword\\b".toRegex()
    regex.findAll(code).forEach { match ->
        builder.addStyle(
            SpanStyle(color = color, fontWeight = FontWeight.Bold),
            match.range.first,
            match.range.last + 1
        )
    }
}

private fun highlightStrings(builder: AnnotatedString.Builder, code: String, color: Color) {
    val stringRegex = "\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'".toRegex()
    stringRegex.findAll(code).forEach { match ->
        builder.addStyle(
            SpanStyle(color = color),
            match.range.first,
            match.range.last + 1
        )
    }
}

private fun highlightComments(builder: AnnotatedString.Builder, code: String, color: Color) {
    // Single line comments
    val singleLineRegex = "//.*$".toRegex(RegexOption.MULTILINE)
    singleLineRegex.findAll(code).forEach { match ->
        builder.addStyle(
            SpanStyle(color = color),
            match.range.first,
            match.range.last + 1
        )
    }
    
    // Multi-line comments
    val multiLineRegex = "/\\*[\\s\\S]*?\\*/".toRegex()
    multiLineRegex.findAll(code).forEach { match ->
        builder.addStyle(
            SpanStyle(color = color),
            match.range.first,
            match.range.last + 1
        )
    }
}

private fun highlightNumbers(builder: AnnotatedString.Builder, code: String, color: Color) {
    val numberRegex = "\\b\\d+(\\.\\d+)?\\b".toRegex()
    numberRegex.findAll(code).forEach { match ->
        builder.addStyle(
            SpanStyle(color = color),
            match.range.first,
            match.range.last + 1
        )
    }
}

// Helper function to get language-specific colors
private fun getLanguageColor(language: String): Color {
    return when (language.lowercase()) {
        "kotlin" -> Color(0xFF7F52FF)
        "java" -> Color(0xFFED8B00)
        "javascript" -> Color(0xFFF7DF1E)
        "typescript" -> Color(0xFF3178C6)
        "python" -> Color(0xFF3776AB)
        "go" -> Color(0xFF00ADD8)
        "rust" -> Color(0xFFCE422B)
        "cpp", "c" -> Color(0xFF00599C)
        "csharp" -> Color(0xFF239120)
        "php" -> Color(0xFF777BB4)
        "ruby" -> Color(0xFFCC342D)
        "swift" -> Color(0xFFFA7343)
        "scala" -> Color(0xFFDC322F)
        "dart" -> Color(0xFF0175C2)
        else -> Color(0xFF6B7280)
    }
}

// AI-related data classes and components
data class AiSuggestion(
    val id: String,
    val type: AiSuggestionType,
    val title: String,
    val description: String,
    val code: String,
    val confidence: Float,
    val startIndex: Int = 0,
    val endIndex: Int = 0
)

enum class AiSuggestionType {
    COMPLETION, REFACTOR, OPTIMIZATION, BUG_FIX, DOCUMENTATION, STYLE_IMPROVEMENT
}

@Composable
private fun AiSuggestionsOverlay(
    suggestions: List<AiSuggestion>,
    onApplySuggestion: (AiSuggestion) -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI Suggestions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
            
            suggestions.forEach { suggestion ->
                AiSuggestionItem(
                    suggestion = suggestion,
                    onApply = { onApplySuggestion(suggestion) }
                )
            }
        }
    }
}

@Composable
private fun AiSuggestionItem(
    suggestion: AiSuggestion,
    onApply: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = suggestion.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = suggestion.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Button(
                    onClick = onApply,
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = "Apply",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            
            if (suggestion.code.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                SelectionContainer {
                    Text(
                        text = suggestion.code,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surface,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AiAssistantPanel(
    code: String,
    language: String,
    onSuggestion: (AiSuggestion) -> Unit,
    onAnalyze: () -> Unit,
    isAnalyzing: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxHeight(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "AI Assistant",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Quick Actions
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    AiActionButton(
                        icon = Icons.Default.Psychology,
                        title = "Analyze Code",
                        description = "Get insights about your code",
                        onClick = onAnalyze,
                        isLoading = isAnalyzing
                    )
                }
                
                item {
                    AiActionButton(
                        icon = Icons.Default.AutoFixHigh,
                        title = "Suggest Improvements",
                        description = "Get optimization suggestions",
                        onClick = {
                            // Generate improvement suggestions
                            onSuggestion(
                                AiSuggestion(
                                    id = "improve_1",
                                    type = AiSuggestionType.OPTIMIZATION,
                                    title = "Performance Optimization",
                                    description = "Use more efficient data structures",
                                    code = "// Optimized code here",
                                    confidence = 0.85f
                                )
                            )
                        }
                    )
                }
                
                item {
                    AiActionButton(
                        icon = Icons.Default.BugReport,
                        title = "Find Issues",
                        description = "Detect potential bugs",
                        onClick = {
                            // Generate bug fix suggestions
                            onSuggestion(
                                AiSuggestion(
                                    id = "bug_1",
                                    type = AiSuggestionType.BUG_FIX,
                                    title = "Null Safety Issue",
                                    description = "Add null checks to prevent crashes",
                                    code = "// Safe code here",
                                    confidence = 0.92f
                                )
                            )
                        }
                    )
                }
                
                item {
                    AiActionButton(
                        icon = Icons.Default.Description,
                        title = "Generate Docs",
                        description = "Create documentation",
                        onClick = {
                            // Generate documentation
                            onSuggestion(
                                AiSuggestion(
                                    id = "docs_1",
                                    type = AiSuggestionType.DOCUMENTATION,
                                    title = "Function Documentation",
                                    description = "Add comprehensive documentation",
                                    code = "/**\n * Function description\n */",
                                    confidence = 0.78f
                                )
                            )
                        }
                    )
                }
                
                item {
                    AiActionButton(
                        icon = Icons.Default.Palette,
                        title = "Code Style",
                        description = "Improve code formatting",
                        onClick = {
                            // Generate style improvements
                            onSuggestion(
                                AiSuggestion(
                                    id = "style_1",
                                    type = AiSuggestionType.STYLE_IMPROVEMENT,
                                    title = "Formatting Improvement",
                                    description = "Apply consistent code style",
                                    code = "// Formatted code here",
                                    confidence = 0.95f
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AiActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    isLoading: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Helper function to apply AI suggestions to code
private fun applySuggestion(currentContent: TextFieldValue, suggestion: AiSuggestion): TextFieldValue {
    val newText = when (suggestion.type) {
        AiSuggestionType.COMPLETION -> {
            // Insert completion at cursor position
            val cursorPos = currentContent.selection.start
            currentContent.text.substring(0, cursorPos) + 
            suggestion.code + 
            currentContent.text.substring(cursorPos)
        }
        AiSuggestionType.REFACTOR, AiSuggestionType.OPTIMIZATION, AiSuggestionType.BUG_FIX -> {
            // Replace selected text or entire content
            if (suggestion.startIndex > 0 && suggestion.endIndex > suggestion.startIndex) {
                currentContent.text.substring(0, suggestion.startIndex) +
                suggestion.code +
                currentContent.text.substring(suggestion.endIndex)
            } else {
                suggestion.code
            }
        }
        AiSuggestionType.DOCUMENTATION -> {
            // Insert documentation at the beginning or before functions
            suggestion.code + "\n" + currentContent.text
        }
        AiSuggestionType.STYLE_IMPROVEMENT -> {
            // Replace with formatted version
            suggestion.code
        }
    }
    
    return currentContent.copy(text = newText)
}