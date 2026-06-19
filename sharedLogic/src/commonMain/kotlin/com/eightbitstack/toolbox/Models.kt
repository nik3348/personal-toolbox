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

data class ShoppingListItem(
    val id: String,
    val name: String,
    val qty: String,
    val checked: Boolean
)

data class RecipeIngredient(
    val name: String,
    val qty: String
)

data class Recipe(
    val id: String,
    val name: String,
    val ingredients: List<RecipeIngredient>,
    val steps: List<String>
)

data class MealPlanEntry(
    val id: String,
    val date: String, // "YYYY-MM-DD"
    val slot: String, // "breakfast", "lunch", "dinner"
    val recipeId: String
)

data class AppSettings(
    val accent: String = "indigo",
    val darkMode: Boolean = false,
    val showFlourishes: Boolean = true,
    val backgroundPattern: String = "grid"
)

data class ToolboxState(
    val quietHoursOn: Boolean,
    val reminders: List<Reminder>,
    val doneIds: List<String>,
    val fridge: List<FridgeItem>,
    val shoppingList: List<ShoppingListItem> = emptyList(),
    val recipes: List<Recipe> = emptyList(),
    val mealPlan: List<MealPlanEntry> = emptyList(),
    val settings: AppSettings = AppSettings(),
    // Last local mutation time (epoch millis). Reserved for future cloud sync
    // conflict resolution (last-write-wins); 0 means "never stamped".
    val updatedAt: Long = 0,
    // Date (YYYY-MM-DD) the daily rollover last ran. Empty until the first run.
    val lastRolloverDate: String = ""
)
