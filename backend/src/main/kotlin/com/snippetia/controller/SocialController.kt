package com.snippetia.controller

import com.snippetia.dto.*
import com.snippetia.service.*
import com.snippetia.security.CurrentUser
import com.snippetia.security.UserPrincipal
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/social")
class SocialController(
    private val starService: StarService,
    private val followService: FollowService,
    private val notificationService: NotificationService
) {

    // Star endpoints
    @PostMapping("/stars/{snippetId}")
    @PreAuthorize("hasRole('USER')")
    fun toggleStar(
        @PathVariable snippetId: Long,
        @CurrentUser userPrincipal: UserPrincipal
    ): ResponseEntity<StarResponse> {
        val isStarred = starService.toggleStar(userPrincipal.id, snippetId)
        val starCount = starService.getSnippetStarCount(snippetId)
        
        return ResponseEntity.ok(StarResponse(isStarred, starCount))
    }

    @GetMapping("/stars/snippets/{snippetId}")
    fun getSnippetStargazers(
        @PathVariable snippetId: Long,
        pageable: Pageable
    ): ResponseEntity<Page<UserSummaryResponse>> {
        return ResponseEntity.ok(starService.getSnippetStargazers(snippetId, pageable))
    }

    @GetMapping("/stars/users/{userId}")
    fun getUserStarredSnippets(
        @PathVariable userId: Long,
        pageable: Pageable
    ): ResponseEntity<Page<SnippetResponse>> {
        return ResponseEntity.ok(starService.getUserStarredSnippets(userId, pageable))
    }

    @GetMapping("/stars/check/{snippetId}")
    @PreAuthorize("hasRole('USER')")
    fun isSnippetStarred(
        @PathVariable snippetId: Long,
        @CurrentUser userPrincipal: UserPrincipal
    ): ResponseEntity<Boolean> {
        return ResponseEntity.ok(starService.isSnippetStarredByUser(userPrincipal.id, snippetId))
    }

    // Follow endpoints
    @PostMapping("/follows/{userId}")
    @PreAuthorize("hasRole('USER')")
    fun toggleFollow(
        @PathVariable userId: Long,
        @CurrentUser userPrincipal: UserPrincipal
    ): ResponseEntity<FollowResponse> {
        val isFollowing = followService.followUser(userPrincipal.id, userId)
        val stats = followService.getFollowStats(userId)
        
        return ResponseEntity.ok(
            FollowResponse(
                isFollowing = isFollowing,
                followerCount = stats.followerCount,
                followingCount = stats.followingCount
            )
        )
    }

    @GetMapping("/follows/{userId}/followers")
    fun getFollowers(
        @PathVariable userId: Long,
        pageable: Pageable
    ): ResponseEntity<Page<UserSummaryResponse>> {
        return ResponseEntity.ok(followService.getFollowers(userId, pageable))
    }

    @GetMapping("/follows/{userId}/following")
    fun getFollowing(
        @PathVariable userId: Long,
        pageable: Pageable
    ): ResponseEntity<Page<UserSummaryResponse>> {
        return ResponseEntity.ok(followService.getFollowing(userId, pageable))
    }

    @GetMapping("/follows/check/{userId}")
    @PreAuthorize("hasRole('USER')")
    fun isFollowing(
        @PathVariable userId: Long,
        @CurrentUser userPrincipal: UserPrincipal
    ): ResponseEntity<Boolean> {
        return ResponseEntity.ok(followService.isFollowing(userPrincipal.id, userId))
    }

    @GetMapping("/follows/{userId}/stats")
    fun getFollowStats(@PathVariable userId: Long): ResponseEntity<FollowStatsResponse> {
        return ResponseEntity.ok(followService.getFollowStats(userId))
    }

    @GetMapping("/follows/mutual/{userId}")
    @PreAuthorize("hasRole('USER')")
    fun getMutualFollows(
        @PathVariable userId: Long,
        @CurrentUser userPrincipal: UserPrincipal,
        pageable: Pageable
    ): ResponseEntity<Page<UserSummaryResponse>> {
        return ResponseEntity.ok(followService.getMutualFollows(userPrincipal.id, userId, pageable))
    }

    @PutMapping("/follows/{userId}/notifications")
    @PreAuthorize("hasRole('USER')")
    fun updateNotificationSettings(
        @PathVariable userId: Long,
        @RequestParam enabled: Boolean,
        @CurrentUser userPrincipal: UserPrincipal
    ): ResponseEntity<Void> {
        followService.updateNotificationSettings(userPrincipal.id, userId, enabled)
        return ResponseEntity.ok().build()
    }

    // Notification endpoints
    @GetMapping("/notifications")
    @PreAuthorize("hasRole('USER')")
    fun getNotifications(
        @CurrentUser userPrincipal: UserPrincipal,
        @RequestParam(defaultValue = "false") unreadOnly: Boolean,
        pageable: Pageable
    ): ResponseEntity<Page<NotificationResponse>> {
        return ResponseEntity.ok(
            notificationService.getUserNotifications(userPrincipal.id, unreadOnly, pageable)
        )
    }

    @GetMapping("/notifications/summary")
    @PreAuthorize("hasRole('USER')")
    fun getNotificationSummary(
        @CurrentUser userPrincipal: UserPrincipal
    ): ResponseEntity<NotificationSummaryResponse> {
        return ResponseEntity.ok(notificationService.getNotificationSummary(userPrincipal.id))
    }

    @PutMapping("/notifications/read")
    @PreAuthorize("hasRole('USER')")
    fun markNotificationsAsRead(
        @RequestBody request: MarkNotificationsReadRequest,
        @CurrentUser userPrincipal: UserPrincipal
    ): ResponseEntity<Void> {
        notificationService.markNotificationsAsRead(userPrincipal.id, request.notificationIds)
        return ResponseEntity.ok().build()
    }

    @PutMapping("/notifications/read-all")
    @PreAuthorize("hasRole('USER')")
    fun markAllNotificationsAsRead(
        @CurrentUser userPrincipal: UserPrincipal
    ): ResponseEntity<Void> {
        notificationService.markAllNotificationsAsRead(userPrincipal.id)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/notifications/unread-count")
    @PreAuthorize("hasRole('USER')")
    fun getUnreadNotificationCount(
        @CurrentUser userPrincipal: UserPrincipal
    ): ResponseEntity<Long> {
        return ResponseEntity.ok(notificationService.getUnreadNotificationCount(userPrincipal.id))
    }
}