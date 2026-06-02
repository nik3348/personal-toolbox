package com.eightbitstack.toolbox

data class Reminder(
    val id: String,
    val title: String,
    val time: String, // "HH:MM"
    val repeat: String, // "", "daily", "weekdays", "weekly", "monthly"
    val mode: String, // "banner", "badge", "buzz", "silent"
    val dueToday: Boolean,
    val on: Boolean
)

data class FridgeItem(
    val id: String,
    val name: String,
    val qty: String,
    val expiry: String, // "YYYY-MM-DD"
    val location: String // "fridge", "freezer", "pantry"
)

data class ToolboxState(
    val quietHoursOn: Boolean,
    val reminders: List<Reminder>,
    val doneIds: List<String>,
    val fridge: List<FridgeItem>
)
