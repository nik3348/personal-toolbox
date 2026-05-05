package com.atebitstack.voidbox.reminders

import kotlinx.datetime.LocalDate

interface GroceryStore {
    fun loadGroceries(): List<GroceryItem>
    fun saveGroceries(groceries: List<GroceryItem>)
}

object InMemoryGroceryStore : GroceryStore {
    private var groceries: List<GroceryItem> = emptyList()

    override fun loadGroceries(): List<GroceryItem> = groceries

    override fun saveGroceries(groceries: List<GroceryItem>) {
        this.groceries = groceries
    }
}