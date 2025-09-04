package com.snippetia.repository

import com.snippetia.model.ModerationAction
import com.snippetia.model.ModerationStatus
import com.snippetia.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface ModerationActionRepository : JpaRepository<ModerationAction, Long> {
    
    fun findByStatusOrderByCreatedAtDesc(status: ModerationStatus, pageable: Pageable): Page<ModerationAction>
    
    fun findByReporterOrderByCreatedAtDesc(reporter: User, pageable: Pageable): Page<ModerationAction>
    
    fun countByStatus(status: ModerationStatus): Long
    
    @Query("""
        SELECT COUNT(m) FROM ModerationAction m 
        WHERE m.status IN ('RESOLVED', 'DISMISSED', 'ACTION_TAKEN') 
        AND DATE(m.reviewedAt) = :date
    """)
    fun countResolvedToday(@Param("date") date: LocalDate): Long
}