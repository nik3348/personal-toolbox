@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.eightbitstack.toolbox

interface StorageProvider {
    fun saveString(key: String, value: String)
    fun getString(key: String): String?
}

expect class KeyValueStorage() : StorageProvider {
    override fun saveString(key: String, value: String)
    override fun getString(key: String): String?
}

