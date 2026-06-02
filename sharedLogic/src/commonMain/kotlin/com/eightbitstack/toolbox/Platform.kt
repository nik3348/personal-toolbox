package com.eightbitstack.toolbox

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform