package com.eightbitstack.toolbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidContext.applicationContext = applicationContext
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            var isDark by remember { mutableStateOf(false) }
            LaunchedEffect(isDark) {
                val window = this@MainActivity.window
                WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = !isDark
            }
            App(onDarkModeChanged = { isDark = it })
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}