package com.atebitstack.voidbox.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class GroceryReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "grocery_channel"
        const val EXTRA_GROCERY_ID = "grocery_id"
        const val EXTRA_GROCERY_NAME = "grocery_name"
        const val EXTRA_GROCERY_DATE = "grocery_date"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val groceryId = intent.getIntExtra(EXTRA_GROCERY_ID, 0)
        val groceryName = intent.getStringExtra(EXTRA_GROCERY_NAME) ?: "Grocery item"
        val groceryDate = intent.getStringExtra(EXTRA_GROCERY_DATE) ?: ""

        createNotificationChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("🛒 $groceryName expires tomorrow!")
            .setContentText("Best before: $groceryDate")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Best before: $groceryDate"))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSound(null)
            .setVibrate(null)
            .setAutoCancel(true)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(groceryId + GROCERY_NOTIFICATION_ID_BASE, notification)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Grocery Expiration",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Notifications for grocery items about to expire — no sound or vibration"
                setSound(null, null)
                enableVibration(false)
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private companion object {
        const val GROCERY_NOTIFICATION_ID_BASE = 100000
    }
}