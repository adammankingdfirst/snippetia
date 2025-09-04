package com.snippetia.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.snippetia.presentation.util.formatTimeAgo
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModerationScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Pending Reports", "Resolved", "Statistics")

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Moderation Dashboard") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

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

        // Content
        when (selectedTab) {
            0 -> PendingReportsTab()
            1 -> ResolvedReportsTab()
            2 -> ModerationStatsTab()
        }
    }
}

@Composable
private fun PendingReportsTab(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(mockPendingReports) { report ->
            ReportCard(
                report = report,
                onApprove = { /* Handle approve */ },
                onReject = { /* Handle reject */ },
                onTakeAction = { /* Handle take action */ }
            )
        }
    }
}

@Composable
private fun ResolvedReportsTab(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(mockResolvedReports) { report ->
            ResolvedReportCard(report = report)
        }
    }
}

@Composable
private fun ModerationStatsTab(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            StatsOverviewCard()
        }
        
        item {
            RecentActivityCard()
        }
        
        item {
            ModerationTrendsCard()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportCard(
    report: ModerationReport,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onTakeAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (report.contentType) {
                            "snippet" -> Icons.Default.Code
                            "comment" -> Icons.Default.Comment
                            else -> Icons.Default.Report
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = report.contentType.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                AssistChip(
                    onClick = { },
                    label = { Text(report.reason) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = when (report.reason) {
                            "spam" -> Color(0xFFFFEBEE)
                            "inappropriate" -> Color(0xFFFFF3E0)
                            "harassment" -> Color(0xFFE8F5E8)
                            else -> MaterialTheme.colorScheme.secondaryContainer
                        }
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Content
            Text(
                text = "Reported by: ${report.reporterName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (report.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = report.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = formatTimeAgo(report.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reject")
                }
                
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Approve")
                }
                
                Button(
                    onClick = onTakeAction,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Action")
                }
            }
        }
    }
}

@Composable
private fun ResolvedReportCard(
    report: ModerationReport,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                Text(
                    text = "${report.contentType.replaceFirstChar { it.uppercase() }} Report",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                AssistChip(
                    onClick = { },
                    label = { Text(report.status) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = when (report.status) {
                            "resolved" -> Color(0xFFE8F5E8)
                            "dismissed" -> Color(0xFFFFEBEE)
                            else -> MaterialTheme.colorScheme.secondaryContainer
                        }
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Reason: ${report.reason}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "Resolved: ${formatTimeAgo(report.resolvedAt ?: report.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatsOverviewCard(
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
                text = "Moderation Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    title = "Pending",
                    value = "12",
                    color = MaterialTheme.colorScheme.error
                )
                StatItem(
                    title = "Resolved Today",
                    value = "8",
                    color = MaterialTheme.colorScheme.primary
                )
                StatItem(
                    title = "Total Reports",
                    value = "156",
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RecentActivityCard(
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
                text = "Recent Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            mockRecentActivity.forEach { activity ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = activity.icon,
                        contentDescription = null,
                        tint = activity.color,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = activity.description,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formatTimeAgo(activity.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ModerationTrendsCard(
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
                text = "Trends",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "• Spam reports decreased by 15% this week",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF4CAF50)
            )
            Text(
                text = "• Average resolution time: 2.3 hours",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "• Most common reason: Inappropriate content",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Sample data
data class ModerationReport(
    val id: Long,
    val contentType: String,
    val contentId: Long,
    val reason: String,
    val description: String,
    val reporterName: String,
    val status: String,
    val createdAt: LocalDateTime,
    val resolvedAt: LocalDateTime? = null
)

data class ModerationActivity(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val description: String,
    val timestamp: LocalDateTime,
    val color: Color
)

private val mockPendingReports = listOf(
    ModerationReport(
        id = 1,
        contentType = "snippet",
        contentId = 123,
        reason = "spam",
        description = "This snippet contains promotional content",
        reporterName = "user123",
        status = "pending",
        createdAt = LocalDateTime.now().minusHours(2)
    ),
    ModerationReport(
        id = 2,
        contentType = "comment",
        contentId = 456,
        reason = "inappropriate",
        description = "Offensive language in comment",
        reporterName = "developer456",
        status = "pending",
        createdAt = LocalDateTime.now().minusHours(5)
    )
)

private val mockResolvedReports = listOf(
    ModerationReport(
        id = 3,
        contentType = "snippet",
        contentId = 789,
        reason = "copyright",
        description = "Code copied without attribution",
        reporterName = "coder789",
        status = "resolved",
        createdAt = LocalDateTime.now().minusDays(1),
        resolvedAt = LocalDateTime.now().minusHours(12)
    )
)

private val mockRecentActivity = listOf(
    ModerationActivity(
        icon = Icons.Default.Check,
        description = "Report #123 resolved",
        timestamp = LocalDateTime.now().minusMinutes(30),
        color = Color(0xFF4CAF50)
    ),
    ModerationActivity(
        icon = Icons.Default.Block,
        description = "Content hidden for policy violation",
        timestamp = LocalDateTime.now().minusHours(1),
        color = Color(0xFFFF9800)
    ),
    ModerationActivity(
        icon = Icons.Default.Report,
        description = "New spam report received",
        timestamp = LocalDateTime.now().minusHours(2),
        color = Color(0xFFF44336)
    )
)