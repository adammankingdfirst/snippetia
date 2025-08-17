package com.snippetia.security

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class JwtAuthenticationEntryPoint : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.status = HttpServletResponse.SC_UNAUTHORIZED

        val body = mapOf(
            "success" to false,
            "message" to "Unauthorized: ${authException.message}",
            "error" to "UNAUTHORIZED",
            "path" to request.requestURI,
            "timestamp" to System.currentTimeMillis()
        )

        val mapper = ObjectMapper()
        mapper.writeValue(response.outputStream, body)
    }
}