package com.snippetia.presentation.util

import kotlinx.datetime.*
import kotlin.math.abs

fun formatTimeAgo(dateTime: LocalDateTime): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val duration = now.toInstant(TimeZone.currentSystemDefault()) - 
                   dateTime.toInstant(TimeZone.currentSystemDefault())
    
    val seconds = duration.inWholeSeconds
    val minutes = duration.inWholeMinutes
    val hours = duration.inWholeHours
    val days = duration.inWholeDays
    
    return when {
        seconds < 60 -> "just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        days < 30 -> "${days / 7}w ago"
        days < 365 -> "${days / 30}mo ago"
        else -> "${days / 365}y ago"
    }
}

fun formatCount(count: Long): String {
    return when {
        count < 1000 -> count.toString()
        count < 1000000 -> {
            val formatted = count / 1000.0
            if (formatted % 1 == 0.0) "${formatted.toInt()}K" else "%.1fK".format(formatted)
        }
        count < 1000000000 -> {
            val formatted = count / 1000000.0
            if (formatted % 1 == 0.0) "${formatted.toInt()}M" else "%.1fM".format(formatted)
        }
        else -> {
            val formatted = count / 1000000000.0
            if (formatted % 1 == 0.0) "${formatted.toInt()}B" else "%.1fB".format(formatted)
        }
    }
}

fun formatFileSize(bytes: Long): String {
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var size = bytes.toDouble()
    var unitIndex = 0
    
    while (size >= 1024 && unitIndex < units.size - 1) {
        size /= 1024
        unitIndex++
    }
    
    return if (size % 1 == 0.0) {
        "${size.toInt()} ${units[unitIndex]}"
    } else {
        "%.1f %s".format(size, units[unitIndex])
    }
}

fun formatDuration(milliseconds: Long): String {
    val seconds = milliseconds / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    
    return when {
        milliseconds < 1000 -> "${milliseconds}ms"
        seconds < 60 -> "${seconds}s"
        minutes < 60 -> "${minutes}m ${seconds % 60}s"
        hours < 24 -> "${hours}h ${minutes % 60}m"
        else -> "${days}d ${hours % 24}h"
    }
}

fun formatPercentage(value: Double, decimals: Int = 1): String {
    return "%.${decimals}f%%".format(value * 100)
}

fun formatCurrency(amount: Double, currency: String = "USD"): String {
    return when (currency.uppercase()) {
        "USD" -> "$%.2f".format(amount)
        "EUR" -> "€%.2f".format(amount)
        "GBP" -> "£%.2f".format(amount)
        "JPY" -> "¥%.0f".format(amount)
        else -> "$amount $currency"
    }
}

fun formatVersion(version: String): String {
    // Ensure version follows semantic versioning format
    val parts = version.split(".")
    return when (parts.size) {
        1 -> "${parts[0]}.0.0"
        2 -> "${parts[0]}.${parts[1]}.0"
        else -> version
    }
}

fun formatLanguageName(language: String): String {
    return when (language.lowercase()) {
        "js" -> "JavaScript"
        "ts" -> "TypeScript"
        "py" -> "Python"
        "rb" -> "Ruby"
        "cpp", "c++" -> "C++"
        "cs", "csharp" -> "C#"
        "kt" -> "Kotlin"
        "java" -> "Java"
        "swift" -> "Swift"
        "go" -> "Go"
        "rust", "rs" -> "Rust"
        "php" -> "PHP"
        "html" -> "HTML"
        "css" -> "CSS"
        "scss" -> "SCSS"
        "sass" -> "Sass"
        "sql" -> "SQL"
        "sh", "bash" -> "Shell"
        "ps1", "powershell" -> "PowerShell"
        "json" -> "JSON"
        "xml" -> "XML"
        "yaml", "yml" -> "YAML"
        "md", "markdown" -> "Markdown"
        "dockerfile" -> "Dockerfile"
        else -> language.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}

fun truncateText(text: String, maxLength: Int, ellipsis: String = "..."): String {
    return if (text.length <= maxLength) {
        text
    } else {
        text.take(maxLength - ellipsis.length) + ellipsis
    }
}

fun formatHashtag(tag: String): String {
    return if (tag.startsWith("#")) tag else "#$tag"
}

fun formatUsername(username: String): String {
    return if (username.startsWith("@")) username else "@$username"
}

fun formatRepositoryUrl(url: String): String {
    return url.removePrefix("https://")
        .removePrefix("http://")
        .removePrefix("www.")
}

fun formatCommitHash(hash: String, length: Int = 7): String {
    return hash.take(length)
}

fun pluralize(count: Long, singular: String, plural: String? = null): String {
    val pluralForm = plural ?: "${singular}s"
    return if (count == 1L) "$count $singular" else "$count $pluralForm"
}

fun formatScore(score: Long): String {
    return when {
        score < 0 -> "−${formatCount(abs(score))}"
        else -> formatCount(score)
    }
}

fun formatRating(rating: Double, maxRating: Int = 5): String {
    val stars = "★".repeat(rating.toInt()) + "☆".repeat(maxRating - rating.toInt())
    return "$stars (%.1f)".format(rating)
}