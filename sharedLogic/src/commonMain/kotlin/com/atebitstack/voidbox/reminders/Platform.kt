package com.atebitstack.voidbox.reminders

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
