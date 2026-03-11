package com.atebitstack.voidbox.reminders

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import androidx.core.content.edit

class AndroidReminderStore(context: Context) : ReminderStore {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun loadReminders(): List<Reminder> {
        val payload = preferences.getString(KEY_REMINDERS, null) ?: return emptyList()
        return runCatching {
            val jsonArray = JSONArray(payload)
            buildList {
                for (index in 0 until jsonArray.length()) {
                    val reminderJson = jsonArray.getJSONObject(index)
                    add(
                        Reminder(
                            id = reminderJson.getInt("id"),
                            hour = reminderJson.getInt("hour"),
                            minute = reminderJson.getInt("minute"),
                            label = reminderJson.getString("label"),
                            description = reminderJson.optString("description", ""),
                            enabled = reminderJson.optBoolean("enabled", true),
                        ),
                    )
                }
            }
        }.getOrElse { emptyList() }
    }

    override fun saveReminders(reminders: List<Reminder>) {
        val jsonArray = JSONArray()
        reminders.forEach { reminder ->
            jsonArray.put(
                JSONObject()
                    .put("id", reminder.id)
                    .put("hour", reminder.hour)
                    .put("minute", reminder.minute)
                    .put("label", reminder.label)
                    .put("description", reminder.description)
                    .put("enabled", reminder.enabled),
            )
        }
        preferences.edit { putString(KEY_REMINDERS, jsonArray.toString()) }
    }

    private companion object {
        const val PREFS_NAME = "reminder_store"
        const val KEY_REMINDERS = "reminders"
    }
}

