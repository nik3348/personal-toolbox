package com.eightbitstack.toolbox

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object ExpiryScheduler {
    private const val PREFS = "expiry_scheduler"
    private const val KEY_SCHEDULED = "scheduled"

    fun sync(context: Context, state: ToolboxState) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val previouslyScheduled = prefs.getBoolean(KEY_SCHEDULED, false)
        val shouldBeScheduled = state.settings.expiryNotificationsOn

        if (previouslyScheduled && !shouldBeScheduled) {
            cancel(context)
            prefs.edit().putBoolean(KEY_SCHEDULED, false).apply()
        } else if (shouldBeScheduled) {
            schedule(context)
            prefs.edit().putBoolean(KEY_SCHEDULED, true).apply()
        }
    }

    private fun alarmManager(context: Context) =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, ExpiryAlarmReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            9999, // Unique request code for expiry alarm
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun schedule(context: Context) {
        val am = alarmManager(context)
        val pi = pendingIntent(context)

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            am.canScheduleExactAlarms()
        } else {
            true
        }

        if (canExact) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
        }
    }

    fun cancel(context: Context) {
        alarmManager(context).cancel(pendingIntent(context))
    }
}
