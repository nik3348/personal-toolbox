package com.atebitstack.voidbox.reminders

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private enum class AppTab { Home, Reminders }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    onReminderSchedulerReady: (ReminderViewModel) -> Unit = {},
    reminderStore: ReminderStore = InMemoryReminderStore,
    viewModel: ReminderViewModel = viewModel { ReminderViewModel(reminderStore) },
) {
    LaunchedEffect(Unit) {
        onReminderSchedulerReady(viewModel)
    }

    val reminders by viewModel.reminders.collectAsState()
    val showAddSheet by viewModel.showAddSheet.collectAsState()
    val editingReminder by viewModel.editingReminder.collectAsState()
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
            color = MaterialTheme.colorScheme.background,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(DarkBackground, Color(0xFF0A0A14)),
                        ),
                    ),
            ) {
                DecorativeGlowBackground()

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
                                icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") },
                                label = { Text("Home") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = AccentPurple,
                                    selectedTextColor = AccentPurple,
                                    indicatorColor = AccentPurple.copy(alpha = 0.15f),
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary,
                                ),
                            )
                            NavigationBarItem(
                                selected = currentTab == AppTab.Reminders,
                                onClick = { currentTab = AppTab.Reminders },
                                icon = { Icon(Icons.Outlined.Notifications, contentDescription = "Reminders") },
                                label = { Text("Reminders") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = AccentPurple,
                                    selectedTextColor = AccentPurple,
                                    indicatorColor = AccentPurple.copy(alpha = 0.15f),
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary,
                                ),
                            )
                        }
                    },
                    floatingActionButton = {
                        if (currentTab == AppTab.Reminders) {
                            FloatingActionButton(
                                onClick = { viewModel.showAddReminder() },
                                containerColor = AccentPurple,
                                contentColor = TextPrimary,
                                shape = CircleShape,
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Add reminder")
                            }
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
                            onEditReminder = viewModel::showEditReminder,
                            modifier = Modifier.padding(innerPadding),
                        )
                    }
                }
            }
        }

        if (showAddSheet) {
            ReminderSheet(
                editing = editingReminder,
                onDismiss = viewModel::dismissAddSheet,
                onConfirm = { hour, minute, label, description ->
                    viewModel.addReminder(hour, minute, label, description)
                },
                onUpdate = { id, hour, minute, label, description ->
                    viewModel.updateReminder(id, hour, minute, label, description)
                },
            )
        }
    }
}

// ── Decorative Background ────────────────────────────────────────────────────

@Composable
private fun DecorativeGlowBackground() {
    Box(
        modifier = Modifier
            .offset(x = (-80).dp, y = (-60).dp)
            .size(260.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(AccentPurple.copy(alpha = 0.22f), Color.Transparent),
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
                    listOf(AccentCyan.copy(alpha = 0.12f), Color.Transparent),
                ),
            ),
    )
}

// ── Reminders Tab ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RemindersTabScreen(
    reminders: List<Reminder>,
    onToggleReminder: (Int) -> Unit,
    onDeleteReminder: (Int) -> Unit,
    onEditReminder: (Reminder) -> Unit,
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
                text = "Swipe right to edit, swipe left to delete.",
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
                        text = "No reminders yet. Tap + to add one.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        } else {
            items(reminders, key = { it.id }) { reminder ->
                SwipeableReminderCard(
                    reminder = reminder,
                    onToggle = { onToggleReminder(reminder.id) },
                    onDelete = { onDeleteReminder(reminder.id) },
                    onEdit = { onEditReminder(reminder) },
                )
            }
        }

        // Bottom spacer so FAB doesn't obscure last item
        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

