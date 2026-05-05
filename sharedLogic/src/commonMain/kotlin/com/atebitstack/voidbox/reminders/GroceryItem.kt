package com.atebitstack.voidbox.reminders

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.todayIn

data class GroceryItem(
    val id: Int,
    val name: String,
    val expirationDate: LocalDate,
    val addedDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
) {
    val daysUntilExpiration: Int
        get() {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            return today.daysUntil(expirationDate)
        }

    val freshnessStatus: FreshnessStatus
        get() = when {
            daysUntilExpiration < 0 -> FreshnessStatus.EXPIRED
            daysUntilExpiration == 0 -> FreshnessStatus.EXPIRING_TODAY
            daysUntilExpiration == 1 -> FreshnessStatus.EXPIRING_TOMORROW
            daysUntilExpiration <= 3 -> FreshnessStatus.WARNING
            else -> FreshnessStatus.FRESH
        }
}

enum class FreshnessStatus {
    FRESH,
    WARNING,
    EXPIRING_TOMORROW,
    EXPIRING_TODAY,
    EXPIRED
}