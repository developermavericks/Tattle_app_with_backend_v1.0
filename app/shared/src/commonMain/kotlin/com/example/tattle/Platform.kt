package com.example.tattle

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform