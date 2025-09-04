package com.snippetia.model

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "event_attendees")
@EntityListeners(AuditingEntityListener::class)
data class EventAttendee(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    var event: Event,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendee_id", nullable = false)
    var attendee: User,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: EventAttendeeStatus = EventAttendeeStatus.CONFIRMED,

    @CreatedDate
    @Column(name = "registered_at", nullable = false, updatable = false)
    var registeredAt: LocalDateTime = LocalDateTime.now()
)

enum class EventAttendeeStatus {
    PENDING, CONFIRMED, CANCELLED, ATTENDED
}