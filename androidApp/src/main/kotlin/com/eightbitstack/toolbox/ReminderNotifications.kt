package com.eightbitstack.toolbox

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object ReminderNotifications {
    private const val CHANNEL_BANNER = "nudge_banner"
    private const val CHANNEL_BADGE = "nudge_badge"
    private const val CHANNEL_BUZZ = "nudge_buzz"
    private const val CHANNEL_SILENT = "nudge_silent"

    fun ensureChannels(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_BANNER, "Banner nudges", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Heads-up reminders"
            }
        )
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_BADGE, "Badge nudges", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Reminders that appear quietly in the shade"
            }
        )
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_BUZZ, "Buzz nudges", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Vibration-only reminders"
                setSound(null, null)
                enableVibration(true)
            }
        )
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_SILENT, "Silent nudges", NotificationManager.IMPORTANCE_MIN).apply {
                description = "Completely silent reminders"
                setShowBadge(false)
            }
        )
    }

    private fun channelFor(mode: String) = when (mode) {
        "badge" -> CHANNEL_BADGE
        "buzz" -> CHANNEL_BUZZ
        "silent" -> CHANNEL_SILENT
        else -> CHANNEL_BANNER
    }

    fun show(context: Context, id: String, title: String, time: String, mode: String) {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return
        ensureChannels(context)

        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java)
                .putExtra("navTo", "reminders")
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelFor(mode))
            .setSmallIcon(R.drawable.ic_nudge)
            .setContentTitle(title)
            .setContentText("Quiet nudge · $time")
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()

        try {
            manager.notify(id.hashCode(), notification)
        } catch (e: SecurityException) {
            // Notification permission revoked between the check and notify
        }
    }
}
