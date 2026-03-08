package com.atebitstack.toolbox.reminders
interface ReminderScheduler {
    fun schedule(reminder: Reminder)
    fun cancel(reminder: Reminder)
}
