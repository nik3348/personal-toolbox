package com.atebitstack.voidbox.reminders

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class ReminderViewModel : ViewModel() {
    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    val reminders: StateFlow<List<Reminder>> get() = _reminders

    private val _showAddSheet = MutableStateFlow(false)
    val showAddSheet: StateFlow<Boolean> get() = _showAddSheet

    private val _editingReminder = MutableStateFlow<Reminder?>(null)
    val editingReminder: StateFlow<Reminder?> get() = _editingReminder

    private var nextId = 1

    var reminderScheduler: ReminderScheduler? = null

    fun showAddReminder() {
        _editingReminder.value = null
        _showAddSheet.value = true
    }

    fun showEditReminder(reminder: Reminder) {
        _editingReminder.value = reminder
        _showAddSheet.value = true
    }

    fun dismissAddSheet() {
        _showAddSheet.value = false
        _editingReminder.value = null
    }

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
        _showAddSheet.value = false
        _editingReminder.value = null
    }

    fun updateReminder(reminderId: Int, hour: Int, minute: Int, label: String, description: String) {
        _reminders.update { list ->
            list.map { reminder ->
                if (reminder.id == reminderId) {
                    val updated = reminder.copy(
                        hour = hour,
                        minute = minute,
                        label = label.ifBlank { "Reminder" },
                        description = description,
                    )
                    if (updated.enabled) {
                        reminderScheduler?.schedule(updated)
                    }
                    updated
                } else reminder
            }
        }
        _showAddSheet.value = false
        _editingReminder.value = null
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
