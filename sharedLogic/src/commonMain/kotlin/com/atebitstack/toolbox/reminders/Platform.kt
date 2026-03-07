package com.atebitstack.toolbox.reminders

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform