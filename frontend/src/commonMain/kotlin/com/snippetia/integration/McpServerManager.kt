package com.snippetia.integration

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

/**
 * Model Context Protocol (MCP) Server Manager
 * Handles communication with MCP servers for enhanced AI capabilities
 */

@Serializable
data class McpServerConfig(
    val id: String,
    val name: String,
    val endpoint: String,
    val apiKey: String? = null,
    val timeout: Long = 30000,
    val retryAttempts: Int = 3,
    val capabilities: List<String> = emptyList(),
    val metadata: Map<String, String> = emptyMap()
)

@Serializable
data class McpRequest(
    val id: String,
    val method: String,
    val params: JsonObject = JsonObject(emptyMap()),
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class McpResponse(
    val id: String,
    val result: JsonElement? = null,
    val error: McpError? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class McpError(
    val code: Int,
    val message: String,
    val data: JsonElement? = null
)

@Serializable
data class McpTool(
    val name: String,
    val description: String,
    val parameters: JsonObject,
    val examples: List<String> = emptyList()
)

@Serializable
data class McpResource(
    val uri: String,
    val name: String,
    val description: String,
    val mimeType: String? = null
)

sealed class McpServerStatus {
    object Disconnected : McpServerStatus()
    object Connecting : McpServerStatus()
    object Connected : McpServerStatus()
    data class Error(val message: String) : McpServerStatus()
}

class McpServerManager(
    private val httpClient: HttpClient,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    private val _servers = MutableStateFlow<Map<String, McpServerInstance>>(emptyMap())
    val servers: StateFlow<Map<String, McpServerInstance>> = _servers.asStateFlow()
    
    private val _events = MutableSharedFlow<McpEvent>()
    val events: SharedFlow<McpEvent> = _events.asSharedFlow()
    
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    /**
     * Register a new MCP server
     */
    suspend fun registerServer(config: McpServerConfig): Result<Unit> {
        return try {
            val instance = McpServerInstance(config, httpClient, json)
            _servers.value = _servers.value + (config.id to instance)
            
            // Attempt to connect
            connectServer(config.id)
            
            _events.emit(McpEvent.ServerRegistered(config.id))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Connect to a registered MCP server
     */
    suspend fun connectServer(serverId: String): Result<Unit> {
        val server = _servers.value[serverId] ?: return Result.failure(
            IllegalArgumentException("Server not found: $serverId")
        )
        
        return server.connect().also { result ->
            if (result.isSuccess) {
                _events.emit(McpEvent.ServerConnected(serverId))
            } else {
                _events.emit(McpEvent.ServerError(serverId, result.exceptionOrNull()?.message ?: "Connection failed"))
            }
        }
    }
    
    /**
     * Disconnect from an MCP server
     */
    suspend fun disconnectServer(serverId: String): Result<Unit> {
        val server = _servers.value[serverId] ?: return Result.failure(
            IllegalArgumentException("Server not found: $serverId")
        )
        
        return server.disconnect().also {
            _events.emit(McpEvent.ServerDisconnected(serverId))
        }
    }
    
    /**
     * Execute a tool on an MCP server
     */
    suspend fun executeTool(
        serverId: String,
        toolName: String,
        parameters: Map<String, JsonElement>
    ): Result<JsonElement> {
        val server = _servers.value[serverId] ?: return Result.failure(
            IllegalArgumentException("Server not found: $serverId")
        )
        
        return server.executeTool(toolName, parameters)
    }
    
    /**
     * Get available tools from an MCP server
     */
    suspend fun getTools(serverId: String): Result<List<McpTool>> {
        val server = _servers.value[serverId] ?: return Result.failure(
            IllegalArgumentException("Server not found: $serverId")
        )
        
        return server.getTools()
    }
    
    /**
     * Get available resources from an MCP server
     */
    suspend fun getResources(serverId: String): Result<List<McpResource>> {
        val server = _servers.value[serverId] ?: return Result.failure(
            IllegalArgumentException("Server not found: $serverId")
        )
        
        return server.getResources()
    }
    
    /**
     * Read a resource from an MCP server
     */
    suspend fun readResource(serverId: String, uri: String): Result<String> {
        val server = _servers.value[serverId] ?: return Result.failure(
            IllegalArgumentException("Server not found: $serverId")
        )
        
        return server.readResource(uri)
    }
    
    /**
     * Send a custom request to an MCP server
     */
    suspend fun sendRequest(
        serverId: String,
        method: String,
        params: JsonObject = JsonObject(emptyMap())
    ): Result<JsonElement> {
        val server = _servers.value[serverId] ?: return Result.failure(
            IllegalArgumentException("Server not found: $serverId")
        )
        
        return server.sendRequest(method, params)
    }
    
    /**
     * Get server status
     */
    fun getServerStatus(serverId: String): McpServerStatus {
        return _servers.value[serverId]?.status?.value ?: McpServerStatus.Disconnected
    }
    
    /**
     * Get all connected servers
     */
    fun getConnectedServers(): List<String> {
        return _servers.value.values
            .filter { it.status.value is McpServerStatus.Connected }
            .map { it.config.id }
    }
    
    /**
     * Auto-discover MCP servers
     */
    suspend fun discoverServers(): List<McpServerConfig> {
        // Implementation would scan for available MCP servers
        // This could include well-known endpoints, local discovery, etc.
        return listOf(
            McpServerConfig(
                id = "github",
                name = "GitHub MCP Server",
                endpoint = "https://api.github.com/mcp",
                capabilities = listOf("repositories", "issues", "pull_requests")
            ),
            McpServerConfig(
                id = "stackoverflow",
                name = "Stack Overflow MCP Server",
                endpoint = "https://api.stackexchange.com/mcp",
                capabilities = listOf("search", "questions", "answers")
            ),
            McpServerConfig(
                id = "docs",
                name = "Documentation MCP Server",
                endpoint = "https://docs.snippetia.com/mcp",
                capabilities = listOf("search_docs", "get_examples", "api_reference")
            )
        )
    }
    
    /**
     * Cleanup resources
     */
    fun cleanup() {
        coroutineScope.cancel()
        _servers.value.values.forEach { server ->
            coroutineScope.launch {
                server.disconnect()
            }
        }
    }
}

class McpServerInstance(
    val config: McpServerConfig,
    private val httpClient: HttpClient,
    private val json: Json
) {
    private val _status = MutableStateFlow<McpServerStatus>(McpServerStatus.Disconnected)
    val status: StateFlow<McpServerStatus> = _status.asStateFlow()
    
    private var requestCounter = 0
    private val activeRequests = mutableMapOf<String, CompletableDeferred<McpResponse>>()
    
    /**
     * Connect to the MCP server
     */
    suspend fun connect(): Result<Unit> {
        return try {
            _status.value = McpServerStatus.Connecting
            
            // Send initialization request
            val initRequest = McpRequest(
                id = generateRequestId(),
                method = "initialize",
                params = JsonObject(mapOf(
                    "protocolVersion" to JsonPrimitive("2024-11-05"),
                    "capabilities" to JsonObject(mapOf(
                        "tools" to JsonPrimitive(true),
                        "resources" to JsonPrimitive(true)
                    )),
                    "clientInfo" to JsonObject(mapOf(
                        "name" to JsonPrimitive("Snippetia"),
                        "version" to JsonPrimitive("1.0.0")
                    ))
                ))
            )
            
            val response = sendHttpRequest(initRequest)
            if (response.error != null) {
                _status.value = McpServerStatus.Error(response.error.message)
                return Result.failure(Exception(response.error.message))
            }
            
            _status.value = McpServerStatus.Connected
            Result.success(Unit)
        } catch (e: Exception) {
            _status.value = McpServerStatus.Error(e.message ?: "Connection failed")
            Result.failure(e)
        }
    }
    
    /**
     * Disconnect from the MCP server
     */
    suspend fun disconnect(): Result<Unit> {
        return try {
            _status.value = McpServerStatus.Disconnected
            activeRequests.values.forEach { deferred ->
                deferred.cancel()
            }
            activeRequests.clear()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Execute a tool on the server
     */
    suspend fun executeTool(toolName: String, parameters: Map<String, JsonElement>): Result<JsonElement> {
        if (_status.value !is McpServerStatus.Connected) {
            return Result.failure(IllegalStateException("Server not connected"))
        }
        
        return try {
            val request = McpRequest(
                id = generateRequestId(),
                method = "tools/call",
                params = JsonObject(mapOf(
                    "name" to JsonPrimitive(toolName),
                    "arguments" to JsonObject(parameters)
                ))
            )
            
            val response = sendHttpRequest(request)
            if (response.error != null) {
                Result.failure(Exception(response.error.message))
            } else {
                Result.success(response.result ?: JsonNull)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get available tools
     */
    suspend fun getTools(): Result<List<McpTool>> {
        if (_status.value !is McpServerStatus.Connected) {
            return Result.failure(IllegalStateException("Server not connected"))
        }
        
        return try {
            val request = McpRequest(
                id = generateRequestId(),
                method = "tools/list"
            )
            
            val response = sendHttpRequest(request)
            if (response.error != null) {
                Result.failure(Exception(response.error.message))
            } else {
                val toolsArray = response.result?.jsonObject?.get("tools")?.jsonArray
                val tools = toolsArray?.map { toolElement ->
                    json.decodeFromJsonElement<McpTool>(toolElement)
                } ?: emptyList()
                Result.success(tools)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get available resources
     */
    suspend fun getResources(): Result<List<McpResource>> {
        if (_status.value !is McpServerStatus.Connected) {
            return Result.failure(IllegalStateException("Server not connected"))
        }
        
        return try {
            val request = McpRequest(
                id = generateRequestId(),
                method = "resources/list"
            )
            
            val response = sendHttpRequest(request)
            if (response.error != null) {
                Result.failure(Exception(response.error.message))
            } else {
                val resourcesArray = response.result?.jsonObject?.get("resources")?.jsonArray
                val resources = resourcesArray?.map { resourceElement ->
                    json.decodeFromJsonElement<McpResource>(resourceElement)
                } ?: emptyList()
                Result.success(resources)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Read a resource
     */
    suspend fun readResource(uri: String): Result<String> {
        if (_status.value !is McpServerStatus.Connected) {
            return Result.failure(IllegalStateException("Server not connected"))
        }
        
        return try {
            val request = McpRequest(
                id = generateRequestId(),
                method = "resources/read",
                params = JsonObject(mapOf(
                    "uri" to JsonPrimitive(uri)
                ))
            )
            
            val response = sendHttpRequest(request)
            if (response.error != null) {
                Result.failure(Exception(response.error.message))
            } else {
                val content = response.result?.jsonObject?.get("contents")?.jsonArray?.firstOrNull()
                    ?.jsonObject?.get("text")?.jsonPrimitive?.content
                Result.success(content ?: "")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Send a custom request
     */
    suspend fun sendRequest(method: String, params: JsonObject): Result<JsonElement> {
        if (_status.value !is McpServerStatus.Connected) {
            return Result.failure(IllegalStateException("Server not connected"))
        }
        
        return try {
            val request = McpRequest(
                id = generateRequestId(),
                method = method,
                params = params
            )
            
            val response = sendHttpRequest(request)
            if (response.error != null) {
                Result.failure(Exception(response.error.message))
            } else {
                Result.success(response.result ?: JsonNull)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun sendHttpRequest(request: McpRequest): McpResponse {
        return try {
            val httpResponse = httpClient.post(config.endpoint) {
                contentType(ContentType.Application.Json)
                config.apiKey?.let { apiKey ->
                    header("Authorization", "Bearer $apiKey")
                }
                setBody(json.encodeToString(request))
            }
            
            if (httpResponse.status.isSuccess()) {
                val responseBody = httpResponse.bodyAsText()
                json.decodeFromString<McpResponse>(responseBody)
            } else {
                McpResponse(
                    id = request.id,
                    error = McpError(
                        code = httpResponse.status.value,
                        message = "HTTP ${httpResponse.status.value}: ${httpResponse.status.description}"
                    )
                )
            }
        } catch (e: Exception) {
            McpResponse(
                id = request.id,
                error = McpError(
                    code = -1,
                    message = e.message ?: "Unknown error"
                )
            )
        }
    }
    
    private fun generateRequestId(): String {
        return "req_${++requestCounter}_${System.currentTimeMillis()}"
    }
}

sealed class McpEvent {
    data class ServerRegistered(val serverId: String) : McpEvent()
    data class ServerConnected(val serverId: String) : McpEvent()
    data class ServerDisconnected(val serverId: String) : McpEvent()
    data class ServerError(val serverId: String, val message: String) : McpEvent()
    data class ToolExecuted(val serverId: String, val toolName: String, val result: JsonElement) : McpEvent()
    data class ResourceRead(val serverId: String, val uri: String, val content: String) : McpEvent()
}

/**
 * High-level MCP integration for AI assistance
 */
class McpAiIntegration(
    private val mcpManager: McpServerManager
) {
    /**
     * Search for code examples using MCP servers
     */
    suspend fun searchCodeExamples(query: String, language: String): List<CodeExample> {
        val results = mutableListOf<CodeExample>()
        
        // Search GitHub
        mcpManager.executeTool(
            "github",
            "search_repositories",
            mapOf(
                "q" to JsonPrimitive("$query language:$language"),
                "sort" to JsonPrimitive("stars"),
                "order" to JsonPrimitive("desc")
            )
        ).onSuccess { result ->
            // Parse GitHub results and add to results
            parseGitHubResults(result)?.let { examples ->
                results.addAll(examples)
            }
        }
        
        // Search Stack Overflow
        mcpManager.executeTool(
            "stackoverflow",
            "search_questions",
            mapOf(
                "q" to JsonPrimitive(query),
                "tagged" to JsonPrimitive(language),
                "sort" to JsonPrimitive("votes")
            )
        ).onSuccess { result ->
            // Parse Stack Overflow results and add to results
            parseStackOverflowResults(result)?.let { examples ->
                results.addAll(examples)
            }
        }
        
        return results
    }
    
    /**
     * Get documentation for a specific API or function
     */
    suspend fun getDocumentation(apiName: String, language: String): String? {
        return mcpManager.executeTool(
            "docs",
            "search_docs",
            mapOf(
                "query" to JsonPrimitive(apiName),
                "language" to JsonPrimitive(language)
            )
        ).getOrNull()?.jsonObject?.get("content")?.jsonPrimitive?.content
    }
    
    /**
     * Analyze code for potential issues using multiple MCP servers
     */
    suspend fun analyzeCode(code: String, language: String): CodeAnalysisResult {
        val issues = mutableListOf<CodeIssue>()
        val suggestions = mutableListOf<CodeSuggestion>()
        
        // Use multiple servers for comprehensive analysis
        val connectedServers = mcpManager.getConnectedServers()
        
        connectedServers.forEach { serverId ->
            mcpManager.executeTool(
                serverId,
                "analyze_code",
                mapOf(
                    "code" to JsonPrimitive(code),
                    "language" to JsonPrimitive(language)
                )
            ).onSuccess { result ->
                // Parse analysis results
                parseAnalysisResult(result)?.let { analysis ->
                    issues.addAll(analysis.issues)
                    suggestions.addAll(analysis.suggestions)
                }
            }
        }
        
        return CodeAnalysisResult(
            issues = issues,
            suggestions = suggestions,
            score = calculateQualityScore(issues)
        )
    }
    
    private fun parseGitHubResults(result: JsonElement): List<CodeExample>? {
        // Implementation to parse GitHub API results
        return null
    }
    
    private fun parseStackOverflowResults(result: JsonElement): List<CodeExample>? {
        // Implementation to parse Stack Overflow API results
        return null
    }
    
    private fun parseAnalysisResult(result: JsonElement): CodeAnalysisResult? {
        // Implementation to parse code analysis results
        return null
    }
    
    private fun calculateQualityScore(issues: List<CodeIssue>): Double {
        if (issues.isEmpty()) return 100.0
        
        val severityWeights = mapOf(
            "error" to 10.0,
            "warning" to 5.0,
            "info" to 1.0
        )
        
        val totalDeduction = issues.sumOf { issue ->
            severityWeights[issue.severity] ?: 1.0
        }
        
        return maxOf(0.0, 100.0 - totalDeduction)
    }
}

// Data classes for AI integration
data class CodeExample(
    val title: String,
    val code: String,
    val language: String,
    val description: String,
    val source: String,
    val url: String? = null
)

data class CodeIssue(
    val line: Int,
    val column: Int,
    val message: String,
    val severity: String,
    val rule: String? = null
)

data class CodeSuggestion(
    val title: String,
    val description: String,
    val code: String,
    val confidence: Double
)

data class CodeAnalysisResult(
    val issues: List<CodeIssue>,
    val suggestions: List<CodeSuggestion>,
    val score: Double
)