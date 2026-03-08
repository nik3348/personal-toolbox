package com.atebitstack.voidbox.reminders

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class AlarmViewModel : ViewModel() {
    private val _alarms = MutableStateFlow<List<Alarm>>(emptyList())
    val alarms: StateFlow<List<Alarm>> get() = _alarms

    private val _showAddSheet = MutableStateFlow(false)
    val showAddSheet: StateFlow<Boolean> get() = _showAddSheet

    private val _editingAlarm = MutableStateFlow<Alarm?>(null)
    val editingAlarm: StateFlow<Alarm?> get() = _editingAlarm

    private var nextId = 1

    var alarmScheduler: AlarmScheduler? = null

    fun showAddAlarm() {
        _editingAlarm.value = null
        _showAddSheet.value = true
    }

    fun showEditAlarm(alarm: Alarm) {
        _editingAlarm.value = alarm
        _showAddSheet.value = true
    }

    fun dismissAddSheet() {
        _showAddSheet.value = false
        _editingAlarm.value = null
    }

    fun addAlarm(hour: Int, minute: Int, label: String = "", description: String = "") {
        val alarm = Alarm(
            id = nextId++,
            hour = hour,
            minute = minute,
            label = label.ifBlank { "Alarm" },
            description = description,
            enabled = true,
        )
        _alarms.update { it + alarm }
        alarmScheduler?.schedule(alarm)
        _showAddSheet.value = false
        _editingAlarm.value = null
    }

    fun updateAlarm(alarmId: Int, hour: Int, minute: Int, label: String, description: String) {
        _alarms.update { list ->
            list.map { alarm ->
                if (alarm.id == alarmId) {
                    val updated = alarm.copy(
                        hour = hour,
                        minute = minute,
                        label = label.ifBlank { "Alarm" },
                        description = description,
                    )
                    if (updated.enabled) {
                        alarmScheduler?.schedule(updated)
                    }
                    updated
                } else alarm
            }
        }
        _showAddSheet.value = false
        _editingAlarm.value = null
    }

    fun toggleAlarm(alarmId: Int) {
        _alarms.update { list ->
            list.map { alarm ->
                if (alarm.id == alarmId) {
                    val toggled = alarm.copy(enabled = !alarm.enabled)
                    if (toggled.enabled) {
                        alarmScheduler?.schedule(toggled)
                    } else {
                        alarmScheduler?.cancel(toggled)
                    }
                    toggled
                } else alarm
            }
        }
    }

    fun deleteAlarm(alarmId: Int) {
        _alarms.update { list ->
            val alarm = list.find { it.id == alarmId }
            if (alarm != null) {
                alarmScheduler?.cancel(alarm)
            }
            list.filter { it.id != alarmId }
        }
    }
}
