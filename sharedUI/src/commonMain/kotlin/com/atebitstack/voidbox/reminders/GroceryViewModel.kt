package com.atebitstack.voidbox.reminders

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class GroceryViewModel(
    private val groceryStore: GroceryStore = InMemoryGroceryStore,
) : ViewModel() {

    private val _groceries = MutableStateFlow(groceryStore.loadGroceries())
    val groceries: StateFlow<List<GroceryItem>> get() = _groceries

    private val _showAddSheet = MutableStateFlow(false)
    val showAddSheet: StateFlow<Boolean> get() = _showAddSheet

    private val _editingGrocery = MutableStateFlow<GroceryItem?>(null)
    val editingGrocery: StateFlow<GroceryItem?> get() = _editingGrocery

    private var nextId = (_groceries.value.maxOfOrNull { it.id } ?: 0) + 1

    var groceryScheduler: GroceryNotificationScheduler? = null
        set(value) {
            field = value
            if (value != null) {
                _groceries.value.forEach(value::schedule)
            }
        }

    fun showAddGrocery() {
        _editingGrocery.value = null
        _showAddSheet.value = true
    }

    fun showEditGrocery(grocery: GroceryItem) {
        _editingGrocery.value = grocery
        _showAddSheet.value = true
    }

    fun dismissAddSheet() {
        _showAddSheet.value = false
        _editingGrocery.value = null
    }

    fun addGrocery(name: String, expirationDate: kotlinx.datetime.LocalDate) {
        val grocery = GroceryItem(
            id = nextId++,
            name = name,
            expirationDate = expirationDate,
        )
        _groceries.update { current ->
            (current + grocery).also(groceryStore::saveGroceries)
        }
        groceryScheduler?.schedule(grocery)
        _showAddSheet.value = false
    }

    fun updateGrocery(id: Int, name: String, expirationDate: kotlinx.datetime.LocalDate) {
        _groceries.update { list ->
            list.map { item ->
                if (item.id == id) {
                    val updated = item.copy(name = name, expirationDate = expirationDate)
                    groceryScheduler?.cancel(item)
                    groceryScheduler?.schedule(updated)
                    updated
                } else item
            }.also(groceryStore::saveGroceries)
        }
        _showAddSheet.value = false
        _editingGrocery.value = null
    }

    fun deleteGrocery(groceryId: Int) {
        _groceries.update { list ->
            val item = list.find { it.id == groceryId }
            if (item != null) {
                groceryScheduler?.cancel(item)
            }
            list.filter { it.id != groceryId }.also(groceryStore::saveGroceries)
        }
    }

    fun removeExpired() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        _groceries.update { list ->
            list.filter { item ->
                if (item.expirationDate < today) {
                    groceryScheduler?.cancel(item)
                    false
                } else true
            }.also(groceryStore::saveGroceries)
        }
    }
}