package com.snippetia.dto

import java.time.LocalDateTime

// User Analytics
data class UserAnalyticsResponse(
    val totalSnippets: Long,
    val totalStars: Long,
    val totalFollowers: Long,
    val totalFollowing: Long,
    val totalCommits: Long,
    val recentCommits: Long,
    val recentSnippets: Long,
    val topSnippets: List<SnippetSummaryResponse>,
    val languageStats: List<LanguageStatResponse>,
    val activityTimeline: List<ActivityTimelineResponse>,
    val profileViews: Long,
    val engagementRate: Double
)

data class SnippetSummaryResponse(
    val id: Long,
    val title: String,
    val language: String,
    val starCount: Long,
    val viewCount: Long
)

data class LanguageStatResponse(
    val language: String,
    val count: Long,
    val percentage: Double
)

data class ActivityTimelineResponse(
    val date: LocalDateTime,
    val snippetsCreated: Int,
    val starsReceived: Int,
    val commitsCount: Int
)

// Platform Analytics
data class PlatformAnalyticsResponse(
    val totalUsers: Long,
    val activeUsers: Long,
    val newUsers: Long,
    val totalSnippets: Long,
    val publicSnippets: Long,
    val newSnippets: Long,
    val totalStars: Long,
    val totalFollows: Long,
    val topLanguages: List<LanguageStatResponse>,
    val userGrowth: List<GrowthMetricResponse>,
    val contentGrowth: List<GrowthMetricResponse>,
    val engagementMetrics: EngagementMetricsResponse
)

data class GrowthMetricResponse(
    val period: String,
    val value: Long,
    val growthRate: Double
)

data class EngagementMetricsResponse(
    val averageStarsPerUser: Double,
    val averageFollowsPerUser: Double,
    val dailyActiveUsers: Long,
    val weeklyActiveUsers: Long,
    val monthlyActiveUsers: Long
)

// Snippet Analytics
data class SnippetAnalyticsResponse(
    val snippetId: Long,
    val totalViews: Long,
    val totalStars: Long,
    val totalForks: Long,
    val viewsTimeline: List<TimelineDataPoint>,
    val starsTimeline: List<TimelineDataPoint>,
    val geographicData: List<GeographicDataPoint>,
    val referrerData: List<ReferrerDataPoint>,
    val averageEngagementTime: Double,
    val conversionRate: Double
)

data class TimelineDataPoint(
    val timestamp: LocalDateTime,
    val value: Long
)

data class GeographicDataPoint(
    val country: String,
    val countryCode: String,
    val views: Long,
    val percentage: Double
)

data class ReferrerDataPoint(
    val source: String,
    val views: Long,
    val percentage: Double
)

// Trending Analytics
data class TrendingAnalyticsResponse(
    val trendingSnippets: List<TrendingSnippetResponse>,
    val trendingUsers: List<TrendingUserResponse>,
    val trendingLanguages: List<TrendingLanguageResponse>,
    val trendingTopics: List<TrendingTopicResponse>,
    val hotRepositories: List<HotRepositoryResponse>
)

data class TrendingSnippetResponse(
    val id: Long,
    val title: String,
    val author: UserSummaryResponse,
    val language: String,
    val starCount: Long,
    val viewCount: Long,
    val trendScore: Double
)

data class TrendingUserResponse(
    val id: Long,
    val username: String,
    val displayName: String,
    val avatarUrl: String?,
    val followerCount: Long,
    val snippetCount: Long,
    val trendScore: Double
)

data class TrendingLanguageResponse(
    val language: String,
    val snippetCount: Long,
    val growthRate: Double
)

data class TrendingTopicResponse(
    val topic: String,
    val snippetCount: Long,
    val growthRate: Double
)

data class HotRepositoryResponse(
    val id: Long,
    val name: String,
    val fullName: String,
    val owner: UserSummaryResponse,
    val starCount: Long,
    val forkCount: Long,
    val language: String?,
    val hotScore: Double
)

// Dashboard Analytics
data class DashboardAnalyticsResponse(
    val userStats: UserDashboardStats,
    val recentActivity: List<RecentActivityItem>,
    val recommendations: List<RecommendationItem>,
    val achievements: List<AchievementItem>,
    val upcomingEvents: List<UpcomingEventItem>
)

data class UserDashboardStats(
    val snippetsThisWeek: Long,
    val starsThisWeek: Long,
    val followersThisWeek: Long,
    val commitsThisWeek: Long,
    val streakDays: Int,
    val totalContributions: Long
)

data class RecentActivityItem(
    val type: String,
    val title: String,
    val description: String,
    val timestamp: LocalDateTime,
    val actionUrl: String?
)

data class RecommendationItem(
    val type: String,
    val title: String,
    val description: String,
    val actionUrl: String,
    val priority: String
)

data class AchievementItem(
    val id: String,
    val title: String,
    val description: String,
    val iconUrl: String?,
    val unlockedAt: LocalDateTime?,
    val progress: Int,
    val maxProgress: Int
)

data class UpcomingEventItem(
    val id: Long,
    val title: String,
    val startTime: LocalDateTime,
    val type: String,
    val isRegistered: Boolean
)

// Search Analytics
data class SearchAnalyticsResponse(
    val totalResults: Long,
    val searchTime: Long,
    val popularQueries: List<PopularQueryResponse>,
    val languageBreakdown: List<LanguageStatResponse>,
    val categoryBreakdown: List<CategoryStatResponse>
)

data class PopularQueryResponse(
    val query: String,
    val count: Long,
    val trend: String // UP, DOWN, STABLE
)

data class CategoryStatResponse(
    val category: String,
    val count: Long,
    val percentage: Double
)

// Performance Analytics
data class PerformanceAnalyticsResponse(
    val averageResponseTime: Double,
    val errorRate: Double,
    val throughput: Long,
    val activeConnections: Long,
    val systemHealth: SystemHealthResponse
)

data class SystemHealthResponse(
    val cpuUsage: Double,
    val memoryUsage: Double,
    val diskUsage: Double,
    val networkLatency: Double,
    val status: String // HEALTHY, WARNING, CRITICAL
)

// Revenue Analytics (for subscription features)
data class RevenueAnalyticsResponse(
    val totalRevenue: String,
    val monthlyRecurringRevenue: String,
    val averageRevenuePerUser: String,
    val churnRate: Double,
    val subscriptionBreakdown: List<SubscriptionBreakdownResponse>,
    val revenueTimeline: List<RevenueTimelineResponse>
)

data class SubscriptionBreakdownResponse(
    val tier: String,
    val count: Long,
    val revenue: String,
    val percentage: Double
)

data class RevenueTimelineResponse(
    val period: String,
    val revenue: String,
    val subscriptions: Long,
    val churn: Long
)

// Export Analytics
data class AnalyticsExportRequest(
    val type: String, // USER, PLATFORM, SNIPPET, TRENDING
    val format: String, // JSON, CSV, PDF
    val dateRange: DateRangeRequest,
    val filters: Map<String, String> = emptyMap()
)

data class DateRangeRequest(
    val startDate: LocalDateTime,
    val endDate: LocalDateTime
)

data class AnalyticsExportResponse(
    val exportId: String,
    val downloadUrl: String,
    val expiresAt: LocalDateTime,
    val fileSize: Long,
    val format: String
)