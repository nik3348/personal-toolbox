package com.atebitstack.voidbox.reminders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
        item { StatsRow(reminderCount = reminderCount, totalReminders = totalReminders) }
        item {
            Text(
                "Tools",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
        item { FeatureGrid(onNavigateToReminders = onNavigateToReminders) }
        item { Spacer(modifier = Modifier.height(88.dp)) }
    }
}

// ── Stats Row ────────────────────────────────────────────────────────────────
@Composable
private fun StatsRow(reminderCount: Int, totalReminders: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatCard(label = "Active", value = reminderCount.toString(), modifier = Modifier.weight(1f))
        StatCard(label = "Total", value = totalReminders.toString(), modifier = Modifier.weight(1f))
        StatCard(label = "Paused", value = (totalReminders - reminderCount).toString(), modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            enabled = true,
            onClick = onNavigateToReminders,
        ),
        FeatureTile(
            title = "Tasks",
            subtitle = "Manage your\nto-do lists",
            icon = Icons.Outlined.CheckCircle,
            enabled = false,
            onClick = {},
        ),
        FeatureTile(
            title = "Calendar",
            subtitle = "Track events\nand schedules",
            icon = Icons.Outlined.CalendarMonth,
            enabled = false,
            onClick = {},
        ),
        FeatureTile(
            title = "Notes",
            subtitle = "Capture your\nideas quickly",
            icon = Icons.AutoMirrored.Outlined.Notes,
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
    val enabled: Boolean,
    val onClick: () -> Unit,
)

@Composable
private fun FeatureTileCard(tile: FeatureTile, modifier: Modifier = Modifier) {
    Card(
        onClick = tile.onClick,
        enabled = tile.enabled,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Icon(
                imageVector = tile.icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                tile.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                tile.subtitle,
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 18.sp,
            )
            if (!tile.enabled) {
                Spacer(Modifier.height(10.dp))
                Text(
                    "Coming soon",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}
