package com.eightbitstack.toolbox

class ToolboxRepository(private val storage: StorageProvider = KeyValueStorage()) {
    
    interface Listener {
        fun onStateChanged(state: ToolboxState)
    }

    private val listeners = mutableListOf<Listener>()
    
    private val storageKey = "toolbox-state-v1"
    
    var state: ToolboxState = loadState()
        private set(value) {
            field = value
            saveState(value)
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
        val newList = state.shoppingList.map { if (it.id == itemId) it.copy(checked = true) else it }
        state = state.copy(fridge = newFridge, shoppingList = newList)
    }

    fun purchaseCheckedShoppingItems(location: String, expiry: String) {
        val checked = state.shoppingList.filter { it.checked }
        if (checked.isEmpty()) return
        val newFridge = state.fridge + checked.map { FridgeItem("f_" + getUniqueId(), it.name, it.qty, expiry, location) }
        state = state.copy(fridge = newFridge, shoppingList = state.shoppingList.filter { !it.checked })
    }

    fun reset() {
        state = getSeedState()
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

    private fun getSeedState(): ToolboxState {
        return ToolboxState(
            quietHoursOn = true,
            reminders = listOf(
                Reminder("r1", "Water the plants", "09:00", "daily", "banner", true, true),
                Reminder("r2", "Stand up + stretch", "11:30", "weekdays", "badge", true, true),
                Reminder("r3", "Reply to Maya about the trip", "14:00", "", "banner", true, true),
                Reminder("r4", "Thaw chicken for dinner", "17:30", "", "buzz", true, true),
                Reminder("r5", "Library books due", "10:00", "", "banner", false, true),
                Reminder("r6", "Pay rent", "09:00", "monthly", "banner", false, false),
                Reminder("r7", "Wind down — close laptop", "22:00", "daily", "silent", true, true)
            ),
            doneIds = listOf("r2"),
            fridge = listOf(
                FridgeItem("f1", "Milk, oat", "½ carton", DateUtils.getTodayPlusDays(1), "fridge"),
                FridgeItem("f2", "Tofu", "1 block", DateUtils.getTodayPlusDays(3), "fridge"),
                FridgeItem("f3", "Kale", "1 bag", DateUtils.getTodayPlusDays(4), "fridge"),
                FridgeItem("f4", "Yogurt", "4-pack", DateUtils.getTodayPlusDays(5), "fridge"),
                FridgeItem("f5", "Salsa verde", "1 jar", DateUtils.getTodayPlusDays(8), "fridge"),
                FridgeItem("f6", "Eggs", "8 left", DateUtils.getTodayPlusDays(9), "fridge"),
                FridgeItem("f7", "Butter", "1 stick", DateUtils.getTodayPlusDays(12), "fridge"),
                FridgeItem("f8", "Cheddar", "~200 g", DateUtils.getTodayPlusDays(21), "fridge"),
                FridgeItem("f9", "Frozen peas", "1 bag", DateUtils.getTodayPlusDays(60), "freezer"),
                FridgeItem("f10", "Frozen berries", "1 bag", DateUtils.getTodayPlusDays(90), "freezer")
            ),
            shoppingList = listOf(
                ShoppingListItem("s1", "Coffee beans", "1 bag", false),
                ShoppingListItem("s2", "Avocados", "3", false),
                ShoppingListItem("s3", "Bread", "1 loaf", false),
                ShoppingListItem("s4", "Olive oil", "1 bottle", true)
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
        return sb.toString()
    }

    private fun deserializeState(raw: String): ToolboxState {
        var quietHoursOn = true
        var doneIds = listOf<String>()
        val reminders = mutableListOf<Reminder>()
        val fridge = mutableListOf<FridgeItem>()
        val shoppingList = mutableListOf<ShoppingListItem>()

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
            }
        }
        return ToolboxState(quietHoursOn, reminders, doneIds, fridge, shoppingList)
    }
}
