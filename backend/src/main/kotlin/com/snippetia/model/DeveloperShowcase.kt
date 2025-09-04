package com.snippetia.model

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "developer_showcases")
@EntityListeners(AuditingEntityListener::class)
data class DeveloperShowcase(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "developer_id", nullable = false)
    var developer: User,

    @Column(name = "title", nullable = false, length = 200)
    var title: String,

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    var description: String,

    @Column(name = "app_name", length = 100)
    var appName: String? = null,

    @Column(name = "app_url")
    var appUrl: String? = null,

    @Column(name = "github_url")
    var githubUrl: String? = null,

    @Column(name = "demo_url")
    var demoUrl: String? = null,

    @Column(name = "video_url")
    var videoUrl: String? = null,

    @ElementCollection
    @CollectionTable(name = "showcase_screenshots", joinColumns = [JoinColumn(name = "showcase_id")])
    @Column(name = "screenshot_url")
    var screenshots: MutableList<String> = mutableListOf(),

    @ElementCollection
    @CollectionTable(name = "showcase_technologies", joinColumns = [JoinColumn(name = "showcase_id")])
    @Column(name = "technology")
    var technologies: MutableSet<String> = mutableSetOf(),

    @ElementCollection
    @CollectionTable(name = "showcase_categories", joinColumns = [JoinColumn(name = "showcase_id")])
    @Column(name = "category")
    var categories: MutableSet<String> = mutableSetOf(),

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: ShowcaseStatus = ShowcaseStatus.PENDING,

    @Column(name = "featured")
    var featured: Boolean = false,

    @Column(name = "view_count")
    var viewCount: Long = 0,

    @Column(name = "like_count")
    var likeCount: Long = 0,

    @Column(name = "contact_email")
    var contactEmail: String? = null,

    @Column(name = "hourly_rate", precision = 10, scale = 2)
    var hourlyRate: BigDecimal? = null,

    @Column(name = "available_for_hire")
    var availableForHire: Boolean = false,

    @Column(name = "contract_types", columnDefinition = "TEXT")
    var contractTypes: String? = null,

    @Column(name = "skills", columnDefinition = "TEXT")
    var skills: String? = null,

    @Column(name = "experience_years")
    var experienceYears: Int? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class ShowcaseStatus {
    PENDING, APPROVED, REJECTED, FEATURED
}