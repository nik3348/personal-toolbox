package com.eightbitstack.toolbox

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Alarms don't survive a reboot, so re-sync them from persisted state. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        AndroidContext.applicationContext = context.applicationContext
        ReminderScheduler.sync(context, ToolboxRepository().state)
    }
}
