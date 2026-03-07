package com.atebitstack.toolbox.reminders

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.AlarmAdd
import androidx.compose.material.icons.outlined.AlarmOff
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.cos
import kotlin.math.sin

// ── Color Palette ──────────────────────────────────────────────────────────
private val DarkBackground    = Color(0xFF0D0D1A)
private val DarkSurface       = Color(0xFF17172A)
private val DarkCard          = Color(0xFF1E1E33)
private val DarkCardDisabled  = Color(0xFF141424)
private val AccentPurple      = Color(0xFF7C5CFC)
private val AccentPurpleLight = Color(0xFFAA8FFF)
private val AccentCyan        = Color(0xFF4FD1C5)
private val TextPrimary       = Color(0xFFEEEEF8)
private val TextSecondary     = Color(0xFF9090B0)
private val TextDisabled      = Color(0xFF50506A)
private val DeleteRed         = Color(0xFFFF6B6B)
private val CardBorderEnabled = Color(0xFF3A3060)
private val CardBorderDisabled= Color(0xFF2A2A40)

private val AppColorScheme = darkColorScheme(
    primary = AccentPurple,
    onPrimary = Color.White,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    outline = CardBorderEnabled,
)

// ── Screen Enum ────────────────────────────────────────────────────────────
private enum class Screen { HOME, REMINDERS }

// ── Main App ───────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    onAlarmSchedulerReady: ((AlarmViewModel) -> Unit)? = null,
    alarmViewModel: AlarmViewModel = viewModel(),
) {
    LaunchedEffect(alarmViewModel) {
        onAlarmSchedulerReady?.invoke(alarmViewModel)
    }

    MaterialTheme(colorScheme = AppColorScheme) {
        val alarms by alarmViewModel.alarms.collectAsStateWithLifecycle()
        val showAddSheet by alarmViewModel.showAddSheet.collectAsStateWithLifecycle()
        val editingAlarm by alarmViewModel.editingAlarm.collectAsStateWithLifecycle()
        var currentScreen by remember { mutableStateOf(Screen.HOME) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground),
        ) {
            AnimatedBackgroundOrbs()

            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = when (currentScreen) {
                                        Screen.HOME -> Icons.Filled.Home
                                        Screen.REMINDERS -> Icons.Outlined.Alarm
                                    },
                                    contentDescription = null,
                                    tint = AccentPurple,
                                    modifier = Modifier.size(26.dp),
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    when (currentScreen) {
                                        Screen.HOME -> "Toolbox"
                                        Screen.REMINDERS -> "Silent Alarms"
                                    },
                                    fontWeight = FontWeight.Black,
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            scrolledContainerColor = DarkSurface,
                            titleContentColor = TextPrimary,
                        ),
                        windowInsets = WindowInsets(0, 0, 0, 0),
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = DarkSurface,
                        tonalElevation = 0.dp,
                    ) {
                        NavigationBarItem(
                            selected = currentScreen == Screen.HOME,
                            onClick = { currentScreen = Screen.HOME },
                            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                            label = { Text("Home") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AccentPurple,
                                selectedTextColor = AccentPurple,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary,
                                indicatorColor = AccentPurple.copy(alpha = 0.15f),
                            ),
                        )
                        NavigationBarItem(
                            selected = currentScreen == Screen.REMINDERS,
                            onClick = { currentScreen = Screen.REMINDERS },
                            icon = { Icon(Icons.Outlined.Alarm, contentDescription = "Reminders") },
                            label = { Text("Reminders") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AccentPurple,
                                selectedTextColor = AccentPurple,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary,
                                indicatorColor = AccentPurple.copy(alpha = 0.15f),
                            ),
                        )
                    }
                },
                floatingActionButton = {
                    if (currentScreen == Screen.REMINDERS) {
                        FloatingActionButton(
                            onClick = { alarmViewModel.showAddAlarm() },
                            shape = CircleShape,
                            containerColor = AccentPurple,
                            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 12.dp),
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Add alarm", tint = Color.White)
                        }
                    }
                },
            ) { paddingValues ->
                when (currentScreen) {
                    Screen.HOME -> HomeScreen(
                        alarmCount = alarms.count { it.enabled },
                        totalAlarms = alarms.size,
                        onNavigateToReminders = { currentScreen = Screen.REMINDERS },
                        modifier = Modifier.padding(paddingValues),
                    )
                    Screen.REMINDERS -> AnimatedContent(
                        targetState = alarms.isEmpty(),
                        transitionSpec = {
                            fadeIn(tween(350)) + scaleIn(tween(350), initialScale = 0.96f) togetherWith
                                fadeOut(tween(250)) + scaleOut(tween(250), targetScale = 0.98f)
                        },
                    ) { empty ->
                        if (empty) {
                            EmptyState(Modifier.padding(paddingValues))
                        } else {
                            AlarmList(
                                alarms = alarms,
                                onToggle = { alarmViewModel.toggleAlarm(it) },
                                onEdit = { alarmViewModel.showEditAlarm(it) },
                                onDelete = { alarmViewModel.deleteAlarm(it) },
                                modifier = Modifier.padding(paddingValues),
                            )
                        }
                    }
                }
            }
        }

        if (showAddSheet) {
            val alarmToEdit = editingAlarm
            AddEditAlarmSheet(
                editingAlarm = alarmToEdit,
                onDismiss = { alarmViewModel.dismissAddSheet() },
                onConfirm = { hour, minute, label, description ->
                    if (alarmToEdit != null) {
                        alarmViewModel.updateAlarm(alarmToEdit.id, hour, minute, label, description)
                    } else {
                        alarmViewModel.addAlarm(hour, minute, label, description)
                    }
                },
            )
        }
    }
}

