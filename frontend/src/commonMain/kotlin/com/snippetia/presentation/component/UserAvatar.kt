package com.snippetia.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

@Composable
fun UserAvatar(
    avatarUrl: String?,
    username: String,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier,
    showBorder: Boolean = false,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null
) {
    val avatarModifier = modifier
        .size(size)
        .clip(CircleShape)
        .let { mod ->
            if (showBorder) {
                mod.border(borderWidth, borderColor, CircleShape)
            } else mod
        }
        .let { mod ->
            if (onClick != null) {
                mod.clickable { onClick() }
            } else mod
        }

    if (!avatarUrl.isNullOrEmpty()) {
        KamelImage(
            resource = asyncPainterResource(avatarUrl),
            contentDescription = "$username's avatar",
            modifier = avatarModifier,
            contentScale = ContentScale.Crop,
            onFailure = {
                // Fallback to initials avatar
                InitialsAvatar(
                    username = username,
                    size = size,
                    modifier = modifier
                )
            }
        )
    } else {
        InitialsAvatar(
            username = username,
            size = size,
            modifier = avatarModifier
        )
    }
}

@Composable
private fun InitialsAvatar(
    username: String,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val initials = username.take(2).uppercase()
    val backgroundColor = getAvatarColor(username)
    
    Box(
        modifier = modifier
            .size(size)
            .background(backgroundColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = Color.White,
            fontSize = (size.value * 0.4).sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun UserAvatarWithStatus(
    avatarUrl: String?,
    username: String,
    isOnline: Boolean = false,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Box(modifier = modifier) {
        UserAvatar(
            avatarUrl = avatarUrl,
            username = username,
            size = size,
            onClick = onClick
        )
        
        if (isOnline) {
            Box(
                modifier = Modifier
                    .size(size * 0.3f)
                    .background(
                        MaterialTheme.colorScheme.surface,
                        CircleShape
                    )
                    .padding(2.dp)
                    .background(
                        Color(0xFF22C55E),
                        CircleShape
                    )
                    .align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
fun UserAvatarGroup(
    avatars: List<Pair<String?, String>>, // (avatarUrl, username)
    maxVisible: Int = 3,
    size: Dp = 32.dp,
    modifier: Modifier = Modifier,
    overlap: Dp = 8.dp
) {
    val visibleAvatars = avatars.take(maxVisible)
    val remainingCount = avatars.size - maxVisible
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(-overlap)
    ) {
        visibleAvatars.forEachIndexed { index, (avatarUrl, username) ->
            UserAvatar(
                avatarUrl = avatarUrl,
                username = username,
                size = size,
                showBorder = true,
                borderColor = MaterialTheme.colorScheme.surface,
                borderWidth = 2.dp,
                modifier = Modifier.zIndex((maxVisible - index).toFloat())
            )
        }
        
        if (remainingCount > 0) {
            Box(
                modifier = Modifier
                    .size(size)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    )
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.surface,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+$remainingCount",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun getAvatarColor(username: String): Color {
    val colors = listOf(
        Color(0xFF6366F1), // Indigo
        Color(0xFF8B5CF6), // Violet
        Color(0xFFEC4899), // Pink
        Color(0xFFEF4444), // Red
        Color(0xFFF97316), // Orange
        Color(0xFFF59E0B), // Amber
        Color(0xFFEAB308), // Yellow
        Color(0xFF84CC16), // Lime
        Color(0xFF22C55E), // Green
        Color(0xFF10B981), // Emerald
        Color(0xFF14B8A6), // Teal
        Color(0xFF06B6D4), // Cyan
        Color(0xFF0EA5E9), // Sky
        Color(0xFF3B82F6), // Blue
    )
    
    val hash = username.hashCode()
    val index = kotlin.math.abs(hash) % colors.size
    return colors[index]
}