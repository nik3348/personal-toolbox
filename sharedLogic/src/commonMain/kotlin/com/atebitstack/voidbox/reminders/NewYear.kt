package com.atebitstack.voidbox.reminders

import kotlinx.datetime.*
import kotlin.time.Clock

fun daysUntilNewYear(): Int {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val jan1 = LocalDate(today.year + 1, 1, 1)
    return today.daysUntil(jan1)
}

fun daysPhrase(): String = "There are only ${daysUntilNewYear()} days until New Year! 🎆"
