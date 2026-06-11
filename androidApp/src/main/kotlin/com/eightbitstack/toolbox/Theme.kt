package com.eightbitstack.toolbox

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

data class BrandPalette(
    val primary: Color,
    val deep: Color,
    val hover: Color,
    val tint: Color,
    val border: Color,
    val label: String
)

val IndigoPalette = BrandPalette(
    primary = Color(0xFF4F46E5),
    deep = Color(0xFF312E81),
    hover = Color(0xFF4338CA),
    tint = Color(0xFFEEF2FF),
    border = Color(0xFFC7D2FE),
    label = "Indigo"
)

val ForestPalette = BrandPalette(
    primary = Color(0xFF15803D),
    deep = Color(0xFF14532D),
    hover = Color(0xFF166534),
    tint = Color(0xFFDCFCE7),
    border = Color(0xFF86EFAC),
    label = "Forest"
)

val CyberPalette = BrandPalette(
    primary = Color(0xFF0E7490),
    deep = Color(0xFF164E63),
    hover = Color(0xFF155E75),
    tint = Color(0xFFCFFAFE),
    border = Color(0xFF67E8F9),
    label = "Cyber"
)

val SunsetPalette = BrandPalette(
    primary = Color(0xFFEA580C),
    deep = Color(0xFF7C2D12),
    hover = Color(0xFFC2410C),
    tint = Color(0xFFFFEDD5),
    border = Color(0xFFFDBA74),
    label = "Sunset"
)

val BrandPalettes = mapOf(
    "indigo" to IndigoPalette,
    "forest" to ForestPalette,
    "cyber" to CyberPalette,
    "sunset" to SunsetPalette
)

object ToolboxTheme {
    val ink: Color
        @Composable
        get() = if (LocalDarkMode.current) Color(0xFFF8FAFC) else Color(0xFF0F172A)

    val inkSoft: Color
        @Composable
        get() = if (LocalDarkMode.current) Color(0xFFE2E8F0) else Color(0xFF334155)

    val inkMute: Color
        @Composable
        get() = if (LocalDarkMode.current) Color(0xFF94A3B8) else Color(0xFF64748B)

    val bg: Color
        @Composable
        get() = if (LocalDarkMode.current) Color(0xFF020617) else Color(0xFFF8FAFC)

    val bgSubtle: Color
        @Composable
        get() = if (LocalDarkMode.current) Color(0xFF0F172A) else Color(0xFFF1F5F9)

    val surface: Color
        @Composable
        get() = if (LocalDarkMode.current) Color(0xFF1E293B) else Color(0xFFFFFFFF)

    val line: Color
        @Composable
        get() = if (LocalDarkMode.current) Color(0x3D94A3B8) else Color(0x140F172A)

    val danger = Color(0xFFEF4444)
    val warn = Color(0xFFF59E0B)
    val ok = Color(0xFF22C55E)
    val pink = Color(0xFFFF00FF)
    val cyan = Color(0xFF00BFFF)

    // Per-tool accents
    val fridgeAccent: Color
        @Composable
        get() = if (LocalDarkMode.current) Color(0xFF22D3EE) else Color(0xFF0891B2)

    val fridgeTint: Color
        @Composable
        get() = if (LocalDarkMode.current) Color(0xFF0891B2).copy(alpha = 0.15f) else Color(0xFFECFEFF)

    val shoppingAccent: Color
        @Composable
        get() = if (LocalDarkMode.current) Color(0xFF4ADE80) else Color(0xFF16A34A)

    val shoppingTint: Color
        @Composable
        get() = if (LocalDarkMode.current) Color(0xFF16A34A).copy(alpha = 0.15f) else Color(0xFFDCFCE7)

    // Inactive switch tracks and similar solid control surfaces
    val control: Color
        @Composable
        get() = if (LocalDarkMode.current) Color(0xFF334155) else Color(0xFFE2E8F0)

    val activePalette: BrandPalette
        @Composable
        @ReadOnlyComposable
        get() = LocalBrandPalette.current

    val activePaletteTint: Color
        @Composable
        get() = if (LocalDarkMode.current) activePalette.primary.copy(alpha = 0.15f) else activePalette.tint

    val activePaletteBorder: Color
        @Composable
        get() = if (LocalDarkMode.current) activePalette.primary.copy(alpha = 0.5f) else activePalette.border

    // Typography Fonts
    val sans = FontFamily.Default
    val serif = FontFamily.Serif
    val mono = FontFamily.Monospace
}

val LocalBrandPalette = staticCompositionLocalOf { IndigoPalette }
val LocalShowFlourishes = staticCompositionLocalOf { true }
val LocalDarkMode = staticCompositionLocalOf { false }
