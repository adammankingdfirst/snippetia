package com.snippetia.repository

import com.snippetia.model.DeveloperShowcase
import com.snippetia.model.ShowcaseStatus
import com.snippetia.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ShowcaseRepository : JpaRepository<DeveloperShowcase, Long> {
    
    fun findByStatusOrderByCreatedAtDesc(status: ShowcaseStatus, pageable: Pageable): Page<DeveloperShowcase>
    
    fun findByFeaturedTrueAndStatusOrderByViewCountDesc(status: ShowcaseStatus, pageable: Pageable): Page<DeveloperShowcase>
    
    fun findByCategoriesContainingAndStatusOrderByCreatedAtDesc(
        category: String, 
        status: ShowcaseStatus, 
        pageable: Pageable
    ): Page<DeveloperShowcase>
    
    fun findByTechnologiesContainingAndStatusOrderByCreatedAtDesc(
        technology: String, 
        status: ShowcaseStatus, 
        pageable: Pageable
    ): Page<DeveloperShowcase>
    
    fun findByDeveloperOrderByCreatedAtDesc(developer: User, pageable: Pageable): Page<DeveloperShowcase>
    
    fun findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndStatus(
        title: String, 
        description: String, 
        status: ShowcaseStatus, 
        pageable: Pageable
    ): Page<DeveloperShowcase>
    
    fun findByAvailableForHireTrueAndStatusOrderByCreatedAtDesc(
        status: ShowcaseStatus, 
        pageable: Pageable
    ): Page<DeveloperShowcase>
}