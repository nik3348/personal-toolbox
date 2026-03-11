package com.atebitstack.voidbox.reminders

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography

// ── Palette ──────────────────────────────────────────────────────────────────
val DarkBackground    = Color(0xFF0D0D1A)
val DarkSurface       = Color(0xFF17172A)
val DarkSurfaceHigh   = Color(0xFF232341)
val DarkCard          = Color(0xFF1E1E33)
val AccentPurple      = Color(0xFF7C5CFC)
val AccentPurpleLight = Color(0xFFAA8FFF)
val AccentCyan        = Color(0xFF4FD1C5)
val AccentGreen       = Color(0xFF48BB78)
val AccentAmber       = Color(0xFFF6AD55)
val TextPrimary       = Color(0xFFEEEEF8)
val TextSecondary     = Color(0xFFB0B0CF)
val CardBorderEnabled  = Color(0xFF3A3060)
val CardBorderDisabled = Color(0xFF252540)
val DarkCardDisabled   = Color(0xFF161628)
val TextDisabled       = Color(0xFF606080)

// ── Typography ───────────────────────────────────────────────────────────────
val AppTypography = Typography(
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 30.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.2).sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.2.sp,
    ),
)
