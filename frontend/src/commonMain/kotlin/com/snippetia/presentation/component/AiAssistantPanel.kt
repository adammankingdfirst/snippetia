package com.snippetia.presentation.component

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * AI Assistant Panel with MCP server integration and agentic coding capabilities
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantPanel(
    code: String,
    language: String,
    onSuggestion: (AiSuggestion) -> Unit,
    onAnalyze: () -> Unit,
    isAnalyzing: Boolean,
    modifier: Modifier = Modifier
) {
    var chatMessages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var currentMessage by remember { mutableStateOf(TextFieldValue("")) }
    var selectedTab by remember { mutableStateOf(AiTab.CHAT) }
    var mcpServers by remember { mutableStateOf<List<McpServer>>(emptyList()) }
    var activeAgents by remember { mutableStateOf<List<CodingAgent>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    
    // Initialize MCP servers
    LaunchedEffect(Unit) {
        mcpServers = initializeMcpServers()
        activeAgents = initializeCodingAgents()
    }
    
    Card(
        modifier = modifier
            .fillMaxHeight()
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // AI Panel Header
            AiPanelHeader(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                isAnalyzing = isAnalyzing,
                onAnalyze = onAnalyze
            )
            
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            
            // Tab Content
            when (selectedTab) {
                AiTab.CHAT -> {
                    AiChatTab(
                        messages = chatMessages,
                        currentMessage = currentMessage,
                        onMessageChange = { currentMessage = it },
                        onSendMessage = { message ->
                            chatMessages = chatMessages + ChatMessage(
                                content = message,
                                isUser = true,
                                timestamp = System.currentTimeMillis()
                            )
                            currentMessage = TextFieldValue("")
                            
                            // Process AI response
                            coroutineScope.launch {
                                delay(1000) // Simulate AI processing
                                val aiResponse = processAiQuery(message, code, language, mcpServers)
                                chatMessages = chatMessages + aiResponse
                                
                                // Generate suggestions if applicable
                                if (aiResponse.suggestions.isNotEmpty()) {
                                    aiResponse.suggestions.forEach { onSuggestion(it) }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                AiTab.AGENTS -> {
                    AgentsTab(
                        agents = activeAgents,
                        onAgentToggle = { agent, enabled ->
                            activeAgents = activeAgents.map {
                                if (it.id == agent.id) it.copy(isActive = enabled) else it
                            }
                        },
                        onAgentConfigure = { agent ->
                            // Handle agent configuration
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                AiTab.MCP -> {
                    McpTab(
                        servers = mcpServers,
                        onServerToggle = { server, enabled ->
                            mcpServers = mcpServers.map {
                                if (it.id == server.id) it.copy(isConnected = enabled) else it
                            }
                        },
                        onServerConfigure = { server ->
                            // Handle MCP server configuration
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                AiTab.SUGGESTIONS -> {
                    SuggestionsTab(
                        code = code,
                        language = language,
                        onSuggestion = onSuggestion,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AiPanelHeader(
    selectedTab: AiTab,
    onTabSelected: (AiTab) -> Unit,
    isAnalyzing: Boolean,
    onAnalyze: () -> Unit
) {
    Column {
        // Title and Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Psychology,
                    contentDescription = "AI Assistant",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "AI Assistant",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            IconButton(
                onClick = onAnalyze,
                enabled = !isAnalyzing
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Default.Analytics,
                        contentDescription = "Analyze Code"
                    )
                }
            }
        }
        
        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            modifier = Modifier.fillMaxWidth(),
            edgePadding = 16.dp
        ) {
            AiTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { onTabSelected(tab) },
                    text = {
                        Text(
                            text = tab.displayName,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.displayName,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun AiChatTab(
    messages: List<ChatMessage>,
    currentMessage: TextFieldValue,
    onMessageChange: (TextFieldValue) -> Unit,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Chat Messages
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            reverseLayout = true
        ) {
            items(messages.reversed()) { message ->
                ChatMessageItem(message = message)
            }
        }
        
        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        
        // Message Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = currentMessage,
                onValueChange = onMessageChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask AI about your code...") },
                maxLines = 3,
                textStyle = MaterialTheme.typography.bodyMedium
            )
            
            IconButton(
                onClick = {
                    if (currentMessage.text.isNotBlank()) {
                        onSendMessage(currentMessage.text)
                    }
                },
                enabled = currentMessage.text.isNotBlank()
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send Message",
                    tint = if (currentMessage.text.isNotBlank()) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    }
                )
            }
        }
    }
}

@Composable
private fun ChatMessageItem(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 16.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (message.isUser) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                
                if (message.codeSnippet.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CodeSnippetDisplay(
                        code = message.codeSnippet,
                        language = message.language
                    )
                }
                
                if (message.suggestions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    message.suggestions.forEach { suggestion ->
                        SuggestionChip(suggestion = suggestion)
                    }
                }
            }
        }
    }
}

@Composable
private fun AgentsTab(
    agents: List<CodingAgent>,
    onAgentToggle: (CodingAgent, Boolean) -> Unit,
    onAgentConfigure: (CodingAgent) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Coding Agents",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "AI agents that can help with specific coding tasks",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        items(agents) { agent ->
            AgentCard(
                agent = agent,
                onToggle = { enabled -> onAgentToggle(agent, enabled) },
                onConfigure = { onAgentConfigure(agent) }
            )
        }
    }
}

@Composable
private fun AgentCard(
    agent: CodingAgent,
    onToggle: (Boolean) -> Unit,
    onConfigure: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = agent.icon,
                        contentDescription = agent.name,
                        tint = agent.color,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Column {
                        Text(
                            text = agent.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = agent.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Switch(
                    checked = agent.isActive,
                    onCheckedChange = onToggle
                )
            }
            
            if (agent.isActive) {
                Spacer(modifier = Modifier.height(12.dp))
                
                // Agent Status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = when (agent.status) {
                                    AgentStatus.IDLE -> Color.Gray
                                    AgentStatus.WORKING -> Color.Orange
                                    AgentStatus.COMPLETED -> Color.Green
                                    AgentStatus.ERROR -> Color.Red
                                },
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    )
                    
                    Text(
                        text = agent.status.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    TextButton(onClick = onConfigure) {
                        Text("Configure")
                    }
                }
                
                // Agent Capabilities
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(agent.capabilities) { capability ->
                        AssistChip(
                            onClick = { },
                            label = {
                                Text(
                                    text = capability,
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

@Composable
private fun McpTab(
    servers: List<McpServer>,
    onServerToggle: (McpServer, Boolean) -> Unit,
    onServerConfigure: (McpServer) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "MCP Servers",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Model Context Protocol servers for enhanced AI capabilities",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        items(servers) { server ->
            McpServerCard(
                server = server,
                onToggle = { connected -> onServerToggle(server, connected) },
                onConfigure = { onServerConfigure(server) }
            )
        }
    }
}

@Composable
private fun McpServerCard(
    server: McpServer,
    onToggle: (Boolean) -> Unit,
    onConfigure: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = server.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = server.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Endpoint: ${server.endpoint}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontFamily = FontFamily.Monospace
                    )
                }
                
                Switch(
                    checked = server.isConnected,
                    onCheckedChange = onToggle
                )
            }
            
            if (server.isConnected) {
                Spacer(modifier = Modifier.height(12.dp))
                
                // Connection Status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (server.isHealthy) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = "Status",
                        tint = if (server.isHealthy) Color.Green else Color.Red,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Text(
                        text = if (server.isHealthy) "Connected" else "Connection Error",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Text(
                        text = "Latency: ${server.latencyMs}ms",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    TextButton(onClick = onConfigure) {
                        Text("Configure")
                    }
                }
                
                // Available Tools
                if (server.availableTools.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Available Tools:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(server.availableTools) { tool ->
                            AssistChip(
                                onClick = { },
                                label = {
                                    Text(
                                        text = tool,
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
}

@Composable
private fun SuggestionsTab(
    code: String,
    language: String,
    onSuggestion: (AiSuggestion) -> Unit,
    modifier: Modifier = Modifier
) {
    var suggestions by remember { mutableStateOf<List<AiSuggestion>>(emptyList()) }
    var isGenerating by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(code, language) {
        if (code.isNotBlank()) {
            isGenerating = true
            delay(1000) // Simulate AI processing
            suggestions = generateCodeSuggestions(code, language)
            isGenerating = false
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Smart Suggestions",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            
            if (isGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (suggestions.isEmpty() && !isGenerating) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = "No suggestions",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No suggestions available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Start typing code to get AI suggestions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(suggestions) { suggestion ->
                    SuggestionCard(
                        suggestion = suggestion,
                        onApply = { onSuggestion(suggestion) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestionCard(
    suggestion: AiSuggestion,
    onApply: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = when (suggestion.type) {
                            AiSuggestionType.COMPLETION -> Icons.Default.AutoAwesome
                            AiSuggestionType.REFACTOR -> Icons.Default.Transform
                            AiSuggestionType.OPTIMIZATION -> Icons.Default.Speed
                            AiSuggestionType.BUG_FIX -> Icons.Default.BugReport
                            AiSuggestionType.DOCUMENTATION -> Icons.Default.Description
                            AiSuggestionType.STYLE_IMPROVEMENT -> Icons.Default.Palette
                        },
                        contentDescription = suggestion.type.name,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Column {
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
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Confidence indicator
                    LinearProgressIndicator(
                        progress = suggestion.confidence,
                        modifier = Modifier.width(40.dp),
                        color = when {
                            suggestion.confidence >= 0.8f -> Color.Green
                            suggestion.confidence >= 0.6f -> Color.Orange
                            else -> Color.Red
                        }
                    )
                    
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
            }
            
            if (suggestion.code.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                CodeSnippetDisplay(
                    code = suggestion.code,
                    language = "kotlin" // Default to kotlin for now
                )
            }
        }
    }
}

@Composable
private fun CodeSnippetDisplay(
    code: String,
    language: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        SelectionContainer {
            Text(
                text = code,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
private fun SuggestionChip(suggestion: AiSuggestion) {
    AssistChip(
        onClick = { },
        label = {
            Text(
                text = suggestion.title,
                style = MaterialTheme.typography.labelSmall
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
        },
        modifier = Modifier.height(24.dp)
    )
}

// Data classes and enums
enum class AiTab(val displayName: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    CHAT("Chat", Icons.Default.Chat),
    AGENTS("Agents", Icons.Default.SmartToy),
    MCP("MCP", Icons.Default.Hub),
    SUGGESTIONS("Suggestions", Icons.Default.Lightbulb)
}

data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long,
    val codeSnippet: String = "",
    val language: String = "",
    val suggestions: List<AiSuggestion> = emptyList()
)

data class CodingAgent(
    val id: String,
    val name: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color,
    val isActive: Boolean,
    val status: AgentStatus,
    val capabilities: List<String>
)

enum class AgentStatus(val displayName: String) {
    IDLE("Idle"),
    WORKING("Working"),
    COMPLETED("Completed"),
    ERROR("Error")
}

data class McpServer(
    val id: String,
    val name: String,
    val description: String,
    val endpoint: String,
    val isConnected: Boolean,
    val isHealthy: Boolean,
    val latencyMs: Int,
    val availableTools: List<String>
)

// Helper functions
private fun initializeMcpServers(): List<McpServer> {
    return listOf(
        McpServer(
            id = "github",
            name = "GitHub MCP",
            description = "Access GitHub repositories and issues",
            endpoint = "mcp://github.com/api",
            isConnected = true,
            isHealthy = true,
            latencyMs = 45,
            availableTools = listOf("search_repos", "create_issue", "get_commits")
        ),
        McpServer(
            id = "stackoverflow",
            name = "Stack Overflow MCP",
            description = "Search Stack Overflow for solutions",
            endpoint = "mcp://stackoverflow.com/api",
            isConnected = false,
            isHealthy = false,
            latencyMs = 0,
            availableTools = listOf("search_questions", "get_answers")
        ),
        McpServer(
            id = "docs",
            name = "Documentation MCP",
            description = "Access language and framework documentation",
            endpoint = "mcp://docs.snippetia.com/api",
            isConnected = true,
            isHealthy = true,
            latencyMs = 23,
            availableTools = listOf("search_docs", "get_examples", "get_api_reference")
        )
    )
}

private fun initializeCodingAgents(): List<CodingAgent> {
    return listOf(
        CodingAgent(
            id = "refactor",
            name = "Refactor Agent",
            description = "Automatically refactor code for better structure",
            icon = Icons.Default.Transform,
            color = Color(0xFF2196F3),
            isActive = true,
            status = AgentStatus.IDLE,
            capabilities = listOf("Extract Method", "Rename Variables", "Simplify Logic")
        ),
        CodingAgent(
            id = "test",
            name = "Test Agent",
            description = "Generate unit tests for your code",
            icon = Icons.Default.Quiz,
            color = Color(0xFF4CAF50),
            isActive = false,
            status = AgentStatus.IDLE,
            capabilities = listOf("Unit Tests", "Integration Tests", "Mock Generation")
        ),
        CodingAgent(
            id = "security",
            name = "Security Agent",
            description = "Scan for security vulnerabilities",
            icon = Icons.Default.Security,
            color = Color(0xFFFF5722),
            isActive = true,
            status = AgentStatus.WORKING,
            capabilities = listOf("Vulnerability Scan", "Code Analysis", "Best Practices")
        ),
        CodingAgent(
            id = "performance",
            name = "Performance Agent",
            description = "Optimize code for better performance",
            icon = Icons.Default.Speed,
            color = Color(0xFFFF9800),
            isActive = false,
            status = AgentStatus.IDLE,
            capabilities = listOf("Performance Analysis", "Memory Optimization", "Algorithm Improvement")
        )
    )
}

private suspend fun processAiQuery(
    query: String,
    code: String,
    language: String,
    mcpServers: List<McpServer>
): ChatMessage {
    // Simulate AI processing with MCP server integration
    delay(1500)
    
    val response = when {
        query.contains("refactor", ignoreCase = true) -> {
            "I can help you refactor this code. Here are some suggestions:\n\n" +
            "1. Extract the complex logic into separate methods\n" +
            "2. Use more descriptive variable names\n" +
            "3. Consider using design patterns for better structure"
        }
        query.contains("test", ignoreCase = true) -> {
            "I'll help you write tests for this code. Let me generate some unit tests based on the current implementation."
        }
        query.contains("optimize", ignoreCase = true) -> {
            "Here are some performance optimizations I found:\n\n" +
            "1. Use lazy initialization for expensive operations\n" +
            "2. Consider caching frequently accessed data\n" +
            "3. Optimize loops and data structures"
        }
        query.contains("bug", ignoreCase = true) || query.contains("error", ignoreCase = true) -> {
            "I've analyzed your code for potential bugs. Here's what I found:\n\n" +
            "1. Potential null pointer exception on line 15\n" +
            "2. Resource leak - remember to close streams\n" +
            "3. Race condition in concurrent code"
        }
        else -> {
            "I'm analyzing your $language code. How can I help you improve it? I can assist with:\n\n" +
            "• Code refactoring and optimization\n" +
            "• Bug detection and fixes\n" +
            "• Test generation\n" +
            "• Documentation\n" +
            "• Best practices"
        }
    }
    
    val suggestions = if (query.contains("refactor", ignoreCase = true)) {
        listOf(
            AiSuggestion(
                id = "refactor_1",
                type = AiSuggestionType.REFACTOR,
                title = "Extract Method",
                description = "Extract complex logic into separate method",
                code = "private fun extractedMethod() {\n    // Extracted logic here\n}",
                confidence = 0.85f
            )
        )
    } else {
        emptyList()
    }
    
    return ChatMessage(
        content = response,
        isUser = false,
        timestamp = System.currentTimeMillis(),
        suggestions = suggestions
    )
}

private fun generateCodeSuggestions(code: String, language: String): List<AiSuggestion> {
    val suggestions = mutableListOf<AiSuggestion>()
    
    // Analyze code and generate suggestions based on patterns
    if (code.contains("TODO", ignoreCase = true)) {
        suggestions.add(
            AiSuggestion(
                id = "todo_1",
                type = AiSuggestionType.COMPLETION,
                title = "Complete TODO",
                description = "Implement the TODO functionality",
                code = "// Implementation suggestion based on context",
                confidence = 0.7f
            )
        )
    }
    
    if (code.contains("fun ") && !code.contains("/**")) {
        suggestions.add(
            AiSuggestion(
                id = "doc_1",
                type = AiSuggestionType.DOCUMENTATION,
                title = "Add Documentation",
                description = "Add KDoc documentation to functions",
                code = "/**\n * Description of the function\n * @param parameter description\n * @return return value description\n */",
                confidence = 0.9f
            )
        )
    }
    
    if (code.contains("var ") && code.count { it == '\n' } > 20) {
        suggestions.add(
            AiSuggestion(
                id = "refactor_1",
                type = AiSuggestionType.REFACTOR,
                title = "Consider Immutability",
                description = "Replace 'var' with 'val' where possible",
                code = "// Use 'val' for immutable variables",
                confidence = 0.8f
            )
        )
    }
    
    return suggestions
}

private fun applySuggestion(currentContent: TextFieldValue, suggestion: AiSuggestion): TextFieldValue {
    // Simple implementation - in a real app, this would be more sophisticated
    val newText = if (suggestion.startIndex > 0 && suggestion.endIndex > suggestion.startIndex) {
        currentContent.text.substring(0, suggestion.startIndex) +
        suggestion.code +
        currentContent.text.substring(suggestion.endIndex)
    } else {
        currentContent.text + "\n\n" + suggestion.code
    }
    
    return TextFieldValue(
        text = newText,
        selection = androidx.compose.ui.text.TextRange(newText.length)
    )
}