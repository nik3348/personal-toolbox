@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.eightbitstack.toolbox

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

actual object DateUtils {
    actual fun getTodayPlusDays(days: Int): String {
        return LocalDate.now().plusDays(days.toLong()).toString()
    }

    actual fun daysUntil(expiry: String): Int {
        return try {
            val exp = LocalDate.parse(expiry)
            ChronoUnit.DAYS.between(LocalDate.now(), exp).toInt()
        } catch (e: Exception) {
            0
        }
    }

    actual fun getTodayFormatted(): String {
        val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())
        return LocalDate.now().format(formatter)
    }

    actual fun getCurrentHour(): Int {
        return LocalTime.now().hour
    }
}
