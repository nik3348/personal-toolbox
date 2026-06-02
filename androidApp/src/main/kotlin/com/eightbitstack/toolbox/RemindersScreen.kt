package com.eightbitstack.toolbox

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RemindersScreen(
    state: ToolboxState,
    onSetQuiet: (Boolean) -> Unit,
    onSetOn: (String, Boolean) -> Unit,
    onSetMode: (String, String) -> Unit,
    onDelete: (String) -> Unit,
    onSaveReminder: (id: String?, title: String, time: String, repeat: String, mode: String) -> Unit
) {
    var filter by remember { mutableStateOf("all") }
    var expandedId by remember { mutableStateOf<String?>(null) }
    var sheetOpen by remember { mutableStateOf(false) }
    var editFor by remember { mutableStateOf<Reminder?>(null) }

    val all = state.reminders
    val counts = mapOf(
        "all" to all.size,
        "today" to all.filter { it.dueToday && it.on }.size,
        "repeat" to all.filter { it.repeat.isNotEmpty() }.size,
        "off" to all.filter { !it.on }.size
    )

    val visible = all.filter { r ->
        when (filter) {
            "all" -> true
            "today" -> r.on && r.dueToday
            "repeat" -> r.repeat.isNotEmpty()
            "off" -> !r.on
            else -> true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(top = 10.dp, bottom = 130.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title Header
            item {
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                    Kicker(text = "Silent · No ring", color = ToolboxTheme.activePalette.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Quiet nudges",
                        fontFamily = ToolboxTheme.serif,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Medium,
                        color = ToolboxTheme.ink,
                        lineHeight = 34.sp
                    )

                    // Quiet hours card banner
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(ToolboxTheme.activePaletteTint)
                            .border(1.dp, ToolboxTheme.activePaletteBorder, RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ToolboxTheme.surface)
                                        .border(1.dp, ToolboxTheme.activePaletteBorder, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🌙", fontSize = 16.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Quiet hours · 10 PM – 7 AM",
                                        fontFamily = ToolboxTheme.sans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = ToolboxTheme.ink
                                    )
                                    Text(
                                        text = "Reminders held silently until morning",
                                        fontFamily = ToolboxTheme.sans,
                                        fontSize = 11.sp,
                                        color = ToolboxTheme.inkSoft
                                    )
                                }
                            }
                            Toggle(value = state.quietHoursOn, onChange = onSetQuiet, size = "sm")
                        }
                    }
                }
            }

            // Filters row
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val labels = listOf("all" to "All", "today" to "Today", "repeat" to "Repeating", "off" to "Off")
                    items(labels) { (key, label) ->
                        FilterChip(
                            active = filter == key,
                            onClick = { filter = key },
                            text = label,
                            count = counts[key]
                        )
                    }
                }
            }

            // Reminders list
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (visible.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp)
                                .border(1.5.dp, ToolboxTheme.line, RoundedCornerShape(14.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Nothing in this filter.", color = ToolboxTheme.inkMute, fontSize = 13.sp)
                        }
                    } else {
                        visible.forEach { r ->
                            val expanded = expandedId == r.id
                            ReminderCard(
                                r = r,
                                expanded = expanded,
                                onToggle = { onSetOn(r.id, it) },
                                onExpand = { expandedId = if (expanded) null else r.id },
                                onSetMode = { onSetMode(r.id, it) },
                                onDelete = {
                                    expandedId = null
                                    onDelete(r.id)
                                },
                                onEditDetails = {
                                    editFor = r
                                    sheetOpen = true
                                    expandedId = null
                                }
                            )
                        }
                    }
                }
            }
        }

        // Floating Action Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 108.dp, end = 12.dp)
        ) {
            ChunkyButton(
                onClick = {
                    editFor = null
                    sheetOpen = true
                },
                text = "Nudge",
                size = "sm",
                icon = { Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                modifier = Modifier.width(80.dp)
            )
        }

        // Add/Edit Bottom Sheet Modal
        Sheet(
            open = sheetOpen,
            onClose = { sheetOpen = false },
            title = if (editFor != null) "Edit nudge" else "New quiet nudge"
        ) {
            ReminderForm(
                initial = editFor,
                onSave = { title, time, repeatVal, modeVal ->
                    onSaveReminder(editFor?.id, title, time, repeatVal, modeVal)
                    sheetOpen = false
                },
                onCancel = { sheetOpen = false }
            )
        }
    }
}

