package com.eightbitstack.toolbox

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ShoppingListScreen(
    state: ToolboxState,
    onSaveItem: (id: String?, name: String, qty: String) -> Unit,
    onToggleItem: (String) -> Unit,
    onDeleteItem: (String) -> Unit,
    onClearChecked: () -> Unit,
    onPurchaseItem: (id: String, location: String, expiry: String) -> Unit,
    onPurchaseChecked: (location: String, expiry: String) -> Unit
) {
    var expandedId by remember { mutableStateOf<String?>(null) }
    var editSheetOpen by remember { mutableStateOf(false) }
    var fridgeSheetOpen by remember { mutableStateOf(false) }
    var bulkFridgeSheetOpen by remember { mutableStateOf(false) }
    var editFor by remember { mutableStateOf<ShoppingListItem?>(null) }
    var fridgeFor by remember { mutableStateOf<ShoppingListItem?>(null) }

    val items = state.shoppingList
    val checkedItems = items.filter { it.checked }
    val uncheckedItems = items.filter { !it.checked }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(top = 10.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Header
            item {
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                    val kickerText = buildString {
                        append("${uncheckedItems.size} left")
                        if (checkedItems.isNotEmpty()) append(" · ${checkedItems.size} in cart")
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Kicker(text = kickerText, color = ToolboxTheme.shoppingAccent)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Shopping",
                                fontFamily = ToolboxTheme.serif,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Medium,
                                color = ToolboxTheme.ink,
                                lineHeight = 34.sp
                            )
                        }
                        if (checkedItems.isNotEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(bottom = 2.dp)
                            ) {
                                ChunkyButton(
                                    onClick = onClearChecked,
                                    text = "Clear done",
                                    variant = "ghost",
                                    size = "sm"
                                )
                                ChunkyButton(
                                    onClick = { bulkFridgeSheetOpen = true },
                                    text = "To fridge",
                                    size = "sm",
                                    icon = { Text("❄", color = Color.White, fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                }
            }

            // Item list — receipt-style card
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .padding(top = 8.dp)
                ) {
                    if (items.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp)
                                .border(1.5.dp, ToolboxTheme.line, RoundedCornerShape(14.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "List is empty. Tap + to add your first item.",
                                color = ToolboxTheme.inkMute,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(ToolboxTheme.surface)
                                .border(1.dp, ToolboxTheme.line, RoundedCornerShape(16.dp))
                        ) {
                            Column {
                                items.forEachIndexed { index, item ->
                                    val expanded = expandedId == item.id
                                    val isLast = index == items.lastIndex
                                    ShoppingItemRow(
                                        item = item,
                                        expanded = expanded,
                                        isLast = isLast,
                                        onExpand = { expandedId = if (expanded) null else item.id },
                                        onToggle = {
                                            onToggleItem(item.id)
                                        },
                                        onEdit = {
                                            editFor = item
                                            editSheetOpen = true
                                            expandedId = null
                                        },
                                        onDelete = {
                                            onDeleteItem(item.id)
                                            expandedId = null
                                        },
                                        onSendToFridge = {
                                            fridgeFor = item
                                            fridgeSheetOpen = true
                                            expandedId = null
                                        }
                                    )
                                }
                            }
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
                    editSheetOpen = true
                },
                text = "Add",
                size = "sm",
                icon = { Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                modifier = Modifier.width(80.dp)
            )
        }

        // Edit Sheet
        Sheet(
            open = editSheetOpen,
            onClose = { editSheetOpen = false },
            title = if (editFor != null) "Edit item" else "Add to list"
        ) {
            ShoppingItemForm(
                initial = editFor,
                onSave = { name, qty ->
                    onSaveItem(editFor?.id, name, qty)
                    editSheetOpen = false
                    editFor = null
                },
                onCancel = {
                    editSheetOpen = false
                    editFor = null
                }
            )
        }

        // Send to Fridge Sheet
        Sheet(
            open = fridgeSheetOpen,
            onClose = { fridgeSheetOpen = false },
            title = "Add to fridge"
        ) {
            val currentItem = fridgeFor
            if (currentItem != null) {
                SendToFridgeForm(
                    message = "Moving ${currentItem.name} from your list into the fridge tracker.",
                    onSave = { location, expiry ->
                        onPurchaseItem(currentItem.id, location, expiry)
                        fridgeSheetOpen = false
                        fridgeFor = null
                    },
                    onCancel = {
                        fridgeSheetOpen = false
                        fridgeFor = null
                    }
                )
            }
        }

        // Bulk Send to Fridge Sheet
        Sheet(
            open = bulkFridgeSheetOpen,
            onClose = { bulkFridgeSheetOpen = false },
            title = "Move bought to fridge"
        ) {
            SendToFridgeForm(
                message = "Moving ${checkedItems.size} bought item${if (checkedItems.size == 1) "" else "s"} from your list into the fridge tracker.",
                onSave = { location, expiry ->
                    onPurchaseChecked(location, expiry)
                    bulkFridgeSheetOpen = false
                },
                onCancel = { bulkFridgeSheetOpen = false }
            )
        }
    }
}

@Composable
fun ShoppingItemRow(
    item: ShoppingListItem,
    expanded: Boolean,
    isLast: Boolean,
    onExpand: () -> Unit,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSendToFridge: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (item.checked) ToolboxTheme.bgSubtle else ToolboxTheme.surface)
                .clickable { onExpand() }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .border(
                        1.5.dp,
                        if (item.checked) ToolboxTheme.activePalette.primary else ToolboxTheme.line,
                        RoundedCornerShape(6.dp)
                    )
                    .background(if (item.checked) ToolboxTheme.activePalette.primary else Color.Transparent)
                    .clickable { onToggle() }
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                if (item.checked) {
                    Text("✓", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (item.checked) ToolboxTheme.inkMute else ToolboxTheme.ink,
                    textDecoration = if (item.checked) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1
                )
                if (item.qty.isNotEmpty()) {
                    Text(
                        text = item.qty,
                        fontFamily = ToolboxTheme.mono,
                        fontSize = 10.sp,
                        color = ToolboxTheme.inkMute,
                        modifier = Modifier.padding(top = 1.dp),
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Expand arrow
            Text(
                text = if (expanded) "↑" else "→",
                fontFamily = ToolboxTheme.mono,
                fontSize = 12.sp,
                color = ToolboxTheme.inkMute,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        if (expanded) {
            HorizontalDivider(color = ToolboxTheme.line, thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ToolboxTheme.bg)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChunkyButton(
                    onClick = onEdit,
                    text = "Edit",
                    variant = "outline",
                    size = "sm"
                )
                if (item.checked) {
                    ChunkyButton(
                        onClick = onSendToFridge,
                        text = "To fridge",
                        size = "sm",
                        icon = { Text("❄", color = Color.White, fontSize = 12.sp) }
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                ChunkyButton(
                    onClick = onDelete,
                    text = "Delete",
                    variant = "ghost",
                    size = "sm",
                    shadowColor = ToolboxTheme.danger
                )
            }
        }

        if (!isLast && !expanded) {
            HorizontalDivider(color = ToolboxTheme.line, thickness = 1.dp)
        }
    }
}

@Composable
fun ShoppingItemForm(
    initial: ShoppingListItem?,
    onSave: (name: String, qty: String) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var qty by remember { mutableStateOf(initial?.qty ?: "") }

    val canSave = name.trim().isNotEmpty()

    Column(modifier = Modifier.fillMaxWidth()) {
        Field(label = "Item") {
            TextInput(
                value = name,
                onChange = { name = it },
                placeholder = "Oat milk",
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
        }

        Field(label = "Quantity") {
            TextInput(
                value = qty,
                onChange = { qty = it },
                placeholder = "2 cartons",
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (canSave) onSave(name, qty.ifEmpty { "1" }) })
            )
        }

        Spacer(modifier = Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            ChunkyButton(onClick = onCancel, text = "Cancel", variant = "outline", modifier = Modifier.weight(1f))
            ChunkyButton(
                onClick = { onSave(name, qty.ifEmpty { "1" }) },
                text = "Save",
                modifier = Modifier.weight(1f),
                enabled = canSave
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun SendToFridgeForm(
    message: String,
    onSave: (location: String, expiry: String) -> Unit,
    onCancel: () -> Unit
) {
    var location by remember { mutableStateOf("fridge") }
    var expiry by remember { mutableStateOf(DateUtils.getTodayPlusDays(7)) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Context banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ToolboxTheme.activePaletteTint)
                .border(1.dp, ToolboxTheme.activePaletteBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("❄", fontSize = 16.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                fontSize = 13.sp,
                color = ToolboxTheme.inkSoft,
                lineHeight = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Field(label = "Best before") {
            TextInput(
                value = expiry,
                onChange = { expiry = it },
                placeholder = "YYYY-MM-DD",
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSave(location, expiry) })
            )
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
                onClick = { onSave(location, expiry) },
                text = "Add to fridge",
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}
