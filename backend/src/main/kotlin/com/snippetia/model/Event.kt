package com.snippetia.model

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "events")
@EntityListeners(AuditingEntityListener::class)
data class Event(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "title", nullable = false, length = 200)
    var title: String,

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id")
    var organizer: User? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    var channel: Channel? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: EventType,

    @Column(name = "start_time", nullable = false)
    var startTime: LocalDateTime,

    @Column(name = "end_time", nullable = false)
    var endTime: LocalDateTime,

    @Column(name = "timezone", nullable = false)
    var timezone: String,

    @Column(name = "location")
    var location: String? = null,

    @Column(name = "virtual_link")
    var virtualLink: String? = null,

    @Column(name = "max_attendees")
    var maxAttendees: Int? = null,

    @Column(name = "current_attendees")
    var currentAttendees: Int = 0,

    @Column(name = "registration_fee", precision = 10, scale = 2)
    var registrationFee: BigDecimal? = null,

    @Column(name = "is_free")
    var isFree: Boolean = true,

    @Column(name = "requires_approval")
    var requiresApproval: Boolean = false,

    @Column(name = "banner_url")
    var bannerUrl: String? = null,

    @ElementCollection
    @CollectionTable(name = "event_tags", joinColumns = [JoinColumn(name = "event_id")])
    @Column(name = "tag")
    var tags: MutableSet<String> = mutableSetOf(),

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: EventStatus = EventStatus.DRAFT,

    @Column(name = "featured")
    var featured: Boolean = false,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class EventType {
    WORKSHOP, WEBINAR, MEETUP, CONFERENCE, HACKATHON, LIVE_CODING, Q_AND_A
}

enum class EventStatus {
    DRAFT, PUBLISHED, CANCELLED, COMPLETED
}