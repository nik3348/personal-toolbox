@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.eightbitstack.toolbox

expect object DateUtils {
    fun getTodayPlusDays(days: Int): String
    fun daysUntil(expiry: String): Int
    fun getTodayFormatted(): String
    fun getCurrentHour(): Int
}
