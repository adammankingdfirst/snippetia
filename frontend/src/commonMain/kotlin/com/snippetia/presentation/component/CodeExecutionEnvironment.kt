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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Advanced Code Execution Environment with sandboxed runtime and real-time monitoring
 */

@Composable
fun CodeExecutionEnvironment(
    code: String,
    language: String,
    onCodeChange: (String) -> Unit,
    onExecute: (ExecutionConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    var executionState by remember { mutableStateOf(ExecutionState.IDLE) }
    var executionResults by remember { mutableStateOf<ExecutionResult?>(null) }
    var selectedRuntime by remember { mutableStateOf<RuntimeEnvironment?>(null) }
    var executionConfig by remember { mutableStateOf(ExecutionConfig()) }
    var isMonitoring by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    // Available runtime environments
    val runtimeEnvironments = remember {
        getRuntimeEnvironments(language)
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header with execution controls
            ExecutionHeader(
                executionState = executionState,
                isMonitoring = isMonitoring,
                onExecute = {
                    executionState = ExecutionState.RUNNING
                    isMonitoring = true
                    
                    coroutineScope.launch {
                        // Simulate code execution
                        delay(2000)
                        
                        executionResults = simulateExecution(code, language, executionConfig)
                        executionState = ExecutionState.COMPLETED
                        isMonitoring = false
                    }
                },
                onStop = {
                    executionState = ExecutionState.STOPPED
                    isMonitoring = false
                },
                onClear = {
                    executionResults = null
                    executionState = ExecutionState.IDLE
                }
            )
            
            HorizontalDivider()
            
            // Runtime Environment Selection
            RuntimeEnvironmentSection(
                environments = runtimeEnvironments,
                selectedRuntime = selectedRuntime,
                onRuntimeSelected = { selectedRuntime = it },
                executionConfig = executionConfig,
                onConfigChange = { executionConfig = it }
            )
            
            HorizontalDivider()
            
            // Code Editor with syntax highlighting
            CodeEditorSection(
                code = code,
                language = language,
                onCodeChange = onCodeChange,
                executionState = executionState
            )
            
            // Execution Results
            if (executionResults != null) {
                HorizontalDivider()
                
                ExecutionResultsSection(
                    result = executionResults!!,
                    isMonitoring = isMonitoring
                )
            }
            
            // Real-time Monitoring
            if (isMonitoring) {
                HorizontalDivider()
                
                RealTimeMonitoringSection(
                    executionState = executionState
                )
            }
        }
    }
}

@Composable
private fun ExecutionHeader(
    executionState: ExecutionState,
    isMonitoring: Boolean,
    onExecute: () -> Unit,
    onStop: () -> Unit,
    onClear: () -> Unit
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
                Icons.Default.PlayCircle,
                contentDescription = "Execute",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Text(
                text = "Code Execution Environment",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Status indicator
            ExecutionStatusIndicator(
                state = executionState,
                isMonitoring = isMonitoring
            )
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Clear button
            IconButton(
                onClick = onClear,
                enabled = executionState != ExecutionState.RUNNING
            ) {
                Icon(
                    Icons.Default.Clear,
                    contentDescription = "Clear Results"
                )
            }
            
            // Stop button
            if (executionState == ExecutionState.RUNNING) {
                Button(
                    onClick = onStop,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Stop,
                        contentDescription = "Stop",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Stop")
                }
            } else {
                // Execute button
                Button(
                    onClick = onExecute,
                    enabled = executionState != ExecutionState.RUNNING
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Execute",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Execute")
                }
            }
        }
    }
}

@Composable
private fun ExecutionStatusIndicator(
    state: ExecutionState,
    isMonitoring: Boolean
) {
    val color = when (state) {
        ExecutionState.IDLE -> MaterialTheme.colorScheme.outline
        ExecutionState.RUNNING -> MaterialTheme.colorScheme.primary
        ExecutionState.COMPLETED -> Color(0xFF4CAF50)
        ExecutionState.ERROR -> MaterialTheme.colorScheme.error
        ExecutionState.STOPPED -> Color(0xFFFF9800)
    }
    
    val text = when (state) {
        ExecutionState.IDLE -> "Ready"
        ExecutionState.RUNNING -> "Running"
        ExecutionState.COMPLETED -> "Completed"
        ExecutionState.ERROR -> "Error"
        ExecutionState.STOPPED -> "Stopped"
    }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, RoundedCornerShape(50%))
        )
        
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
        
        if (isMonitoring) {
            CircularProgressIndicator(
                modifier = Modifier.size(12.dp),
                strokeWidth = 1.dp,
                color = color
            )
        }
    }
}

