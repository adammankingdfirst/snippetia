package com.snippetia.repository

import com.snippetia.model.Event
import com.snippetia.model.EventStatus
import com.snippetia.model.Channel
import com.snippetia.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface EventRepository : JpaRepository<Event, Long> {
    
    fun findByStatusOrderByStartTimeAsc(status: EventStatus, pageable: Pageable): Page<Event>
    
    @Query("""
        SELECT e FROM Event e 
        WHERE e.status = 'PUBLISHED' 
        AND e.startTime > :now 
        ORDER BY e.startTime ASC
    """)
    fun findUpcomingEvents(@Param("now") now: LocalDateTime, pageable: Pageable): Page<Event>
    
    fun findByFeaturedTrueAndStatusOrderByStartTimeAsc(status: EventStatus, pageable: Pageable): Page<Event>
    
    fun findByChannelAndStatusOrderByStartTimeAsc(channel: Channel, status: EventStatus, pageable: Pageable): Page<Event>
    
    fun findByOrganizerOrderByStartTimeAsc(organizer: User, pageable: Pageable): Page<Event>
}