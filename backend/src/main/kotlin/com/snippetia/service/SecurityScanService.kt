package com.snippetia.service

import com.snippetia.model.CodeSnippet
import com.snippetia.repository.CodeSnippetRepository
import org.springframework.stereotype.Service

@Service
class SecurityScanService(
    private val codeSnippetRepository: CodeSnippetRepository
) {

    fun scanSnippet(snippet: CodeSnippet): SecurityScanResult {
        val vulnerabilities = mutableListOf<SecurityVulnerability>()
        val warnings = mutableListOf<SecurityWarning>()
        
        // Perform various security checks
        vulnerabilities.addAll(checkForCommonVulnerabilities(snippet))
        vulnerabilities.addAll(checkForSecretsAndCredentials(snippet))
        vulnerabilities.addAll(checkForMaliciousPatterns(snippet))
        
        warnings.addAll(checkForSecurityWarnings(snippet))
        
        val riskLevel = calculateRiskLevel(vulnerabilities, warnings)
        
        return SecurityScanResult(
            snippetId = snippet.id!!,
            riskLevel = riskLevel,
            vulnerabilities = vulnerabilities,
            warnings = warnings,
            scanTimestamp = java.time.LocalDateTime.now(),
            recommendations = generateRecommendations(vulnerabilities, warnings)
        )
    }

    private fun checkForCommonVulnerabilities(snippet: CodeSnippet): List<SecurityVulnerability> {
        val vulnerabilities = mutableListOf<SecurityVulnerability>()
        val content = snippet.content.lowercase()
        
        // SQL Injection patterns
        if (content.contains(Regex("(select|insert|update|delete).*from.*where.*['\"].*\\+.*['\"]"))) {
            vulnerabilities.add(
                SecurityVulnerability(
                    type = VulnerabilityType.SQL_INJECTION,
                    severity = Severity.HIGH,
                    description = "Potential SQL injection vulnerability detected",
                    line = findLineNumber(snippet.content, "select|insert|update|delete")
                )
            )
        }
        
        // XSS patterns
        if (content.contains(Regex("document\\.write|innerhtml.*\\+|eval\\("))) {
            vulnerabilities.add(
                SecurityVulnerability(
                    type = VulnerabilityType.XSS,
                    severity = Severity.MEDIUM,
                    description = "Potential XSS vulnerability detected",
                    line = findLineNumber(snippet.content, "document\\.write|innerhtml|eval")
                )
            )
        }
        
        // Command injection
        if (content.contains(Regex("runtime\\.exec|processbuilder|system\\(|exec\\("))) {
            vulnerabilities.add(
                SecurityVulnerability(
                    type = VulnerabilityType.COMMAND_INJECTION,
                    severity = Severity.HIGH,
                    description = "Potential command injection vulnerability detected",
                    line = findLineNumber(snippet.content, "runtime\\.exec|processbuilder|system|exec")
                )
            )
        }
        
        return vulnerabilities
    }

    private fun checkForSecretsAndCredentials(snippet: CodeSnippet): List<SecurityVulnerability> {
        val vulnerabilities = mutableListOf<SecurityVulnerability>()
        val content = snippet.content
        
        // API keys
        if (content.contains(Regex("(api[_-]?key|apikey)\\s*[=:]\\s*['\"][a-zA-Z0-9]{20,}['\"]", RegexOption.IGNORE_CASE))) {
            vulnerabilities.add(
                SecurityVulnerability(
                    type = VulnerabilityType.EXPOSED_SECRETS,
                    severity = Severity.CRITICAL,
                    description = "API key detected in code",
                    line = findLineNumber(content, "api[_-]?key|apikey")
                )
            )
        }
        
        // Database passwords
        if (content.contains(Regex("(password|pwd)\\s*[=:]\\s*['\"][^'\"]{6,}['\"]", RegexOption.IGNORE_CASE))) {
            vulnerabilities.add(
                SecurityVulnerability(
                    type = VulnerabilityType.EXPOSED_SECRETS,
                    severity = Severity.HIGH,
                    description = "Hardcoded password detected",
                    line = findLineNumber(content, "password|pwd")
                )
            )
        }
        
        // JWT tokens
        if (content.contains(Regex("eyJ[a-zA-Z0-9_-]+\\.[a-zA-Z0-9_-]+\\.[a-zA-Z0-9_-]+"))) {
            vulnerabilities.add(
                SecurityVulnerability(
                    type = VulnerabilityType.EXPOSED_SECRETS,
                    severity = Severity.HIGH,
                    description = "JWT token detected in code",
                    line = findLineNumber(content, "eyJ[a-zA-Z0-9_-]+")
                )
            )
        }
        
        return vulnerabilities
    }

    private fun checkForMaliciousPatterns(snippet: CodeSnippet): List<SecurityVulnerability> {
        val vulnerabilities = mutableListOf<SecurityVulnerability>()
        val content = snippet.content.lowercase()
        
        // Suspicious network activity
        if (content.contains(Regex("(socket|http).*connect.*\\d+\\.\\d+\\.\\d+\\.\\d+"))) {
            vulnerabilities.add(
                SecurityVulnerability(
                    type = VulnerabilityType.SUSPICIOUS_NETWORK,
                    severity = Severity.MEDIUM,
                    description = "Suspicious network connection detected",
                    line = findLineNumber(snippet.content, "socket|http.*connect")
                )
            )
        }
        
        // File system access
        if (content.contains(Regex("(file|path).*delete|rm\\s+-rf|format\\s+c:"))) {
            vulnerabilities.add(
                SecurityVulnerability(
                    type = VulnerabilityType.DESTRUCTIVE_OPERATIONS,
                    severity = Severity.HIGH,
                    description = "Potentially destructive file operations detected",
                    line = findLineNumber(snippet.content, "delete|rm\\s+-rf|format")
                )
            )
        }
        
        return vulnerabilities
    }

    private fun checkForSecurityWarnings(snippet: CodeSnippet): List<SecurityWarning> {
        val warnings = mutableListOf<SecurityWarning>()
        val content = snippet.content.lowercase()
        
        // Insecure random
        if (content.contains("math.random") || content.contains("random()")) {
            warnings.add(
                SecurityWarning(
                    type = "WEAK_RANDOM",
                    description = "Using weak random number generator",
                    recommendation = "Use cryptographically secure random number generator"
                )
            )
        }
        
        // HTTP instead of HTTPS
        if (content.contains("http://")) {
            warnings.add(
                SecurityWarning(
                    type = "INSECURE_PROTOCOL",
                    description = "Using HTTP instead of HTTPS",
                    recommendation = "Use HTTPS for secure communication"
                )
            )
        }
        
        return warnings
    }

    private fun calculateRiskLevel(
        vulnerabilities: List<SecurityVulnerability>,
        warnings: List<SecurityWarning>
    ): RiskLevel {
        val criticalCount = vulnerabilities.count { it.severity == Severity.CRITICAL }
        val highCount = vulnerabilities.count { it.severity == Severity.HIGH }
        val mediumCount = vulnerabilities.count { it.severity == Severity.MEDIUM }
        
        return when {
            criticalCount > 0 -> RiskLevel.CRITICAL
            highCount > 2 -> RiskLevel.HIGH
            highCount > 0 || mediumCount > 3 -> RiskLevel.MEDIUM
            mediumCount > 0 || warnings.isNotEmpty() -> RiskLevel.LOW
            else -> RiskLevel.SAFE
        }
    }

    private fun generateRecommendations(
        vulnerabilities: List<SecurityVulnerability>,
        warnings: List<SecurityWarning>
    ): List<String> {
        val recommendations = mutableListOf<String>()
        
        if (vulnerabilities.any { it.type == VulnerabilityType.SQL_INJECTION }) {
            recommendations.add("Use parameterized queries or prepared statements to prevent SQL injection")
        }
        
        if (vulnerabilities.any { it.type == VulnerabilityType.XSS }) {
            recommendations.add("Sanitize user input and use proper output encoding to prevent XSS")
        }
        
        if (vulnerabilities.any { it.type == VulnerabilityType.EXPOSED_SECRETS }) {
            recommendations.add("Remove hardcoded secrets and use environment variables or secure vaults")
        }
        
        if (vulnerabilities.any { it.type == VulnerabilityType.COMMAND_INJECTION }) {
            recommendations.add("Validate and sanitize input before executing system commands")
        }
        
        return recommendations
    }

    private fun findLineNumber(content: String, pattern: String): Int {
        val lines = content.split("\n")
        val regex = Regex(pattern, RegexOption.IGNORE_CASE)
        
        lines.forEachIndexed { index, line ->
            if (regex.containsMatchIn(line)) {
                return index + 1
            }
        }
        return 1
    }
}

// Data classes for security scanning
data class SecurityScanResult(
    val snippetId: Long,
    val riskLevel: RiskLevel,
    val vulnerabilities: List<SecurityVulnerability>,
    val warnings: List<SecurityWarning>,
    val scanTimestamp: java.time.LocalDateTime,
    val recommendations: List<String>
)

data class SecurityVulnerability(
    val type: VulnerabilityType,
    val severity: Severity,
    val description: String,
    val line: Int
)

data class SecurityWarning(
    val type: String,
    val description: String,
    val recommendation: String
)

enum class VulnerabilityType {
    SQL_INJECTION, XSS, COMMAND_INJECTION, EXPOSED_SECRETS, 
    SUSPICIOUS_NETWORK, DESTRUCTIVE_OPERATIONS
}

enum class Severity {
    LOW, MEDIUM, HIGH, CRITICAL
}

enum class RiskLevel {
    SAFE, LOW, MEDIUM, HIGH, CRITICAL
}