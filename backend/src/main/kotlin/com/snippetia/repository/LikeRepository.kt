package com.snippetia.repository

import com.snippetia.model.Like
import com.snippetia.model.User
import com.snippetia.model.CodeSnippet
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LikeRepository : JpaRepository<Like, Long> {
    
    fun findByUserAndSnippet(user: User, snippet: CodeSnippet): Like?
}