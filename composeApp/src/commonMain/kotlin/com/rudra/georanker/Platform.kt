package com.rudra.georanker

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
