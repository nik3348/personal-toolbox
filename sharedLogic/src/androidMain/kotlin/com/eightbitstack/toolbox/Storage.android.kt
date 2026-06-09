@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.eightbitstack.toolbox

import android.content.Context

actual class KeyValueStorage actual constructor() : StorageProvider {
    private val sharedPrefs by lazy {
        AndroidContext.applicationContext?.getSharedPreferences("toolbox_prefs", Context.MODE_PRIVATE)
    }

    actual override fun saveString(key: String, value: String) {
        sharedPrefs?.edit()?.putString(key, value)?.apply()
    }

    actual override fun getString(key: String): String? {
        return sharedPrefs?.getString(key, null)
    }
}
