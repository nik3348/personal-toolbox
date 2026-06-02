package com.eightbitstack.toolbox

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.foundation.layout.IntrinsicSize

@Composable
fun FridgeScreen(
    state: ToolboxState,
    onConsume: (String) -> Unit,
    onNudge: (String) -> Unit,
    onSaveItem: (id: String?, name: String, qty: String, expiry: String, location: String) -> Unit
) {
    var sortBy by remember { mutableStateOf("expiry") }
    var expandedId by remember { mutableStateOf<String?>(null) }
    var sheetOpen by remember { mutableStateOf(false) }
    var editFor by remember { mutableStateOf<FridgeItem?>(null) }

    val items = state.fridge
    val itemsWithDays = items.map { it to DateUtils.daysUntil(it.expiry) }

    val sorted = itemsWithDays.sortedWith(Comparator { a, b ->
        when (sortBy) {
            "name" -> a.first.name.compareTo(b.first.name, ignoreCase = true)
            "location" -> a.first.location.compareTo(b.first.location, ignoreCase = true)
            else -> a.second.compareTo(b.second) // expiry
        }
    })

    val urgentCount = itemsWithDays.filter { it.second <= 3 }.size

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(top = 10.dp, bottom = 130.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Title
            item {
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                    Kicker(
                        text = "${items.size} items · $urgentCount need attention",
                        color = ToolboxTheme.ok
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Fridge",
                        fontFamily = ToolboxTheme.serif,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Medium,
                        color = ToolboxTheme.ink,
                        lineHeight = 34.sp
                    )

                    // Sort controls
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SORT",
                            fontFamily = ToolboxTheme.mono,
                            fontSize = 11.sp,
                            color = ToolboxTheme.inkMute,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("expiry" to "Expiry", "name" to "Name", "location" to "Location").forEach { (key, label) ->
                                val active = sortBy == key
                                Text(
                                    text = label,
                                    fontFamily = ToolboxTheme.mono,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (active) ToolboxTheme.activePalette.primary else ToolboxTheme.inkMute,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(if (active) ToolboxTheme.activePaletteTint else Color.Transparent)
                                        .clickable { sortBy = key }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Fridge list items
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (sorted.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp)
                                .border(1.5.dp, ToolboxTheme.line, RoundedCornerShape(14.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Empty fridge. Add what's in there →", color = ToolboxTheme.inkMute, fontSize = 13.sp)
                        }
                    } else {
                        sorted.forEach { (item, days) ->
                            val expanded = expandedId == item.id
                            FridgeRow(
                                item = item,
                                days = days,
                                expanded = expanded,
                                onExpand = { expandedId = if (expanded) null else item.id },
                                onUsed = {
                                    expandedId = null
                                    onConsume(item.id)
                                },
                                onToss = {
                                    expandedId = null
                                    onConsume(item.id)
                                },
                                onNudge = {
                                    expandedId = null
                                    onNudge(item.name)
                                },
                                onEdit = {
                                    editFor = item
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
                text = "Add",
                size = "sm",
                icon = { Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                modifier = Modifier.width(80.dp)
            )
        }

        // Sheet Modal Form
        Sheet(
            open = sheetOpen,
            onClose = { sheetOpen = false },
            title = if (editFor != null) "Edit item" else "Add to fridge"
        ) {
            FridgeForm(
                initial = editFor,
                onSave = { name, qty, expiryVal, locationVal ->
                    onSaveItem(editFor?.id, name, qty, expiryVal, locationVal)
                    sheetOpen = false
                },
                onCancel = { sheetOpen = false }
            )
        }
    }
}

@Composable
fun FridgeRow(
    item: FridgeItem,
    days: Int,
    expanded: Boolean,
    onExpand: () -> Unit,
    onUsed: () -> Unit,
    onToss: () -> Unit,
    onNudge: () -> Unit,
    onEdit: () -> Unit
) {
    val urgencyColor = when {
        days <= 1 -> ToolboxTheme.danger
        days <= 3 -> ToolboxTheme.warn
        else -> ToolboxTheme.ok
    }

    val isFreezer = item.location.lowercase() == "freezer"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .clip(RoundedCornerShape(14.dp))
            .background(ToolboxTheme.surface)
            .border(
                1.dp,
                if (expanded) ToolboxTheme.activePaletteBorder else ToolboxTheme.line,
                RoundedCornerShape(14.dp)
            )
    ) {
        // Urgency color block
        Box(
            modifier = Modifier
                .width(6.dp)
                .fillMaxHeight()
                .background(urgencyColor)
        )

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier
                    .clickable { onExpand() }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.name,
                            fontFamily = ToolboxTheme.serif,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = ToolboxTheme.ink,
                            lineHeight = 20.sp
                        )
                        if (isFreezer) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("❄", fontSize = 13.sp, color = ToolboxTheme.cyan)
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = item.qty, fontSize = 12.sp, color = ToolboxTheme.inkSoft)
                        Text(" · ", color = ToolboxTheme.line, fontSize = 12.sp)
                        Text(
                            text = item.location.uppercase(),
                            fontFamily = ToolboxTheme.mono,
                            fontSize = 10.sp,
                            color = ToolboxTheme.inkMute,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatDaysLabel(days),
                        fontFamily = ToolboxTheme.mono,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = urgencyColor,
                        letterSpacing = 0.6.sp
                    )
                    Text(
                        text = if (days < 0) "past exp." else "til exp.",
                        fontFamily = ToolboxTheme.mono,
                        fontSize = 9.sp,
                        color = ToolboxTheme.inkMute,
                        modifier = Modifier.padding(top = 2.dp),
                        letterSpacing = 0.6.sp
                    )
                }
            }

            if (expanded) {
                HorizontalDivider(color = ToolboxTheme.line, thickness = 1.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ToolboxTheme.bg)
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ChunkyButton(
                            onClick = onUsed,
                            text = "Used",
                            size = "sm",
                            icon = { Text("✓", color = Color.White, fontWeight = FontWeight.Bold) }
                        )
                        ChunkyButton(
                            onClick = onToss,
                            text = "Toss",
                            variant = "outline",
                            size = "sm"
                        )
                        ChunkyButton(
                            onClick = onNudge,
                            text = "Nudge me",
                            variant = "ghost",
                            size = "sm",
                            modifier = Modifier.wrapContentWidth()
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        ChunkyButton(
                            onClick = onEdit,
                            text = "Edit",
                            variant = "ghost",
                            size = "sm"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FridgeForm(
    initial: FridgeItem?,
    onSave: (name: String, qty: String, expiry: String, location: String) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var qty by remember { mutableStateOf(initial?.qty ?: "") }
    var expiry by remember { mutableStateOf(initial?.expiry ?: DateUtils.getTodayPlusDays(5)) }
    var location by remember { mutableStateOf(initial?.location ?: "fridge") }

    val canSave = name.trim().isNotEmpty()

    Column(modifier = Modifier.fillMaxWidth()) {
        Field(label = "Item") {
            TextInput(value = name, onChange = { name = it }, placeholder = "Milk, oat")
        }

        Field(label = "Quantity") {
            TextInput(value = qty, onChange = { qty = it }, placeholder = "1 carton")
        }

        Field(label = "Best before") {
            TextInput(value = expiry, onChange = { expiry = it }, placeholder = "YYYY-MM-DD")
        }

        Field(label = "Where") {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("fridge", "freezer", "pantry").forEach { loc ->
                    FilterChip(
                        active = location == loc,
                        onClick = { location = loc },
                        text = loc
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            ChunkyButton(onClick = onCancel, text = "Cancel", variant = "outline", modifier = Modifier.weight(1f))
            ChunkyButton(
                onClick = { onSave(name, qty.ifEmpty { "1" }, expiry, location) },
                text = "Save",
                modifier = Modifier.weight(1f),
                enabled = canSave
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}
