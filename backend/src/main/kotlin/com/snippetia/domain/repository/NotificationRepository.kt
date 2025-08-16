package com.snippetia.domain.repository

import com.snippetia.domain.entity.Notification
import com.snippetia.domain.entity.NotificationType
import com.snippetia.domain.entity.NotificationPriority
import com.snippetia.domain.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface NotificationRepository : JpaRepository<Notification, Long> {
    
    fun findByUser(user: User, pageable: Pageable): Page<Notification>
    
    fun findByUserAndIsReadFalse(user: User, pageable: Pageable): Page<Notification>
    
    fun findByUserAndType(user: User, type: NotificationType, pageable: Pageable): Page<Notification>
    
    fun findByUserAndPriority(user: User, priority: NotificationPriority, pageable: Pageable): Page<Notification>
    
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.isRead = false ORDER BY n.priority DESC, n.createdAt DESC")
    fun findUnreadNotificationsByUser(@Param("user") user: User, pageable: Pageable): Page<Notification>
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user = :user AND n.isRead = false")
    fun countUnreadNotifications(@Param("user") user: User): Long
    
    @Query("SELECT n FROM Notification n WHERE n.isSent = false AND n.createdAt <= :cutoff")
    fun findUnsentNotifications(@Param("cutoff") cutoff: LocalDateTime): List<Notification>
    
    @Query("SELECT n FROM Notification n WHERE n.expiresAt IS NOT NULL AND n.expiresAt <= :now")
    fun findExpiredNotifications(@Param("now") now: LocalDateTime): List<Notification>
    
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.id = :notificationId")
    fun markAsRead(@Param("notificationId") notificationId: Long, @Param("readAt") readAt: LocalDateTime)
    
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.user = :user AND n.isRead = false")
    fun markAllAsRead(@Param("user") user: User, @Param("readAt") readAt: LocalDateTime)
    
    @Modifying
    @Query("UPDATE Notification n SET n.isSent = true, n.sentAt = :sentAt WHERE n.id = :notificationId")
    fun markAsSent(@Param("notificationId") notificationId: Long, @Param("sentAt") sentAt: LocalDateTime)
    
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.expiresAt IS NOT NULL AND n.expiresAt <= :now")
    fun deleteExpiredNotifications(@Param("now") now: LocalDateTime)
    
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.user = :user AND n.createdAt <= :cutoff")
    fun deleteOldNotifications(@Param("user") user: User, @Param("cutoff") cutoff: LocalDateTime)
}