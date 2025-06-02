package com.example.snippetia

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform