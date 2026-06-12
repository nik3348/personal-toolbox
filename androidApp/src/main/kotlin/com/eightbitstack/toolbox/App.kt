package com.eightbitstack.toolbox

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun App(onDarkModeChanged: ((Boolean) -> Unit)? = null) {
    val repository = remember { ToolboxRepository() }
    var appState by remember { mutableStateOf(repository.state) }

    DisposableEffect(repository) {
        val listener = object : ToolboxRepository.Listener {
            override fun onStateChanged(state: ToolboxState) {
                appState = state
            }
        }
        repository.addListener(listener)
        onDispose {
            repository.removeListener(listener)
        }
    }

    // Settings & customization state (synced locally via the same repository storage in production, or kept locally here)
    var activeTab by remember { mutableStateOf("home") }
    var accent by remember { mutableStateOf("indigo") }
    var showFlourishes by remember { mutableStateOf(true) }
    var backgroundPattern by remember { mutableStateOf("grid") }

    val activePalette = BrandPalettes[accent] ?: IndigoPalette

    var darkMode by remember { mutableStateOf(false) }

    LaunchedEffect(darkMode) {
        onDarkModeChanged?.invoke(darkMode)
    }

    CompositionLocalProvider(
        LocalBrandPalette provides activePalette,
        LocalShowFlourishes provides showFlourishes,
        LocalDarkMode provides darkMode
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ToolboxTheme.bgSubtle)
                .drawBehind {
                    if (backgroundPattern == "grid") {
                        val step = 32.dp.toPx()
                        val lineCol = if (darkMode) Color(0x0AFFFFFF) else Color(0x080F172A)
                        var x = 0f
                        while (x < size.width) {
                            drawLine(lineCol, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.dp.toPx())
                            x += step
                        }
                        var y = 0f
                        while (y < size.height) {
                            drawLine(lineCol, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
                            y += step
                        }
                    } else if (backgroundPattern == "dots") {
                        val step = 24.dp.toPx()
                        val dotCol = if (darkMode) Color(0x15FFFFFF) else Color(0x120F172A)
                        var x = 0f
                        while (x < size.width) {
                            var y = 0f
                            while (y < size.height) {
                                drawCircle(dotCol, radius = 1.5.dp.toPx(), center = Offset(x, y))
                                y += step
                            }
                            x += step
                        }
                    }
                }
        ) {
            // Main content based on active screen tab
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when (activeTab) {
                    "home" -> HomeScreen(
                        state = appState,
                        onNavigate = { activeTab = it },
                        onToggleReminder = { repository.toggleDone(it) },
                        showFlourishes = showFlourishes
                    )
                    "reminders" -> RemindersScreen(
                        state = appState,
                        onSetQuiet = { repository.setQuiet(it) },
                        onSetOn = { id, on -> repository.setOn(id, on) },
                        onSetMode = { id, mode -> repository.setMode(id, mode) },
                        onDelete = { repository.deleteReminder(it) },
                        onSaveReminder = { id, title, time, repeat, mode ->
                            if (id != null) {
                                repository.updateReminder(id, title, time, repeat, mode)
                            } else {
                                repository.addReminder(title, time, repeat, mode)
                            }
                        }
                    )
                    "fridge" -> FridgeScreen(
                        state = appState,
                        onConsume = { repository.consumeFridge(it) },
                        onRestock = { repository.restockFridgeItem(it) },
                        onNudge = { repository.nudgeFromFridge(it) },
                        onSaveItem = { id, name, qty, expiry, location ->
                            if (id != null) {
                                repository.updateFridge(id, name, qty, expiry, location)
                            } else {
                                repository.addFridge(name, qty, expiry, location)
                            }
                        }
                    )
                    "shopping" -> ShoppingListScreen(
                        state = appState,
                        onSaveItem = { id, name, qty ->
                            if (id != null) {
                                repository.updateShoppingItem(id, name, qty)
                            } else {
                                repository.addShoppingItem(name, qty)
                            }
                        },
                        onToggleItem = { repository.toggleShoppingItem(it) },
                        onDeleteItem = { repository.deleteShoppingItem(it) },
                        onClearChecked = { repository.clearCheckedShoppingItems() },
                        onPurchaseItem = { id, location, expiry ->
                            repository.purchaseShoppingItem(id, location, expiry)
                        },
                        onPurchaseChecked = { location, expiry ->
                            repository.purchaseCheckedShoppingItems(location, expiry)
                        }
                    )
                    "me" -> MeScreen(
                        onReset = { repository.reset() },
                        accent = accent,
                        onAccentChange = { accent = it },
                        showFlourishes = showFlourishes,
                        onShowFlourishesChange = { showFlourishes = it },
                        backgroundPattern = backgroundPattern,
                        onBackgroundPatternChange = { backgroundPattern = it },
                        darkMode = darkMode,
                        onDarkModeChange = { darkMode = it }
                    )
                }
            }

            // Tab bar container at the bottom
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(999.dp))
                        .background(ToolboxTheme.surface) // matches light/dark background
                        .border(1.dp, ToolboxTheme.line, RoundedCornerShape(999.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val tabs = listOf(
                        Triple("home", "Home", "🏠"),
                        Triple("reminders", "Quiet", "🔕"),
                        Triple("fridge", "Fridge", "❄"),
                        Triple("shopping", "List", "🛒"),
                        Triple("me", "Me", "👤")
                    )

                    tabs.forEach { (tabId, label, emoji) ->
                        val isActive = activeTab == tabId
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (isActive) activePalette.primary else Color.Transparent)
                                .clickable { activeTab = tabId }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = emoji,
                                    fontSize = 18.sp,
                                    color = if (isActive) Color.White else ToolboxTheme.inkMute
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = label.uppercase(),
                                    fontFamily = ToolboxTheme.mono,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    color = if (isActive) Color.White else ToolboxTheme.inkMute
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}