@Composable
private fun RuntimeEnvironmentSection(
    environments: List<RuntimeEnvironment>,
    selectedRuntime: RuntimeEnvironment?,
    onRuntimeSelected: (RuntimeEnvironment) -> Unit,
    executionConfig: ExecutionConfig,
    onConfigChange: (ExecutionConfig) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Runtime Environment",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(environments) { environment ->
                RuntimeEnvironmentCard(
                    environment = environment,
                    isSelected = selectedRuntime == environment,
                    onSelect = { onRuntimeSelected(environment) }
                )
            }
        }
        
        if (selectedRuntime != null) {
            Spacer(modifier = Modifier.height(12.dp))
            
            ExecutionConfigSection(
                config = executionConfig,
                onConfigChange = onConfigChange,
                runtime = selectedRuntime
            )
        }
    }
}

@Composable
private fun RuntimeEnvironmentCard(
    environment: RuntimeEnvironment,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = environment.icon,
                contentDescription = environment.name,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = environment.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = environment.version,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ExecutionConfigSection(
    config: ExecutionConfig,
    onConfigChange: (ExecutionConfig) -> Unit,
    runtime: RuntimeEnvironment
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Execution Configuration",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Timeout setting
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Timeout (seconds)",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (config.timeoutSeconds > 1) {
                                onConfigChange(config.copy(timeoutSeconds = config.timeoutSeconds - 1))
                            }
                        }
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                    }
                    
                    Text(
                        text = config.timeoutSeconds.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.width(40.dp),
                        textAlign = TextAlign.Center
                    )
                    
                    IconButton(
                        onClick = {
                            if (config.timeoutSeconds < 60) {
                                onConfigChange(config.copy(timeoutSeconds = config.timeoutSeconds + 1))
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase")
                    }
                }
            }
            
            // Memory limit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Memory Limit (MB)",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "${config.memoryLimitMB} MB",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Slider(
                value = config.memoryLimitMB.toFloat(),
                onValueChange = { 
                    onConfigChange(config.copy(memoryLimitMB = it.toInt()))
                },
                valueRange = 64f..1024f,
                steps = 15
            )
            
            // Security options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sandboxed Execution",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Switch(
                    checked = config.sandboxed,
                    onCheckedChange = { 
                        onConfigChange(config.copy(sandboxed = it))
                    }
                )
            }
        }
    }
}

