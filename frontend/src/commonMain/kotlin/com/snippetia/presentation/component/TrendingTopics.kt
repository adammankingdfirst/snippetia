package com.snippetia.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendingTopics(
    topics: List<String>,
    onTopicClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(topics) { topic ->
            FilterChip(
                onClick = { onTopicClick(topic) },
                label = { 
                    Text(
                        text = "#$topic",
                        style = MaterialTheme.typography.labelMedium
                    ) 
                },
                selected = false,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
        }
    }
}

@Composable
fun TrendingTopicsGrid(
    topics: List<TrendingTopic>,
    onTopicClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        topics.forEach { topic ->
            TrendingTopicCard(
                topic = topic,
                onClick = { onTopicClick(topic.name) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrendingTopicCard(
    topic: TrendingTopic,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "#${topic.name}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${topic.snippetCount} snippets",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = topic.trendingIndicator,
                style = MaterialTheme.typography.labelSmall,
                color = when {
                    topic.trendingIndicator.startsWith("↗") -> MaterialTheme.colorScheme.primary
                    topic.trendingIndicator.startsWith("↘") -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

data class TrendingTopic(
    val name: String,
    val snippetCount: Int,
    val trendingIndicator: String, // e.g., "↗ +15%", "↘ -5%", "→ 0%"
    val category: String? = null
)