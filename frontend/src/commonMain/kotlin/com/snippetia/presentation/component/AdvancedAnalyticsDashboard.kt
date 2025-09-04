package com.snippetia.presentation.component

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * Advanced Analytics Dashboard with real-time metrics, interactive charts, and AI insights
 */

@Composable
fun AdvancedAnalyticsDashboard(
    userId: Long,
    timeRange: AnalyticsTimeRange,
    onTimeRangeChange: (AnalyticsTimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    var analyticsData by remember { mutableStateOf<AnalyticsData?>(null) }
    var selectedMetric by remember { mutableStateOf<AnalyticsMetric?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var aiInsights by remember { mutableStateOf<List<AIInsight>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(userId, timeRange) {
        isLoading = true
        delay(1000) // Simulate API call
        analyticsData = generateAnalyticsData(timeRange)
        aiInsights = generateAIInsights(analyticsData!!)
        isLoading = false
    }
    
    if (isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            // Dashboard Header
            AnalyticsDashboardHeader(
                timeRange = timeRange,
                onTimeRangeChange = onTimeRangeChange,
                totalMetrics = analyticsData?.summary
            )
        }
        
        item {
            // Key Performance Indicators
            KPISection(
                kpis = analyticsData?.kpis ?: emptyList(),
                onKPIClick = { selectedMetric = it }
            )
        }
        
        item {
            // Interactive Charts Section
            InteractiveChartsSection(
                chartData = analyticsData?.chartData ?: emptyList(),
                selectedMetric = selectedMetric,
                onMetricSelect = { selectedMetric = it }
            )
        }
        
        item {
            // AI Insights Section
            AIInsightsSection(
                insights = aiInsights
            )
        }
        
        item {
            // Detailed Analytics Grid
            DetailedAnalyticsGrid(
                analytics = analyticsData?.detailedMetrics ?: emptyList()
            )
        }
        
        item {
            // Real-time Activity Feed
            RealTimeActivityFeed(
                activities = analyticsData?.recentActivities ?: emptyList()
            )
        }
    }
}

@Composable
private fun AnalyticsDashboardHeader(
    timeRange: AnalyticsTimeRange,
    onTimeRangeChange: (AnalyticsTimeRange) -> Unit,
    totalMetrics: AnalyticsSummary?
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Analytics Dashboard",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (totalMetrics != null) {
                        Text(
                            text = "Total Views: ${totalMetrics.totalViews} • Snippets: ${totalMetrics.totalSnippets}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Time Range Selector
                TimeRangeSelector(
                    selectedRange = timeRange,
                    onRangeChange = onTimeRangeChange
                )
            }
            
            if (totalMetrics != null) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    QuickStatCard(
                        title = "Growth Rate",
                        value = "${totalMetrics.growthRate}%",
                        trend = if (totalMetrics.growthRate > 0) TrendDirection.UP else TrendDirection.DOWN,
                        modifier = Modifier.weight(1f)
                    )
                    
                    QuickStatCard(
                        title = "Engagement",
                        value = "${totalMetrics.engagementRate}%",
                        trend = TrendDirection.UP,
                        modifier = Modifier.weight(1f)
                    )
                    
                    QuickStatCard(
                        title = "Active Users",
                        value = "${totalMetrics.activeUsers}",
                        trend = TrendDirection.UP,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeRangeSelector(
    selectedRange: AnalyticsTimeRange,
    onRangeChange: (AnalyticsTimeRange) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(AnalyticsTimeRange.values()) { range ->
            FilterChip(
                selected = selectedRange == range,
                onClick = { onRangeChange(range) },
                label = {
                    Text(
                        text = range.displayName,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            )
        }
    }
}

@Composable
private fun QuickStatCard(
    title: String,
    value: String,
    trend: TrendDirection,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Icon(
                    imageVector = when (trend) {
                        TrendDirection.UP -> Icons.Default.TrendingUp
                        TrendDirection.DOWN -> Icons.Default.TrendingDown
                        TrendDirection.STABLE -> Icons.Default.TrendingFlat
                    },
                    contentDescription = trend.name,
                    tint = when (trend) {
                        TrendDirection.UP -> Color(0xFF4CAF50)
                        TrendDirection.DOWN -> MaterialTheme.colorScheme.error
                        TrendDirection.STABLE -> MaterialTheme.colorScheme.outline
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun KPISection(
    kpis: List<KPIMetric>,
    onKPIClick: (AnalyticsMetric) -> Unit
) {
    Column {
        Text(
            text = "Key Performance Indicators",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(kpis) { kpi ->
                KPICard(
                    kpi = kpi,
                    onClick = { onKPIClick(kpi.metric) }
                )
            }
        }
    }
}

@Composable
private fun KPICard(
    kpi: KPIMetric,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = kpi.color.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, kpi.color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = kpi.icon,
                    contentDescription = kpi.title,
                    tint = kpi.color,
                    modifier = Modifier.size(24.dp)
                )
                
                Text(
                    text = "${kpi.changePercentage}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (kpi.changePercentage > 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = kpi.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = kpi.value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = kpi.color
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Mini chart
            MiniChart(
                data = kpi.trendData,
                color = kpi.color,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            )
        }
    }
}

@Composable
private fun MiniChart(
    data: List<Float>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas
        
        val maxValue = data.maxOrNull() ?: 1f
        val minValue = data.minOrNull() ?: 0f
        val range = maxValue - minValue
        
        val stepX = size.width / (data.size - 1).coerceAtLeast(1)
        
        val path = Path()
        data.forEachIndexed { index, value ->
            val x = index * stepX
            val y = size.height - ((value - minValue) / range) * size.height
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun InteractiveChartsSection(
    chartData: List<ChartData>,
    selectedMetric: AnalyticsMetric?,
    onMetricSelect: (AnalyticsMetric) -> Unit
) {
    Column {
        Text(
            text = "Interactive Charts",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        chartData.forEach { chart ->
            InteractiveChart(
                chartData = chart,
                isSelected = selectedMetric == chart.metric,
                onSelect = { onMetricSelect(chart.metric) }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun InteractiveChart(
    chartData: ChartData,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null
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
                    text = chartData.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = chartData.type.displayName,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            when (chartData.type) {
                ChartType.LINE -> LineChart(
                    data = chartData.dataPoints,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
                ChartType.BAR -> BarChart(
                    data = chartData.dataPoints,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
                ChartType.PIE -> PieChart(
                    data = chartData.dataPoints,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
                ChartType.AREA -> AreaChart(
                    data = chartData.dataPoints,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }
    }
}

@Composable
private fun LineChart(
    data: List<DataPoint>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas
        
        val maxValue = data.maxOfOrNull { it.value } ?: 1f
        val minValue = data.minOfOrNull { it.value } ?: 0f
        val range = maxValue - minValue
        
        val stepX = size.width / (data.size - 1).coerceAtLeast(1)
        
        val path = Path()
        data.forEachIndexed { index, point ->
            val x = index * stepX
            val y = size.height - ((point.value - minValue) / range) * size.height
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        
        // Draw line
        drawPath(
            path = path,
            color = Color(0xFF2196F3),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        
        // Draw points
        data.forEachIndexed { index, point ->
            val x = index * stepX
            val y = size.height - ((point.value - minValue) / range) * size.height
            
            drawCircle(
                color = Color(0xFF2196F3),
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}@Compos
able
private fun BarChart(
    data: List<DataPoint>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas
        
        val maxValue = data.maxOfOrNull { it.value } ?: 1f
        val barWidth = size.width / data.size * 0.8f
        val spacing = size.width / data.size * 0.2f
        
        data.forEachIndexed { index, point ->
            val barHeight = (point.value / maxValue) * size.height
            val x = index * (barWidth + spacing) + spacing / 2
            
            drawRect(
                color = Color(0xFF4CAF50),
                topLeft = Offset(x, size.height - barHeight),
                size = Size(barWidth, barHeight)
            )
        }
    }
}

@Composable
private fun PieChart(
    data: List<DataPoint>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas
        
        val total = data.sumOf { it.value.toDouble() }.toFloat()
        val center = Offset(size.width / 2, size.height / 2)
        val radius = minOf(size.width, size.height) / 2 * 0.8f
        
        var startAngle = -90f
        val colors = listOf(
            Color(0xFF2196F3),
            Color(0xFF4CAF50),
            Color(0xFFFF9800),
            Color(0xFFF44336),
            Color(0xFF9C27B0),
            Color(0xFF607D8B)
        )
        
        data.forEachIndexed { index, point ->
            val sweepAngle = (point.value / total) * 360f
            val color = colors[index % colors.size]
            
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )
            
            startAngle += sweepAngle
        }
    }
}

@Composable
private fun AreaChart(
    data: List<DataPoint>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas
        
        val maxValue = data.maxOfOrNull { it.value } ?: 1f
        val minValue = data.minOfOrNull { it.value } ?: 0f
        val range = maxValue - minValue
        
        val stepX = size.width / (data.size - 1).coerceAtLeast(1)
        
        val path = Path()
        path.moveTo(0f, size.height)
        
        data.forEachIndexed { index, point ->
            val x = index * stepX
            val y = size.height - ((point.value - minValue) / range) * size.height
            
            if (index == 0) {
                path.lineTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        
        path.lineTo(size.width, size.height)
        path.close()
        
        // Draw filled area
        drawPath(
            path = path,
            color = Color(0xFF2196F3).copy(alpha = 0.3f)
        )
        
        // Draw line
        val linePath = Path()
        data.forEachIndexed { index, point ->
            val x = index * stepX
            val y = size.height - ((point.value - minValue) / range) * size.height
            
            if (index == 0) {
                linePath.moveTo(x, y)
            } else {
                linePath.lineTo(x, y)
            }
        }
        
        drawPath(
            path = linePath,
            color = Color(0xFF2196F3),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun AIInsightsSection(
    insights: List<AIInsight>
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = "AI Insights",
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "AI-Powered Insights",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.heightIn(max = 300.dp)
        ) {
            items(insights) { insight ->
                AIInsightCard(insight = insight)
            }
        }
    }
}

@Composable
private fun AIInsightCard(
    insight: AIInsight
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = when (insight.type) {
                InsightType.OPPORTUNITY -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                InsightType.WARNING -> Color(0xFFFF9800).copy(alpha = 0.1f)
                InsightType.TREND -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                InsightType.RECOMMENDATION -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            }
        ),
        border = BorderStroke(
            1.dp,
            when (insight.type) {
                InsightType.OPPORTUNITY -> Color(0xFF4CAF50).copy(alpha = 0.3f)
                InsightType.WARNING -> Color(0xFFFF9800).copy(alpha = 0.3f)
                InsightType.TREND -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                InsightType.RECOMMENDATION -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = when (insight.type) {
                    InsightType.OPPORTUNITY -> Icons.Default.TrendingUp
                    InsightType.WARNING -> Icons.Default.Warning
                    InsightType.TREND -> Icons.Default.Analytics
                    InsightType.RECOMMENDATION -> Icons.Default.Lightbulb
                },
                contentDescription = insight.type.name,
                tint = when (insight.type) {
                    InsightType.OPPORTUNITY -> Color(0xFF4CAF50)
                    InsightType.WARNING -> Color(0xFFFF9800)
                    InsightType.TREND -> MaterialTheme.colorScheme.primary
                    InsightType.RECOMMENDATION -> MaterialTheme.colorScheme.tertiary
                },
                modifier = Modifier.size(24.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = insight.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                text = "${(insight.confidence * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = insight.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (insight.actionItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    insight.actionItems.forEach { action ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            
                            Text(
                                text = action,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailedAnalyticsGrid(
    analytics: List<DetailedMetric>
) {
    Column {
        Text(
            text = "Detailed Analytics",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.heightIn(max = 400.dp)
        ) {
            items(analytics) { metric ->
                DetailedMetricCard(metric = metric)
            }
        }
    }
}

@Composable
private fun DetailedMetricCard(
    metric: DetailedMetric
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = metric.icon,
                contentDescription = metric.name,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = metric.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = metric.value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            if (metric.change != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (metric.change > 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = "Trend",
                        tint = if (metric.change > 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Text(
                        text = "${if (metric.change > 0) "+" else ""}${metric.change}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (metric.change > 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun RealTimeActivityFeed(
    activities: List<ActivityItem>
) {
    Column {
        Text(
            text = "Real-time Activity Feed",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            LazyColumn(
                modifier = Modifier
                    .heightIn(max = 300.dp)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(activities) { activity ->
                    ActivityItemCard(activity = activity)
                }
            }
        }
    }
}

@Composable
private fun ActivityItemCard(
    activity: ActivityItem
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    activity.color.copy(alpha = 0.2f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = activity.icon,
                contentDescription = activity.type,
                tint = activity.color,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = activity.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Text(
                text = activity.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Text(
            text = activity.timestamp,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Helper functions and data generation
private fun generateAnalyticsData(timeRange: AnalyticsTimeRange): AnalyticsData {
    val summary = AnalyticsSummary(
        totalViews = (10000..50000).random(),
        totalSnippets = (100..1000).random(),
        growthRate = (-10..25).random(),
        engagementRate = (60..95).random(),
        activeUsers = (500..2000).random()
    )
    
    val kpis = listOf(
        KPIMetric(
            metric = AnalyticsMetric.VIEWS,
            title = "Total Views",
            value = "${summary.totalViews}",
            changePercentage = (5..15).random(),
            icon = Icons.Default.Visibility,
            color = Color(0xFF2196F3),
            trendData = generateTrendData()
        ),
        KPIMetric(
            metric = AnalyticsMetric.LIKES,
            title = "Total Likes",
            value = "${(summary.totalViews * 0.1).toInt()}",
            changePercentage = (8..20).random(),
            icon = Icons.Default.Favorite,
            color = Color(0xFFF44336),
            trendData = generateTrendData()
        ),
        KPIMetric(
            metric = AnalyticsMetric.SHARES,
            title = "Shares",
            value = "${(summary.totalViews * 0.05).toInt()}",
            changePercentage = (3..12).random(),
            icon = Icons.Default.Share,
            color = Color(0xFF4CAF50),
            trendData = generateTrendData()
        ),
        KPIMetric(
            metric = AnalyticsMetric.COMMENTS,
            title = "Comments",
            value = "${(summary.totalViews * 0.02).toInt()}",
            changePercentage = (1..8).random(),
            icon = Icons.Default.Comment,
            color = Color(0xFFFF9800),
            trendData = generateTrendData()
        )
    )
    
    val chartData = listOf(
        ChartData(
            metric = AnalyticsMetric.VIEWS,
            title = "Views Over Time",
            type = ChartType.LINE,
            dataPoints = generateDataPoints("Views", 30)
        ),
        ChartData(
            metric = AnalyticsMetric.ENGAGEMENT,
            title = "Engagement Distribution",
            type = ChartType.PIE,
            dataPoints = listOf(
                DataPoint("Likes", 45f),
                DataPoint("Shares", 25f),
                DataPoint("Comments", 20f),
                DataPoint("Saves", 10f)
            )
        ),
        ChartData(
            metric = AnalyticsMetric.PERFORMANCE,
            title = "Performance Metrics",
            type = ChartType.BAR,
            dataPoints = generateDataPoints("Performance", 7)
        )
    )
    
    val detailedMetrics = listOf(
        DetailedMetric("Unique Visitors", "${(summary.activeUsers * 1.2).toInt()}", Icons.Default.People, 12),
        DetailedMetric("Bounce Rate", "${(20..40).random()}%", Icons.Default.ExitToApp, -5),
        DetailedMetric("Avg. Session", "${(2..8).random()}m", Icons.Default.Timer, 8),
        DetailedMetric("Conversion Rate", "${(1..5).random()}.${(0..9).random()}%", Icons.Default.TrendingUp, 15)
    )
    
    val recentActivities = generateRecentActivities()
    
    return AnalyticsData(
        summary = summary,
        kpis = kpis,
        chartData = chartData,
        detailedMetrics = detailedMetrics,
        recentActivities = recentActivities
    )
}

private fun generateAIInsights(data: AnalyticsData): List<AIInsight> {
    return listOf(
        AIInsight(
            type = InsightType.OPPORTUNITY,
            title = "Peak Engagement Window Detected",
            description = "Your content performs 40% better between 2-4 PM. Consider scheduling more posts during this time.",
            confidence = 0.87f,
            actionItems = listOf(
                "Schedule 3 more posts between 2-4 PM",
                "Analyze competitor activity during this window"
            )
        ),
        AIInsight(
            type = InsightType.TREND,
            title = "Rising Interest in Kotlin Snippets",
            description = "Kotlin-related content has seen a 25% increase in engagement over the past week.",
            confidence = 0.92f,
            actionItems = listOf(
                "Create more Kotlin tutorials",
                "Focus on advanced Kotlin features"
            )
        ),
        AIInsight(
            type = InsightType.WARNING,
            title = "Declining Mobile Engagement",
            description = "Mobile users are spending 15% less time on your content compared to last month.",
            confidence = 0.78f,
            actionItems = listOf(
                "Optimize content for mobile viewing",
                "Review mobile user experience"
            )
        ),
        AIInsight(
            type = InsightType.RECOMMENDATION,
            title = "Cross-Platform Promotion Opportunity",
            description = "Your GitHub integration could drive 30% more traffic with better promotion.",
            confidence = 0.85f,
            actionItems = listOf(
                "Add GitHub links to popular snippets",
                "Create GitHub-specific content"
            )
        )
    )
}

private fun generateTrendData(): List<Float> {
    return (1..10).map { (50..100).random().toFloat() }
}

private fun generateDataPoints(label: String, count: Int): List<DataPoint> {
    return (1..count).map { index ->
        DataPoint("$label $index", (10..100).random().toFloat())
    }
}

private fun generateRecentActivities(): List<ActivityItem> {
    val activities = listOf(
        ActivityItem(
            type = "view",
            title = "New snippet view",
            description = "React Hooks Tutorial viewed by @john_doe",
            timestamp = "2 min ago",
            icon = Icons.Default.Visibility,
            color = Color(0xFF2196F3)
        ),
        ActivityItem(
            type = "like",
            title = "Snippet liked",
            description = "Python Data Analysis snippet received a like",
            timestamp = "5 min ago",
            icon = Icons.Default.Favorite,
            color = Color(0xFFF44336)
        ),
        ActivityItem(
            type = "comment",
            title = "New comment",
            description = "Comment on JavaScript Async/Await guide",
            timestamp = "8 min ago",
            icon = Icons.Default.Comment,
            color = Color(0xFF4CAF50)
        ),
        ActivityItem(
            type = "share",
            title = "Snippet shared",
            description = "Kotlin Coroutines example shared on Twitter",
            timestamp = "12 min ago",
            icon = Icons.Default.Share,
            color = Color(0xFFFF9800)
        ),
        ActivityItem(
            type = "follow",
            title = "New follower",
            description = "@jane_smith started following you",
            timestamp = "15 min ago",
            icon = Icons.Default.PersonAdd,
            color = Color(0xFF9C27B0)
        )
    )
    
    return activities.shuffled().take(5)
}

// Data classes
data class AnalyticsData(
    val summary: AnalyticsSummary,
    val kpis: List<KPIMetric>,
    val chartData: List<ChartData>,
    val detailedMetrics: List<DetailedMetric>,
    val recentActivities: List<ActivityItem>
)

data class AnalyticsSummary(
    val totalViews: Int,
    val totalSnippets: Int,
    val growthRate: Int,
    val engagementRate: Int,
    val activeUsers: Int
)

data class KPIMetric(
    val metric: AnalyticsMetric,
    val title: String,
    val value: String,
    val changePercentage: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color,
    val trendData: List<Float>
)

data class ChartData(
    val metric: AnalyticsMetric,
    val title: String,
    val type: ChartType,
    val dataPoints: List<DataPoint>
)

data class DataPoint(
    val label: String,
    val value: Float
)

data class DetailedMetric(
    val name: String,
    val value: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val change: Int?
)

data class ActivityItem(
    val type: String,
    val title: String,
    val description: String,
    val timestamp: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)

data class AIInsight(
    val type: InsightType,
    val title: String,
    val description: String,
    val confidence: Float,
    val actionItems: List<String>
)

enum class AnalyticsTimeRange(val displayName: String) {
    LAST_7_DAYS("7 Days"),
    LAST_30_DAYS("30 Days"),
    LAST_3_MONTHS("3 Months"),
    LAST_6_MONTHS("6 Months"),
    LAST_YEAR("1 Year")
}

enum class AnalyticsMetric {
    VIEWS, LIKES, SHARES, COMMENTS, ENGAGEMENT, PERFORMANCE
}

enum class ChartType(val displayName: String) {
    LINE("Line Chart"),
    BAR("Bar Chart"),
    PIE("Pie Chart"),
    AREA("Area Chart")
}

enum class TrendDirection {
    UP, DOWN, STABLE
}

enum class InsightType {
    OPPORTUNITY, WARNING, TREND, RECOMMENDATION
}