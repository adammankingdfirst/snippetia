package com.snippetia.presentation.component

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Advanced Agentic Debugging Component with AI-powered code analysis
 */

@Composable
fun AgenticDebuggingPanel(
    code: String,
    language: String,
    onFixSuggestion: (DebugFix) -> Unit,
    onExplainCode: () -> Unit,
    onOptimizeCode: () -> Unit,
    modifier: Modifier = Modifier
) {
    var debugState by remember { mutableStateOf(DebugState.IDLE) }
    var debugResults by remember { mutableStateOf<List<DebugResult>>(emptyList()) }
    var selectedAgent by remember { mutableStateOf<DebugAgent?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    // Available debugging agents
    val debugAgents = remember {
        listOf(
            DebugAgent(
                id = "syntax_analyzer",
                name = "Syntax Analyzer",
                description = "Detects syntax errors and formatting issues",
                icon = Icons.Default.Code,
                color = Color(0xFF2196F3),
                capabilities = listOf("Syntax Validation", "Code Formatting", "Style Checking")
            ),
            DebugAgent(
                id = "logic_analyzer",
                name = "Logic Analyzer",
                description = "Identifies logical errors and potential bugs",
                icon = Icons.Default.Psychology,
                color = Color(0xFF4CAF50),
                capabilities = listOf("Logic Validation", "Bug Detection", "Flow Analysis")
            ),
            DebugAgent(
                id = "performance_analyzer",
                name = "Performance Analyzer",
                description = "Suggests performance optimizations",
                icon = Icons.Default.Speed,
                color = Color(0xFFFF9800),
                capabilities = listOf("Performance Analysis", "Optimization", "Complexity Analysis")
            ),
            DebugAgent(
                id = "security_analyzer",
                name = "Security Analyzer",
                description = "Scans for security vulnerabilities",
                icon = Icons.Default.Security,
                color = Color(0xFFF44336),
                capabilities = listOf("Vulnerability Scan", "Security Best Practices", "Risk Assessment")
            ),
            DebugAgent(
                id = "ai_explainer",
                name = "AI Code Explainer",
                description = "Provides detailed code explanations",
                icon = Icons.Default.AutoAwesome,
                color = Color(0xFF9C27B0),
                capabilities = listOf("Code Explanation", "Documentation", "Learning Assistance")
            )
        )
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header
            AgenticDebugHeader(
                isAnalyzing = isAnalyzing,
                onStartAnalysis = {
                    isAnalyzing = true
                    debugState = DebugState.ANALYZING
                    
                    coroutineScope.launch {
                        // Simulate AI analysis
                        delay(2000)
                        
                        debugResults = generateDebugResults(code, language)
                        debugState = DebugState.RESULTS_READY
                        isAnalyzing = false
                    }
                },
                onExplainCode = onExplainCode,
                onOptimizeCode = onOptimizeCode
            )
            
            HorizontalDivider()
            
            // Debug Agents Selection
            if (debugState == DebugState.IDLE || debugState == DebugState.ANALYZING) {
                DebugAgentsSection(
                    agents = debugAgents,
                    selectedAgent = selectedAgent,
                    onAgentSelected = { selectedAgent = it },
                    isAnalyzing = isAnalyzing
                )
            }
            
            // Analysis Results
            if (debugState == DebugState.RESULTS_READY && debugResults.isNotEmpty()) {
                HorizontalDivider()
                
                DebugResultsSection(
                    results = debugResults,
                    onFixSuggestion = onFixSuggestion,
                    onApplyFix = { fix ->
                        // Apply the suggested fix
                        onFixSuggestion(fix)
                    }
                )
            }
            
            // Real-time Code Analysis
            if (code.isNotBlank()) {
                HorizontalDivider()
                
                RealTimeAnalysisSection(
                    code = code,
                    language = language
                )
            }
        }
    }
}

@Composable
private fun AgenticDebugHeader(
    isAnalyzing: Boolean,
    onStartAnalysis: () -> Unit,
    onExplainCode: () -> Unit,
    onOptimizeCode: () -> Unit
) {
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
                Icons.Default.BugReport,
                contentDescription = "Debug",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Text(
                text = "AI Debugging Assistant",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onExplainCode) {
                Icon(
                    Icons.Default.Help,
                    contentDescription = "Explain Code"
                )
            }
            
            IconButton(onClick = onOptimizeCode) {
                Icon(
                    Icons.Default.TrendingUp,
                    contentDescription = "Optimize Code"
                )
            }
            
            Button(
                onClick = onStartAnalysis,
                enabled = !isAnalyzing
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analyzing...")
                } else {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Start Analysis",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analyze Code")
                }
            }
        }
    }
}

