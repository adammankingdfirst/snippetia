package com.snippetia.repository

import com.snippetia.model.Notification
import com.snippetia.model.NotificationType
import com.snippetia.model.User
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
    
    fun findByUserOrderByCreatedAtDesc(user: User, pageable: Pageable): Page<Notification>
    
    fun findByUserAndIsReadFalseOrderByCreatedAtDesc(user: User, pageable: Pageable): Page<Notification>
    
    fun findByUserAndTypeOrderByCreatedAtDesc(user: User, type: NotificationType, pageable: Pageable): Page<Notification>
    
    fun countByUserAndIsReadFalse(user: User): Long
    
    @Query("""
        SELECT n FROM Notification n 
        WHERE n.user = :user 
        AND n.createdAt >= :since 
        ORDER BY n.createdAt DESC
    """)
    fun findRecentNotifications(
        @Param("user") user: User, 
        @Param("since") since: LocalDateTime, 
        pageable: Pageable
    ): Page<Notification>
    
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id IN :notificationIds")
    fun markAsRead(@Param("notificationIds") notificationIds: List<Long>)
    
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user = :user")
    fun markAllAsReadForUser(@Param("user") user: User)
}