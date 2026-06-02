package com.eightbitstack.toolbox

expect object DateUtils {
    fun getTodayPlusDays(days: Int): String
    fun daysUntil(expiry: String): Int
    fun getTodayFormatted(): String
    fun getCurrentHour(): Int
}
