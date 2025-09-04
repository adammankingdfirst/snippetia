package com.snippetia.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.snippetia.presentation.component.*
import com.snippetia.presentation.util.formatCount
import com.snippetia.presentation.util.formatTimeAgo
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToSnippets: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToEvents: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Analytics", "Activity", "Recommendations")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Dashboard,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Dashboard",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Welcome back! Here's what's happening.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Tab Row
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }

        // Content
        when (selectedTab) {
            0 -> OverviewTab(
                onNavigateToSnippets = onNavigateToSnippets,
                onNavigateToProfile = onNavigateToProfile,
                onNavigateToEvents = onNavigateToEvents
            )
            1 -> AnalyticsTab(onNavigateToAnalytics = onNavigateToAnalytics)
            2 -> ActivityTab()
            3 -> RecommendationsTab()
        }
    }
}

@Composable
private fun OverviewTab(
    onNavigateToSnippets: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToEvents: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Stats
        item {
            QuickStatsSection()
        }

        // Quick Actions
        item {
            QuickActionsSection(
                onNavigateToSnippets = onNavigateToSnippets,
                onNavigateToProfile = onNavigateToProfile,
                onNavigateToEvents = onNavigateToEvents
            )
        }

        // Recent Activity
        item {
            RecentActivitySection()
        }

        // Trending Content
        item {
            TrendingContentSection()
        }

        // Achievements
        item {
            AchievementsSection()
        }
    }
}

@Composable
private fun QuickStatsSection(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Your Stats",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(200.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(quickStats) { stat ->
                    StatCard(
                        icon = stat.icon,
                        title = stat.title,
                        value = stat.value,
                        change = stat.change,
                        color = stat.color
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    title: String,
    value: String,
    change: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (change.isNotEmpty()) {
                Text(
                    text = change,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (change.startsWith("+")) Color.Green else Color.Red
                )
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    onNavigateToSnippets: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToEvents: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(quickActions) { action ->
                    QuickActionCard(
                        icon = action.icon,
                        title = action.title,
                        description = action.description,
                        onClick = when (action.title) {
                            "Create Snippet" -> onNavigateToSnippets
                            "View Profile" -> onNavigateToProfile
                            "Browse Events" -> onNavigateToEvents
                            else -> { }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickActionCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.width(160.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun RecentActivitySection(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                TextButton(onClick = { /* Navigate to full activity */ }) {
                    Text("View All")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            recentActivities.forEach { activity ->
                ActivityItem(
                    icon = activity.icon,
                    title = activity.title,
                    description = activity.description,
                    timestamp = activity.timestamp,
                    color = activity.color
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ActivityItem(
    icon: ImageVector,
    title: String,
    description: String,
    timestamp: LocalDateTime,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier
                .size(32.dp)
                .background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                .padding(6.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Text(
            text = formatTimeAgo(timestamp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TrendingContentSection(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Trending Now",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            TrendingTopics(
                topics = listOf("kotlin", "compose", "android", "spring", "react"),
                onTopicClick = { /* Handle topic click */ }
            )
        }
    }
}

@Composable
private fun AchievementsSection(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    text = "Achievements",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                TextButton(onClick = { /* Navigate to achievements */ }) {
                    Text("View All")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(achievements) { achievement ->
                    AchievementBadge(
                        title = achievement.title,
                        icon = achievement.icon,
                        isUnlocked = achievement.isUnlocked,
                        progress = achievement.progress
                    )
                }
            }
        }
    }
}

@Composable
private fun AchievementBadge(
    title: String,
    icon: ImageVector,
    isUnlocked: Boolean,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.size(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isUnlocked) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    },
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isUnlocked) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    }
                )
            }
            
            if (!isUnlocked && progress > 0) {
                CircularProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
private fun AnalyticsTab(
    onNavigateToAnalytics: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Analytics content would go here
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Analytics,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Analytics Dashboard",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onNavigateToAnalytics) {
                Text("View Detailed Analytics")
            }
        }
    }
}

@Composable
private fun ActivityTab(
    modifier: Modifier = Modifier
) {
    // Activity feed would go here
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(recentActivities) { activity ->
            ActivityItem(
                icon = activity.icon,
                title = activity.title,
                description = activity.description,
                timestamp = activity.timestamp,
                color = activity.color
            )
        }
    }
}

@Composable
private fun RecommendationsTab(
    modifier: Modifier = Modifier
) {
    // Recommendations would go here
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(recommendations) { recommendation ->
            RecommendationCard(
                title = recommendation.title,
                description = recommendation.description,
                action = recommendation.action,
                onActionClick = { /* Handle action */ }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecommendationCard(
    title: String,
    description: String,
    action: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onActionClick) {
                    Text(action)
                }
            }
        }
    }
}

// Sample data
private data class QuickStat(
    val icon: ImageVector,
    val title: String,
    val value: String,
    val change: String,
    val color: Color
)

private data class QuickAction(
    val icon: ImageVector,
    val title: String,
    val description: String
)

private data class ActivityItem(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val timestamp: LocalDateTime,
    val color: Color
)

private data class Achievement(
    val title: String,
    val icon: ImageVector,
    val isUnlocked: Boolean,
    val progress: Float
)

private data class Recommendation(
    val title: String,
    val description: String,
    val action: String
)

private val quickStats = listOf(
    QuickStat(Icons.Default.Code, "Snippets", "42", "+5", Color(0xFF4CAF50)),
    QuickStat(Icons.Default.Star, "Stars", "128", "+12", Color(0xFFFFD700)),
    QuickStat(Icons.Default.People, "Followers", "89", "+3", Color(0xFF2196F3)),
    QuickStat(Icons.Default.Visibility, "Views", "1.2K", "+89", Color(0xFF9C27B0))
)

private val quickActions = listOf(
    QuickAction(Icons.Default.Add, "Create Snippet", "Share your code"),
    QuickAction(Icons.Default.Person, "View Profile", "Check your profile"),
    QuickAction(Icons.Default.Event, "Browse Events", "Find events"),
    QuickAction(Icons.Default.Analytics, "View Analytics", "See your stats")
)

private val recentActivities = listOf(
    ActivityItem(
        Icons.Default.Star,
        "New Star",
        "Someone starred your Kotlin snippet",
        LocalDateTime.now().minusHours(2),
        Color(0xFFFFD700)
    ),
    ActivityItem(
        Icons.Default.PersonAdd,
        "New Follower",
        "john_dev started following you",
        LocalDateTime.now().minusHours(5),
        Color(0xFF2196F3)
    ),
    ActivityItem(
        Icons.Default.Code,
        "Snippet Created",
        "You created 'React Hooks Example'",
        LocalDateTime.now().minusDays(1),
        Color(0xFF4CAF50)
    )
)

private val achievements = listOf(
    Achievement("First Star", Icons.Default.Star, true, 1.0f),
    Achievement("10 Snippets", Icons.Default.Code, true, 1.0f),
    Achievement("100 Views", Icons.Default.Visibility, false, 0.7f),
    Achievement("Contributor", Icons.Default.EmojiEvents, false, 0.3f)
)

private val recommendations = listOf(
    Recommendation(
        "Complete Your Profile",
        "Add a bio and profile picture to get more followers",
        "Update Profile"
    ),
    Recommendation(
        "Try AI Assistant",
        "Get help with code analysis and optimization",
        "Open AI Chat"
    ),
    Recommendation(
        "Join an Event",
        "Participate in upcoming coding events and workshops",
        "Browse Events"
    )
)