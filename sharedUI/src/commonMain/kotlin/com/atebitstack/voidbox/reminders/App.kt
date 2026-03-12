package com.atebitstack.voidbox.reminders

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
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
    var darkTheme by remember { mutableStateOf(false) }

    MaterialTheme(
        colorScheme = if (darkTheme) AppDarkColorScheme else AppLightColorScheme,
        typography = AppTypography,
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = "Voidbox",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                            )
                        },
                        actions = {
                            IconButton(onClick = { darkTheme = !darkTheme }) {
                                Icon(
                                    imageVector = if (darkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                    contentDescription = if (darkTheme) "Switch to light mode" else "Switch to dark mode",
                                )
                            }
                        },
                    )
                },
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = currentTab == AppTab.Home,
                            onClick = { currentTab = AppTab.Home },
                            icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") },
                            label = { Text("Home") },
                        )
                        NavigationBarItem(
                            selected = currentTab == AppTab.Reminders,
                            onClick = { currentTab = AppTab.Reminders },
                            icon = { Icon(Icons.Outlined.Notifications, contentDescription = "Reminders") },
                            label = { Text("Reminders") },
                        )
                    }
                },
                floatingActionButton = {
                    if (currentTab == AppTab.Reminders) {
                        FloatingActionButton(onClick = { viewModel.showAddReminder() }) {
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
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Swipe right to edit, swipe left to delete.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (reminders.isEmpty()) {
            item {
                Card {
                    Text(
                        text = "No reminders yet. Tap + to add one.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        when (dismissState.currentValue) {
            SwipeToDismissBoxValue.StartToEnd -> {
                onEdit()
                dismissState.reset()
            }
            SwipeToDismissBoxValue.EndToStart -> onDelete()
            SwipeToDismissBoxValue.Settled -> Unit
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection

            val backgroundColor by animateColorAsState(
                when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.secondaryContainer
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                    else -> Color.Transparent
                },
            )

            val icon = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Icons.Filled.Edit
                else -> Icons.Filled.Delete
            }

            val iconTint = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.onSecondaryContainer
                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.onErrorContainer
                else -> Color.Transparent
            }

            val alignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                else -> Alignment.CenterEnd
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
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

// ── Reminder Card ────────────────────────────────────────────────────────────

@Composable
private fun ReminderCard(reminder: Reminder, onToggle: () -> Unit) {
    val disabledAlpha = if (reminder.enabled) 1f else 0.38f

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    reminder.timeFormatted,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.W200,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = disabledAlpha),
                    letterSpacing = 0.5.sp,
                    lineHeight = 44.sp,
                )

                if (reminder.label.isNotBlank()) {
                    Text(
                        reminder.label,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = disabledAlpha),
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
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = disabledAlpha),
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            reminder.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = disabledAlpha),
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
    val timePickerState = rememberTimePickerState(
        initialHour = editing?.hour ?: 8,
        initialMinute = editing?.minute ?: 0,
        is24Hour = false,
    )
    var label by remember { mutableStateOf(editing?.label ?: "") }
    var description by remember { mutableStateOf(editing?.description ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
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
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = { Text("Optional") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
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
                ) {
                    Text(if (editing != null) "Update" else "Save")
                }
            }
        }
    }
}
