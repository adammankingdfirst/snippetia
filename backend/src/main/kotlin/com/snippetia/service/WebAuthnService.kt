package com.snippetia.service

import com.snippetia.dto.*
import org.springframework.stereotype.Service

@Service
class WebAuthnService {

    fun beginRegistration(userId: Long): WebAuthnRegistrationBeginResponse {
        // TODO: Implement WebAuthn registration begin
        throw NotImplementedError("WebAuthn registration not implemented yet")
    }

    fun finishRegistration(userId: Long, request: WebAuthnRegistrationFinishRequest) {
        // TODO: Implement WebAuthn registration finish
        throw NotImplementedError("WebAuthn registration not implemented yet")
    }

    fun beginAuthentication(username: String): WebAuthnAuthenticationBeginResponse {
        // TODO: Implement WebAuthn authentication begin
        throw NotImplementedError("WebAuthn authentication not implemented yet")
    }

    fun finishAuthentication(request: WebAuthnAuthenticationFinishRequest): AuthResponse {
        // TODO: Implement WebAuthn authentication finish
        throw NotImplementedError("WebAuthn authentication not implemented yet")
    }
}