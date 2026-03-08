package com.atebitstack.voidbox.reminders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Typography
import androidx.lifecycle.viewmodel.compose.viewModel

// ── Palette (mirrors HomeScreen.kt) ──────────────────────────────────────────
private val DarkBackground   = Color(0xFF0D0D1A)
private val DarkSurface      = Color(0xFF17172A)
private val DarkSurfaceHigh  = Color(0xFF232341)
private val AccentPurple     = Color(0xFF7C5CFC)
private val AccentCyan       = Color(0xFF4FD1C5)
private val TextPrimary      = Color(0xFFEEEEF8)
private val TextSecondary    = Color(0xFFB0B0CF)

private val AppTypography = Typography(
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 30.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.2).sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.2.sp,
    ),
)

private enum class AppTab {
    Home,
    Reminders,
}

@Composable
fun App(
    onReminderSchedulerReady: (ReminderViewModel) -> Unit = {},
    viewModel: ReminderViewModel = viewModel { ReminderViewModel() }
) {
    LaunchedEffect(Unit) {
        onReminderSchedulerReady(viewModel)
    }

    val reminders by viewModel.reminders.collectAsState()
    var currentTab by remember { mutableStateOf(AppTab.Home) }

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = DarkBackground,
            surface = DarkSurface,
            primary = AccentPurple,
            secondary = AccentCyan,
            tertiary = DarkSurfaceHigh,
            onBackground = TextPrimary,
            onSurface = TextPrimary,
            onPrimary = TextPrimary,
            onSecondary = DarkBackground,
            onTertiary = TextSecondary,
        ),
        typography = AppTypography,
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                DarkBackground,
                                Color(0xFF0A0A14),
                            ),
                        ),
                    ),
            ) {
                // Decorative glow layers add depth while keeping content legible.
                Box(
                    modifier = Modifier
                        .offset(x = (-80).dp, y = (-60).dp)
                        .size(260.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    AccentPurple.copy(alpha = 0.22f),
                                    Color.Transparent,
                                ),
                            ),
                        ),
                )
                Box(
                    modifier = Modifier
                        .offset(x = 220.dp, y = 340.dp)
                        .size(220.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    AccentCyan.copy(alpha = 0.12f),
                                    Color.Transparent,
                                ),
                            ),
                        ),
                )

                Scaffold(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    bottomBar = {
                        NavigationBar(
                            containerColor = DarkSurface.copy(alpha = 0.95f),
                            tonalElevation = 0.dp,
                        ) {
                            NavigationBarItem(
                                selected = currentTab == AppTab.Home,
                                onClick = { currentTab = AppTab.Home },
                                icon = {
                                    Icon(Icons.Outlined.Home, contentDescription = "Home")
                                },
                                label = { Text("Home") },
                            )
                            NavigationBarItem(
                                selected = currentTab == AppTab.Reminders,
                                onClick = { currentTab = AppTab.Reminders },
                                icon = {
                                    Icon(Icons.Outlined.Notifications, contentDescription = "Reminders")
                                },
                                label = { Text("Reminders") },
                            )
                        }
                    },
                ) { innerPadding ->
                    when (currentTab) {
                        AppTab.Home -> HomeScreen(
                            reminderCount = reminders.count { it.enabled },
                            totalReminders = reminders.size,
                            onNavigateToReminders = { currentTab = AppTab.Reminders },
                            modifier = Modifier.padding(innerPadding),
                        )

                        AppTab.Reminders -> RemindersTabScreen(
                            reminders = reminders,
                            onToggleReminder = viewModel::toggleReminder,
                            onDeleteReminder = viewModel::deleteReminder,
                            modifier = Modifier.padding(innerPadding),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RemindersTabScreen(
    reminders: List<Reminder>,
    onToggleReminder: (Int) -> Unit,
    onDeleteReminder: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "Reminders",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Manage, toggle, or delete your scheduled reminders.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }

        if (reminders.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceHigh.copy(alpha = 0.65f)),
                ) {
                    Text(
                        text = "No reminders yet. Add one from your existing reminder creation flow.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        } else {
            items(reminders, key = { it.id }) { reminder ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceHigh.copy(alpha = 0.72f)),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = reminder.label,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${reminder.hour.toString().padStart(2, '0')}:${reminder.minute.toString().padStart(2, '0')}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AccentCyan,
                            )
                            if (reminder.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = reminder.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.SpaceBetween) {
                            Switch(
                                checked = reminder.enabled,
                                onCheckedChange = { onToggleReminder(reminder.id) },
                            )
                            IconButton(onClick = { onDeleteReminder(reminder.id) }) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Delete reminder",
                                    tint = TextSecondary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
