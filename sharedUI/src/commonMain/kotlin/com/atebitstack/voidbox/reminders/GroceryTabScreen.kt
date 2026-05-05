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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

private enum class AppTab { Home, Reminders, Groceries }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryTabScreen(
    groceries: List<GroceryItem>,
    onToggleGrocery: (Int) -> Unit = {},
    onDeleteGrocery: (Int) -> Unit = {},
    onEditGrocery: (GroceryItem) -> Unit = {},
    onAddGrocery: (String, kotlinx.datetime.LocalDate) -> Unit = { _, _ -> },
    onUpdateGrocery: (Int, String, kotlinx.datetime.LocalDate) -> Unit = { _, _, _ -> },
    showAddSheet: Boolean,
    editingGrocery: GroceryItem?,
    onShowAddSheet: () -> Unit = {},
    onDismissAddSheet: () -> Unit = {},
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
                text = "Groceries",
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Swipe right to edit, swipe left to delete.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (groceries.isEmpty()) {
            item {
                Card {
                    Text(
                        text = "No groceries yet. Tap + to add one.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        } else {
            val expired = groceries.filter { it.freshnessStatus == FreshnessStatus.EXPIRED }
            val expiringToday = groceries.filter { it.freshnessStatus == FreshnessStatus.EXPIRING_TODAY }
            val expiringTomorrow = groceries.filter { it.freshnessStatus == FreshnessStatus.EXPIRING_TOMORROW }
            val warning = groceries.filter { it.freshnessStatus == FreshnessStatus.WARNING }
            val fresh = groceries.filter { it.freshnessStatus == FreshnessStatus.FRESH }

            if (expired.isNotEmpty()) {
                item {
                    Text(
                        text = "Expired",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                items(expired, key = { it.id }) { grocery ->
                    SwipeableGroceryCard(
                        grocery = grocery,
                        onDelete = { onDeleteGrocery(grocery.id) },
                        onEdit = { onEditGrocery(grocery) },
                    )
                }
            }

            if (expiringToday.isNotEmpty()) {
                item {
                    Text(
                        text = "Expiring Today",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                items(expiringToday, key = { it.id }) { grocery ->
                    SwipeableGroceryCard(
                        grocery = grocery,
                        onDelete = { onDeleteGrocery(grocery.id) },
                        onEdit = { onEditGrocery(grocery) },
                    )
                }
            }

            if (expiringTomorrow.isNotEmpty()) {
                item {
                    Text(
                        text = "Expiring Tomorrow",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color(0xFFFF6B00),
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                items(expiringTomorrow, key = { it.id }) { grocery ->
                    SwipeableGroceryCard(
                        grocery = grocery,
                        onDelete = { onDeleteGrocery(grocery.id) },
                        onEdit = { onEditGrocery(grocery) },
                    )
                }
            }

            if (warning.isNotEmpty()) {
                item {
                    Text(
                        text = "Expiring Soon",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color(0xFFFFB800),
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                items(warning, key = { it.id }) { grocery ->
                    SwipeableGroceryCard(
                        grocery = grocery,
                        onDelete = { onDeleteGrocery(grocery.id) },
                        onEdit = { onEditGrocery(grocery) },
                    )
                }
            }

            if (fresh.isNotEmpty()) {
                item {
                    Text(
                        text = "Fresh",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                items(fresh, key = { it.id }) { grocery ->
                    SwipeableGroceryCard(
                        grocery = grocery,
                        onDelete = { onDeleteGrocery(grocery.id) },
                        onEdit = { onEditGrocery(grocery) },
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }

    if (showAddSheet) {
        GrocerySheet(
            editing = editingGrocery,
            onDismiss = onDismissAddSheet,
            onConfirm = onAddGrocery,
            onUpdate = onUpdateGrocery,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableGroceryCard(
    grocery: GroceryItem,
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
        GroceryCard(grocery = grocery)
    }
}

@Composable
private fun GroceryCard(grocery: GroceryItem) {
    val (bgColor, textColor) = when (grocery.freshnessStatus) {
        FreshnessStatus.EXPIRED -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        FreshnessStatus.EXPIRING_TODAY -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        FreshnessStatus.EXPIRING_TOMORROW -> Color(0xFFFF6B00).copy(alpha = 0.15f) to Color(0xFFFF6B00)
        FreshnessStatus.WARNING -> Color(0xFFFFB800).copy(alpha = 0.15f) to Color(0xFFFFB800)
        FreshnessStatus.FRESH -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
    }

    val statusLabel = when (grocery.freshnessStatus) {
        FreshnessStatus.EXPIRED -> "Expired"
        FreshnessStatus.EXPIRING_TODAY -> "Today"
        FreshnessStatus.EXPIRING_TOMORROW -> "Tomorrow"
        FreshnessStatus.WARNING -> "In ${grocery.daysUntilExpiration} days"
        FreshnessStatus.FRESH -> "${grocery.daysUntilExpiration} days"
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    grocery.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Best before: ${grocery.expirationDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .background(bgColor, MaterialTheme.shapes.small)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(
                    statusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GrocerySheet(
    editing: GroceryItem?,
    onDismiss: () -> Unit,
    onConfirm: (String, kotlinx.datetime.LocalDate) -> Unit,
    onUpdate: (Int, String, kotlinx.datetime.LocalDate) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf(editing?.name ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }

    val initialDate = editing?.expirationDate
        ?: kotlinx.datetime.Clock.System.todayIn(kotlinx.datetime.TimeZone.currentSystemDefault()).plusDays(7)

    var selectedDate by remember { mutableStateOf(initialDate) }

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
                text = if (editing != null) "Edit Grocery" else "New Grocery",
                style = MaterialTheme.typography.headlineSmall,
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Item name") },
                placeholder = { Text("e.g., Milk, Eggs, Bread") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Expiration date",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Card(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = selectedDate.toString(),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Icon(
                            imageVector = Icons.Outlined.ShoppingCart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (editing != null) {
                            onUpdate(editing.id, name, selectedDate)
                        } else {
                            onConfirm(name, selectedDate)
                        }
                    },
                    enabled = name.isNotBlank(),
                ) {
                    Text(if (editing != null) "Update" else "Save")
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = Instant.fromEpochMilliseconds(millis)
                                .toLocalDateTime(TimeZone.currentSystemDefault())
                                .date
                        }
                        showDatePicker = false
                    },
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}