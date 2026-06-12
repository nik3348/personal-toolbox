package com.eightbitstack.toolbox

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.Calendar

class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra("id") ?: return
        val title = intent.getStringExtra("title") ?: "Reminder"
        val time = intent.getStringExtra("time") ?: ""
        val repeat = intent.getStringExtra("repeat") ?: ""
        val mode = intent.getStringExtra("mode") ?: "banner"

        // Quiet hours (10 PM – 7 AM): reminders are held silently until morning
        AndroidContext.applicationContext = context.applicationContext
        val quietHoursOn = try {
            ToolboxRepository().state.quietHoursOn
        } catch (e: Exception) {
            false
        }
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val effectiveMode = if (quietHoursOn && (hour >= 22 || hour < 7)) "silent" else mode

        ReminderNotifications.show(context, id, title, time, effectiveMode)

        if (repeat.isNotEmpty()) {
            ReminderScheduler.schedule(
                context,
                Reminder(id, title, time, repeat, mode, dueToday = true, on = true)
            )
        }
    }
}
