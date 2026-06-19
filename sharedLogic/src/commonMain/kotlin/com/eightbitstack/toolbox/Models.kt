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

/** True if a fridge item matches this ingredient by name (case/whitespace-insensitive).
 *  Shared by both platforms' recipe "have / need" displays. */
fun ingredientInFridge(name: String, fridge: List<FridgeItem>): Boolean {
    val key = name.trim().lowercase()
    return key.isNotEmpty() && fridge.any { it.name.trim().lowercase() == key }
}

/** Short weekday name ("Mon".."Sun") for a "YYYY-MM-DD" string, computed purely
 *  (Zeller's congruence) so it needs no platform date APIs. Empty if unparseable. */
fun weekdayShort(date: String): String {
    val p = date.split("-")
    val y = p.getOrNull(0)?.toIntOrNull() ?: return ""
    var m = p.getOrNull(1)?.toIntOrNull() ?: return ""
    val d = p.getOrNull(2)?.toIntOrNull() ?: return ""
    var yy = y
    if (m < 3) { m += 12; yy -= 1 }
    val k = yy % 100
    val j = yy / 100
    val h = (d + (13 * (m + 1)) / 5 + k + k / 4 + j / 4 + 5 * j) % 7
    // Zeller h: 0=Sat,1=Sun,2=Mon,...,6=Fri
    return listOf("Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri")[h]
}

data class AppSettings(
    val accent: String = "indigo",
    val darkMode: Boolean = false,
    val showFlourishes: Boolean = true,
    val backgroundPattern: String = "grid",
    val expiryNotificationsOn: Boolean = true
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
