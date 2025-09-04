package com.snippetia.debug

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Advanced debugging tools for Kotlin Multiplatform Compose applications
 */

@Serializable
data class DebugLog(
    val timestamp: Long,
    val level: LogLevel,
    val tag: String,
    val message: String,
    val exception: String? = null,
    val metadata: Map<String, String> = emptyMap()
)

enum class LogLevel(val displayName: String, val color: Color) {
    VERBOSE("VERBOSE", Color(0xFF9E9E9E)),
    DEBUG("DEBUG", Color(0xFF2196F3)),
    INFO("INFO", Color(0xFF4CAF50)),
    WARN("WARN", Color(0xFFFF9800)),
    ERROR("ERROR", Color(0xFFF44336)),
    FATAL("FATAL", Color(0xFF9C27B0))
}

@Serializable
data class PerformanceMetric(
    val name: String,
    val value: Double,
    val unit: String,
    val timestamp: Long,
    val category: String
)

@Serializable
data class NetworkRequest(
    val id: String,
    val url: String,
    val method: String,
    val statusCode: Int?,
    val requestTime: Long,
    val responseTime: Long?,
    val requestSize: Long,
    val responseSize: Long?,
    val headers: Map<String, String>,
    val error: String? = null
)

class DebugManager {
    private val _logs = MutableStateFlow<List<DebugLog>>(emptyList())
    val logs: StateFlow<List<DebugLog>> = _logs.asStateFlow()
    
    private val _performanceMetrics = MutableStateFlow<List<PerformanceMetric>>(emptyList())
    val performanceMetrics: StateFlow<List<PerformanceMetric>> = _performanceMetrics.asStateFlow()
    
    private val _networkRequests = MutableStateFlow<List<NetworkRequest>>(emptyList())
    val networkRequests: StateFlow<List<NetworkRequest>> = _networkRequests.asStateFlow()
    
    private val _isEnabled = MutableStateFlow(false)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()
    
    private val maxLogs = 1000
    private val maxMetrics = 500
    private val maxRequests = 100
    
    fun enable() {
        _isEnabled.value = true
    }
    
    fun disable() {
        _isEnabled.value = false
    }
    
    fun log(level: LogLevel, tag: String, message: String, exception: Throwable? = null, metadata: Map<String, String> = emptyMap()) {
        if (!_isEnabled.value) return
        
        val log = DebugLog(
            timestamp = System.currentTimeMillis(),
            level = level,
            tag = tag,
            message = message,
            exception = exception?.stackTraceToString(),
            metadata = metadata
        )
        
        _logs.value = (_logs.value + log).takeLast(maxLogs)
    }
    
    fun addPerformanceMetric(name: String, value: Double, unit: String, category: String = "General") {
        if (!_isEnabled.value) return
        
        val metric = PerformanceMetric(
            name = name,
            value = value,
            unit = unit,
            timestamp = System.currentTimeMillis(),
            category = category
        )
        
        _performanceMetrics.value = (_performanceMetrics.value + metric).takeLast(maxMetrics)
    }
    
    fun addNetworkRequest(request: NetworkRequest) {
        if (!_isEnabled.value) return
        
        _networkRequests.value = (_networkRequests.value + request).takeLast(maxRequests)
    }
    
    fun clearLogs() {
        _logs.value = emptyList()
    }
    
    fun clearMetrics() {
        _performanceMetrics.value = emptyList()
    }
    
    fun clearNetworkRequests() {
        _networkRequests.value = emptyList()
    }
    
    fun exportLogs(): String {
        return Json.encodeToString(DebugLog.serializer(), _logs.value.first())
    }
    
    companion object {
        @Volatile
        private var INSTANCE: DebugManager? = null
        
        fun getInstance(): DebugManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DebugManager().also { INSTANCE = it }
            }
        }
    }
}

// Extension functions for easy logging
fun Any.logV(message: String, exception: Throwable? = null, metadata: Map<String, String> = emptyMap()) {
    DebugManager.getInstance().log(LogLevel.VERBOSE, this::class.simpleName ?: "Unknown", message, exception, metadata)
}

fun Any.logD(message: String, exception: Throwable? = null, metadata: Map<String, String> = emptyMap()) {
    DebugManager.getInstance().log(LogLevel.DEBUG, this::class.simpleName ?: "Unknown", message, exception, metadata)
}

fun Any.logI(message: String, exception: Throwable? = null, metadata: Map<String, String> = emptyMap()) {
    DebugManager.getInstance().log(LogLevel.INFO, this::class.simpleName ?: "Unknown", message, exception, metadata)
}

fun Any.logW(message: String, exception: Throwable? = null, metadata: Map<String, String> = emptyMap()) {
    DebugManager.getInstance().log(LogLevel.WARN, this::class.simpleName ?: "Unknown", message, exception, metadata)
}

fun Any.logE(message: String, exception: Throwable? = null, metadata: Map<String, String> = emptyMap()) {
    DebugManager.getInstance().log(LogLevel.ERROR, this::class.simpleName ?: "Unknown", message, exception, metadata)
}

