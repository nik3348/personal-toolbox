package com.eightbitstack.toolbox

import platform.Foundation.*

actual object DateUtils {
    actual fun getTodayPlusDays(days: Int): String {
        val calendar = NSCalendar.currentCalendar
        val today = NSDate()
        val comps = NSDateComponents().apply {
            day = days.toLong()
        }
        val futureDate = calendar.dateByAddingComponents(comps, today, 0UL) ?: today
        val formatter = NSDateFormatter().apply {
            dateFormat = "yyyy-MM-dd"
        }
        return formatter.stringFromDate(futureDate)
    }

    actual fun daysUntil(expiry: String): Int {
        val formatter = NSDateFormatter().apply {
            dateFormat = "yyyy-MM-dd"
        }
        val expDate = formatter.dateFromString(expiry) ?: return 0
        val calendar = NSCalendar.currentCalendar
        val today = NSDate()

        val flags = NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay
        val todayStart = calendar.dateFromComponents(calendar.components(flags, today)) ?: today
        val expStart = calendar.dateFromComponents(calendar.components(flags, expDate)) ?: expDate

        val components = calendar.components(NSCalendarUnitDay, todayStart, expStart, 0UL)
        return components.day.toInt()
    }

    actual fun getTodayFormatted(): String {
        val formatter = NSDateFormatter().apply {
            dateFormat = "EEEE, MMMM d"
            locale = NSLocale.currentLocale
        }
        return formatter.stringFromDate(NSDate())
    }

    actual fun getCurrentHour(): Int {
        val calendar = NSCalendar.currentCalendar
        val components = calendar.components(NSCalendarUnitHour, NSDate())
        return components.hour.toInt()
    }

    actual fun epochMillis(): Long {
        return (NSDate().timeIntervalSince1970 * 1000.0).toLong()
    }

    actual fun isTodayWeekend(): Boolean {
        val calendar = NSCalendar.currentCalendar
        // NSCalendar weekday: 1 = Sunday … 7 = Saturday
        val weekday = calendar.components(NSCalendarUnitWeekday, NSDate()).weekday
        return weekday == 1L || weekday == 7L
    }
}
