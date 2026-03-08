package com.atebitstack.voidbox.reminders

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Palette (mirrors App.kt) ────────────────────────────────────────────────
private val DarkBackground   = Color(0xFF0D0D1A)
private val DarkSurface      = Color(0xFF17172A)
private val DarkCard         = Color(0xFF1E1E33)
private val AccentPurple     = Color(0xFF7C5CFC)
private val AccentPurpleLight= Color(0xFFAA8FFF)
private val AccentCyan       = Color(0xFF4FD1C5)
private val AccentGreen      = Color(0xFF48BB78)
private val AccentAmber      = Color(0xFFF6AD55)
private val TextPrimary      = Color(0xFFEEEEF8)
private val TextSecondary    = Color(0xFF9090B0)
private val CardBorderEnabled= Color(0xFF3A3060)

// ── Home Screen ─────────────────────────────────────────────────────────────
@Composable
fun HomeScreen(
    reminderCount: Int,
    totalReminders: Int,
    onNavigateToReminders: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { WelcomeBanner() }
        item { StatsRow(reminderCount = reminderCount, totalReminders = totalReminders) }
        item {
            Text(
                "Tools",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
        item {
            FeatureGrid(onNavigateToReminders = onNavigateToReminders)
        }
        item { Spacer(modifier = Modifier.height(88.dp)) }
    }
}

// ── Welcome Banner ───────────────────────────────────────────────────────────
@Composable
private fun WelcomeBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        AccentPurple.copy(alpha = 0.35f),
                        AccentCyan.copy(alpha = 0.18f),
                    ),
                ),
            )
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(AccentPurple.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = null,
                        tint = AccentPurpleLight,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "Voidbox",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                    )
                    Text(
                        "Your personal productivity suite",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }
        }
    }
}

// ── Stats Row ────────────────────────────────────────────────────────────────
@Composable
private fun StatsRow(reminderCount: Int, totalReminders: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatCard(
            label = "Active",
            value = reminderCount.toString(),
            subtitle = "reminders",
            accentColor = AccentPurple,
            modifier = Modifier.weight(1f),
        )
        StatCard(
            label = "Total",
            value = totalReminders.toString(),
            subtitle = "reminders",
            accentColor = AccentCyan,
            modifier = Modifier.weight(1f),
        )
        StatCard(
            label = "Paused",
            value = (totalReminders - reminderCount).toString(),
            subtitle = "reminders",
            accentColor = AccentAmber,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
    )
    Card(
        modifier = modifier.shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = accentColor.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = animatedAlpha)),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = accentColor,
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
        }
    }
}

// ── Feature Grid ─────────────────────────────────────────────────────────────
@Composable
private fun FeatureGrid(onNavigateToReminders: () -> Unit) {
    val features = listOf(
        FeatureTile(
            title = "Reminders",
            subtitle = "Manage your\nscheduled alerts",
            icon = Icons.Outlined.Notifications,
            accentColor = AccentPurple,
            enabled = true,
            onClick = onNavigateToReminders,
        ),
        FeatureTile(
            title = "Tasks",
            subtitle = "Manage your\nto-do lists",
            icon = Icons.Outlined.CheckCircle,
            accentColor = AccentGreen,
            enabled = false,
            onClick = {},
        ),
        FeatureTile(
            title = "Calendar",
            subtitle = "Track events\nand schedules",
            icon = Icons.Outlined.CalendarMonth,
            accentColor = AccentCyan,
            enabled = false,
            onClick = {},
        ),
        FeatureTile(
            title = "Notes",
            subtitle = "Capture your\nideas quickly",
            icon = Icons.AutoMirrored.Outlined.Notes,
            accentColor = AccentAmber,
            enabled = false,
            onClick = {},
        ),
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        features.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                rowItems.forEach { tile ->
                    FeatureTileCard(tile = tile, modifier = Modifier.weight(1f))
                }
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

private data class FeatureTile(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color,
    val enabled: Boolean,
    val onClick: () -> Unit,
)

@Composable
private fun FeatureTileCard(tile: FeatureTile, modifier: Modifier = Modifier) {
    val containerColor = if (tile.enabled) DarkCard else DarkSurface
    val borderAlpha = if (tile.enabled) 1f else 0.4f
    val contentAlpha = if (tile.enabled) 1f else 0.45f

    Card(
        modifier = modifier
            .shadow(
                elevation = if (tile.enabled) 8.dp else 2.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = tile.accentColor.copy(alpha = if (tile.enabled) 0.2f else 0f),
            )
            .clickable(enabled = tile.enabled, onClick = tile.onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(tile.accentColor.copy(alpha = 0.18f * borderAlpha)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = tile.icon,
                    contentDescription = null,
                    tint = tile.accentColor.copy(alpha = contentAlpha),
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                tile.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary.copy(alpha = contentAlpha),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                tile.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary.copy(alpha = contentAlpha),
                lineHeight = 18.sp,
            )

            if (!tile.enabled) {
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkCard.copy(alpha = 0.8f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(
                        "Coming soon",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }
}
