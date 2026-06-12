package com.eightbitstack.toolbox

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object ReminderScheduler {
    private const val PREFS = "reminder_scheduler"
    private const val KEY_SCHEDULED = "scheduled_ids"

    /** Aligns scheduled alarms with the current state: schedules every active
     *  reminder and cancels alarms whose reminders were deleted or turned off. */
    fun sync(context: Context, state: ToolboxState) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val previous = prefs.getStringSet(KEY_SCHEDULED, emptySet()) ?: emptySet()

        val active = state.reminders.filter { it.on && shouldSchedule(it, state.doneIds) }
        val activeIds = active.map { it.id }.toSet()

        for (staleId in previous - activeIds) {
            alarmManager(context).cancel(pendingIntent(context, staleId, null))
        }
        for (reminder in active) {
            schedule(context, reminder)
        }

        prefs.edit().putStringSet(KEY_SCHEDULED, activeIds).apply()
    }

    fun schedule(context: Context, reminder: Reminder) {
        val at = nextTriggerMillis(reminder.time, reminder.repeat, System.currentTimeMillis()) ?: return
        val pi = pendingIntent(context, reminder.id, reminder)
        val am = alarmManager(context)
        if (am.canScheduleExactAlarms()) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        }
    }

    // One-shot reminders already marked done stay quiet; repeating ones always fire
    private fun shouldSchedule(r: Reminder, doneIds: List<String>) =
        r.repeat.isNotEmpty() || !doneIds.contains(r.id)

    private fun alarmManager(context: Context) =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private fun pendingIntent(context: Context, id: String, reminder: Reminder?): PendingIntent {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra("id", id)
            if (reminder != null) {
                putExtra("title", reminder.title)
                putExtra("time", reminder.time)
                putExtra("repeat", reminder.repeat)
                putExtra("mode", reminder.mode)
            }
        }
        return PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /** Next occurrence of HH:MM after [nowMillis]. Weekly anchors to the same
     *  weekday as now, monthly to the same day of month, weekdays skips weekends. */
    fun nextTriggerMillis(time: String, repeat: String, nowMillis: Long): Long? {
        val parts = time.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: return null
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: return null
        if (hour !in 0..23 || minute !in 0..59) return null

        val now = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val cal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        when (repeat) {
            "monthly" -> {
                if (cal.timeInMillis <= nowMillis) cal.add(Calendar.MONTH, 1)
            }
            "weekly" -> {
                if (cal.timeInMillis <= nowMillis) cal.add(Calendar.DAY_OF_YEAR, 7)
            }
            "weekdays" -> {
                if (cal.timeInMillis <= nowMillis) cal.add(Calendar.DAY_OF_YEAR, 1)
                while (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                    cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
                ) {
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            else -> { // "" one-shot and "daily"
                if (cal.timeInMillis <= nowMillis) cal.add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return cal.timeInMillis
    }
}
