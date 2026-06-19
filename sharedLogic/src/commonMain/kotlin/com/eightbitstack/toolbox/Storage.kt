@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.eightbitstack.toolbox

/**
 * Single persistence seam for the app. Today the only implementation is the
 * local key-value [KeyValueStorage] (SharedPreferences on Android, NSUserDefaults
 * on iOS). A future cloud sync can plug in here as a `RemoteStorageProvider`
 * (or a composite that mirrors local + remote) without touching the repository;
 * `ToolboxState.updatedAt` is carried for last-write-wins conflict resolution.
 */
interface StorageProvider {
    fun saveString(key: String, value: String)
    fun getString(key: String): String?
}

expect class KeyValueStorage() : StorageProvider {
    override fun saveString(key: String, value: String)
    override fun getString(key: String): String?
}

