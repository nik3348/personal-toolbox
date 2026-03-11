package com.atebitstack.voidbox.reminders

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class ReminderViewModel(
    private val reminderStore: ReminderStore = InMemoryReminderStore,
) : ViewModel() {
    private val _reminders = MutableStateFlow(reminderStore.loadReminders())
    val reminders: StateFlow<List<Reminder>> get() = _reminders

    private val _showAddSheet = MutableStateFlow(false)
    val showAddSheet: StateFlow<Boolean> get() = _showAddSheet

    private val _editingReminder = MutableStateFlow<Reminder?>(null)
    val editingReminder: StateFlow<Reminder?> get() = _editingReminder

    private var nextId = (_reminders.value.maxOfOrNull { it.id } ?: 0) + 1

    var reminderScheduler: ReminderScheduler? = null
        set(value) {
            field = value
            if (value != null) {
                _reminders.value.filter { it.enabled }.forEach(value::schedule)
            }
        }

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
        _reminders.update { current ->
            (current + reminder).also(reminderStore::saveReminders)
        }
        reminderScheduler?.schedule(reminder)
        _showAddSheet.value = false
    }

    fun updateReminder(id: Int, hour: Int, minute: Int, label: String, description: String) {
        _reminders.update { list ->
            list.map { reminder ->
                if (reminder.id == id) {
                    val updated = reminder.copy(
                        hour = hour,
                        minute = minute,
                        label = label.ifBlank { "Reminder" },
                        description = description,
                    )
                    if (updated.enabled) {
                        reminderScheduler?.cancel(reminder)
                        reminderScheduler?.schedule(updated)
                    }
                    updated
                } else reminder
            }.also(reminderStore::saveReminders)
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
            }.also(reminderStore::saveReminders)
        }
    }

    fun deleteReminder(reminderId: Int) {
        _reminders.update { list ->
            val reminder = list.find { it.id == reminderId }
            if (reminder != null) {
                reminderScheduler?.cancel(reminder)
            }
            list.filter { it.id != reminderId }.also(reminderStore::saveReminders)
        }
    }
}
