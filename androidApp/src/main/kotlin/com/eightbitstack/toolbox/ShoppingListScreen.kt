package com.eightbitstack.toolbox

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    onPurchaseItem: (id: String, location: String, expiry: String) -> Unit
) {
    var expandedId by remember { mutableStateOf<String?>(null) }
    var editSheetOpen by remember { mutableStateOf(false) }
    var fridgeSheetOpen by remember { mutableStateOf(false) }
    var editFor by remember { mutableStateOf<ShoppingListItem?>(null) }
    var fridgeFor by remember { mutableStateOf<ShoppingListItem?>(null) }

    val items = state.shoppingList
    val checkedItems = items.filter { it.checked }
    val uncheckedItems = items.filter { !it.checked }

    val totalCount = items.size
    val checkedCount = checkedItems.size
    val progress = if (totalCount > 0) checkedCount.toFloat() / totalCount else 0f

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding(),
            contentPadding = PaddingValues(top = 10.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Title & Progress
            item {
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                    val kickerText = if (totalCount == 0) {
                        "Your list is empty"
                    } else {
                        "$checkedCount of $totalCount items completed"
                    }
                    Kicker(
                        text = kickerText,
                        color = ToolboxTheme.activePalette.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Shopping List",
                            fontFamily = ToolboxTheme.serif,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Medium,
                            color = ToolboxTheme.ink,
                            lineHeight = 34.sp
                        )
                        if (checkedItems.isNotEmpty()) {
                            Text(
                                text = "CLEAR ALL CHECKED",
                                fontFamily = ToolboxTheme.mono,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ToolboxTheme.danger,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { onClearChecked() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Progress bar
                    if (totalCount > 0) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(ToolboxTheme.line)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progress)
                                    .background(ToolboxTheme.activePalette.primary)
                            )
                        }
                    }
                }
            }

            // Shopping Items
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                text = "No items in your shopping list yet.",
                                color = ToolboxTheme.inkMute,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        // Unchecked section
                        if (uncheckedItems.isNotEmpty()) {
                            uncheckedItems.forEach { item ->
                                val expanded = expandedId == item.id
                                ShoppingItemRow(
                                    item = item,
                                    expanded = expanded,
                                    onExpand = { expandedId = if (expanded) null else item.id },
                                    onToggle = { onToggleItem(item.id) },
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

                        // Checked Section Separator
                        if (checkedItems.isNotEmpty()) {
                            if (uncheckedItems.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Kicker(
                                    text = "Checked items",
                                    color = ToolboxTheme.inkMute,
                                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                                )
                            }
                            checkedItems.forEach { item ->
                                val expanded = expandedId == item.id
                                ShoppingItemRow(
                                    item = item,
                                    expanded = expanded,
                                    onExpand = { expandedId = if (expanded) null else item.id },
                                    onToggle = { onToggleItem(item.id) },
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
            title = if (editFor != null) "Edit shopping item" else "Add item"
        ) {
            ShoppingItemForm(
                initial = editFor,
                onSave = { name, qty ->
                    onSaveItem(editFor?.id, name, qty)
                    editSheetOpen = false
                },
                onCancel = { editSheetOpen = false }
            )
        }

        // Send to Fridge Sheet
        Sheet(
            open = fridgeSheetOpen,
            onClose = { fridgeSheetOpen = false },
            title = "Send to fridge"
        ) {
            val currentItem = fridgeFor
            if (currentItem != null) {
                SendToFridgeForm(
                    item = currentItem,
                    onSave = { location, expiry ->
                        onPurchaseItem(currentItem.id, location, expiry)
                        fridgeSheetOpen = false
                    },
                    onCancel = { fridgeSheetOpen = false }
                )
            }
        }
    }
}

@Composable
fun ShoppingItemRow(
    item: ShoppingListItem,
    expanded: Boolean,
    onExpand: () -> Unit,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSendToFridge: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ToolboxTheme.surface)
            .border(
                1.dp,
                if (expanded) ToolboxTheme.activePaletteBorder else ToolboxTheme.line,
                RoundedCornerShape(14.dp)
            )
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier
                    .clickable { onExpand() }
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // custom checkbox style
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
                        fontFamily = ToolboxTheme.serif,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (item.checked) ToolboxTheme.inkMute else ToolboxTheme.ink,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "QTY: ${item.qty}",
                        fontFamily = ToolboxTheme.mono,
                        fontSize = 11.sp,
                        color = ToolboxTheme.inkMute
                    )
                }
            }

            if (expanded) {
                HorizontalDivider(color = ToolboxTheme.line, thickness = 1.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ToolboxTheme.bg)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ChunkyButton(
                            onClick = onSendToFridge,
                            text = "Send to fridge",
                            size = "sm",
                            icon = { Text("❄", color = Color.White, fontSize = 12.sp) }
                        )
                        ChunkyButton(
                            onClick = onDelete,
                            text = "Delete",
                            variant = "outline",
                            size = "sm"
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
fun ShoppingItemForm(
    initial: ShoppingListItem?,
    onSave: (name: String, qty: String) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var qty by remember { mutableStateOf(initial?.qty ?: "") }

    val canSave = name.trim().isNotEmpty()

    Column(modifier = Modifier.fillMaxWidth()) {
        Field(label = "Item Name") {
            TextInput(
                value = name,
                onChange = { name = it },
                placeholder = "Bananas",
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
        }

        Field(label = "Quantity") {
            TextInput(
                value = qty,
                onChange = { qty = it },
                placeholder = "6",
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
    item: ShoppingListItem,
    onSave: (location: String, expiry: String) -> Unit,
    onCancel: () -> Unit
) {
    var location by remember { mutableStateOf("fridge") }
    var expiry by remember { mutableStateOf(DateUtils.getTodayPlusDays(5)) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Move ${item.name} (${item.qty}) to your fridge inventory",
            fontSize = 14.sp,
            color = ToolboxTheme.inkSoft,
            modifier = Modifier.padding(bottom = 14.dp)
        )

        Field(label = "Best before date") {
            TextInput(
                value = expiry,
                onChange = { expiry = it },
                placeholder = "YYYY-MM-DD",
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSave(location, expiry) })
            )
        }

        Field(label = "Location") {
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
                text = "Send",
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}