@Composable
private fun CodeEditorSection(
    code: String,
    language: String,
    onCodeChange: (String) -> Unit,
    executionState: ExecutionState
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
                text = "Code Editor",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            
            AssistChip(
                onClick = { },
                label = {
                    Text(
                        text = language.uppercase(),
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Code,
                        contentDescription = "Language",
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp, max = 400.dp)
        ) {
            OutlinedTextField(
                value = code,
                onValueChange = onCodeChange,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace
                ),
                placeholder = {
                    Text(
                        text = "Enter your ${language} code here...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                enabled = executionState != ExecutionState.RUNNING,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )
        }
    }
}@Comp
osable
private fun ExecutionResultsSection(
    result: ExecutionResult,
    isMonitoring: Boolean
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
                text = "Execution Results",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = "${result.executionTime}ms",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = "Execution Time",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
                
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = "${result.memoryUsed}MB",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Memory,
                            contentDescription = "Memory Used",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Output section
        if (result.output.isNotBlank()) {
            ExecutionOutputCard(
                title = "Output",
                content = result.output,
                icon = Icons.Default.Output,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Error section
        if (result.error.isNotBlank()) {
            ExecutionOutputCard(
                title = "Error",
                content = result.error,
                icon = Icons.Default.Error,
                color = MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Performance metrics
        if (result.performanceMetrics.isNotEmpty()) {
            PerformanceMetricsCard(
                metrics = result.performanceMetrics
            )
        }
    }
}

@Composable
private fun ExecutionOutputCard(
    title: String,
    content: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = color
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(4.dp)
                    )
                    .padding(8.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PerformanceMetricsCard(
    metrics: Map<String, String>
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Analytics,
                    contentDescription = "Performance Metrics",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(20.dp)
                )
                
                Text(
                    text = "Performance Metrics",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            metrics.forEach { (key, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = key,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun RealTimeMonitoringSection(
    executionState: ExecutionState
) {
    var cpuUsage by remember { mutableStateOf(0f) }
    var memoryUsage by remember { mutableStateOf(0f) }
    var networkActivity by remember { mutableStateOf(0f) }
    
    LaunchedEffect(executionState) {
        while (executionState == ExecutionState.RUNNING) {
            // Simulate real-time monitoring data
            cpuUsage = (10f..90f).random()
            memoryUsage = (20f..80f).random()
            networkActivity = (0f..50f).random()
            delay(500)
        }
    }
    
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Real-time Monitoring",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MonitoringMetricCard(
                title = "CPU Usage",
                value = cpuUsage,
                unit = "%",
                icon = Icons.Default.Memory,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            
            MonitoringMetricCard(
                title = "Memory",
                value = memoryUsage,
                unit = "%",
                icon = Icons.Default.Storage,
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
            
            MonitoringMetricCard(
                title = "Network",
                value = networkActivity,
                unit = "KB/s",
                icon = Icons.Default.NetworkCheck,
                color = Color(0xFFFF9800),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MonitoringMetricCard(
    title: String,
    value: Float,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "${value.toInt()}$unit",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            
            // Progress indicator
            LinearProgressIndicator(
                progress = { value / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.2f)
            )
        }
    }
}

// Helper functions
private fun getRuntimeEnvironments(language: String): List<RuntimeEnvironment> {
    return when (language.lowercase()) {
        "kotlin" -> listOf(
            RuntimeEnvironment("kotlin_jvm", "Kotlin/JVM", "1.9.20", Icons.Default.Code),
            RuntimeEnvironment("kotlin_native", "Kotlin/Native", "1.9.20", Icons.Default.Memory),
            RuntimeEnvironment("kotlin_js", "Kotlin/JS", "1.9.20", Icons.Default.Web)
        )
        "javascript" -> listOf(
            RuntimeEnvironment("node", "Node.js", "20.9.0", Icons.Default.Javascript),
            RuntimeEnvironment("deno", "Deno", "1.37.0", Icons.Default.Security),
            RuntimeEnvironment("browser", "Browser", "Latest", Icons.Default.Web)
        )
        "python" -> listOf(
            RuntimeEnvironment("python3", "Python 3", "3.11.5", Icons.Default.Code),
            RuntimeEnvironment("pypy", "PyPy", "3.10.13", Icons.Default.Speed),
            RuntimeEnvironment("micropython", "MicroPython", "1.20.0", Icons.Default.Memory)
        )
        "java" -> listOf(
            RuntimeEnvironment("openjdk", "OpenJDK", "21.0.0", Icons.Default.Code),
            RuntimeEnvironment("graalvm", "GraalVM", "21.0.0", Icons.Default.Speed)
        )
        else -> listOf(
            RuntimeEnvironment("generic", "Generic Runtime", "1.0.0", Icons.Default.Code)
        )
    }
}

private fun simulateExecution(
    code: String,
    language: String,
    config: ExecutionConfig
): ExecutionResult {
    // Simulate execution results
    val executionTime = (100..2000).random()
    val memoryUsed = (config.memoryLimitMB * 0.1).toInt()..(config.memoryLimitMB * 0.8).random()
    
    val output = when {
        code.contains("print") || code.contains("println") -> "Hello, World!\nExecution completed successfully."
        code.contains("error") || code.contains("throw") -> ""
        else -> "Code executed successfully.\nResult: ${(1..100).random()}"
    }
    
    val error = when {
        code.contains("error") -> "RuntimeError: Simulated error occurred at line 1"
        code.contains("throw") -> "Exception: Simulated exception thrown"
        else -> ""
    }
    
    val performanceMetrics = mapOf(
        "CPU Cycles" to "${(1000000..5000000).random()}",
        "Memory Allocations" to "${(100..1000).random()}",
        "GC Collections" to "${(0..5).random()}",
        "I/O Operations" to "${(0..50).random()}"
    )
    
    return ExecutionResult(
        output = output,
        error = error,
        executionTime = executionTime,
        memoryUsed = memoryUsed,
        performanceMetrics = performanceMetrics,
        exitCode = if (error.isBlank()) 0 else 1
    )
}

// Data classes
data class RuntimeEnvironment(
    val id: String,
    val name: String,
    val version: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

data class ExecutionConfig(
    val timeoutSeconds: Int = 10,
    val memoryLimitMB: Int = 256,
    val sandboxed: Boolean = true,
    val enableNetworking: Boolean = false,
    val enableFileSystem: Boolean = false
)

data class ExecutionResult(
    val output: String,
    val error: String,
    val executionTime: Int,
    val memoryUsed: Int,
    val performanceMetrics: Map<String, String>,
    val exitCode: Int
)

enum class ExecutionState {
    IDLE, RUNNING, COMPLETED, ERROR, STOPPED
}