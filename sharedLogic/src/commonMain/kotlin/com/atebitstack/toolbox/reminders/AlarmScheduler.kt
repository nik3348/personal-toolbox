package com.atebitstack.toolbox.reminders
interface AlarmScheduler {
    fun schedule(alarm: Alarm)
    fun cancel(alarm: Alarm)
}
