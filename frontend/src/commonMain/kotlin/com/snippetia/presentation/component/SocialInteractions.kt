package com.snippetia.presentation.component

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snippetia.presentation.util.formatCount
import com.snippetia.presentation.util.formatTimeAgo
import java.time.LocalDateTime

@Composable
fun SocialInteractionBar(
    isStarred: Boolean,
    starCount: Long,
    isFollowing: Boolean,
    viewCount: Long,
    forkCount: Long,
    onStarClick: () -> Unit,
    onFollowClick: () -> Unit,
    onShareClick: () -> Unit,
    onForkClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Star Button
            SocialActionButton(
                icon = if (isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                count = starCount,
                isActive = isStarred,
                activeColor = Color(0xFFFFD700), // Gold
                onClick = onStarClick,
                contentDescription = if (isStarred) "Unstar" else "Star"
            )

            // Follow Button
            SocialActionButton(
                icon = if (isFollowing) Icons.Filled.PersonRemove else Icons.Filled.PersonAdd,
                text = if (isFollowing) "Following" else "Follow",
                isActive = isFollowing,
                activeColor = MaterialTheme.colorScheme.primary,
                onClick = onFollowClick,
                contentDescription = if (isFollowing) "Unfollow" else "Follow"
            )

            // View Count
            SocialActionButton(
                icon = Icons.Outlined.Visibility,
                count = viewCount,
                isActive = false,
                onClick = { },
                contentDescription = "Views",
                enabled = false
            )

            // Fork Button
            SocialActionButton(
                icon = Icons.Outlined.CallSplit,
                count = forkCount,
                isActive = false,
                onClick = onForkClick,
                contentDescription = "Fork"
            )

            // Share Button
            SocialActionButton(
                icon = Icons.Outlined.Share,
                isActive = false,
                onClick = onShareClick,
                contentDescription = "Share"
            )
        }
    }
}

@Composable
private fun SocialActionButton(
    icon: ImageVector,
    count: Long? = null,
    text: String? = null,
    isActive: Boolean,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
    contentDescription: String,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val buttonColor = if (isActive) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
    val backgroundColor = if (isActive) activeColor.copy(alpha = 0.1f) else Color.Transparent

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        AnimatedContent(
            targetState = isActive,
            transitionSpec = {
                scaleIn() + fadeIn() with scaleOut() + fadeOut()
            }
        ) { active ->
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (active) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        when {
            count != null -> {
                Text(
                    text = formatCount(count),
                    style = MaterialTheme.typography.labelSmall,
                    color = buttonColor,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                )
            }
            text != null -> {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall,
                    color = buttonColor,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun NotificationBadge(
    count: Long,
    modifier: Modifier = Modifier
) {
    if (count > 0) {
        Badge(
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.error
        ) {
            Text(
                text = if (count > 99) "99+" else count.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onError
            )
        }
    }
}

@Composable
fun NotificationList(
    notifications: List<NotificationItem>,
    onNotificationClick: (NotificationItem) -> Unit,
    onMarkAsRead: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(notifications) { notification ->
            NotificationCard(
                notification = notification,
                onClick = { onNotificationClick(notification) },
                onMarkAsRead = { onMarkAsRead(notification.id) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationCard(
    notification: NotificationItem,
    onClick: () -> Unit,
    onMarkAsRead: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Notification Icon
            Icon(
                imageVector = getNotificationIcon(notification.type),
                contentDescription = null,
                tint = getNotificationColor(notification.type),
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        getNotificationColor(notification.type).copy(alpha = 0.1f),
                        CircleShape
                    )
                    .padding(4.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTimeAgo(notification.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (notification.priority == "HIGH" || notification.priority == "URGENT") {
                        Spacer(modifier = Modifier.width(8.dp))
                        Badge(
                            containerColor = if (notification.priority == "URGENT") {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.tertiary
                            }
                        ) {
                            Text(
                                text = notification.priority,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            // Mark as read button
            if (!notification.isRead) {
                IconButton(
                    onClick = onMarkAsRead,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MarkEmailRead,
                        contentDescription = "Mark as read",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FollowersList(
    followers: List<UserSummary>,
    onUserClick: (UserSummary) -> Unit,
    onFollowClick: (UserSummary) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(followers) { user ->
            UserFollowCard(
                user = user,
                onUserClick = { onUserClick(user) },
                onFollowClick = { onFollowClick(user) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserFollowCard(
    user: UserSummary,
    onUserClick: () -> Unit,
    onFollowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onUserClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                avatarUrl = user.avatarUrl,
                displayName = user.displayName,
                size = 48.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "@${user.username}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onFollowClick,
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
            ) {
                Text(
                    text = if (user.isFollowing) "Following" else "Follow",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
fun TrendingTopics(
    topics: List<String>,
    onTopicClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "Trending Topics",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(topics) { topic ->
                TrendingTopicChip(
                    topic = topic,
                    onClick = { onTopicClick(topic) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrendingTopicChip(
    topic: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        onClick = onClick,
        label = { Text("#$topic") },
        selected = false,
        modifier = modifier,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }
    )
}

// Helper functions
private fun getNotificationIcon(type: String): ImageVector {
    return when (type) {
        "NEW_SNIPPET" -> Icons.Default.Code
        "NEW_FOLLOWER" -> Icons.Default.PersonAdd
        "SNIPPET_LIKED" -> Icons.Default.Favorite
        "SNIPPET_COMMENTED" -> Icons.Default.Comment
        "SUBSCRIPTION_CREATED" -> Icons.Default.Payment
        "EVENT_REMINDER" -> Icons.Default.Event
        "SYSTEM_ANNOUNCEMENT" -> Icons.Default.Announcement
        "SECURITY_ALERT" -> Icons.Default.Security
        "VCS_PUSH" -> Icons.Default.Upload
        "VCS_MERGE_REQUEST" -> Icons.Default.MergeType
        "VCS_ISSUE_CREATED" -> Icons.Default.BugReport
        else -> Icons.Default.Notifications
    }
}

private fun getNotificationColor(type: String): Color {
    return when (type) {
        "NEW_SNIPPET" -> Color(0xFF4CAF50)
        "NEW_FOLLOWER" -> Color(0xFF2196F3)
        "SNIPPET_LIKED" -> Color(0xFFE91E63)
        "SNIPPET_COMMENTED" -> Color(0xFF9C27B0)
        "SUBSCRIPTION_CREATED" -> Color(0xFFFF9800)
        "EVENT_REMINDER" -> Color(0xFF00BCD4)
        "SYSTEM_ANNOUNCEMENT" -> Color(0xFF607D8B)
        "SECURITY_ALERT" -> Color(0xFFF44336)
        "VCS_PUSH" -> Color(0xFF8BC34A)
        "VCS_MERGE_REQUEST" -> Color(0xFF3F51B5)
        "VCS_ISSUE_CREATED" -> Color(0xFFFF5722)
        else -> Color(0xFF9E9E9E)
    }
}

// Data classes
data class NotificationItem(
    val id: Long,
    val type: String,
    val title: String,
    val message: String,
    val isRead: Boolean,
    val actionUrl: String?,
    val priority: String,
    val createdAt: LocalDateTime
)

data class UserSummary(
    val id: Long,
    val username: String,
    val displayName: String,
    val avatarUrl: String?,
    val isFollowing: Boolean = false
)