@Composable
fun ReminderCard(
    r: Reminder,
    expanded: Boolean,
    onToggle: (Boolean) -> Unit,
    onExpand: () -> Unit,
    onSetMode: (String) -> Unit,
    onDelete: () -> Unit,
    onEditDetails: () -> Unit
) {
    val scheduleStr = if (r.repeat.isNotEmpty()) {
        "${format12hTime(r.time)} · ${r.repeat}"
    } else {
        "${format12hTime(r.time)} · ${if (r.dueToday) "today" else "once"}"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ToolboxTheme.surface)
            .border(
                1.dp,
                if (expanded) ToolboxTheme.activePalette.border else ToolboxTheme.line,
                RoundedCornerShape(16.dp)
            )
            .clickable { onExpand() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = r.title,
                    fontFamily = ToolboxTheme.serif,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Medium,
                    color = ToolboxTheme.ink,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Tag(
                        text = scheduleStr,
                        icon = { Text("🕒", fontSize = 10.sp) }
                    )
                    Tag(
                        text = r.mode,
                        bg = ToolboxTheme.activePaletteTint,
                        color = ToolboxTheme.activePalette.primary,
                        icon = { Text("🔕", fontSize = 10.sp) }
                    )
                }
            }
            Toggle(value = r.on, onChange = onToggle)
        }

        if (expanded) {
            HorizontalDivider(color = ToolboxTheme.line, thickness = 1.dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ToolboxTheme.bg)
                    .padding(14.dp)
            ) {
                Kicker(text = "How it alerts", color = ToolboxTheme.inkMute)
                Spacer(modifier = Modifier.height(8.dp))
                
                // Grid of modes
                val alertModes = listOf(
                    "banner" to Pair("Banner Only", "Slides in, doesn't ring"),
                    "badge" to Pair("Badge Only", "Just a red dot on the app"),
                    "buzz" to Pair("One Buzz", "Single haptic, no sound"),
                    "silent" to Pair("Fully Silent", "In the list only")
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    alertModes.chunked(2).forEach { rowModes ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            rowModes.forEach { (modeKey, modeInfo) ->
                                val active = r.mode == modeKey
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (active) ToolboxTheme.activePaletteTint else ToolboxTheme.surface)
                                        .border(
                                            1.5.dp,
                                            if (active) ToolboxTheme.activePaletteBorder else ToolboxTheme.line,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { onSetMode(modeKey) }
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = when (modeKey) {
                                            "banner" -> "🔔"
                                            "badge" -> "✨"
                                            "buzz" -> "📳"
                                            else -> "🔕"
                                        },
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = modeInfo.first,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ToolboxTheme.ink
                                        )
                                        Text(
                                            text = modeInfo.second,
                                            fontSize = 10.sp,
                                            color = ToolboxTheme.inkMute
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ChunkyButton(
                        onClick = onDelete,
                        text = "Delete",
                        variant = "ghost",
                        size = "sm",
                        shadowColor = ToolboxTheme.danger,
                        modifier = Modifier.wrapContentWidth()
                    )
                    ChunkyButton(
                        onClick = onEditDetails,
                        text = "Edit details",
                        size = "sm",
                        modifier = Modifier.wrapContentWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun ReminderForm(
    initial: Reminder?,
    onSave: (title: String, time: String, repeat: String, mode: String) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var time by remember { mutableStateOf(initial?.time ?: "09:00") }
    var mode by remember { mutableStateOf(initial?.mode ?: "banner") }
    var repeat by remember { mutableStateOf(initial?.repeat ?: "") }

    val canSave = title.trim().isNotEmpty()

    Column(modifier = Modifier.fillMaxWidth()) {
        Field(label = "What") {
            TextInput(value = title, onChange = { title = it }, placeholder = "Water plants")
        }

        Field(label = "Time") {
            // Draw a basic text input for time (HH:MM format)
            TextInput(
                value = time,
                onChange = { if (it.length <= 5) time = it },
                placeholder = "09:00"
            )
        }

        Field(label = "Repeat") {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val repeatOptions = listOf("" to "once", "daily" to "daily", "weekdays" to "weekdays", "weekly" to "weekly", "monthly" to "monthly")
                items(repeatOptions) { (optValue, optLabel) ->
                    FilterChip(
                        active = repeat == optValue,
                        onClick = { repeat = optValue },
                        text = optLabel
                    )
                }
            }
        }

        Field(label = "Alert mode", hint = "None of these will make sound.") {
            val alertModes = listOf(
                "banner" to "Banner Only",
                "badge" to "Badge Only",
                "buzz" to "One Buzz",
                "silent" to "Fully Silent"
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                alertModes.chunked(2).forEach { chunk ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        chunk.forEach { (modeKey, modeLabel) ->
                            val active = mode == modeKey
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (active) ToolboxTheme.activePaletteTint else ToolboxTheme.surface)
                                    .border(
                                        1.5.dp,
                                        if (active) ToolboxTheme.activePaletteBorder else ToolboxTheme.line,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { mode = modeKey }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when (modeKey) {
                                        "banner" -> "🔔"
                                        "badge" -> "✨"
                                        "buzz" -> "📳"
                                        else -> "🔕"
                                    },
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = modeLabel,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ToolboxTheme.ink
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            ChunkyButton(onClick = onCancel, text = "Cancel", variant = "outline", modifier = Modifier.weight(1f))
            ChunkyButton(
                onClick = { onSave(title, time, repeat, mode) },
                text = "Save",
                modifier = Modifier.weight(1f),
                enabled = canSave
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}
