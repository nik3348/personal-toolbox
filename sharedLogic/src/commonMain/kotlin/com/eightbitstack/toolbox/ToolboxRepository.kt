package com.eightbitstack.toolbox

class ToolboxRepository(private val storage: StorageProvider = KeyValueStorage()) {
    
    interface Listener {
        fun onStateChanged(state: ToolboxState)
    }

    private val listeners = mutableListOf<Listener>()
    
    private val storageKey = "toolbox-state-v1"
    
    var state: ToolboxState = loadState()
        private set(value) {
            // Stamp every mutation so a future cloud sync has a conflict key.
            field = value.copy(updatedAt = DateUtils.epochMillis())
            saveState(field)
            notifyListeners()
        }

    fun addListener(listener: Listener) {
        listeners.add(listener)
        listener.onStateChanged(state)
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    private fun notifyListeners() {
        val currentState = state
        for (l in listeners) {
            l.onStateChanged(currentState)
        }
    }

    // --- Action methods (Reducer equivalent) ---

    fun toggleDone(id: String) {
        val currentDone = state.doneIds
        val newDone = if (currentDone.contains(id)) {
            currentDone.filter { it != id }
        } else {
            currentDone + id
        }
        state = state.copy(doneIds = newDone)
    }

    fun setOn(id: String, on: Boolean) {
        val newReminders = state.reminders.map {
            if (it.id == id) it.copy(on = on) else it
        }
        state = state.copy(reminders = newReminders)
    }

    fun setMode(id: String, mode: String) {
        val newReminders = state.reminders.map {
            if (it.id == id) it.copy(mode = mode) else it
        }
        state = state.copy(reminders = newReminders)
    }

    fun setQuiet(on: Boolean) {
        state = state.copy(quietHoursOn = on)
    }

    fun deleteReminder(id: String) {
        val newReminders = state.reminders.filter { it.id != id }
        val newDone = state.doneIds.filter { it != id }
        state = state.copy(reminders = newReminders, doneIds = newDone)
    }

    fun updateReminder(id: String, title: String, time: String, repeat: String, mode: String) {
        val newReminders = state.reminders.map {
            if (it.id == id) it.copy(title = title, time = time, repeat = repeat, mode = mode) else it
        }
        state = state.copy(reminders = newReminders)
    }

    fun addReminder(title: String, time: String, repeat: String, mode: String) {
        val id = "r_" + getUniqueId()
        val reminder = Reminder(id, title, time, repeat, mode, dueToday = true, on = true)
        state = state.copy(reminders = state.reminders + reminder)
    }

    fun consumeFridge(id: String) {
        val newFridge = state.fridge.filter { it.id != id }
        state = state.copy(fridge = newFridge)
    }

    fun updateFridge(id: String, name: String, qty: String, expiry: String, location: String) {
        val newFridge = state.fridge.map {
            if (it.id == id) it.copy(name = name, qty = qty, expiry = expiry, location = location) else it
        }
        state = state.copy(fridge = newFridge)
    }

    fun addFridge(name: String, qty: String, expiry: String, location: String) {
        val id = "f_" + getUniqueId()
        val item = FridgeItem(id, name, qty, expiry, location)
        state = state.copy(fridge = state.fridge + item)
    }

    fun nudgeFromFridge(itemName: String) {
        addReminder(
            title = "Use the ${itemName.lowercase()}",
            time = "18:00",
            repeat = "",
            mode = "banner"
        )
    }

    fun toggleShoppingItem(id: String) {
        val newList = state.shoppingList.map {
            if (it.id == id) it.copy(checked = !it.checked) else it
        }
        state = state.copy(shoppingList = newList)
    }

    fun deleteShoppingItem(id: String) {
        val newList = state.shoppingList.filter { it.id != id }
        state = state.copy(shoppingList = newList)
    }

    fun updateShoppingItem(id: String, name: String, qty: String) {
        val newList = state.shoppingList.map {
            if (it.id == id) it.copy(name = name, qty = qty) else it
        }
        state = state.copy(shoppingList = newList)
    }

    fun addShoppingItem(name: String, qty: String) {
        val id = "s_" + getUniqueId()
        val item = ShoppingListItem(id, name, qty, checked = false)
        state = state.copy(shoppingList = state.shoppingList + item)
    }

    fun clearCheckedShoppingItems() {
        val newList = state.shoppingList.filter { !it.checked }
        state = state.copy(shoppingList = newList)
    }

    fun restockFridgeItem(id: String) {
        val item = state.fridge.firstOrNull { it.id == id } ?: return
        val alreadyListed = state.shoppingList.any { !it.checked && it.name.equals(item.name, ignoreCase = true) }
        val newList = if (alreadyListed) {
            state.shoppingList
        } else {
            state.shoppingList + ShoppingListItem("s_" + getUniqueId(), item.name, item.qty, checked = false)
        }
        state = state.copy(fridge = state.fridge.filter { it.id != id }, shoppingList = newList)
    }

    fun purchaseShoppingItem(itemId: String, location: String, expiry: String) {
        val item = state.shoppingList.firstOrNull { it.id == itemId } ?: return
        val newFridge = state.fridge + FridgeItem("f_" + getUniqueId(), item.name, item.qty, expiry, location)
        state = state.copy(fridge = newFridge, shoppingList = state.shoppingList.filter { it.id != itemId })
    }

    fun purchaseCheckedShoppingItems(location: String, expiry: String) {
        val checked = state.shoppingList.filter { it.checked }
        if (checked.isEmpty()) return
        val newFridge = state.fridge + checked.map { FridgeItem("f_" + getUniqueId(), it.name, it.qty, expiry, location) }
        state = state.copy(fridge = newFridge, shoppingList = state.shoppingList.filter { !it.checked })
    }

    fun addRecipe(name: String, ingredients: List<RecipeIngredient>, steps: List<String>) {
        val id = "rc_" + getUniqueId()
        val recipe = Recipe(id, name, ingredients, steps)
        state = state.copy(recipes = state.recipes + recipe)
    }

    fun updateRecipe(id: String, name: String, ingredients: List<RecipeIngredient>, steps: List<String>) {
        val newRecipes = state.recipes.map {
            if (it.id == id) it.copy(name = name, ingredients = ingredients, steps = steps) else it
        }
        state = state.copy(recipes = newRecipes)
    }

    fun deleteRecipe(id: String) {
        state = state.copy(recipes = state.recipes.filter { it.id != id })
    }

    fun sendRecipeToShoppingList(id: String) {
        val recipe = state.recipes.firstOrNull { it.id == id } ?: return
        val existingNames = (state.fridge.map { it.name.trim().lowercase() } +
            state.shoppingList.filter { !it.checked }.map { it.name.trim().lowercase() }).toSet()
        val missing = recipe.ingredients
            .distinctBy { it.name.trim().lowercase() }
            .filter { it.name.trim().isNotEmpty() && !existingNames.contains(it.name.trim().lowercase()) }
        if (missing.isEmpty()) return
        val newItems = missing.map { ShoppingListItem("s_" + getUniqueId(), it.name.trim(), it.qty.trim().ifEmpty { "1" }, checked = false) }
        state = state.copy(shoppingList = state.shoppingList + newItems)
    }

    // --- Meal planner ---

    /** Assigns a recipe to a day's slot, replacing whatever was there (one per slot). */
    fun setMealSlot(date: String, slot: String, recipeId: String) {
        val others = state.mealPlan.filter { !(it.date == date && it.slot == slot) }
        val entry = MealPlanEntry("mp_" + getUniqueId(), date, slot, recipeId)
        state = state.copy(mealPlan = others + entry)
    }

    fun clearMealSlot(date: String, slot: String) {
        state = state.copy(mealPlan = state.mealPlan.filter { !(it.date == date && it.slot == slot) })
    }

    /** Adds the ingredients of every recipe planned in the next [days] days to the
     *  shopping list, smart-skipping anything already in the fridge or on the list. */
    fun sendPlannedMealsToShoppingList(days: Int = 7) {
        val window = (0 until days).map { DateUtils.getTodayPlusDays(it) }.toSet()
        val recipeIds = state.mealPlan.filter { it.date in window }.map { it.recipeId }.toSet()
        if (recipeIds.isEmpty()) return
        val ingredients = state.recipes.filter { it.id in recipeIds }.flatMap { it.ingredients }
        val existingNames = (state.fridge.map { it.name.trim().lowercase() } +
            state.shoppingList.filter { !it.checked }.map { it.name.trim().lowercase() }).toSet()
        val seen = mutableSetOf<String>()
        val missing = ingredients.filter {
            val key = it.name.trim().lowercase()
            it.name.trim().isNotEmpty() && key !in existingNames && seen.add(key)
        }
        if (missing.isEmpty()) return
        val newItems = missing.map { ShoppingListItem("s_" + getUniqueId(), it.name.trim(), it.qty.trim().ifEmpty { "1" }, checked = false) }
        state = state.copy(shoppingList = state.shoppingList + newItems)
    }

    fun setAccent(accent: String) {
        state = state.copy(settings = state.settings.copy(accent = accent))
    }

    fun setDarkMode(on: Boolean) {
        state = state.copy(settings = state.settings.copy(darkMode = on))
    }

    fun setShowFlourishes(on: Boolean) {
        state = state.copy(settings = state.settings.copy(showFlourishes = on))
    }

    fun setBackgroundPattern(pattern: String) {
        state = state.copy(settings = state.settings.copy(backgroundPattern = pattern))
    }

    fun reset() {
        // Resets demo data but keeps the user's appearance settings
        state = getSeedState().copy(settings = state.settings)
    }

    /** Reconciles per-day reminder state with the calendar. Call on app open.
     *  Once the date changes: clears the "done" checkmarks of repeating reminders
     *  (they recur) while leaving one-shot completions intact, and recomputes the
     *  deterministic repeats' due-today flag (daily every day, weekdays Mon–Fri).
     *  No-op if it already ran today. */
    fun rolloverIfNeeded() {
        val today = DateUtils.getTodayPlusDays(0)
        if (state.lastRolloverDate == today) return

        val weekend = DateUtils.isTodayWeekend()
        val newReminders = state.reminders.map { r ->
            val due = when (r.repeat) {
                "daily" -> true
                "weekdays" -> !weekend
                else -> r.dueToday // weekly/monthly anchors and one-shots are left as-is
            }
            if (due == r.dueToday) r else r.copy(dueToday = due)
        }
        val repeatingIds = state.reminders.filter { it.repeat.isNotEmpty() }.map { it.id }.toSet()
        val newDone = state.doneIds.filter { !repeatingIds.contains(it) }

        state = state.copy(reminders = newReminders, doneIds = newDone, lastRolloverDate = today)
    }

    // --- Persistence helper ---


    private fun loadState(): ToolboxState {
        val raw = storage.getString(storageKey) ?: return getSeedState()
        return try {
            deserializeState(raw)
        } catch (e: Exception) {
            getSeedState()
        }
    }

    private fun saveState(s: ToolboxState) {
        try {
            storage.saveString(storageKey, serializeState(s))
        } catch (e: Exception) {
            // Ignore
        }
    }

    // --- Helper math & seed generator ---

    private fun getUniqueId(): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..6)
            .map { allowedChars.random() }
            .joinToString("")
    }

    // A small, realistic starter set shown on first launch and after a reset.
    // Intentionally minimal and example-like so new users can clear it quickly.
    private fun getSeedState(): ToolboxState {
        return ToolboxState(
            quietHoursOn = true,
            reminders = listOf(
                Reminder("r1", "Drink some water", "10:00", "daily", "banner", dueToday = true, on = true),
                Reminder("r2", "Tidy up for five minutes", "19:00", "daily", "badge", dueToday = true, on = true)
            ),
            doneIds = emptyList(),
            fridge = listOf(
                FridgeItem("f1", "Milk", "1 carton", DateUtils.getTodayPlusDays(2), "fridge"),
                FridgeItem("f2", "Leftovers", "1 box", DateUtils.getTodayPlusDays(5), "fridge")
            ),
            shoppingList = listOf(
                ShoppingListItem("s1", "Bananas", "1 bunch", false),
                ShoppingListItem("s2", "Bread", "1 loaf", false)
            ),
            recipes = listOf(
                Recipe(
                    id = "rc1",
                    name = "Creamy tomato pasta",
                    ingredients = listOf(
                        RecipeIngredient("Pasta", "200 g"),
                        RecipeIngredient("Tomatoes", "2"),
                        RecipeIngredient("Milk", "splash"),
                        RecipeIngredient("Garlic", "2 cloves")
                    ),
                    steps = listOf(
                        "Boil the pasta until just tender.",
                        "Soften the garlic, add chopped tomatoes and a splash of milk.",
                        "Toss the pasta through the sauce and serve."
                    )
                ),
                Recipe(
                    id = "rc2",
                    name = "Leftovers night",
                    ingredients = listOf(
                        RecipeIngredient("Leftovers", "1 box")
                    ),
                    steps = listOf(
                        "Reheat the leftovers and enjoy a no-cook evening."
                    )
                )
            ),
            mealPlan = listOf(
                MealPlanEntry("mp1", DateUtils.getTodayPlusDays(0), "dinner", "rc1")
            )
        )
    }

    // --- Custom Text-based Serialization ---

    private fun escape(s: String): String = s.replace("\\", "\\\\").replace("|", "\\p").replace("\n", "\\n")
    private fun unescape(s: String): String = s.replace("\\n", "\n").replace("\\p", "|").replace("\\\\", "\\")

    private fun serializeState(s: ToolboxState): String {
        val sb = StringBuilder()
        sb.append("QUIETHOURS=").append(s.quietHoursOn).append("\n")
        sb.append("DONEIDS=").append(s.doneIds.joinToString(",")).append("\n")
        sb.append("ACCENT=").append(escape(s.settings.accent)).append("\n")
        sb.append("DARKMODE=").append(s.settings.darkMode).append("\n")
        sb.append("FLOURISHES=").append(s.settings.showFlourishes).append("\n")
        sb.append("PATTERN=").append(escape(s.settings.backgroundPattern)).append("\n")
        sb.append("UPDATEDAT=").append(s.updatedAt).append("\n")
        sb.append("ROLLOVER=").append(escape(s.lastRolloverDate)).append("\n")
        sb.append("[REMINDERS]\n")
        for (r in s.reminders) {
            sb.append(escape(r.id)).append("|")
              .append(escape(r.title)).append("|")
              .append(escape(r.time)).append("|")
              .append(escape(r.repeat)).append("|")
              .append(escape(r.mode)).append("|")
              .append(r.dueToday).append("|")
              .append(r.on).append("\n")
        }
        sb.append("[FRIDGE]\n")
        for (f in s.fridge) {
            sb.append(escape(f.id)).append("|")
              .append(escape(f.name)).append("|")
              .append(escape(f.qty)).append("|")
              .append(escape(f.expiry)).append("|")
              .append(escape(f.location)).append("\n")
        }
        sb.append("[SHOPPING]\n")
        for (sh in s.shoppingList) {
            sb.append(escape(sh.id)).append("|")
              .append(escape(sh.name)).append("|")
              .append(escape(sh.qty)).append("|")
              .append(sh.checked).append("\n")
        }
        sb.append("[RECIPES]\n")
        // Each recipe is a multi-line record: an R header line followed by
        // its I (ingredient) and S (step) lines, until the next R line
        for (rc in s.recipes) {
            sb.append("R|").append(escape(rc.id)).append("|").append(escape(rc.name)).append("\n")
            for (ing in rc.ingredients) {
                sb.append("I|").append(escape(ing.name)).append("|").append(escape(ing.qty)).append("\n")
            }
            for (step in rc.steps) {
                sb.append("S|").append(escape(step)).append("\n")
            }
        }
        sb.append("[MEALPLAN]\n")
        for (mp in s.mealPlan) {
            sb.append(escape(mp.id)).append("|")
              .append(escape(mp.date)).append("|")
              .append(escape(mp.slot)).append("|")
              .append(escape(mp.recipeId)).append("\n")
        }
        return sb.toString()
    }

    private fun deserializeState(raw: String): ToolboxState {
        var quietHoursOn = true
        var doneIds = listOf<String>()
        var settings = AppSettings()
        var updatedAt = 0L
        var lastRolloverDate = ""
        val reminders = mutableListOf<Reminder>()
        val fridge = mutableListOf<FridgeItem>()
        val shoppingList = mutableListOf<ShoppingListItem>()
        val recipes = mutableListOf<Recipe>()
        val mealPlan = mutableListOf<MealPlanEntry>()

        var section = ""
        val lines = raw.split("\n")
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                section = trimmed
                continue
            }
            if (section.isEmpty()) {
                if (trimmed.startsWith("QUIETHOURS=")) {
                    quietHoursOn = trimmed.substringAfter("QUIETHOURS=").toBoolean()
                } else if (trimmed.startsWith("DONEIDS=")) {
                    val rawIds = trimmed.substringAfter("DONEIDS=")
                    doneIds = if (rawIds.isEmpty()) emptyList() else rawIds.split(",")
                } else if (trimmed.startsWith("ACCENT=")) {
                    settings = settings.copy(accent = unescape(trimmed.substringAfter("ACCENT=")))
                } else if (trimmed.startsWith("DARKMODE=")) {
                    settings = settings.copy(darkMode = trimmed.substringAfter("DARKMODE=").toBoolean())
                } else if (trimmed.startsWith("FLOURISHES=")) {
                    settings = settings.copy(showFlourishes = trimmed.substringAfter("FLOURISHES=").toBoolean())
                } else if (trimmed.startsWith("PATTERN=")) {
                    settings = settings.copy(backgroundPattern = unescape(trimmed.substringAfter("PATTERN=")))
                } else if (trimmed.startsWith("UPDATEDAT=")) {
                    updatedAt = trimmed.substringAfter("UPDATEDAT=").toLongOrNull() ?: 0L
                } else if (trimmed.startsWith("ROLLOVER=")) {
                    lastRolloverDate = unescape(trimmed.substringAfter("ROLLOVER="))
                }
            } else if (section == "[REMINDERS]") {
                val parts = trimmed.split("|")
                if (parts.size >= 7) {
                    reminders.add(
                        Reminder(
                            id = unescape(parts[0]),
                            title = unescape(parts[1]),
                            time = unescape(parts[2]),
                            repeat = unescape(parts[3]),
                            mode = unescape(parts[4]),
                            dueToday = parts[5].toBoolean(),
                            on = parts[6].toBoolean()
                        )
                    )
                }
            } else if (section == "[FRIDGE]") {
                val parts = trimmed.split("|")
                if (parts.size >= 5) {
                    fridge.add(
                        FridgeItem(
                            id = unescape(parts[0]),
                            name = unescape(parts[1]),
                            qty = unescape(parts[2]),
                            expiry = unescape(parts[3]),
                            location = unescape(parts[4])
                        )
                    )
                }
            } else if (section == "[SHOPPING]") {
                val parts = trimmed.split("|")
                if (parts.size >= 4) {
                    shoppingList.add(
                        ShoppingListItem(
                            id = unescape(parts[0]),
                            name = unescape(parts[1]),
                            qty = unescape(parts[2]),
                            checked = parts[3].toBoolean()
                        )
                    )
                }
            } else if (section == "[RECIPES]") {
                val parts = trimmed.split("|")
                if (parts.isNotEmpty()) {
                    when (parts[0]) {
                        "R" -> if (parts.size >= 3) {
                            recipes.add(Recipe(
                                id = unescape(parts[1]),
                                name = unescape(parts[2]),
                                ingredients = emptyList(),
                                steps = emptyList()
                            ))
                        }
                        "I" -> if (parts.size >= 2 && recipes.isNotEmpty()) {
                            val last = recipes.last()
                            val qty = if (parts.size >= 3) unescape(parts[2]) else ""
                            recipes[recipes.lastIndex] = last.copy(
                                ingredients = last.ingredients + RecipeIngredient(unescape(parts[1]), qty)
                            )
                        }
                        "S" -> if (parts.size >= 2 && recipes.isNotEmpty()) {
                            val last = recipes.last()
                            recipes[recipes.lastIndex] = last.copy(steps = last.steps + unescape(parts[1]))
                        }
                    }
                }
            } else if (section == "[MEALPLAN]") {
                val parts = trimmed.split("|")
                if (parts.size >= 4) {
                    mealPlan.add(
                        MealPlanEntry(
                            id = unescape(parts[0]),
                            date = unescape(parts[1]),
                            slot = unescape(parts[2]),
                            recipeId = unescape(parts[3])
                        )
                    )
                }
            }
        }
        return ToolboxState(quietHoursOn, reminders, doneIds, fridge, shoppingList, recipes, mealPlan, settings, updatedAt, lastRolloverDate)
    }
}
