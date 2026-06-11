package com.eightbitstack.toolbox

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.shape.CircleShape

@Composable
fun HomeScreen(
    state: ToolboxState,
    onNavigate: (String) -> Unit,
    onToggleReminder: (String) -> Unit,
    showFlourishes: Boolean
) {
    val todayReminders = state.reminders
        .filter { it.on && it.dueToday }
        .sortedBy { it.time }

    val expiringSoon = state.fridge
        .map { it to DateUtils.daysUntil(it.expiry) }
        .filter { it.second <= 3 }
        .sortedBy { it.second }

    val currentHour = DateUtils.getCurrentHour()
    val greeting = when {
        currentHour < 5 -> "Up late?"
        currentHour < 12 -> "Good morning"
        currentHour < 18 -> "Afternoon"
        else -> "Evening"
    }

    val dateStr = DateUtils.getTodayFormatted()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 8.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero / Greeting
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ToolboxMark(size = 22.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (showFlourishes) "toolbox" else "8bit toolbox",
                            fontFamily = if (showFlourishes) ToolboxTheme.mono else ToolboxTheme.sans,
                            fontWeight = if (showFlourishes) FontWeight.Normal else FontWeight.ExtraBold,
                            fontSize = if (showFlourishes) 10.sp else 16.sp,
                            letterSpacing = if (showFlourishes) 1.sp else (-0.2).sp,
                            color = ToolboxTheme.ink
                        )
                    }
                    val infiniteTransition = rememberInfiniteTransition()
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1.0f,
                        targetValue = 2.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1400, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        )
                    )
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.8f,
                        targetValue = 0.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1400, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        )
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(start = 6.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .graphicsLayer(
                                        scaleX = pulseScale,
                                        scaleY = pulseScale,
                                        alpha = pulseAlpha
                                    )
                                    .background(ToolboxTheme.ok, CircleShape)
                            )
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(ToolboxTheme.ok, CircleShape)
                            )
                        }
                        Kicker(
                            text = "Online",
                            color = ToolboxTheme.ok
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = greeting,
                    fontFamily = ToolboxTheme.serif,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Medium,
                    color = ToolboxTheme.ink,
                    lineHeight = 36.sp
                )
                Text(
                    text = dateStr,
                    fontFamily = ToolboxTheme.sans,
                    fontSize = 13.sp,
                    color = ToolboxTheme.inkMute,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Today's Nudges
        item {
            DashWidget(
                kicker = "Today's quiet nudges",
                kickerColor = ToolboxTheme.activePalette.primary,
                countText = "${todayReminders.size} of ${state.reminders.filter { it.on }.size}",
                onOpen = { onNavigate("reminders") },
                isEmpty = todayReminders.isEmpty(),
                emptyMsg = "Nothing today. Quiet day."
            ) {
                Column {
                    todayReminders.take(4).forEach { reminder ->
                        val isDone = state.doneIds.contains(reminder.id)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            // Checkbox
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .border(
                                        1.5.dp,
                                        if (isDone) ToolboxTheme.activePalette.primary else ToolboxTheme.line,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .background(if (isDone) ToolboxTheme.activePalette.primary else Color.Transparent)
                                    .clickable { onToggleReminder(reminder.id) }
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isDone) {
                                    Text("✓", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = reminder.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDone) ToolboxTheme.inkMute else ToolboxTheme.ink
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text = format12hTime(reminder.time),
                                        fontFamily = ToolboxTheme.mono,
                                        fontSize = 11.sp,
                                        color = ToolboxTheme.inkMute
                                    )
                                    Text(
                                        text = " · ",
                                        color = ToolboxTheme.line,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "🔕 ${reminder.mode}",
                                        fontFamily = ToolboxTheme.mono,
                                        fontSize = 11.sp,
                                        color = ToolboxTheme.inkMute
                                    )
                                }
                            }
                        }
                    }
                    if (todayReminders.size > 4) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "+ ${todayReminders.size - 4} more",
                            fontSize = 12.sp,
                            color = ToolboxTheme.inkMute
                        )
                    }
                }
            }
        }

        // Expiring Soon
        item {
            DashWidget(
                kicker = "Use these soon",
                kickerColor = ToolboxTheme.warn,
                countText = "${expiringSoon.size} item${if (expiringSoon.size == 1) "" else "s"}",
                onOpen = { onNavigate("fridge") },
                isEmpty = expiringSoon.isEmpty(),
                emptyMsg = "Nothing about to expire. Nice fridge."
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        expiringSoon.take(4).forEach { (item, days) ->
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(IntrinsicSize.Max)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ToolboxTheme.bgSubtle)
                                    .border(1.dp, ToolboxTheme.line, RoundedCornerShape(10.dp))
                            ) {
                                // Left urgency accent block
                                val urgencyColor = when {
                                    days <= 1 -> ToolboxTheme.danger
                                    days <= 3 -> ToolboxTheme.warn
                                    else -> ToolboxTheme.ok
                                }
                                Box(
                                    modifier = Modifier
                                        .width(5.dp)
                                        .fillMaxHeight()
                                        .background(urgencyColor)
                                )
                                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                                    Text(
                                        text = item.name,
                                        fontFamily = ToolboxTheme.serif,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ToolboxTheme.ink,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = formatDaysLabel(days),
                                        fontFamily = ToolboxTheme.mono,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = urgencyColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Shopping List widget
        item {
            val uncheckedCount = state.shoppingList.count { !it.checked }
            val checkedCount = state.shoppingList.count { it.checked }
            val uncheckedItems = state.shoppingList.filter { !it.checked }
            val countLabel = if (uncheckedCount == 0 && checkedCount > 0)
                "All $checkedCount in cart ✓"
            else
                "$uncheckedCount left"
            DashWidget(
                kicker = "Shopping list",
                kickerColor = ToolboxTheme.shoppingAccent,
                countText = countLabel,
                onOpen = { onNavigate("shopping") },
                isEmpty = state.shoppingList.isEmpty(),
                emptyMsg = "List is empty. Tap to add items."
            ) {
                Column {
                    if (uncheckedCount == 0 && checkedCount > 0) {
                        Text(
                            text = "All $checkedCount items in cart ✓",
                            fontSize = 13.sp,
                            color = ToolboxTheme.inkMute,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    } else {
                        uncheckedItems.take(3).forEach { item ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 7.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .border(1.5.dp, ToolboxTheme.inkMute, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = item.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ToolboxTheme.ink,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1
                                )
                                if (item.qty.isNotEmpty()) {
                                    Text(
                                        text = item.qty,
                                        fontFamily = ToolboxTheme.mono,
                                        fontSize = 10.sp,
                                        color = ToolboxTheme.inkMute
                                    )
                                }
                            }
                        }
                        if (uncheckedItems.size > 3) {
                            Text(
                                text = "+ ${uncheckedItems.size - 3} more",
                                fontSize = 12.sp,
                                color = ToolboxTheme.inkMute,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Tools grid
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Kicker(text = "Your tools", color = ToolboxTheme.activePalette.primary)
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ToolTile(
                        label = "Quiet Reminders",
                        sub = "${state.reminders.filter { it.on }.size} active",
                        color = ToolboxTheme.activePalette.primary,
                        tint = ToolboxTheme.activePalette.tint,
                        onClick = { onNavigate("reminders") },
                        modifier = Modifier.weight(1f)
                    )
                    ToolTile(
                        label = "Fridge",
                        sub = "${state.fridge.size} items",
                        color = ToolboxTheme.fridgeAccent,
                        tint = ToolboxTheme.fridgeTint,
                        onClick = { onNavigate("fridge") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ToolTile(
                        label = "Shopping",
                        sub = "${state.shoppingList.count { !it.checked }} items left",
                        color = ToolboxTheme.shoppingAccent,
                        tint = ToolboxTheme.shoppingTint,
                        onClick = { onNavigate("shopping") },
                        modifier = Modifier.weight(1f)
                    )
                    ToolTile(
                        label = "Timers",
                        sub = "Coming next",
                        color = ToolboxTheme.inkMute,
                        tint = ToolboxTheme.bgSubtle,
                        onClick = {},
                        disabled = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun DashWidget(
    kicker: String,
    kickerColor: Color,
    countText: String,
    onOpen: () -> Unit,
    isEmpty: Boolean,
    emptyMsg: String,
    content: @Composable () -> Unit
) {
    Card(onClick = onOpen) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Kicker(text = kicker, color = kickerColor)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = countText,
                        fontFamily = ToolboxTheme.mono,
                        fontSize = 11.sp,
                        color = ToolboxTheme.inkMute
                    )
                    Text(" →", color = ToolboxTheme.inkMute, fontSize = 11.sp)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            if (isEmpty) {
                Text(text = emptyMsg, fontSize = 13.sp, color = ToolboxTheme.inkMute)
            } else {
                content()
            }
        }
    }
}

@Composable
fun ToolTile(
    label: String,
    sub: String,
    color: Color,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    disabled: Boolean = false
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(ToolboxTheme.surface)
            .border(1.dp, ToolboxTheme.line, RoundedCornerShape(16.dp))
            .clickable(enabled = !disabled) { onClick() }
            .padding(14.dp)
            .heightIn(min = 96.dp)
    ) {
        Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxHeight()) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(tint),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when {
                        label.contains("Fridge") -> "❄"
                        label.contains("List") -> "🛒"
                        else -> "🔕"
                    },
                    fontSize = 20.sp,
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Column {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (disabled) ToolboxTheme.inkMute else ToolboxTheme.ink
                )
                Text(
                    text = sub,
                    fontFamily = ToolboxTheme.mono,
                    fontSize = 10.sp,
                    color = ToolboxTheme.inkMute,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
        }
    }
}

// Helpers for formatted text
fun format12hTime(timeStr: String): String {
    val parts = timeStr.split(":")
    if (parts.size < 2) return timeStr
    val h = parts[0].toIntOrNull() ?: 9
    val m = parts[1].toIntOrNull() ?: 0
    val am = h < 12
    val h12 = if (h == 0) 12 else if (h > 12) h - 12 else h
    return "$h12:${m.toString().padStart(2, '0')} ${if (am) "AM" else "PM"}"
}

fun formatDaysLabel(days: Int): String {
    return when {
        days < 0 -> "${-days}d ago"
        days == 0 -> "TODAY"
        days == 1 -> "TOMORROW"
        else -> "$days days"
    }
}
