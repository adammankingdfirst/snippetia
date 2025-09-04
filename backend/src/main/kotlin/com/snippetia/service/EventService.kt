package com.snippetia.service

import com.snippetia.dto.*
import com.snippetia.model.*
import com.snippetia.repository.*
import com.snippetia.exception.ResourceNotFoundException
import com.snippetia.exception.BusinessException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
@Transactional
class EventService(
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
    private val channelRepository: ChannelRepository,
    private val eventAttendeeRepository: EventAttendeeRepository,
    private val notificationService: NotificationService
) {

    fun createEvent(userId: Long, request: CreateEventRequest): EventResponse {
        val organizer = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        val channel = request.channelId?.let { channelId ->
            val ch = channelRepository.findById(channelId)
                .orElseThrow { ResourceNotFoundException("Channel not found") }
            
            if (ch.owner.id != userId) {
                throw BusinessException("Not authorized to create events for this channel")
            }
            ch
        }

        val event = Event(
            title = request.title,
            description = request.description,
            organizer = organizer,
            channel = channel,
            type = EventType.valueOf(request.type.uppercase()),
            startTime = request.startTime,
            endTime = request.endTime,
            timezone = request.timezone,
            location = request.location,
            virtualLink = request.virtualLink,
            maxAttendees = request.maxAttendees,
            registrationFee = request.registrationFee?.let { BigDecimal(it) },
            isFree = request.registrationFee == null,
            requiresApproval = request.requiresApproval,
            tags = request.tags.toMutableSet()
        )

        val savedEvent = eventRepository.save(event)
        return mapToEventResponse(savedEvent)
    }

    fun updateEvent(userId: Long, eventId: Long, request: CreateEventRequest): EventResponse {
        val event = eventRepository.findById(eventId)
            .orElseThrow { ResourceNotFoundException("Event not found") }

        if (event.organizer?.id != userId && event.channel?.owner?.id != userId) {
            throw BusinessException("Not authorized to update this event")
        }

        event.title = request.title
        event.description = request.description
        event.type = EventType.valueOf(request.type.uppercase())
        event.startTime = request.startTime
        event.endTime = request.endTime
        event.timezone = request.timezone
        event.location = request.location
        event.virtualLink = request.virtualLink
        event.maxAttendees = request.maxAttendees
        event.registrationFee = request.registrationFee?.let { BigDecimal(it) }
        event.isFree = request.registrationFee == null
        event.requiresApproval = request.requiresApproval
        event.tags = request.tags.toMutableSet()

        val savedEvent = eventRepository.save(event)
        return mapToEventResponse(savedEvent)
    }

    fun getEvent(eventId: Long): EventResponse {
        val event = eventRepository.findById(eventId)
            .orElseThrow { ResourceNotFoundException("Event not found") }
        return mapToEventResponse(event)
    }

    fun getEvents(pageable: Pageable): Page<EventResponse> {
        return eventRepository.findByStatusOrderByStartTimeAsc(EventStatus.PUBLISHED, pageable)
            .map { mapToEventResponse(it) }
    }

    fun getUpcomingEvents(pageable: Pageable): Page<EventResponse> {
        return eventRepository.findUpcomingEvents(LocalDateTime.now(), pageable)
            .map { mapToEventResponse(it) }
    }

    fun getFeaturedEvents(pageable: Pageable): Page<EventResponse> {
        return eventRepository.findByFeaturedTrueAndStatusOrderByStartTimeAsc(EventStatus.PUBLISHED, pageable)
            .map { mapToEventResponse(it) }
    }

    fun getChannelEvents(channelId: Long, pageable: Pageable): Page<EventResponse> {
        val channel = channelRepository.findById(channelId)
            .orElseThrow { ResourceNotFoundException("Channel not found") }

        return eventRepository.findByChannelAndStatusOrderByStartTimeAsc(channel, EventStatus.PUBLISHED, pageable)
            .map { mapToEventResponse(it) }
    }

    fun getUserEvents(userId: Long, pageable: Pageable): Page<EventResponse> {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        return eventRepository.findByOrganizerOrderByStartTimeAsc(user, pageable)
            .map { mapToEventResponse(it) }
    }

    fun registerForEvent(userId: Long, eventId: Long): EventRegistrationResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        val event = eventRepository.findById(eventId)
            .orElseThrow { ResourceNotFoundException("Event not found") }

        if (event.status != EventStatus.PUBLISHED) {
            throw BusinessException("Event is not available for registration")
        }

        if (event.startTime.isBefore(LocalDateTime.now())) {
            throw BusinessException("Cannot register for past events")
        }

        // Check if already registered
        val existingRegistration = eventAttendeeRepository.findByEventAndAttendee(event, user)
        if (existingRegistration != null) {
            throw BusinessException("Already registered for this event")
        }

        // Check capacity
        if (event.maxAttendees != null && event.currentAttendees >= event.maxAttendees) {
            throw BusinessException("Event is at full capacity")
        }

        // Process payment if required
        if (!event.isFree && event.registrationFee != null) {
            // Payment processing would go here
            // For now, we'll assume payment is successful
        }

        val attendee = EventAttendee(
            event = event,
            attendee = user,
            status = if (event.requiresApproval) EventAttendeeStatus.PENDING else EventAttendeeStatus.CONFIRMED
        )

        eventAttendeeRepository.save(attendee)

        if (!event.requiresApproval) {
            event.currentAttendees++
            eventRepository.save(event)
        }

        // Send confirmation notification
        notificationService.createNotification(
            userId = userId,
            type = NotificationType.EVENT_REMINDER,
            title = "Event Registration Confirmed",
            message = "You're registered for ${event.title}",
            actionUrl = "/events/${event.id}"
        )

        return EventRegistrationResponse(
            registered = true,
            status = attendee.status.name,
            requiresApproval = event.requiresApproval
        )
    }

    fun cancelEventRegistration(userId: Long, eventId: Long) {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        val event = eventRepository.findById(eventId)
            .orElseThrow { ResourceNotFoundException("Event not found") }

        val registration = eventAttendeeRepository.findByEventAndAttendee(event, user)
            ?: throw ResourceNotFoundException("Registration not found")

        eventAttendeeRepository.delete(registration)

        if (registration.status == EventAttendeeStatus.CONFIRMED) {
            event.currentAttendees = maxOf(0, event.currentAttendees - 1)
            eventRepository.save(event)
        }
    }

    fun publishEvent(userId: Long, eventId: Long): EventResponse {
        val event = eventRepository.findById(eventId)
            .orElseThrow { ResourceNotFoundException("Event not found") }

        if (event.organizer?.id != userId && event.channel?.owner?.id != userId) {
            throw BusinessException("Not authorized to publish this event")
        }

        event.status = EventStatus.PUBLISHED
        val savedEvent = eventRepository.save(event)

        // Notify followers if it's a channel event
        event.channel?.let { channel ->
            // Implementation would notify channel subscribers
        }

        return mapToEventResponse(savedEvent)
    }

    fun deleteEvent(userId: Long, eventId: Long) {
        val event = eventRepository.findById(eventId)
            .orElseThrow { ResourceNotFoundException("Event not found") }

        if (event.organizer?.id != userId && event.channel?.owner?.id != userId) {
            throw BusinessException("Not authorized to delete this event")
        }

        if (event.currentAttendees > 0) {
            throw BusinessException("Cannot delete event with registered attendees")
        }

        eventRepository.delete(event)
    }

    private fun mapToEventResponse(event: Event): EventResponse {
        return EventResponse(
            id = event.id!!,
            title = event.title,
            description = event.description,
            organizer = event.organizer?.let { 
                UserSummaryResponse(
                    id = it.id!!,
                    username = it.username,
                    displayName = it.displayName,
                    avatarUrl = it.avatarUrl
                )
            },
            channel = event.channel?.let {
                ChannelSummaryResponse(
                    id = it.id!!,
                    name = it.name,
                    displayName = it.displayName,
                    avatarUrl = it.avatarUrl
                )
            },
            type = event.type.name,
            startTime = event.startTime,
            endTime = event.endTime,
            timezone = event.timezone,
            location = event.location,
            virtualLink = event.virtualLink,
            maxAttendees = event.maxAttendees,
            currentAttendees = event.currentAttendees,
            registrationFee = event.registrationFee?.toString(),
            isFree = event.isFree,
            requiresApproval = event.requiresApproval,
            bannerUrl = event.bannerUrl,
            tags = event.tags.toList(),
            status = event.status.name,
            featured = event.featured,
            createdAt = event.createdAt
        )
    }
}

data class EventRegistrationResponse(
    val registered: Boolean,
    val status: String,
    val requiresApproval: Boolean
)