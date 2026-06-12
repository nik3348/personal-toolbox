package com.eightbitstack.toolbox

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RecipesScreen(
    state: ToolboxState,
    onSaveRecipe: (id: String?, name: String, ingredients: List<RecipeIngredient>, steps: List<String>) -> Unit,
    onDeleteRecipe: (String) -> Unit,
    onSendToShoppingList: (String) -> Unit
) {
    var editSheetOpen by remember { mutableStateOf(false) }
    var detailSheetOpen by remember { mutableStateOf(false) }
    var editFor by remember { mutableStateOf<Recipe?>(null) }
    var detailFor by remember { mutableStateOf<Recipe?>(null) }

    val recipes = state.recipes.sortedBy { it.name.lowercase() }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(top = 10.dp, bottom = 130.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            item {
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                    Kicker(
                        text = "${recipes.size} saved",
                        color = ToolboxTheme.activePalette.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Recipes",
                        fontFamily = ToolboxTheme.serif,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Medium,
                        color = ToolboxTheme.ink,
                        lineHeight = 34.sp
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (recipes.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp)
                                .border(1.5.dp, ToolboxTheme.line, RoundedCornerShape(14.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No recipes yet. Tap + to save your first one.",
                                color = ToolboxTheme.inkMute,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        recipes.forEach { recipe ->
                            RecipeCard(
                                recipe = recipe,
                                onOpen = {
                                    detailFor = recipe
                                    detailSheetOpen = true
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
                    editSheetOpen = true
                },
                text = "Add",
                size = "sm",
                icon = { Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                modifier = Modifier.width(80.dp)
            )
        }

        // Detail Sheet
        Sheet(
            open = detailSheetOpen,
            onClose = { detailSheetOpen = false },
            title = detailFor?.name
        ) {
            val recipe = detailFor
            if (recipe != null) {
                RecipeDetail(
                    recipe = recipe,
                    onEdit = {
                        editFor = recipe
                        detailSheetOpen = false
                        editSheetOpen = true
                    },
                    onDelete = {
                        onDeleteRecipe(recipe.id)
                        detailSheetOpen = false
                        detailFor = null
                    },
                    onSendToShoppingList = {
                        onSendToShoppingList(recipe.id)
                        detailSheetOpen = false
                    }
                )
            }
        }

        // Add / Edit Sheet
        Sheet(
            open = editSheetOpen,
            onClose = { editSheetOpen = false },
            title = if (editFor != null) "Edit recipe" else "New recipe"
        ) {
            RecipeForm(
                initial = editFor,
                onSave = { name, ingredients, steps ->
                    onSaveRecipe(editFor?.id, name, ingredients, steps)
                    editSheetOpen = false
                    editFor = null
                    detailFor = null
                },
                onCancel = {
                    editSheetOpen = false
                    editFor = null
                }
            )
        }
    }
}

@Composable
fun RecipeCard(
    recipe: Recipe,
    onOpen: () -> Unit
) {
    Card(onClick = onOpen) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recipe.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ToolboxTheme.ink,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Tag(text = "${recipe.ingredients.size} ingredients")
                    Tag(text = "${recipe.steps.size} steps")
                }
            }
            Text(
                text = "→",
                fontFamily = ToolboxTheme.mono,
                fontSize = 12.sp,
                color = ToolboxTheme.inkMute,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
fun RecipeDetail(
    recipe: Recipe,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSendToShoppingList: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Kicker(text = "Ingredients", color = ToolboxTheme.inkMute)
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ToolboxTheme.bgSubtle)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                recipe.ingredients.forEachIndexed { index, ing ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = ing.name,
                            fontSize = 14.sp,
                            color = ToolboxTheme.ink,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = ing.qty,
                            fontFamily = ToolboxTheme.mono,
                            fontSize = 11.sp,
                            color = ToolboxTheme.inkMute,
                            letterSpacing = 0.5.sp
                        )
                    }
                    if (index != recipe.ingredients.lastIndex) {
                        HorizontalDivider(color = ToolboxTheme.line, thickness = 1.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Kicker(text = "Steps", color = ToolboxTheme.inkMute)
            Spacer(modifier = Modifier.height(8.dp))
            recipe.steps.forEachIndexed { index, step ->
                Row(modifier = Modifier.padding(bottom = 12.dp)) {
                    Text(
                        text = "${index + 1}".padStart(2, '0'),
                        fontFamily = ToolboxTheme.mono,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ToolboxTheme.activePalette.primary,
                        modifier = Modifier.padding(end = 10.dp, top = 2.dp)
                    )
                    Text(
                        text = step,
                        fontSize = 14.sp,
                        color = ToolboxTheme.inkSoft,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            ChunkyButton(onClick = onEdit, text = "Edit", variant = "outline", size = "sm")
            ChunkyButton(
                onClick = onSendToShoppingList,
                text = "To list",
                size = "sm",
                icon = { Text("🛒", fontSize = 12.sp) }
            )
            Spacer(modifier = Modifier.weight(1f))
            ChunkyButton(
                onClick = onDelete,
                text = "Delete",
                variant = "ghost",
                size = "sm",
                shadowColor = ToolboxTheme.danger
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun RecipeForm(
    initial: Recipe?,
    onSave: (name: String, ingredients: List<RecipeIngredient>, steps: List<String>) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    // Editable rows; blank rows are dropped on save
    var ingNames = remember { mutableStateListOf<String>() }
    var ingQtys = remember { mutableStateListOf<String>() }
    var steps = remember { mutableStateListOf<String>() }

    LaunchedEffect(initial) {
        ingNames.clear(); ingQtys.clear(); steps.clear()
        if (initial != null) {
            initial.ingredients.forEach { ingNames.add(it.name); ingQtys.add(it.qty) }
            steps.addAll(initial.steps)
        }
        if (ingNames.isEmpty()) { ingNames.add(""); ingQtys.add("") }
        if (steps.isEmpty()) steps.add("")
    }

    val canSave = name.trim().isNotEmpty() && ingNames.any { it.trim().isNotEmpty() }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Field(label = "Name") {
                TextInput(
                    value = name,
                    onChange = { name = it },
                    placeholder = "Garlic pasta",
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
            }

            Field(label = "Ingredients") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ingNames.forEachIndexed { index, ingName ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextInput(
                                value = ingName,
                                onChange = { ingNames[index] = it },
                                placeholder = "Spaghetti",
                                modifier = Modifier.weight(1.6f)
                            )
                            TextInput(
                                value = ingQtys[index],
                                onChange = { ingQtys[index] = it },
                                placeholder = "200 g",
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "✕",
                                fontSize = 14.sp,
                                color = ToolboxTheme.inkMute,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        if (ingNames.size > 1) {
                                            ingNames.removeAt(index)
                                            ingQtys.removeAt(index)
                                        } else {
                                            ingNames[0] = ""; ingQtys[0] = ""
                                        }
                                    }
                                    .padding(6.dp)
                            )
                        }
                    }
                    ChunkyButton(
                        onClick = { ingNames.add(""); ingQtys.add("") },
                        text = "+ Ingredient",
                        variant = "ghost",
                        size = "sm"
                    )
                }
            }

            Field(label = "Steps") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    steps.forEachIndexed { index, step ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}".padStart(2, '0'),
                                fontFamily = ToolboxTheme.mono,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ToolboxTheme.activePalette.primary
                            )
                            TextInput(
                                value = step,
                                onChange = { steps[index] = it },
                                placeholder = "Boil the pasta",
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "✕",
                                fontSize = 14.sp,
                                color = ToolboxTheme.inkMute,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        if (steps.size > 1) steps.removeAt(index) else steps[0] = ""
                                    }
                                    .padding(6.dp)
                            )
                        }
                    }
                    ChunkyButton(
                        onClick = { steps.add("") },
                        text = "+ Step",
                        variant = "ghost",
                        size = "sm"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            ChunkyButton(onClick = onCancel, text = "Cancel", variant = "outline", modifier = Modifier.weight(1f))
            ChunkyButton(
                onClick = {
                    val ingredients = ingNames.indices
                        .filter { ingNames[it].trim().isNotEmpty() }
                        .map { RecipeIngredient(ingNames[it].trim(), ingQtys[it].trim()) }
                    val cleanSteps = steps.map { it.trim() }.filter { it.isNotEmpty() }
                    onSave(name.trim(), ingredients, cleanSteps)
                },
                text = "Save",
                modifier = Modifier.weight(1f),
                enabled = canSave
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}
