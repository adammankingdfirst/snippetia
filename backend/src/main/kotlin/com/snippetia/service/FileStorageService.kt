package com.snippetia.service

import org.springframework.stereotype.Service

@Service
class FileStorageService {

    fun detectLanguage(filename: String): String {
        return when (filename.substringAfterLast('.', "").lowercase()) {
            "js" -> "javascript"
            "ts" -> "typescript"
            "py" -> "python"
            "java" -> "java"
            "kt" -> "kotlin"
            "cpp", "cc", "cxx" -> "cpp"
            "c" -> "c"
            "cs" -> "csharp"
            "go" -> "go"
            "rs" -> "rust"
            "php" -> "php"
            "rb" -> "ruby"
            "swift" -> "swift"
            "scala" -> "scala"
            "sh" -> "bash"
            "sql" -> "sql"
            "html" -> "html"
            "css" -> "css"
            "json" -> "json"
            "xml" -> "xml"
            "yaml", "yml" -> "yaml"
            "md" -> "markdown"
            else -> "text"
        }
    }
}