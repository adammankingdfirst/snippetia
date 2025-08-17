package com.snippetia.security

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*

@Component
class JwtTokenProvider {

    @Value("\${app.jwt.secret}")
    private lateinit var jwtSecret: String

    @Value("\${app.jwt.access-token-expiration-ms:3600000}") // 1 hour
    private var accessTokenExpirationMs: Long = 3600000

    @Value("\${app.jwt.refresh-token-expiration-ms:604800000}") // 7 days
    private var refreshTokenExpirationMs: Long = 604800000

    private val key: Key by lazy {
        Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }

    fun generateAccessToken(username: String): String {
        val now = Date()
        val expiryDate = Date(now.time + accessTokenExpirationMs)

        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .claim("type", "access")
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }

    fun generateRefreshToken(username: String): String {
        val now = Date()
        val expiryDate = Date(now.time + refreshTokenExpirationMs)

        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .claim("type", "refresh")
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }

    fun getUsernameFromToken(token: String): String {
        val claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body

        return claims.subject
    }

    fun validateToken(token: String): Boolean {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
            return true
        } catch (ex: SecurityException) {
            // Invalid JWT signature
        } catch (ex: MalformedJwtException) {
            // Invalid JWT token
        } catch (ex: ExpiredJwtException) {
            // Expired JWT token
        } catch (ex: UnsupportedJwtException) {
            // Unsupported JWT token
        } catch (ex: IllegalArgumentException) {
            // JWT claims string is empty
        }
        return false
    }

    fun getAccessTokenExpirationTime(): Long = accessTokenExpirationMs / 1000
}