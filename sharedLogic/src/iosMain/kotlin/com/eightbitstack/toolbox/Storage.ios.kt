package com.eightbitstack.toolbox

import platform.Foundation.NSUserDefaults

actual class KeyValueStorage actual constructor() : StorageProvider {
    private val defaults = NSUserDefaults.standardUserDefaults

    actual override fun saveString(key: String, value: String) {
        defaults.setObject(value, key)
    }

    actual override fun getString(key: String): String? {
        return defaults.stringForKey(key)
    }
}
