package com.snippetia.service

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class RedisService(
    private val redisTemplate: RedisTemplate<String, Any>
) {

    fun blacklistToken(token: String) {
        val key = "blacklisted_token:$token"
        redisTemplate.opsForValue().set(key, "true", Duration.ofDays(7))
    }

    fun isTokenBlacklisted(token: String): Boolean {
        val key = "blacklisted_token:$token"
        return redisTemplate.hasKey(key)
    }

    fun set(key: String, value: Any, duration: Duration) {
        redisTemplate.opsForValue().set(key, value, duration)
    }

    fun get(key: String): Any? {
        return redisTemplate.opsForValue().get(key)
    }

    fun delete(key: String) {
        redisTemplate.delete(key)
    }

    fun hasKey(key: String): Boolean {
        return redisTemplate.hasKey(key)
    }
}