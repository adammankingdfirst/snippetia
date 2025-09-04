package com.snippetia.service

import com.snippetia.dto.*
import com.snippetia.model.*
import com.snippetia.repository.*
import com.snippetia.exception.ResourceNotFoundException
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Service
@Transactional(readOnly = true)
class AnalyticsService(
    private val userRepository: UserRepository,
    private val snippetRepository: CodeSnippetRepository,
    private val starRepository: StarRepository,
    private val followRepository: FollowRepository,
    private val commitRepository: CommitRepository,
    private val repositoryRepository: RepositoryRepository
) {

    fun getUserAnalytics(userId: Long): UserAnalyticsResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        val now = LocalDateTime.now()
        val thirtyDaysAgo = now.minus(30, ChronoUnit.DAYS)
        val sevenDaysAgo = now.minus(7, ChronoUnit.DAYS)

        // Basic stats
        val totalSnippets = snippetRepository.countByAuthor(user)
        val totalStars = starRepository.countByUser(user)
        val totalFollowers = followRepository.countByFollowing(user)
        val totalFollowing = followRepository.countByFollower(user)
        val totalCommits = commitRepository.countByAuthor(user)

        // Recent activity
        val recentCommits = commitRepository.countUserCommitsSince(user, sevenDaysAgo)
        val recentSnippets = snippetRepository.countByAuthorAndCreatedAtAfter(user, sevenDaysAgo)

        // Top snippets
        val topSnippets = snippetRepository.findByAuthorOrderByStarCountDesc(
            user, PageRequest.of(0, 5)
        ).content.map { mapToSnippetSummary(it) }

        // Language distribution
        val languageStats = getLanguageDistribution(userId)

        // Activity timeline
        val activityTimeline = getActivityTimeline(userId, thirtyDaysAgo)

        return UserAnalyticsResponse(
            totalSnippets = totalSnippets,
            totalStars = totalStars,
            totalFollowers = totalFollowers,
            totalFollowing = totalFollowing,
            totalCommits = totalCommits,
            recentCommits = recentCommits,
            recentSnippets = recentSnippets,
            topSnippets = topSnippets,
            languageStats = languageStats,
            activityTimeline = activityTimeline,
            profileViews = getUserProfileViews(userId, thirtyDaysAgo),
            engagementRate = calculateEngagementRate(userId)
        )
    }

    fun getPlatformAnalytics(): PlatformAnalyticsResponse {
        val now = LocalDateTime.now()
        val thirtyDaysAgo = now.minus(30, ChronoUnit.DAYS)

        // User stats
        val totalUsers = userRepository.count()
        val activeUsers = userRepository.countActiveUsersSince(thirtyDaysAgo)
        val newUsers = userRepository.countByCreatedAtAfter(thirtyDaysAgo)

        // Content stats
        val totalSnippets = snippetRepository.count()
        val publicSnippets = snippetRepository.countByIsPublicTrue()
        val newSnippets = snippetRepository.countByCreatedAtAfter(thirtyDaysAgo)

        // Engagement stats
        val totalStars = starRepository.count()
        val totalFollows = followRepository.count()

        // Top languages
        val topLanguages = getTopLanguages()

        // Growth metrics
        val userGrowth = getUserGrowthMetrics(thirtyDaysAgo)
        val contentGrowth = getContentGrowthMetrics(thirtyDaysAgo)

        return PlatformAnalyticsResponse(
            totalUsers = totalUsers,
            activeUsers = activeUsers,
            newUsers = newUsers,
            totalSnippets = totalSnippets,
            publicSnippets = publicSnippets,
            newSnippets = newSnippets,
            totalStars = totalStars,
            totalFollows = totalFollows,
            topLanguages = topLanguages,
            userGrowth = userGrowth,
            contentGrowth = contentGrowth,
            engagementMetrics = getEngagementMetrics()
        )
    }

    fun getSnippetAnalytics(snippetId: Long): SnippetAnalyticsResponse {
        val snippet = snippetRepository.findById(snippetId)
            .orElseThrow { ResourceNotFoundException("Snippet not found") }

        val now = LocalDateTime.now()
        val thirtyDaysAgo = now.minus(30, ChronoUnit.DAYS)

        // View analytics (would need view tracking implementation)
        val viewsTimeline = getSnippetViewsTimeline(snippetId, thirtyDaysAgo)
        val starsTimeline = getSnippetStarsTimeline(snippetId, thirtyDaysAgo)

        // Geographic data (would need IP tracking)
        val geographicData = getSnippetGeographicData(snippetId)

        // Referrer data
        val referrerData = getSnippetReferrerData(snippetId)

        return SnippetAnalyticsResponse(
            snippetId = snippetId,
            totalViews = snippet.viewCount,
            totalStars = snippet.starCount,
            totalForks = snippet.forkCount,
            viewsTimeline = viewsTimeline,
            starsTimeline = starsTimeline,
            geographicData = geographicData,
            referrerData = referrerData,
            averageEngagementTime = calculateAverageEngagementTime(snippetId),
            conversionRate = calculateSnippetConversionRate(snippetId)
        )
    }

    fun getTrendingAnalytics(): TrendingAnalyticsResponse {
        val now = LocalDateTime.now()
        val sevenDaysAgo = now.minus(7, ChronoUnit.DAYS)

        // Trending snippets
        val trendingSnippets = getTrendingSnippets(sevenDaysAgo)

        // Trending users
        val trendingUsers = getTrendingUsers(sevenDaysAgo)

        // Trending languages
        val trendingLanguages = getTrendingLanguages(sevenDaysAgo)

        // Trending topics/tags
        val trendingTopics = getTrendingTopics(sevenDaysAgo)

        return TrendingAnalyticsResponse(
            trendingSnippets = trendingSnippets,
            trendingUsers = trendingUsers,
            trendingLanguages = trendingLanguages,
            trendingTopics = trendingTopics,
            hotRepositories = getHotRepositories(sevenDaysAgo)
        )
    }

    private fun getLanguageDistribution(userId: Long): List<LanguageStatResponse> {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        return snippetRepository.findLanguageDistributionByAuthor(user)
            .map { (language, count) ->
                LanguageStatResponse(
                    language = language,
                    count = count,
                    percentage = 0.0 // Calculate based on total
                )
            }
    }

    private fun getActivityTimeline(userId: Long, since: LocalDateTime): List<ActivityTimelineResponse> {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        // This would typically aggregate data by day/week
        return emptyList() // Simplified for now
    }

    private fun getUserProfileViews(userId: Long, since: LocalDateTime): Long {
        // Would need view tracking implementation
        return 0L
    }

    private fun calculateEngagementRate(userId: Long): Double {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        val totalSnippets = snippetRepository.countByAuthor(user)
        val totalStars = starRepository.countByUser(user)

        return if (totalSnippets > 0) {
            totalStars.toDouble() / totalSnippets.toDouble()
        } else 0.0
    }

    private fun getTopLanguages(): List<LanguageStatResponse> {
        return snippetRepository.findTopLanguages(PageRequest.of(0, 10))
            .map { (language, count) ->
                LanguageStatResponse(
                    language = language,
                    count = count,
                    percentage = 0.0 // Calculate based on total
                )
            }
    }

    private fun getUserGrowthMetrics(since: LocalDateTime): List<GrowthMetricResponse> {
        // Would aggregate user registrations by time period
        return emptyList()
    }

    private fun getContentGrowthMetrics(since: LocalDateTime): List<GrowthMetricResponse> {
        // Would aggregate snippet creation by time period
        return emptyList()
    }

    private fun getEngagementMetrics(): EngagementMetricsResponse {
        val totalUsers = userRepository.count()
        val totalStars = starRepository.count()
        val totalFollows = followRepository.count()

        return EngagementMetricsResponse(
            averageStarsPerUser = if (totalUsers > 0) totalStars.toDouble() / totalUsers.toDouble() else 0.0,
            averageFollowsPerUser = if (totalUsers > 0) totalFollows.toDouble() / totalUsers.toDouble() else 0.0,
            dailyActiveUsers = 0L, // Would need session tracking
            weeklyActiveUsers = 0L,
            monthlyActiveUsers = 0L
        )
    }

    private fun getSnippetViewsTimeline(snippetId: Long, since: LocalDateTime): List<TimelineDataPoint> {
        // Would need view tracking implementation
        return emptyList()
    }

    private fun getSnippetStarsTimeline(snippetId: Long, since: LocalDateTime): List<TimelineDataPoint> {
        // Would aggregate stars by time period
        return emptyList()
    }

    private fun getSnippetGeographicData(snippetId: Long): List<GeographicDataPoint> {
        // Would need IP geolocation tracking
        return emptyList()
    }

    private fun getSnippetReferrerData(snippetId: Long): List<ReferrerDataPoint> {
        // Would need referrer tracking
        return emptyList()
    }

    private fun calculateAverageEngagementTime(snippetId: Long): Double {
        // Would need time tracking implementation
        return 0.0
    }

    private fun calculateSnippetConversionRate(snippetId: Long): Double {
        // Would calculate views to stars conversion
        return 0.0
    }

    private fun getTrendingSnippets(since: LocalDateTime): List<TrendingSnippetResponse> {
        return snippetRepository.findTrendingSnippets(since, PageRequest.of(0, 10))
            .map { snippet ->
                TrendingSnippetResponse(
                    id = snippet.id!!,
                    title = snippet.title,
                    author = mapToUserSummary(snippet.author),
                    language = snippet.language,
                    starCount = snippet.starCount,
                    viewCount = snippet.viewCount,
                    trendScore = calculateTrendScore(snippet, since)
                )
            }
    }

    private fun getTrendingUsers(since: LocalDateTime): List<TrendingUserResponse> {
        return userRepository.findTrendingUsers(since, PageRequest.of(0, 10))
            .map { user ->
                TrendingUserResponse(
                    id = user.id!!,
                    username = user.username,
                    displayName = user.displayName,
                    avatarUrl = user.avatarUrl,
                    followerCount = user.followerCount,
                    snippetCount = snippetRepository.countByAuthor(user),
                    trendScore = calculateUserTrendScore(user, since)
                )
            }
    }

    private fun getTrendingLanguages(since: LocalDateTime): List<TrendingLanguageResponse> {
        return snippetRepository.findTrendingLanguages(since, PageRequest.of(0, 10))
            .map { (language, count) ->
                TrendingLanguageResponse(
                    language = language,
                    snippetCount = count,
                    growthRate = calculateLanguageGrowthRate(language, since)
                )
            }
    }

    private fun getTrendingTopics(since: LocalDateTime): List<TrendingTopicResponse> {
        return snippetRepository.findTrendingTags(since, PageRequest.of(0, 10))
            .map { (tag, count) ->
                TrendingTopicResponse(
                    topic = tag,
                    snippetCount = count,
                    growthRate = calculateTopicGrowthRate(tag, since)
                )
            }
    }

    private fun getHotRepositories(since: LocalDateTime): List<HotRepositoryResponse> {
        return repositoryRepository.findHotRepositories(since, PageRequest.of(0, 10))
            .map { repo ->
                HotRepositoryResponse(
                    id = repo.id!!,
                    name = repo.name,
                    fullName = repo.fullName,
                    owner = mapToUserSummary(repo.owner),
                    starCount = repo.starCount,
                    forkCount = repo.forkCount,
                    language = repo.primaryLanguage,
                    hotScore = calculateRepoHotScore(repo, since)
                )
            }
    }

    private fun calculateTrendScore(snippet: CodeSnippet, since: LocalDateTime): Double {
        // Calculate trend score based on recent activity
        return snippet.starCount.toDouble() + snippet.viewCount.toDouble() * 0.1
    }

    private fun calculateUserTrendScore(user: User, since: LocalDateTime): Double {
        // Calculate user trend score based on recent activity
        return user.followerCount.toDouble()
    }

    private fun calculateLanguageGrowthRate(language: String, since: LocalDateTime): Double {
        // Calculate growth rate for language
        return 0.0
    }

    private fun calculateTopicGrowthRate(topic: String, since: LocalDateTime): Double {
        // Calculate growth rate for topic
        return 0.0
    }

    private fun calculateRepoHotScore(repository: Repository, since: LocalDateTime): Double {
        // Calculate repository hot score
        return repository.starCount.toDouble() + repository.forkCount.toDouble() * 2
    }

    private fun mapToSnippetSummary(snippet: CodeSnippet): SnippetSummaryResponse {
        return SnippetSummaryResponse(
            id = snippet.id!!,
            title = snippet.title,
            language = snippet.language,
            starCount = snippet.starCount,
            viewCount = snippet.viewCount
        )
    }

    private fun mapToUserSummary(user: User): UserSummaryResponse {
        return UserSummaryResponse(
            id = user.id!!,
            username = user.username,
            displayName = user.displayName,
            avatarUrl = user.avatarUrl
        )
    }
}