// ── Animated Background Orbs ───────────────────────────────────────────────
@Composable
private fun AnimatedBackgroundOrbs() {
    val transition = rememberInfiniteTransition()
    val angle1 by transition.animateFloat(0f, 360f, infiniteRepeatable(tween(20000, easing = LinearEasing)))
    val angle2 by transition.animateFloat(360f, 0f, infiniteRepeatable(tween(28000, easing = LinearEasing)))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val cx = size.width / 2
                val cy = size.height / 2
                val r = size.minDimension / 2.8f

                val x1 = cx + r * cos(Math.toRadians(angle1.toDouble())).toFloat()
                val y1 = cy + r * sin(Math.toRadians(angle1.toDouble())).toFloat() * 0.5f
                val x2 = cx + r * cos(Math.toRadians(angle2.toDouble())).toFloat()
                val y2 = cy + r * sin(Math.toRadians(angle2.toDouble())).toFloat() * 0.5f

                drawCircle(
                    Brush.radialGradient(listOf(AccentPurple.copy(alpha = 0.13f), Color.Transparent)),
                    320f,
                    Offset(x1, y1),
                )
                drawCircle(
                    Brush.radialGradient(listOf(AccentCyan.copy(alpha = 0.09f), Color.Transparent)),
                    260f,
                    Offset(x2, y2),
                )
            },
    )
}

// ── Empty State ────────────────────────────────────────────────────────────
@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition()
    val pulse by transition.animateFloat(
        1f,
        1.06f,
        infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
    )

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 48.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(pulse)
                    .shadow(24.dp, CircleShape, ambientColor = AccentPurple.copy(alpha = 0.3f))
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                AccentPurple.copy(alpha = 0.22f),
                                AccentPurple.copy(alpha = 0.06f),
                                Color.Transparent,
                            ),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.AlarmOff,
                    contentDescription = null,
                    tint = AccentPurpleLight,
                    modifier = Modifier.size(52.dp),
                )
            }

            Spacer(Modifier.height(28.dp))

            Text(
                "No alarms yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )

            Spacer(Modifier.height(10.dp))

            Text(
                "Tap + to add your first silent alarm and stay notified without disruption",
                color = TextSecondary,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
            )
        }
    }
}

// ── Alarm List ─────────────────────────────────────────────────────────────
@Composable
private fun AlarmList(
    alarms: List<Alarm>,
    onToggle: (Int) -> Unit,
    onEdit: (Alarm) -> Unit,
    onDelete: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(alarms, key = { it.id }) { alarm ->
            AnimatedVisibility(visible = true, enter = fadeIn() + slideInVertically { it / 4 }) {
                SwipeableAlarmCard(
                    alarm = alarm,
                    onToggle = { onToggle(alarm.id) },
                    onEdit = { onEdit(alarm) },
                    onDelete = { onDelete(alarm.id) },
                )
            }
        }
        item { Spacer(modifier = Modifier.height(88.dp)) }
    }
}

