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
        assertEquals(7, state.reminders.size)
        assertEquals(1, state.doneIds.size)
        assertEquals("r2", state.doneIds.first())
        assertEquals(10, state.fridge.size)
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
