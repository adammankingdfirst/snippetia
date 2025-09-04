package com.snippetia.repository

import com.snippetia.model.ShowcaseLike
import com.snippetia.model.DeveloperShowcase
import com.snippetia.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ShowcaseLikeRepository : JpaRepository<ShowcaseLike, Long> {
    
    fun findByUserAndShowcase(user: User, showcase: DeveloperShowcase): ShowcaseLike?
}