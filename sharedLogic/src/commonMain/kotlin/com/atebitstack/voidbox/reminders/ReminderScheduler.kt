package com.atebitstack.voidbox.reminders
interface ReminderScheduler {
    fun schedule(reminder: Reminder)
    fun cancel(reminder: Reminder)
}