@Composable
private fun DebugAgentsSection(
    agents: List<DebugAgent>,
    selectedAgent: DebugAgent?,
    onAgentSelected: (DebugAgent) -> Unit,
    isAnalyzing: Boolean
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "AI Debug Agents",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        
        Text(
            text = "Select specialized AI agents to analyze your code",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(agents) { agent ->
                DebugAgentCard(
                    agent = agent,
                    isSelected = selectedAgent == agent,
                    isAnalyzing = isAnalyzing,
                    onSelect = { onAgentSelected(agent) }
                )
            }
        }
    }
}

@Composable
private fun DebugAgentCard(
    agent: DebugAgent,
    isSelected: Boolean,
    isAnalyzing: Boolean,
    onSelect: () -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    Card(
        modifier = Modifier
            .width(200.dp)
            .scale(animatedScale)
            .clickable(enabled = !isAnalyzing) { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                agent.color.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, agent.color)
        } else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = agent.color.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = agent.icon,
                    contentDescription = agent.name,
                    tint = agent.color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = agent.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = agent.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Capabilities
            agent.capabilities.take(2).forEach { capability ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = agent.color
                    )
                    Text(
                        text = capability,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// Continue with remaining components...@Compo
sable
private fun DebugResultsSection(
    results: List<DebugResult>,
    onFixSuggestion: (DebugFix) -> Unit,
    onApplyFix: (DebugFix) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Analysis Results",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.heightIn(max = 400.dp)
        ) {
            items(results) { result ->
                DebugResultCard(
                    result = result,
                    onFixSuggestion = onFixSuggestion,
                    onApplyFix = onApplyFix
                )
            }
        }
    }
}

@Composable
private fun DebugResultCard(
    result: DebugResult,
    onFixSuggestion: (DebugFix) -> Unit,
    onApplyFix: (DebugFix) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = when (result.severity) {
                DebugSeverity.ERROR -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                DebugSeverity.WARNING -> Color(0xFFFF9800).copy(alpha = 0.2f)
                DebugSeverity.INFO -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                DebugSeverity.SUGGESTION -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            }
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
                        imageVector = when (result.severity) {
                            DebugSeverity.ERROR -> Icons.Default.Error
                            DebugSeverity.WARNING -> Icons.Default.Warning
                            DebugSeverity.INFO -> Icons.Default.Info
                            DebugSeverity.SUGGESTION -> Icons.Default.Lightbulb
                        },
                        contentDescription = result.severity.name,
                        tint = when (result.severity) {
                            DebugSeverity.ERROR -> MaterialTheme.colorScheme.error
                            DebugSeverity.WARNING -> Color(0xFFFF9800)
                            DebugSeverity.INFO -> MaterialTheme.colorScheme.primary
                            DebugSeverity.SUGGESTION -> MaterialTheme.colorScheme.tertiary
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Text(
                        text = result.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = result.category,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = result.description,
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (result.codeLocation != null) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Line ${result.codeLocation.line}:${result.codeLocation.column}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = result.codeLocation.code,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(8.dp)
                        )
                    }
                }
            }
            
            if (result.suggestedFix != null) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onFixSuggestion(result.suggestedFix) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = "View Fix",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View Fix")
                    }
                    
                    Button(
                        onClick = { onApplyFix(result.suggestedFix) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.AutoFixHigh,
                            contentDescription = "Apply Fix",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Apply Fix")
                    }
                }
            }
        }
    }
}

@Composable
private fun RealTimeAnalysisSection(
    code: String,
    language: String
) {
    var realTimeIssues by remember { mutableStateOf<List<RealTimeIssue>>(emptyList()) }
    
    LaunchedEffect(code) {
        // Simulate real-time analysis
        delay(500)
        realTimeIssues = analyzeCodeRealTime(code, language)
    }
    
    if (realTimeIssues.isNotEmpty()) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Real-time Analysis",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(realTimeIssues) { issue ->
                    RealTimeIssueChip(issue = issue)
                }
            }
        }
    }
}

