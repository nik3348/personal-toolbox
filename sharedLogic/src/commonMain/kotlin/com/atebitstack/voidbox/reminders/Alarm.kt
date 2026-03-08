package com.atebitstack.voidbox.reminders

data class Alarm(
    val id: Int,
    val hour: Int,
    val minute: Int,
    val label: String,
    val description: String = "",
    val enabled: Boolean = true,
) {
    val timeFormatted: String
        get() {
            val period = if (hour < 12) "AM" else "PM"
            val displayHour = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            val minuteStr = if (minute < 10) "0$minute" else "$minute"
            return "$displayHour:$minuteStr $period"
        }
}
