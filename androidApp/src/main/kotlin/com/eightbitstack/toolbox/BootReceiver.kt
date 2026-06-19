package com.eightbitstack.toolbox

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Alarms don't survive a reboot, so re-sync them from persisted state. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        AndroidContext.applicationContext = context.applicationContext
        val state = ToolboxRepository().state
        ReminderScheduler.sync(context, state)
        ExpiryScheduler.sync(context, state)
    }
}
