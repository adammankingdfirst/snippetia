package com.snippetia.controller

import com.snippetia.dto.*
import com.snippetia.service.AnalyticsService
import com.snippetia.security.CurrentUser
import com.snippetia.security.UserPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/analytics")
class AnalyticsController(
    private val analyticsService: AnalyticsService
) {

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    fun getUserAnalytics(
        @CurrentUser userPrincipal: UserPrincipal
    ): ResponseEntity<UserAnalyticsResponse> {
        return ResponseEntity.ok(analyticsService.getUserAnalytics(userPrincipal.id))
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER')")
    fun getUserAnalytics(
        @PathVariable userId: Long,
        @CurrentUser userPrincipal: UserPrincipal
    ): ResponseEntity<UserAnalyticsResponse> {
        // Users can only view their own analytics unless they're admin
        if (userId != userPrincipal.id && !userPrincipal.authorities.any { it.authority == "ROLE_ADMIN" }) {
            return ResponseEntity.forbidden().build()
        }
        return ResponseEntity.ok(analyticsService.getUserAnalytics(userId))
    }

    @GetMapping("/platform")
    @PreAuthorize("hasRole('ADMIN')")
    fun getPlatformAnalytics(): ResponseEntity<PlatformAnalyticsResponse> {
        return ResponseEntity.ok(analyticsService.getPlatformAnalytics())
    }

    @GetMapping("/snippet/{snippetId}")
    @PreAuthorize("hasRole('USER')")
    fun getSnippetAnalytics(
        @PathVariable snippetId: Long,
        @CurrentUser userPrincipal: UserPrincipal
    ): ResponseEntity<SnippetAnalyticsResponse> {
        // TODO: Add authorization check - only snippet owner or admin can view analytics
        return ResponseEntity.ok(analyticsService.getSnippetAnalytics(snippetId))
    }

    @GetMapping("/trending")
    fun getTrendingAnalytics(): ResponseEntity<TrendingAnalyticsResponse> {
        return ResponseEntity.ok(analyticsService.getTrendingAnalytics())
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('USER')")
    fun getDashboardAnalytics(
        @CurrentUser userPrincipal: UserPrincipal
    ): ResponseEntity<DashboardAnalyticsResponse> {
        // This would be implemented to provide dashboard-specific analytics
        val dashboardAnalytics = DashboardAnalyticsResponse(
            userStats = UserDashboardStats(
                snippetsThisWeek = 0L,
                starsThisWeek = 0L,
                followersThisWeek = 0L,
                commitsThisWeek = 0L,
                streakDays = 0,
                totalContributions = 0L
            ),
            recentActivity = emptyList(),
            recommendations = emptyList(),
            achievements = emptyList(),
            upcomingEvents = emptyList()
        )
        return ResponseEntity.ok(dashboardAnalytics)
    }

    @PostMapping("/export")
    @PreAuthorize("hasRole('USER')")
    fun exportAnalytics(
        @RequestBody request: AnalyticsExportRequest,
        @CurrentUser userPrincipal: UserPrincipal
    ): ResponseEntity<AnalyticsExportResponse> {
        // This would be implemented to export analytics data
        val exportResponse = AnalyticsExportResponse(
            exportId = "export_${System.currentTimeMillis()}",
            downloadUrl = "/api/analytics/download/export_${System.currentTimeMillis()}",
            expiresAt = java.time.LocalDateTime.now().plusHours(24),
            fileSize = 0L,
            format = request.format
        )
        return ResponseEntity.ok(exportResponse)
    }

    @GetMapping("/search")
    fun getSearchAnalytics(
        @RequestParam(required = false) query: String?,
        @RequestParam(required = false) language: String?,
        @RequestParam(required = false) category: String?
    ): ResponseEntity<SearchAnalyticsResponse> {
        // This would be implemented to provide search analytics
        val searchAnalytics = SearchAnalyticsResponse(
            totalResults = 0L,
            searchTime = 0L,
            popularQueries = emptyList(),
            languageBreakdown = emptyList(),
            categoryBreakdown = emptyList()
        )
        return ResponseEntity.ok(searchAnalytics)
    }

    @GetMapping("/performance")
    @PreAuthorize("hasRole('ADMIN')")
    fun getPerformanceAnalytics(): ResponseEntity<PerformanceAnalyticsResponse> {
        // This would be implemented to provide system performance metrics
        val performanceAnalytics = PerformanceAnalyticsResponse(
            averageResponseTime = 0.0,
            errorRate = 0.0,
            throughput = 0L,
            activeConnections = 0L,
            systemHealth = SystemHealthResponse(
                cpuUsage = 0.0,
                memoryUsage = 0.0,
                diskUsage = 0.0,
                networkLatency = 0.0,
                status = "HEALTHY"
            )
        )
        return ResponseEntity.ok(performanceAnalytics)
    }

    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    fun getRevenueAnalytics(): ResponseEntity<RevenueAnalyticsResponse> {
        // This would be implemented to provide revenue analytics
        val revenueAnalytics = RevenueAnalyticsResponse(
            totalRevenue = "$0.00",
            monthlyRecurringRevenue = "$0.00",
            averageRevenuePerUser = "$0.00",
            churnRate = 0.0,
            subscriptionBreakdown = emptyList(),
            revenueTimeline = emptyList()
        )
        return ResponseEntity.ok(revenueAnalytics)
    }
}