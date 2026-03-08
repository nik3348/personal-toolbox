package com.atebitstack.voidbox.reminders

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class ReminderViewModel : ViewModel() {
    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    val reminders: StateFlow<List<Reminder>> get() = _reminders

    private var nextId = 1

    var reminderScheduler: ReminderScheduler? = null

    fun addReminder(hour: Int, minute: Int, label: String = "", description: String = "") {
        val reminder = Reminder(
            id = nextId++,
            hour = hour,
            minute = minute,
            label = label.ifBlank { "Reminder" },
            description = description,
            enabled = true,
        )
        _reminders.update { it + reminder }
        reminderScheduler?.schedule(reminder)
    }

    fun toggleReminder(reminderId: Int) {
        _reminders.update { list ->
            list.map { reminder ->
                if (reminder.id == reminderId) {
                    val toggled = reminder.copy(enabled = !reminder.enabled)
                    if (toggled.enabled) {
                        reminderScheduler?.schedule(toggled)
                    } else {
                        reminderScheduler?.cancel(toggled)
                    }
                    toggled
                } else reminder
            }
        }
    }

    fun deleteReminder(reminderId: Int) {
        _reminders.update { list ->
            val reminder = list.find { it.id == reminderId }
            if (reminder != null) {
                reminderScheduler?.cancel(reminder)
            }
            list.filter { it.id != reminderId }
        }
    }
}
