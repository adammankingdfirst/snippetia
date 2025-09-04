package com.snippetia.service

import com.snippetia.dto.*
import com.snippetia.model.*
import com.snippetia.repository.*
import com.snippetia.exception.ResourceNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository
) {

    fun createNotification(
        userId: Long,
        type: NotificationType,
        title: String,
        message: String,
        actionUrl: String? = null,
        metadata: String? = null,
        priority: NotificationPriority = NotificationPriority.NORMAL
    ): Notification {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        val notification = Notification(
            user = user,
            type = type,
            title = title,
            message = message,
            actionUrl = actionUrl,
            metadata = metadata,
            priority = priority
        )

        return notificationRepository.save(notification)
    }

    fun getUserNotifications(
        userId: Long,
        unreadOnly: Boolean = false,
        pageable: Pageable
    ): Page<NotificationResponse> {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        val notifications = if (unreadOnly) {
            notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user, pageable)
        } else {
            notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable)
        }

        return notifications.map { mapToNotificationResponse(it) }
    }

    fun getNotificationSummary(userId: Long): NotificationSummaryResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        val unreadCount = notificationRepository.countByUserAndIsReadFalse(user)
        val recentNotifications = notificationRepository.findByUserOrderByCreatedAtDesc(
            user, 
            PageRequest.of(0, 5)
        ).map { mapToNotificationResponse(it) }.content

        return NotificationSummaryResponse(
            unreadCount = unreadCount,
            recentNotifications = recentNotifications
        )
    }

    fun markNotificationsAsRead(userId: Long, notificationIds: List<Long>) {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        // Verify all notifications belong to the user
        val notifications = notificationRepository.findAllById(notificationIds)
        val userNotificationIds = notifications
            .filter { it.user.id == userId }
            .map { it.id!! }

        if (userNotificationIds.isNotEmpty()) {
            notificationRepository.markAsRead(userNotificationIds)
        }
    }

    fun markAllNotificationsAsRead(userId: Long) {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        notificationRepository.markAllAsReadForUser(user)
    }

    fun getUnreadNotificationCount(userId: Long): Long {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        return notificationRepository.countByUserAndIsReadFalse(user)
    }

    fun getNotificationsByType(
        userId: Long,
        type: NotificationType,
        pageable: Pageable
    ): Page<NotificationResponse> {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        val notifications = notificationRepository.findByUserAndTypeOrderByCreatedAtDesc(
            user, type, pageable
        )

        return notifications.map { mapToNotificationResponse(it) }
    }

    fun getRecentNotifications(
        userId: Long,
        since: LocalDateTime,
        pageable: Pageable
    ): Page<NotificationResponse> {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        val notifications = notificationRepository.findRecentNotifications(user, since, pageable)
        return notifications.map { mapToNotificationResponse(it) }
    }

    fun deleteNotification(userId: Long, notificationId: Long) {
        val notification = notificationRepository.findById(notificationId)
            .orElseThrow { ResourceNotFoundException("Notification not found") }

        if (notification.user.id != userId) {
            throw ResourceNotFoundException("Notification not found")
        }

        notificationRepository.delete(notification)
    }

    fun createBulkNotifications(
        userIds: List<Long>,
        type: NotificationType,
        title: String,
        message: String,
        actionUrl: String? = null,
        metadata: String? = null,
        priority: NotificationPriority = NotificationPriority.NORMAL
    ) {
        val users = userRepository.findAllById(userIds)
        
        val notifications = users.map { user ->
            Notification(
                user = user,
                type = type,
                title = title,
                message = message,
                actionUrl = actionUrl,
                metadata = metadata,
                priority = priority
            )
        }

        notificationRepository.saveAll(notifications)
    }

    // Helper method to notify followers about new snippets
    fun notifyFollowersAboutNewSnippet(authorId: Long, snippetId: Long, snippetTitle: String) {
        val author = userRepository.findById(authorId)
            .orElseThrow { ResourceNotFoundException("Author not found") }

        // This would typically be injected to avoid circular dependency
        // For now, we'll assume we have access to follow service
        // In production, consider using events/messaging
        
        createBulkNotifications(
            userIds = emptyList(), // Would get from follow service
            type = NotificationType.NEW_SNIPPET,
            title = "New snippet from ${author.displayName}",
            message = "Check out the new snippet: $snippetTitle",
            actionUrl = "/snippets/$snippetId"
        )
    }

    private fun mapToNotificationResponse(notification: Notification): NotificationResponse {
        return NotificationResponse(
            id = notification.id!!,
            type = notification.type.name,
            title = notification.title,
            message = notification.message,
            isRead = notification.isRead,
            actionUrl = notification.actionUrl,
            priority = notification.priority.name,
            createdAt = notification.createdAt
        )
    }
}