package com.eightbitstack.toolbox

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Breakfast / lunch / dinner, in display order.
val MEAL_SLOTS = listOf("breakfast" to "Breakfast", "lunch" to "Lunch", "dinner" to "Dinner")

@Composable
fun MealPlannerScreen(
    state: ToolboxState,
    onSetMealSlot: (date: String, slot: String, recipeId: String) -> Unit,
    onClearMealSlot: (date: String, slot: String) -> Unit,
    onSendWeekToShopping: () -> Unit
) {
    // A pending (date, slot) the user tapped to assign a recipe to.
    var picking by remember { mutableStateOf<Pair<String, String>?>(null) }

    val days = remember { (0 until 7).map { DateUtils.getTodayPlusDays(it) } }
    val plannedCount = state.mealPlan.count { it.date in days.toSet() }

    fun recipeName(id: String): String? = state.recipes.firstOrNull { it.id == id }?.name
    fun entryFor(date: String, slot: String): MealPlanEntry? =
        state.mealPlan.firstOrNull { it.date == date && it.slot == slot }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(top = 10.dp, bottom = 130.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            item {
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Kicker(text = "Next 7 days", color = ToolboxTheme.activePalette.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Meal planner",
                                fontFamily = ToolboxTheme.serif,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Medium,
                                color = ToolboxTheme.ink,
                                lineHeight = 34.sp
                            )
                        }
                        if (plannedCount > 0) {
                            ChunkyButton(
                                onClick = onSendWeekToShopping,
                                text = "To list",
                                size = "sm",
                                icon = { Text("🛒", fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }

            if (state.recipes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 40.dp)
                            .border(1.5.dp, ToolboxTheme.line, RoundedCornerShape(14.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Save a recipe first, then plan it onto a day.",
                            color = ToolboxTheme.inkMute,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(days.size) { index ->
                    val date = days[index]
                    val label = when (index) {
                        0 -> "Today"
                        1 -> "Tomorrow"
                        else -> weekdayShort(date)
                    }
                    Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                        DayCard(
                            label = label,
                            slotEntry = { slot -> entryFor(date, slot)?.let { recipeName(it.recipeId) } },
                            onPick = { slot -> picking = date to slot }
                        )
                    }
                }
            }
        }

        // Recipe picker for the tapped (date, slot)
        val pick = picking
        Sheet(
            open = pick != null,
            onClose = { picking = null },
            title = pick?.let { (_, slot) -> "Plan ${MEAL_SLOTS.firstOrNull { it.first == slot }?.second ?: slot}" }
        ) {
            if (pick != null) {
                val (date, slot) = pick
                val current = entryFor(date, slot)
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (current != null) {
                        ChunkyButton(
                            onClick = {
                                onClearMealSlot(date, slot)
                                picking = null
                            },
                            text = "Clear this slot",
                            variant = "ghost",
                            fullWidth = true,
                            size = "sm",
                            shadowColor = ToolboxTheme.danger
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    state.recipes.sortedBy { it.name.lowercase() }.forEach { recipe ->
                        val selected = current?.recipeId == recipe.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) ToolboxTheme.activePaletteTint else ToolboxTheme.surface)
                                .border(
                                    1.dp,
                                    if (selected) ToolboxTheme.activePaletteBorder else ToolboxTheme.line,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    onSetMealSlot(date, slot, recipe.id)
                                    picking = null
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = recipe.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ToolboxTheme.ink,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${recipe.ingredients.size} ingr.",
                                fontFamily = ToolboxTheme.mono,
                                fontSize = 10.sp,
                                color = ToolboxTheme.inkMute
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun DayCard(
    label: String,
    slotEntry: (slot: String) -> String?,
    onPick: (slot: String) -> Unit
) {
    Card {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label,
                fontFamily = ToolboxTheme.sans,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = ToolboxTheme.ink
            )
            Spacer(modifier = Modifier.height(8.dp))
            MEAL_SLOTS.forEachIndexed { index, (slotId, slotLabel) ->
                val planned = slotEntry(slotId)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onPick(slotId) }
                        .padding(vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = slotLabel,
                        fontFamily = ToolboxTheme.mono,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = ToolboxTheme.inkMute,
                        modifier = Modifier.width(78.dp)
                    )
                    if (planned != null) {
                        Text(
                            text = planned,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ToolboxTheme.ink,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Text(
                            text = "— tap to plan",
                            fontSize = 13.sp,
                            color = ToolboxTheme.inkMute,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                if (index != MEAL_SLOTS.lastIndex) {
                    HorizontalDivider(color = ToolboxTheme.line, thickness = 1.dp)
                }
            }
        }
    }
}
