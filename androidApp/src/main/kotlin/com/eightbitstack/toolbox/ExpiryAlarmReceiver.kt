package com.eightbitstack.toolbox

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.Calendar

class ExpiryAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AndroidContext.applicationContext = context.applicationContext

        val repo = try {
            ToolboxRepository()
        } catch (e: Exception) {
            return
        }

        val state = repo.state
        if (!state.settings.expiryNotificationsOn) {
            return
        }

        // Find items expiring today or tomorrow
        val expiringToday = mutableListOf<String>()
        val expiringTomorrow = mutableListOf<String>()

        for (item in state.fridge) {
            if (item.expiry.isNotEmpty()) {
                val days = DateUtils.daysUntil(item.expiry)
                if (days == 0) {
                    expiringToday.add(item.name)
                } else if (days == 1) {
                    expiringTomorrow.add(item.name)
                }
            }
        }

        val totalExpiring = expiringToday.size + expiringTomorrow.size

        if (totalExpiring > 0) {
            // Build the title and body
            val title = "Fridge Expiry Alert"
            val body = if (expiringToday.isNotEmpty() && expiringTomorrow.isEmpty()) {
                if (expiringToday.size == 1) {
                    "${expiringToday.first()} expires today!"
                } else {
                    "${expiringToday.size} items expire today: ${expiringToday.joinToString(", ")}"
                }
            } else if (expiringTomorrow.isNotEmpty() && expiringToday.isEmpty()) {
                if (expiringTomorrow.size == 1) {
                    "${expiringTomorrow.first()} expires tomorrow!"
                } else {
                    "${expiringTomorrow.size} items expire tomorrow: ${expiringTomorrow.joinToString(", ")}"
                }
            } else {
                val todayStr = expiringToday.joinToString(", ")
                val tomorrowStr = expiringTomorrow.joinToString(", ")
                "Expiring soon: $todayStr (today), $tomorrowStr (tomorrow)"
            }

            ReminderNotifications.ensureChannels(context)

            val openApp = PendingIntent.getActivity(
                context,
                1001, // Unique request code for fridge deep link
                Intent(context, MainActivity::class.java)
                    .putExtra("navTo", "fridge")
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, "nudge_banner")
                .setSmallIcon(R.drawable.ic_nudge)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(openApp)
                .setAutoCancel(true)
                .build()

            val manager = NotificationManagerCompat.from(context)
            try {
                if (manager.areNotificationsEnabled()) {
                    manager.notify("fridge_expiry".hashCode(), notification)
                }
            } catch (e: SecurityException) {
                // Ignore if permission revoked
            }
        }

        // Reschedule for tomorrow
        ExpiryScheduler.schedule(context)
    }
}