// ── Swipeable Alarm Card ───────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableAlarmCard(
    alarm: Alarm,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val state = rememberSwipeToDismissBoxState()

    LaunchedEffect(state.currentValue) {
        when (state.currentValue) {
            SwipeToDismissBoxValue.StartToEnd -> {
                onEdit()
                state.snapTo(SwipeToDismissBoxValue.Settled)
            }
            SwipeToDismissBoxValue.EndToStart -> onDelete()
            SwipeToDismissBoxValue.Settled -> Unit
        }
    }

    SwipeToDismissBox(
        state = state,
        backgroundContent = {
            val direction = state.dismissDirection
            val shape = RoundedCornerShape(24.dp)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .background(
                        when (direction) {
                            SwipeToDismissBoxValue.StartToEnd -> AccentPurple.copy(alpha = 0.85f)
                            SwipeToDismissBoxValue.EndToStart -> DeleteRed.copy(alpha = 0.88f)
                            SwipeToDismissBoxValue.Settled -> Color.Transparent
                        },
                    )
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = when (direction) {
                    SwipeToDismissBoxValue.EndToStart -> Arrangement.End
                    else -> Arrangement.Start
                },
            ) {
                when (direction) {
                    SwipeToDismissBoxValue.EndToStart -> {
                        Icon(Icons.Filled.Delete, null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Delete", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                    SwipeToDismissBoxValue.StartToEnd -> {
                        Icon(Icons.Filled.Edit, null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Edit", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                    else -> {}
                }
            }
        },
    ) {
        AlarmCard(alarm = alarm, onToggle = onToggle)
    }
}

// ── Alarm Card ─────────────────────────────────────────────────────────────
@Composable
private fun AlarmCard(alarm: Alarm, onToggle: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = if (alarm.enabled) 1f else 0.98f,
        animationSpec = spring(),
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation = if (alarm.enabled) 10.dp else 3.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = AccentPurple.copy(alpha = if (alarm.enabled) 0.25f else 0f),
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (alarm.enabled) DarkCard else DarkCardDisabled,
        ),
        border = BorderStroke(
            1.dp,
            if (alarm.enabled) CardBorderEnabled else CardBorderDisabled,
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
                            if (alarm.enabled) {
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
                    alarm.timeFormatted,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.W200,
                    color = if (alarm.enabled) TextPrimary else TextDisabled,
                    letterSpacing = 0.5.sp,
                    lineHeight = 44.sp,
                )

                if (alarm.label.isNotBlank()) {
                    Text(
                        alarm.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (alarm.enabled) TextPrimary.copy(alpha = 0.9f) else TextDisabled,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (alarm.description.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Notes,
                            contentDescription = null,
                            tint = TextSecondary.copy(alpha = if (alarm.enabled) 0.7f else 0.4f),
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            alarm.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary.copy(alpha = if (alarm.enabled) 0.85f else 0.5f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            Switch(
                checked = alarm.enabled,
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

// ── Add/Edit Alarm Bottom Sheet ────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditAlarmSheet(
    editingAlarm: Alarm?,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int, label: String, description: String) -> Unit,
) {
    val isEditing = editingAlarm != null
    val timePickerState = rememberTimePickerState(
        initialHour = editingAlarm?.hour ?: 8,
        initialMinute = editingAlarm?.minute ?: 0,
        is24Hour = false,
    )
    var label by remember { mutableStateOf(editingAlarm?.label ?: "") }
    var description by remember { mutableStateOf(editingAlarm?.description ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = DarkSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(TextDisabled.copy(alpha = 0.5f)),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Sheet header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isEditing) Icons.Filled.Edit else Icons.Outlined.AlarmAdd,
                    contentDescription = null,
                    tint = AccentPurpleLight,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    if (isEditing) "Edit Alarm" else "New Alarm",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                if (isEditing) "Update your alarm details" else "Choose a time and add a label",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )

            Spacer(Modifier.height(20.dp))

            // Time picker
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = DarkBackground.copy(alpha = 0.8f),
                border = BorderStroke(1.dp, CardBorderEnabled),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier.padding(8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = DarkCard,
                            selectorColor = AccentPurple,
                            containerColor = Color.Transparent,
                            clockDialSelectedContentColor = Color.White,
                            clockDialUnselectedContentColor = TextSecondary,
                            timeSelectorSelectedContainerColor = AccentPurple.copy(alpha = 0.3f),
                            timeSelectorUnselectedContainerColor = DarkCard,
                            timeSelectorSelectedContentColor = Color.White,
                            timeSelectorUnselectedContentColor = TextSecondary,
                        ),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Alarm Name") },
                placeholder = { Text("e.g. Morning workout") },
                leadingIcon = {
                    Icon(Icons.AutoMirrored.Outlined.Label, contentDescription = null, modifier = Modifier.size(20.dp))
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentPurple,
                    unfocusedBorderColor = CardBorderEnabled,
                    focusedLabelColor = AccentPurpleLight,
                    focusedContainerColor = DarkCard.copy(alpha = 0.4f),
                    unfocusedContainerColor = DarkBackground.copy(alpha = 0.4f),
                ),
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Note (optional)") },
                placeholder = { Text("e.g. Take vitamins, check oven") },
                leadingIcon = {
                    Icon(Icons.AutoMirrored.Outlined.Notes, contentDescription = null, modifier = Modifier.size(20.dp))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 88.dp),
                maxLines = 3,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentPurple,
                    unfocusedBorderColor = CardBorderEnabled,
                    focusedLabelColor = AccentPurpleLight,
                    focusedContainerColor = DarkCard.copy(alpha = 0.4f),
                    unfocusedContainerColor = DarkBackground.copy(alpha = 0.4f),
                ),
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    onConfirm(timePickerState.hour, timePickerState.minute, label, description)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
            ) {
                Icon(Icons.Filled.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isEditing) "Update Alarm" else "Set Alarm",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                )
            }

            TextButton(onClick = onDismiss, modifier = Modifier.padding(bottom = 4.dp)) {
                Text("Cancel", color = TextSecondary)
            }
        }
    }
}