@Composable
private fun RealTimeIssueChip(issue: RealTimeIssue) {
    AssistChip(
        onClick = { },
        label = {
            Text(
                text = "${issue.type}: ${issue.count}",
                style = MaterialTheme.typography.labelSmall
            )
        },
        leadingIcon = {
            Icon(
                imageVector = when (issue.type) {
                    "Syntax" -> Icons.Default.Code
                    "Logic" -> Icons.Default.Psychology
                    "Performance" -> Icons.Default.Speed
                    "Security" -> Icons.Default.Security
                    else -> Icons.Default.Info
                },
                contentDescription = issue.type,
                modifier = Modifier.size(16.dp)
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = when (issue.severity) {
                DebugSeverity.ERROR -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                DebugSeverity.WARNING -> Color(0xFFFF9800).copy(alpha = 0.2f)
                else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            }
        )
    )
}

// Helper functions
private fun generateDebugResults(code: String, language: String): List<DebugResult> {
    // Simulate AI analysis results
    return listOf(
        DebugResult(
            id = "1",
            title = "Potential Null Pointer Exception",
            description = "Variable 'user' might be null when accessing properties. Consider adding null checks.",
            severity = DebugSeverity.ERROR,
            category = "Logic",
            codeLocation = CodeLocation(
                line = 15,
                column = 8,
                code = "user.name.length"
            ),
            suggestedFix = DebugFix(
                id = "fix_1",
                title = "Add Null Safety Check",
                description = "Add null safety operator or explicit null check",
                originalCode = "user.name.length",
                fixedCode = "user?.name?.length ?: 0",
                confidence = 0.95f
            )
        ),
        DebugResult(
            id = "2",
            title = "Inefficient Loop Structure",
            description = "This loop can be optimized using built-in collection functions for better performance.",
            severity = DebugSeverity.SUGGESTION,
            category = "Performance",
            codeLocation = CodeLocation(
                line = 23,
                column = 4,
                code = "for (item in list) { ... }"
            ),
            suggestedFix = DebugFix(
                id = "fix_2",
                title = "Use Collection Functions",
                description = "Replace manual loop with optimized collection function",
                originalCode = "for (item in list) {\n    if (item.isValid) result.add(item)\n}",
                fixedCode = "val result = list.filter { it.isValid }",
                confidence = 0.88f
            )
        ),
        DebugResult(
            id = "3",
            title = "Missing Input Validation",
            description = "User input should be validated before processing to prevent security vulnerabilities.",
            severity = DebugSeverity.WARNING,
            category = "Security",
            codeLocation = null,
            suggestedFix = DebugFix(
                id = "fix_3",
                title = "Add Input Validation",
                description = "Add comprehensive input validation",
                originalCode = "processUserInput(input)",
                fixedCode = "if (isValidInput(input)) {\n    processUserInput(input)\n} else {\n    throw IllegalArgumentException(\"Invalid input\")\n}",
                confidence = 0.92f
            )
        )
    )
}

private fun analyzeCodeRealTime(code: String, language: String): List<RealTimeIssue> {
    // Simulate real-time analysis
    return listOf(
        RealTimeIssue("Syntax", 0, DebugSeverity.INFO),
        RealTimeIssue("Logic", 1, DebugSeverity.WARNING),
        RealTimeIssue("Performance", 2, DebugSeverity.SUGGESTION),
        RealTimeIssue("Security", 1, DebugSeverity.WARNING)
    )
}

// Data classes
data class DebugAgent(
    val id: String,
    val name: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color,
    val capabilities: List<String>
)

data class DebugResult(
    val id: String,
    val title: String,
    val description: String,
    val severity: DebugSeverity,
    val category: String,
    val codeLocation: CodeLocation?,
    val suggestedFix: DebugFix?
)

data class DebugFix(
    val id: String,
    val title: String,
    val description: String,
    val originalCode: String,
    val fixedCode: String,
    val confidence: Float
)

data class CodeLocation(
    val line: Int,
    val column: Int,
    val code: String
)

data class RealTimeIssue(
    val type: String,
    val count: Int,
    val severity: DebugSeverity
)

enum class DebugState {
    IDLE, ANALYZING, RESULTS_READY
}

enum class DebugSeverity {
    ERROR, WARNING, INFO, SUGGESTION
}