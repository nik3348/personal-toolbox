package com.atebitstack.voidbox.reminders

interface ReminderStore {
    fun loadReminders(): List<Reminder>
    fun saveReminders(reminders: List<Reminder>)
}

object InMemoryReminderStore : ReminderStore {
    private var reminders: List<Reminder> = emptyList()

    override fun loadReminders(): List<Reminder> = reminders

    override fun saveReminders(reminders: List<Reminder>) {
        this.reminders = reminders
    }
}