// ── Swipeable Reminder Card ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableReminderCard(
    reminder: Reminder,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onEdit()
                    false // don't dismiss, just trigger edit
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    true
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection

            val backgroundColor by animateColorAsState(
                when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> AccentCyan.copy(alpha = 0.25f)
                    SwipeToDismissBoxValue.EndToStart -> Color(0xFF802020)
                    else -> Color.Transparent
                },
            )

            val icon = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Icons.Filled.Edit
                SwipeToDismissBoxValue.EndToStart -> Icons.Filled.Delete
                else -> Icons.Filled.Edit
            }

            val iconTint = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> AccentCyan
                SwipeToDismissBoxValue.EndToStart -> Color(0xFFFF6B6B)
                else -> Color.Transparent
            }

            val alignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                else -> Alignment.CenterEnd
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(backgroundColor)
                    .padding(horizontal = 24.dp),
                contentAlignment = alignment,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(28.dp),
                )
            }
        },
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
    ) {
        ReminderCard(reminder = reminder, onToggle = onToggle)
    }
}

// ── Reminder Card (AlarmCard style) ─────────────────────────────────────────

@Composable
private fun ReminderCard(reminder: Reminder, onToggle: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = if (reminder.enabled) 1f else 0.98f,
        animationSpec = spring(),
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation = if (reminder.enabled) 10.dp else 3.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = AccentPurple.copy(alpha = if (reminder.enabled) 0.25f else 0f),
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.enabled) DarkCard else DarkCardDisabled,
        ),
        border = BorderStroke(
            1.dp,
            if (reminder.enabled) CardBorderEnabled else CardBorderDisabled,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Colored accent bar
            Box(
                modifier = Modifier
                    .size(width = 4.dp, height = 56.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.verticalGradient(
                            if (reminder.enabled) {
                                listOf(AccentPurple, AccentCyan)
                            } else {
                                listOf(TextDisabled, TextDisabled)
                            },
                        ),
                    ),
            )

            Spacer(Modifier.width(14.dp))

            // Time + label + description
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    reminder.timeFormatted,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.W200,
                    color = if (reminder.enabled) TextPrimary else TextDisabled,
                    letterSpacing = 0.5.sp,
                    lineHeight = 44.sp,
                )

                if (reminder.label.isNotBlank()) {
                    Text(
                        reminder.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (reminder.enabled) TextPrimary.copy(alpha = 0.9f) else TextDisabled,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (reminder.description.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Notes,
                            contentDescription = null,
                            tint = TextSecondary.copy(alpha = if (reminder.enabled) 0.7f else 0.4f),
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            reminder.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary.copy(alpha = if (reminder.enabled) 0.85f else 0.5f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            Switch(
                checked = reminder.enabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = AccentPurple,
                    checkedThumbColor = Color.White,
                    uncheckedTrackColor = DarkSurface,
                    uncheckedThumbColor = TextDisabled,
                ),
            )
        }
    }
}

// ── Add / Edit Reminder Bottom Sheet ─────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderSheet(
    editing: Reminder?,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int, label: String, description: String) -> Unit,
    onUpdate: (id: Int, hour: Int, minute: Int, label: String, description: String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val initialHour = editing?.hour ?: 8
    val initialMinute = editing?.minute ?: 0
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false,
    )
    var label by remember { mutableStateOf(editing?.label ?: "") }
    var description by remember { mutableStateOf(editing?.description ?: "") }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AccentPurple,
        unfocusedBorderColor = DarkSurfaceHigh,
        cursorColor = AccentPurple,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        focusedLabelColor = AccentCyan,
        unfocusedLabelColor = TextSecondary,
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        contentColor = TextPrimary,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = if (editing != null) "Edit Reminder" else "New Reminder",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
            )

            TimePicker(
                state = timePickerState,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Label") },
                placeholder = { Text("Reminder") },
                singleLine = true,
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = { Text("Optional") },
                singleLine = true,
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = TextSecondary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (editing != null) {
                            onUpdate(
                                editing.id,
                                timePickerState.hour,
                                timePickerState.minute,
                                label,
                                description,
                            )
                        } else {
                            onConfirm(
                                timePickerState.hour,
                                timePickerState.minute,
                                label,
                                description,
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentPurple,
                        contentColor = Color.White,
                    ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(if (editing != null) "Update" else "Save")
                }
            }
        }
    }
}