fun Any.logF(message: String, exception: Throwable? = null, metadata: Map<String, String> = emptyMap()) {
    DebugManager.getInstance().log(LogLevel.FATAL, this::class.simpleName ?: "Unknown", message, exception, metadata)
}

@Composable
fun DebugPanel(
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {}
) {
    val debugManager = remember { DebugManager.getInstance() }
    val logs by debugManager.logs.collectAsState()
    val metrics by debugManager.performanceMetrics.collectAsState()
    val networkRequests by debugManager.networkRequests.collectAsState()
    val isEnabled by debugManager.isEnabled.collectAsState()
    
    var selectedTab by remember { mutableStateOf(DebugTab.LOGS) }
    var logFilter by remember { mutableStateOf<LogLevel?>(null) }
    
    Card(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            DebugPanelHeader(
                isEnabled = isEnabled,
                onToggleEnabled = {
                    if (isEnabled) debugManager.disable() else debugManager.enable()
                },
                onClose = onClose,
                onClearAll = {
                    debugManager.clearLogs()
                    debugManager.clearMetrics()
                    debugManager.clearNetworkRequests()
                }
            )
            
            Divider()
            
            // Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                modifier = Modifier.fillMaxWidth()
            ) {
                DebugTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.displayName,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(tab.displayName)
                                
                                // Badge with count
                                val count = when (tab) {
                                    DebugTab.LOGS -> logs.size
                                    DebugTab.PERFORMANCE -> metrics.size
                                    DebugTab.NETWORK -> networkRequests.size
                                    DebugTab.SYSTEM -> 0
                                }
                                
                                if (count > 0) {
                                    Badge {
                                        Text(
                                            text = count.toString(),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }
            
            Divider()
            
            // Tab Content
            when (selectedTab) {
                DebugTab.LOGS -> {
                    LogsTab(
                        logs = logs,
                        filter = logFilter,
                        onFilterChange = { logFilter = it },
                        modifier = Modifier.weight(1f)
                    )
                }
                DebugTab.PERFORMANCE -> {
                    PerformanceTab(
                        metrics = metrics,
                        modifier = Modifier.weight(1f)
                    )
                }
                DebugTab.NETWORK -> {
                    NetworkTab(
                        requests = networkRequests,
                        modifier = Modifier.weight(1f)
                    )
                }
                DebugTab.SYSTEM -> {
                    SystemTab(
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DebugPanelHeader(
    isEnabled: Boolean,
    onToggleEnabled: () -> Unit,
    onClose: () -> Unit,
    onClearAll: () -> Unit
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
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.BugReport,
                contentDescription = "Debug Panel",
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Debug Panel",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Switch(
                checked = isEnabled,
                onCheckedChange = { onToggleEnabled() }
            )
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onClearAll) {
                Icon(
                    Icons.Default.ClearAll,
                    contentDescription = "Clear All"
                )
            }
            
            IconButton(onClick = onClose) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close"
                )
            }
        }
    }
}

@Composable
private fun LogsTab(
    logs: List<DebugLog>,
    filter: LogLevel?,
    onFilterChange: (LogLevel?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Filter Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = filter == null,
                    onClick = { onFilterChange(null) },
                    label = { Text("All") }
                )
            }
            
            items(LogLevel.values()) { level ->
                FilterChip(
                    selected = filter == level,
                    onClick = { onFilterChange(if (filter == level) null else level) },
                    label = { Text(level.displayName) },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(level.color, androidx.compose.foundation.shape.CircleShape)
                        )
                    }
                )
            }
        }
        
        Divider()
        
        // Logs List
        val filteredLogs = if (filter != null) {
            logs.filter { it.level == filter }
        } else {
            logs
        }
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            reverseLayout = true
        ) {
            items(filteredLogs.reversed()) { log ->
                LogItem(log = log)
            }
        }
    }
}

