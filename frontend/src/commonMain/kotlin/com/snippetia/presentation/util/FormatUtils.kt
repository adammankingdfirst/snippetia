package com.snippetia.presentation.util

import kotlinx.datetime.*

fun formatTimeAgo(dateTime: LocalDateTime): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val duration = now.toInstant(TimeZone.currentSystemDefault()) - dateTime.toInstant(TimeZone.currentSystemDefault())
    
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
            val value = count / 1000.0
            if (value % 1 == 0.0) "${value.toInt()}K" else String.format("%.1fK", value)
        }
        else -> {
            val value = count / 1000000.0
            if (value % 1 == 0.0) "${value.toInt()}M" else String.format("%.1fM", value)
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
        String.format("%.1f %s", size, units[unitIndex])
    }
}