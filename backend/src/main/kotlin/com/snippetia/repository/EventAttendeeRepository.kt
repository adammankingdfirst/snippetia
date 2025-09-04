package com.snippetia.repository

import com.snippetia.model.EventAttendee
import com.snippetia.model.Event
import com.snippetia.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EventAttendeeRepository : JpaRepository<EventAttendee, Long> {
    
    fun findByEventAndAttendee(event: Event, attendee: User): EventAttendee?
}