@Composable
private fun LogItem(log: DebugLog) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { expanded = !expanded },
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(log.level.color, androidx.compose.foundation.shape.CircleShape)
                    )
                    
                    Text(
                        text = log.level.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = log.level.color
                    )
                    
                    Text(
                        text = log.tag,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = formatTimestamp(log.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = log.message,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (expanded) Int.MAX_VALUE else 2
            )
            
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    if (log.exception != null) {
                        Text(
                            text = "Exception:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        
                        Text(
                            text = log.exception,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(8.dp)
                        )
                    }
                    
                    if (log.metadata.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Metadata:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        log.metadata.forEach { (key, value) ->
                            Row {
                                Text(
                                    text = "$key: ",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = value,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PerformanceTab(
    metrics: List<PerformanceMetric>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Group metrics by category
        val groupedMetrics = metrics.groupBy { it.category }
        
        groupedMetrics.forEach { (category, categoryMetrics) ->
            item {
                Text(
                    text = category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(categoryMetrics.sortedByDescending { it.timestamp }) { metric ->
                PerformanceMetricItem(metric = metric)
            }
        }
    }
}

@Composable
private fun PerformanceMetricItem(metric: PerformanceMetric) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = metric.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = formatTimestamp(metric.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = "${metric.value} ${metric.unit}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun NetworkTab(
    requests: List<NetworkRequest>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(requests.sortedByDescending { it.requestTime }) { request ->
            NetworkRequestItem(request = request)
        }
    }
}

@Composable
private fun NetworkRequestItem(request: NetworkRequest) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
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
                    Text(
                        text = request.method,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = getMethodColor(request.method)
                    )
                    
                    Text(
                        text = request.statusCode?.toString() ?: "Pending",
                        style = MaterialTheme.typography.labelMedium,
                        color = getStatusColor(request.statusCode)
                    )
                }
                
                Text(
                    text = formatTimestamp(request.requestTime),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = request.url,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (expanded) Int.MAX_VALUE else 1
            )
            
            if (request.responseTime != null) {
                val duration = request.responseTime - request.requestTime
                Text(
                    text = "Duration: ${duration}ms",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    if (request.headers.isNotEmpty()) {
                        Text(
                            text = "Headers:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        request.headers.forEach { (key, value) ->
                            Row {
                                Text(
                                    text = "$key: ",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = value,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                    
                    if (request.error != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Error:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        
                        Text(
                            text = request.error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SystemTab(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SystemInfoCard()
        }
        
        item {
            MemoryInfoCard()
        }
        
        item {
            ComposeInfoCard()
        }
    }
}

@Composable
private fun SystemInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "System Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            SystemInfoRow("Platform", getPlatformName())
            SystemInfoRow("Version", getSystemVersion())
            SystemInfoRow("Architecture", getArchitecture())
            SystemInfoRow("Available Processors", getProcessorCount().toString())
        }
    }
}

@Composable
private fun MemoryInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Memory Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            SystemInfoRow("Used Memory", "${getUsedMemory()} MB")
            SystemInfoRow("Free Memory", "${getFreeMemory()} MB")
            SystemInfoRow("Total Memory", "${getTotalMemory()} MB")
        }
    }
}

@Composable
private fun ComposeInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Compose Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            SystemInfoRow("Compose Version", getComposeVersion())
            SystemInfoRow("Kotlin Version", getKotlinVersion())
            SystemInfoRow("Recomposition Count", getRecompositionCount().toString())
        }
    }
}

@Composable
private fun SystemInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

// Enums and data classes
enum class DebugTab(val displayName: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    LOGS("Logs", Icons.Default.List),
    PERFORMANCE("Performance", Icons.Default.Speed),
    NETWORK("Network", Icons.Default.NetworkCheck),
    SYSTEM("System", Icons.Default.Info)
}

// Helper functions
private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 1000 -> "Just now"
        diff < 60000 -> "${diff / 1000}s ago"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        else -> "${diff / 86400000}d ago"
    }
}

private fun getMethodColor(method: String): Color {
    return when (method.uppercase()) {
        "GET" -> Color(0xFF4CAF50)
        "POST" -> Color(0xFF2196F3)
        "PUT" -> Color(0xFFFF9800)
        "DELETE" -> Color(0xFFF44336)
        "PATCH" -> Color(0xFF9C27B0)
        else -> Color(0xFF9E9E9E)
    }
}

private fun getStatusColor(statusCode: Int?): Color {
    return when (statusCode) {
        in 200..299 -> Color(0xFF4CAF50)
        in 300..399 -> Color(0xFFFF9800)
        in 400..499 -> Color(0xFFF44336)
        in 500..599 -> Color(0xFF9C27B0)
        else -> Color(0xFF9E9E9E)
    }
}

// Platform-specific implementations (would be actual implementations in real code)
private fun getPlatformName(): String = "Android" // or iOS, Desktop, Web
private fun getSystemVersion(): String = "API 34"
private fun getArchitecture(): String = "arm64-v8a"
private fun getProcessorCount(): Int = Runtime.getRuntime().availableProcessors()
private fun getUsedMemory(): Long = 128 // MB
private fun getFreeMemory(): Long = 256 // MB
private fun getTotalMemory(): Long = 384 // MB
private fun getComposeVersion(): String = "1.5.4"
private fun getKotlinVersion(): String = "1.9.20"
private fun getRecompositionCount(): Int = 42

/**
 * Performance profiler for Compose
 */
@Composable
fun rememberPerformanceProfiler(): PerformanceProfiler {
    return remember { PerformanceProfiler() }
}

class PerformanceProfiler {
    private val debugManager = DebugManager.getInstance()
    
    fun startMeasurement(name: String): MeasurementHandle {
        return MeasurementHandle(name, System.nanoTime())
    }
    
    inner class MeasurementHandle(
        private val name: String,
        private val startTime: Long
    ) {
        fun end() {
            val endTime = System.nanoTime()
            val durationMs = (endTime - startTime) / 1_000_000.0
            debugManager.addPerformanceMetric(name, durationMs, "ms", "Compose")
        }
    }
}

/**
 * Composable function to measure recomposition performance
 */
@Composable
fun <T> MeasuredComposable(
    name: String,
    content: @Composable () -> T
): T {
    val profiler = rememberPerformanceProfiler()
    
    return remember(name) {
        val handle = profiler.startMeasurement("Recomposition: $name")
        val result = content()
        handle.end()
        result
    }
}