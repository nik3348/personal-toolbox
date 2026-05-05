package com.atebitstack.voidbox.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

class GroceryNotificationScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(grocery: GroceryItem) {
        val notificationDate = grocery.expirationDate.minusDays(1)
        if (notificationDate < kotlinx.datetime.Clock.System.todayIn(TimeZone.currentSystemDefault())) {
            return
        }

        val intent = Intent(context, GroceryReminderReceiver::class.java).apply {
            putExtra(GroceryReminderReceiver.EXTRA_GROCERY_ID, grocery.id)
            putExtra(GroceryReminderReceiver.EXTRA_GROCERY_NAME, grocery.name)
            putExtra(GroceryReminderReceiver.EXTRA_GROCERY_DATE, grocery.expirationDate.toString())
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            grocery.id + GROCERY_REQUEST_CODE_BASE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notificationDateTime = LocalDateTime(
            date = notificationDate,
            time = LocalTime(hour = 9, minute = 0),
        )

        val instant = notificationDateTime.atStartOfDayIn(TimeZone.currentSystemDefault())

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            instant.toEpochMilliseconds(),
            pendingIntent,
        )
    }

    fun cancel(grocery: GroceryItem) {
        val intent = Intent(context, GroceryReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            grocery.id + GROCERY_REQUEST_CODE_BASE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarmManager.cancel(pendingIntent)
    }

    private companion object {
        const val GROCERY_REQUEST_CODE_BASE = 100000
    }
}