package com.atebitstack.voidbox.reminders

import android.content.Context
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDate
import org.json.JSONArray
import org.json.JSONObject
import androidx.core.content.edit

class AndroidGroceryStore(context: Context) : GroceryStore {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun loadGroceries(): List<GroceryItem> {
        val payload = preferences.getString(KEY_GROCERIES, null) ?: return emptyList()
        return runCatching {
            val jsonArray = JSONArray(payload)
            buildList {
                for (index in 0 until jsonArray.length()) {
                    val itemJson = jsonArray.getJSONObject(index)
                    add(
                        GroceryItem(
                            id = itemJson.getInt("id"),
                            name = itemJson.getString("name"),
                            expirationDate = itemJson.getString("expirationDate").toLocalDate(),
                            addedDate = itemJson.getString("addedDate").toLocalDate(),
                        ),
                    )
                }
            }
        }.getOrElse { emptyList() }
    }

    override fun saveGroceries(groceries: List<GroceryItem>) {
        val jsonArray = JSONArray()
        groceries.forEach { item ->
            jsonArray.put(
                JSONObject()
                    .put("id", item.id)
                    .put("name", item.name)
                    .put("expirationDate", item.expirationDate.toString())
                    .put("addedDate", item.addedDate.toString()),
            )
        }
        preferences.edit { putString(KEY_GROCERIES, jsonArray.toString()) }
    }

    private companion object {
        const val PREFS_NAME = "grocery_store"
        const val KEY_GROCERIES = "groceries"
    }
}