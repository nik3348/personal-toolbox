package com.eightbitstack.toolbox

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MeScreen(
    onReset: () -> Unit,
    accent: String,
    onAccentChange: (String) -> Unit,
    showFlourishes: Boolean,
    onShowFlourishesChange: (Boolean) -> Unit,
    backgroundPattern: String,
    onBackgroundPatternChange: (String) -> Unit,
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 8.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                Kicker(text = "Settings · Profile", color = ToolboxTheme.activePalette.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Me",
                    fontFamily = ToolboxTheme.serif,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Medium,
                    color = ToolboxTheme.ink,
                    lineHeight = 34.sp
                )
            }
        }

        // Profile details
        item {
            Card {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(ToolboxTheme.activePalette.primary, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        ToolboxMark(size = 22.dp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "You",
                            fontFamily = ToolboxTheme.sans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = ToolboxTheme.ink
                        )
                        Text(
                            text = "Local-only · No account yet",
                            fontSize = 12.sp,
                            color = ToolboxTheme.inkMute,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }

        // Brand tweaks
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Kicker(text = "Brand", color = ToolboxTheme.inkMute)
                Spacer(modifier = Modifier.height(8.dp))
                Card {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Accent selector
                        Column {
                            Text("Accent Palette", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ToolboxTheme.ink)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                BrandPalettes.keys.forEach { key ->
                                    val active = accent == key
                                    FilterChip(
                                        active = active,
                                        onClick = { onAccentChange(key) },
                                        text = key
                                    )
                                }
                            }
                        }

                        // 8-bit flourishes toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("8-bit flourishes", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ToolboxTheme.ink)
                            Toggle(value = showFlourishes, onChange = onShowFlourishesChange, size = "sm")
                        }

                        // Dark mode toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Dark mode", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ToolboxTheme.ink)
                            Toggle(value = darkMode, onChange = onDarkModeChange, size = "sm")
                        }
                    }
                }
            }
        }

        // Backdrop Tweaks
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Kicker(text = "Backdrop", color = ToolboxTheme.inkMute)
                Spacer(modifier = Modifier.height(8.dp))
                Card {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Pattern Style", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ToolboxTheme.ink)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("grid" to "Grid", "plain" to "Plain", "dots" to "Dots").forEach { (key, label) ->
                                val active = backgroundPattern == key
                                FilterChip(
                                    active = active,
                                    onClick = { onBackgroundPatternChange(key) },
                                    text = label
                                )
                            }
                        }
                    }
                }
            }
        }

        // Future tools hint
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Kicker(text = "Future tools", color = ToolboxTheme.inkMute)
                Spacer(modifier = Modifier.height(8.dp))
                Card {
                    Text(
                        text = "Notes, timers, and a quick scratchpad land here next. Tell me which to prioritise.",
                        fontSize = 13.sp,
                        color = ToolboxTheme.inkSoft,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }

        // Reset button
        item {
            Spacer(modifier = Modifier.height(6.dp))
            ChunkyButton(
                onClick = onReset,
                text = "Reset demo data",
                variant = "outline",
                fullWidth = true,
                size = "sm"
            )
        }
    }
}
