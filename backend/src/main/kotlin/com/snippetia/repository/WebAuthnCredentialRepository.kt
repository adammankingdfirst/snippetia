package com.snippetia.repository

import com.snippetia.model.WebAuthnCredential
import com.snippetia.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WebAuthnCredentialRepository : JpaRepository<WebAuthnCredential, Long> {
    
    fun findByUser(user: User): List<WebAuthnCredential>
    
    fun findByCredentialId(credentialId: String): WebAuthnCredential?
    
    fun findByUserAndName(user: User, name: String): WebAuthnCredential?
}