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
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

@Service
@Transactional
class ShowcaseService(
    private val showcaseRepository: ShowcaseRepository,
    private val userRepository: UserRepository,
    private val showcaseLikeRepository: ShowcaseLikeRepository,
    private val notificationService: NotificationService
) {

    private val objectMapper = jacksonObjectMapper()

    fun createShowcase(userId: Long, request: CreateShowcaseRequest): ShowcaseResponse {
        val developer = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        val showcase = DeveloperShowcase(
            developer = developer,
            title = request.title,
            description = request.description,
            appName = request.appName,
            appUrl = request.appUrl,
            githubUrl = request.githubUrl,
            demoUrl = request.demoUrl,
            videoUrl = request.videoUrl,
            screenshots = request.screenshots.toMutableList(),
            technologies = request.technologies.toMutableSet(),
            categories = request.categories.toMutableSet(),
            contactEmail = request.contactEmail,
            hourlyRate = request.hourlyRate?.let { java.math.BigDecimal(it) },
            availableForHire = request.availableForHire,
            contractTypes = objectMapper.writeValueAsString(request.contractTypes),
            skills = objectMapper.writeValueAsString(request.skills),
            experienceYears = request.experienceYears
        )

        val savedShowcase = showcaseRepository.save(showcase)
        return mapToShowcaseResponse(savedShowcase)
    }

    fun updateShowcase(userId: Long, showcaseId: Long, request: CreateShowcaseRequest): ShowcaseResponse {
        val showcase = showcaseRepository.findById(showcaseId)
            .orElseThrow { ResourceNotFoundException("Showcase not found") }

        if (showcase.developer.id != userId) {
            throw BusinessException("Not authorized to update this showcase")
        }

        showcase.title = request.title
        showcase.description = request.description
        showcase.appName = request.appName
        showcase.appUrl = request.appUrl
        showcase.githubUrl = request.githubUrl
        showcase.demoUrl = request.demoUrl
        showcase.videoUrl = request.videoUrl
        showcase.screenshots = request.screenshots.toMutableList()
        showcase.technologies = request.technologies.toMutableSet()
        showcase.categories = request.categories.toMutableSet()
        showcase.contactEmail = request.contactEmail
        showcase.hourlyRate = request.hourlyRate?.let { java.math.BigDecimal(it) }
        showcase.availableForHire = request.availableForHire
        showcase.contractTypes = objectMapper.writeValueAsString(request.contractTypes)
        showcase.skills = objectMapper.writeValueAsString(request.skills)
        showcase.experienceYears = request.experienceYears

        val savedShowcase = showcaseRepository.save(showcase)
        return mapToShowcaseResponse(savedShowcase)
    }

    fun getShowcase(showcaseId: Long): ShowcaseResponse {
        val showcase = showcaseRepository.findById(showcaseId)
            .orElseThrow { ResourceNotFoundException("Showcase not found") }

        // Increment view count
        showcase.viewCount++
        showcaseRepository.save(showcase)

        return mapToShowcaseResponse(showcase)
    }

    fun getShowcases(pageable: Pageable): Page<ShowcaseResponse> {
        return showcaseRepository.findByStatusOrderByCreatedAtDesc(ShowcaseStatus.APPROVED, pageable)
            .map { mapToShowcaseResponse(it) }
    }

    fun getFeaturedShowcases(pageable: Pageable): Page<ShowcaseResponse> {
        return showcaseRepository.findByFeaturedTrueAndStatusOrderByViewCountDesc(ShowcaseStatus.APPROVED, pageable)
            .map { mapToShowcaseResponse(it) }
    }

    fun getShowcasesByCategory(category: String, pageable: Pageable): Page<ShowcaseResponse> {
        return showcaseRepository.findByCategoriesContainingAndStatusOrderByCreatedAtDesc(category, ShowcaseStatus.APPROVED, pageable)
            .map { mapToShowcaseResponse(it) }
    }

    fun getShowcasesByTechnology(technology: String, pageable: Pageable): Page<ShowcaseResponse> {
        return showcaseRepository.findByTechnologiesContainingAndStatusOrderByCreatedAtDesc(technology, ShowcaseStatus.APPROVED, pageable)
            .map { mapToShowcaseResponse(it) }
    }

    fun getUserShowcases(userId: Long, pageable: Pageable): Page<ShowcaseResponse> {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        return showcaseRepository.findByDeveloperOrderByCreatedAtDesc(user, pageable)
            .map { mapToShowcaseResponse(it) }
    }

    fun searchShowcases(query: String, pageable: Pageable): Page<ShowcaseResponse> {
        return showcaseRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndStatus(
            query, query, ShowcaseStatus.APPROVED, pageable
        ).map { mapToShowcaseResponse(it) }
    }

    fun getAvailableDevelopers(pageable: Pageable): Page<ShowcaseResponse> {
        return showcaseRepository.findByAvailableForHireTrueAndStatusOrderByCreatedAtDesc(ShowcaseStatus.APPROVED, pageable)
            .map { mapToShowcaseResponse(it) }
    }

    fun likeShowcase(userId: Long, showcaseId: Long): Boolean {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        val showcase = showcaseRepository.findById(showcaseId)
            .orElseThrow { ResourceNotFoundException("Showcase not found") }

        // Check if already liked (simplified - in production, use a separate likes table)
        val existingLike = showcaseLikeRepository.findByUserAndShowcase(user, showcase)
        
        return if (existingLike != null) {
            showcaseLikeRepository.delete(existingLike)
            showcase.likeCount = maxOf(0, showcase.likeCount - 1)
            showcaseRepository.save(showcase)
            false
        } else {
            val like = ShowcaseLike(user = user, showcase = showcase)
            showcaseLikeRepository.save(like)
            showcase.likeCount++
            showcaseRepository.save(showcase)
            
            // Send notification to developer
            if (showcase.developer.id != userId) {
                notificationService.createNotification(
                    userId = showcase.developer.id!!,
                    type = NotificationType.SNIPPET_LIKED,
                    title = "Your showcase was liked!",
                    message = "${user.displayName} liked your showcase '${showcase.title}'",
                    actionUrl = "/showcase/${showcase.id}"
                )
            }
            true
        }
    }

    fun approveShowcase(showcaseId: Long): ShowcaseResponse {
        val showcase = showcaseRepository.findById(showcaseId)
            .orElseThrow { ResourceNotFoundException("Showcase not found") }

        showcase.status = ShowcaseStatus.APPROVED
        val savedShowcase = showcaseRepository.save(showcase)

        // Send notification to developer
        notificationService.createNotification(
            userId = showcase.developer.id!!,
            type = NotificationType.SYSTEM_ANNOUNCEMENT,
            title = "Showcase Approved!",
            message = "Your showcase '${showcase.title}' has been approved and is now live",
            actionUrl = "/showcase/${showcase.id}"
        )

        return mapToShowcaseResponse(savedShowcase)
    }

    fun rejectShowcase(showcaseId: Long, reason: String): ShowcaseResponse {
        val showcase = showcaseRepository.findById(showcaseId)
            .orElseThrow { ResourceNotFoundException("Showcase not found") }

        showcase.status = ShowcaseStatus.REJECTED
        val savedShowcase = showcaseRepository.save(showcase)

        // Send notification to developer
        notificationService.createNotification(
            userId = showcase.developer.id!!,
            type = NotificationType.SYSTEM_ANNOUNCEMENT,
            title = "Showcase Rejected",
            message = "Your showcase '${showcase.title}' was rejected. Reason: $reason",
            actionUrl = "/showcase/${showcase.id}"
        )

        return mapToShowcaseResponse(savedShowcase)
    }

    fun featureShowcase(showcaseId: Long): ShowcaseResponse {
        val showcase = showcaseRepository.findById(showcaseId)
            .orElseThrow { ResourceNotFoundException("Showcase not found") }

        showcase.featured = true
        showcase.status = ShowcaseStatus.FEATURED
        val savedShowcase = showcaseRepository.save(showcase)

        // Send notification to developer
        notificationService.createNotification(
            userId = showcase.developer.id!!,
            type = NotificationType.SYSTEM_ANNOUNCEMENT,
            title = "Showcase Featured!",
            message = "Your showcase '${showcase.title}' has been featured on the platform",
            actionUrl = "/showcase/${showcase.id}"
        )

        return mapToShowcaseResponse(savedShowcase)
    }

    fun deleteShowcase(userId: Long, showcaseId: Long) {
        val showcase = showcaseRepository.findById(showcaseId)
            .orElseThrow { ResourceNotFoundException("Showcase not found") }

        if (showcase.developer.id != userId) {
            throw BusinessException("Not authorized to delete this showcase")
        }

        showcaseRepository.delete(showcase)
    }

    private fun mapToShowcaseResponse(showcase: DeveloperShowcase): ShowcaseResponse {
        val contractTypes = try {
            objectMapper.readValue(showcase.contractTypes ?: "[]", List::class.java) as List<String>
        } catch (e: Exception) {
            emptyList<String>()
        }

        val skills = try {
            objectMapper.readValue(showcase.skills ?: "[]", List::class.java) as List<String>
        } catch (e: Exception) {
            emptyList<String>()
        }

        return ShowcaseResponse(
            id = showcase.id!!,
            developer = UserSummaryResponse(
                id = showcase.developer.id!!,
                username = showcase.developer.username,
                displayName = showcase.developer.displayName,
                avatarUrl = showcase.developer.avatarUrl
            ),
            title = showcase.title,
            description = showcase.description,
            appName = showcase.appName,
            appUrl = showcase.appUrl,
            githubUrl = showcase.githubUrl,
            demoUrl = showcase.demoUrl,
            videoUrl = showcase.videoUrl,
            screenshots = showcase.screenshots,
            technologies = showcase.technologies.toList(),
            categories = showcase.categories.toList(),
            status = showcase.status.name,
            featured = showcase.featured,
            viewCount = showcase.viewCount,
            likeCount = showcase.likeCount,
            contactEmail = showcase.contactEmail,
            hourlyRate = showcase.hourlyRate?.toString(),
            availableForHire = showcase.availableForHire,
            contractTypes = contractTypes,
            skills = skills,
            experienceYears = showcase.experienceYears,
            createdAt = showcase.createdAt
        )
    }
}

