package com.eightbitstack.toolbox

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ToolboxRepositoryTest {

    @Test
    fun testDefaultStateSeeding() {
        val repo = ToolboxRepository(MockStorage())
        val state = repo.state
        assertTrue(state.quietHoursOn)
        // Production starter set: a small, example-like seed (not the old demo data).
        assertEquals(2, state.reminders.size)
        assertTrue(state.doneIds.isEmpty())
        assertEquals(2, state.fridge.size)
        assertEquals(2, state.shoppingList.size)
    }

    @Test
    fun testRolloverClearsRepeatingDoneButKeepsOneShot() {
        val repo = ToolboxRepository(MockStorage())
        // A repeating (daily) and a one-shot reminder, both marked done today.
        repo.addReminder("Daily thing", "08:00", "daily", "banner")
        repo.addReminder("Once thing", "08:00", "", "banner")
        val daily = repo.state.reminders.first { it.title == "Daily thing" }
        val once = repo.state.reminders.first { it.title == "Once thing" }
        repo.toggleDone(daily.id)
        repo.toggleDone(once.id)
        assertTrue(repo.state.doneIds.contains(daily.id))
        assertTrue(repo.state.doneIds.contains(once.id))

        // A fresh repo has never rolled over (lastRolloverDate is empty), so the
        // first call runs and reconciles done-state as if the day changed.
        repo.rolloverIfNeeded()

        // Repeating reminder's checkmark resets; one-shot completion is preserved.
        assertTrue(!repo.state.doneIds.contains(daily.id))
        assertTrue(repo.state.doneIds.contains(once.id))
    }

    @Test
    fun testRolloverIsNoOpWhenAlreadyRunToday() {
        val repo = ToolboxRepository(MockStorage())
        repo.rolloverIfNeeded()
        repo.addReminder("Daily", "08:00", "daily", "banner")
        val daily = repo.state.reminders.first { it.title == "Daily" }
        repo.toggleDone(daily.id)
        // Same day: another rollover should not wipe today's done state.
        repo.rolloverIfNeeded()
        assertTrue(repo.state.doneIds.contains(daily.id))
    }

    @Test
    fun testRolloverDateRoundTrips() {
        val storage = MockStorage()
        val repo = ToolboxRepository(storage)
        repo.rolloverIfNeeded()
        val stamped = repo.state.lastRolloverDate
        assertTrue(stamped.isNotEmpty())

        val repo2 = ToolboxRepository(storage)
        assertEquals(stamped, repo2.state.lastRolloverDate)
    }

    @Test
    fun testUpdatedAtStampedOnMutationAndRoundTrips() {
        val storage = MockStorage()
        val repo = ToolboxRepository(storage)
        // Fresh seed has never been stamped.
        assertEquals(0L, repo.state.updatedAt)

        repo.addShoppingItem("Marker", "1")
        assertTrue(repo.state.updatedAt > 0L, "mutation should stamp updatedAt")

        // Persisted timestamp survives a reload.
        val repo2 = ToolboxRepository(storage)
        assertEquals(repo.state.updatedAt, repo2.state.updatedAt)
    }

    @Test
    fun testToggleDone() {
        val repo = ToolboxRepository(MockStorage())
        repo.toggleDone("r1")
        assertTrue(repo.state.doneIds.contains("r1"))
        repo.toggleDone("r1")
        assertTrue(!repo.state.doneIds.contains("r1"))
    }

    @Test
    fun testAddReminder() {
        val repo = ToolboxRepository(MockStorage())
        repo.addReminder("Test Nudge", "08:00", "daily", "banner")
        val found = repo.state.reminders.firstOrNull { it.title == "Test Nudge" }
        assertTrue(found != null)
        assertEquals("08:00", found.time)
        assertEquals("daily", found.repeat)
        assertEquals("banner", found.mode)
    }

    @Test
    fun testShoppingListOperations() {
        val repo = ToolboxRepository(MockStorage())
        
        // Add
        repo.addShoppingItem("Apples", "6")
        val apples = repo.state.shoppingList.firstOrNull { it.name == "Apples" }
        assertTrue(apples != null)
        assertEquals("6", apples.qty)
        assertTrue(!apples.checked)

        // Toggle
        repo.toggleShoppingItem(apples.id)
        assertTrue(repo.state.shoppingList.first { it.id == apples.id }.checked)

        // Update
        repo.updateShoppingItem(apples.id, "Organic Apples", "10")
        val updatedApples = repo.state.shoppingList.first { it.id == apples.id }
        assertEquals("Organic Apples", updatedApples.name)
        assertEquals("10", updatedApples.qty)

        // Purchase / Move to fridge
        val originalFridgeSize = repo.state.fridge.size
        repo.purchaseShoppingItem(apples.id, "fridge", "2026-06-15")
        assertEquals(originalFridgeSize + 1, repo.state.fridge.size)
        assertTrue(repo.state.fridge.any { it.name == "Organic Apples" && it.qty == "10" && it.location == "fridge" })

        // Clear checked
        repo.clearCheckedShoppingItems()
        assertTrue(repo.state.shoppingList.none { it.id == apples.id })
    }

    @Test
    fun testPurchaseShoppingItemMovesToFridge() {
        val repo = ToolboxRepository(MockStorage())
        repo.addShoppingItem("Lemons", "4")
        val lemons = repo.state.shoppingList.first { it.name == "Lemons" }

        repo.purchaseShoppingItem(lemons.id, "pantry", "2026-07-01")

        assertTrue(repo.state.shoppingList.none { it.id == lemons.id })
        assertTrue(repo.state.fridge.any { it.name == "Lemons" && it.location == "pantry" && it.expiry == "2026-07-01" })
    }

    @Test
    fun testRestockFridgeItem() {
        val repo = ToolboxRepository(MockStorage())
        repo.addFridge("Hummus", "1 tub", "2026-06-20", "fridge")
        val hummus = repo.state.fridge.first { it.name == "Hummus" }

        repo.restockFridgeItem(hummus.id)

        assertTrue(repo.state.fridge.none { it.id == hummus.id })
        val listed = repo.state.shoppingList.first { it.name == "Hummus" }
        assertEquals("1 tub", listed.qty)
        assertTrue(!listed.checked)
    }

    @Test
    fun testRestockSkipsDuplicateAlreadyOnList() {
        val repo = ToolboxRepository(MockStorage())
        repo.addShoppingItem("Hummus", "2 tubs")
        repo.addFridge("hummus", "1 tub", "2026-06-20", "fridge")
        val fridgeItem = repo.state.fridge.first { it.name == "hummus" }

        repo.restockFridgeItem(fridgeItem.id)

        assertTrue(repo.state.fridge.none { it.id == fridgeItem.id })
        assertEquals(1, repo.state.shoppingList.count { it.name.equals("hummus", ignoreCase = true) })
    }

    @Test
    fun testPurchaseCheckedShoppingItemsMovesAllToFridge() {
        val repo = ToolboxRepository(MockStorage())
        repo.addShoppingItem("Bagels", "6")
        repo.addShoppingItem("Cream cheese", "1 tub")
        repo.addShoppingItem("Capers", "1 jar")
        val bagels = repo.state.shoppingList.first { it.name == "Bagels" }
        val creamCheese = repo.state.shoppingList.first { it.name == "Cream cheese" }
        repo.toggleShoppingItem(bagels.id)
        repo.toggleShoppingItem(creamCheese.id)

        repo.purchaseCheckedShoppingItems("freezer", "2026-08-01")

        assertTrue(repo.state.fridge.any { it.name == "Bagels" && it.location == "freezer" && it.expiry == "2026-08-01" })
        assertTrue(repo.state.fridge.any { it.name == "Cream cheese" && it.location == "freezer" })
        assertTrue(repo.state.shoppingList.none { it.checked })
        assertTrue(repo.state.shoppingList.any { it.name == "Capers" })
    }

    @Test
    fun testSerializationRoundTripWithSpecialCharacters() {
        val storage = MockStorage()
        val repo = ToolboxRepository(storage)
        repo.addShoppingItem("Salt | pepper \\ mix", "1")
        repo.addFridge("Multi\nline", "2", "2026-06-20", "pantry")
        repo.addReminder("Pipes | and \\ slashes", "07:15", "weekly", "badge")

        val repo2 = ToolboxRepository(storage)

        assertTrue(repo2.state.shoppingList.any { it.name == "Salt | pepper \\ mix" })
        assertTrue(repo2.state.fridge.any { it.name == "Multi\nline" && it.location == "pantry" })
        val reminder = repo2.state.reminders.first { it.title == "Pipes | and \\ slashes" }
        assertEquals("07:15", reminder.time)
        assertEquals("weekly", reminder.repeat)
    }

    @Test
    fun testSettingsRoundTrip() {
        val storage = MockStorage()
        val repo = ToolboxRepository(storage)
        repo.setAccent("forest")
        repo.setDarkMode(true)
        repo.setShowFlourishes(false)
        repo.setBackgroundPattern("dots")

        val repo2 = ToolboxRepository(storage)

        assertEquals(AppSettings("forest", true, false, "dots"), repo2.state.settings)
    }

    @Test
    fun testResetKeepsSettings() {
        val repo = ToolboxRepository(MockStorage())
        repo.setAccent("sunset")
        repo.setDarkMode(true)
        repo.addShoppingItem("Scratch item", "1")

        repo.reset()

        assertEquals("sunset", repo.state.settings.accent)
        assertTrue(repo.state.settings.darkMode)
        assertTrue(repo.state.shoppingList.none { it.name == "Scratch item" })
    }

    @Test
    fun testRecipeOperations() {
        val repo = ToolboxRepository(MockStorage())

        repo.addRecipe(
            "Pasta",
            listOf(RecipeIngredient("Spaghetti", "200 g"), RecipeIngredient("Garlic", "2 cloves")),
            listOf("Boil the pasta.", "Fry the garlic and toss together.")
        )
        val pasta = repo.state.recipes.first { it.name == "Pasta" }
        assertEquals(2, pasta.ingredients.size)
        assertEquals(2, pasta.steps.size)

        repo.updateRecipe(
            pasta.id,
            "Garlic pasta",
            pasta.ingredients + RecipeIngredient("Chilli flakes", "1 pinch"),
            pasta.steps
        )
        val updated = repo.state.recipes.first { it.id == pasta.id }
        assertEquals("Garlic pasta", updated.name)
        assertEquals(3, updated.ingredients.size)

        repo.deleteRecipe(pasta.id)
        assertTrue(repo.state.recipes.none { it.id == pasta.id })
    }

    @Test
    fun testRecipeSerializationRoundTrip() {
        val storage = MockStorage()
        val repo = ToolboxRepository(storage)
        repo.addRecipe(
            "Tricky | recipe \\ name",
            listOf(RecipeIngredient("Salt | flakes", "1 tsp")),
            listOf("Step with | pipe", "Step with\nnewline")
        )

        val repo2 = ToolboxRepository(storage)

        val recipe = repo2.state.recipes.first { it.name == "Tricky | recipe \\ name" }
        assertEquals(RecipeIngredient("Salt | flakes", "1 tsp"), recipe.ingredients.single())
        assertEquals(listOf("Step with | pipe", "Step with\nnewline"), recipe.steps)
    }

    @Test
    fun testSendRecipeToShoppingListOnlyAddsMissing() {
        val repo = ToolboxRepository(MockStorage())
        // Set up the scenario explicitly so it doesn't depend on seed contents:
        // "Tofu" is in the fridge; "Coffee beans" is already on the list.
        repo.addFridge("Tofu", "1 block", "2026-12-31", "fridge")
        repo.addShoppingItem("Coffee beans", "1 bag")
        repo.addRecipe(
            "Test meal",
            listOf(
                RecipeIngredient("Tofu", "1 block"),
                RecipeIngredient("Coffee beans", "1 bag"),
                RecipeIngredient("Coconut milk", "1 can")
            ),
            listOf("Cook it.")
        )
        val recipe = repo.state.recipes.first { it.name == "Test meal" }
        val sizeBefore = repo.state.shoppingList.size

        repo.sendRecipeToShoppingList(recipe.id)

        assertEquals(sizeBefore + 1, repo.state.shoppingList.size)
        assertTrue(repo.state.shoppingList.any { it.name == "Coconut milk" && !it.checked })
    }

    @Test
    fun testGarbageStorageDoesNotCrash() {
        val storage = MockStorage()
        storage.saveString("toolbox-state-v1", "not a valid state\n[GARBAGE]\n???|x")
        val repo = ToolboxRepository(storage)
        // Lenient parser skips unparseable lines rather than throwing
        assertTrue(repo.state.reminders.isEmpty())
        assertTrue(repo.state.quietHoursOn)
    }

    @Test
    fun testPersistence() {
        val storage = MockStorage()
        val repo = ToolboxRepository(storage)
        repo.toggleDone("r1") // modify state
        
        // Re-instantiate repo with same storage to check if modified state is loaded
        val repo2 = ToolboxRepository(storage)
        assertTrue(repo2.state.doneIds.contains("r1"))
    }
}

class MockStorage : StorageProvider {
    private val map = mutableMapOf<String, String>()
    
    override fun saveString(key: String, value: String) {
        map[key] = value
    }

    override fun getString(key: String): String? {
        return map[key]
    }
}
// Note: Overriding expect class is allowed in Kotlin multiplatform test mock as long as the mock has the same methods.
// To avoid compilation errors about expect class sub-classing, we can make KeyValueStorage open or define an interface, 
// but since KeyValueStorage is expect class, let's make sure it compiles. If expect class cannot be subclassed, 
// MockStorage can just implement a simple delegate or be defined natively. Let's see if this compiles!
