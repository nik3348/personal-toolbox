package com.eightbitstack.toolbox

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    // Tab a tapped notification wants us to open; observed by setContent below.
    private val requestedTab: MutableState<String?> = mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidContext.applicationContext = applicationContext
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        requestedTab.value = intent?.getStringExtra("navTo")

        ReminderNotifications.ensureChannels(this)
        if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 0)
        }

        setContent {
            var isDark by remember { mutableStateOf(false) }
            LaunchedEffect(isDark) {
                val window = this@MainActivity.window
                WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = !isDark
            }
            App(
                onDarkModeChanged = { isDark = it },
                requestedTab = requestedTab.value,
                onRequestedTabHandled = { requestedTab.value = null }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        requestedTab.value = intent.getStringExtra("navTo")
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}