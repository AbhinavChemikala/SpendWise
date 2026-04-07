package com.yourapp.spendwise.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ── Light palette (warm cream + purple) ──────────────────────────────────────
private val LightColors = lightColorScheme(
    primary            = Color(0xFF6F49FF),
    onPrimary          = Color.White,
    secondary          = Color(0xFF24B6D3),
    background         = Color(0xFFF8EFE5),
    surface            = Color(0xFFFFFBF7),
    surfaceVariant     = Color(0xFFF1E7DC),
    onSurface          = Color(0xFF262833),
    onSurfaceVariant   = Color(0xFF8F8A9E),
    outline            = Color(0xFFE3D6CC)
)

// ── Dark palette (deep slate + same accents) ─────────────────────────────────
private val DarkColors = darkColorScheme(
    primary            = Color(0xFF9B7FFF),   // lighter purple for dark bg
    onPrimary          = Color(0xFF1A0060),
    secondary          = Color(0xFF38D4EF),
    background         = Color(0xFF12111A),   // near-black
    surface            = Color(0xFF1C1B27),   // dark card bg
    surfaceVariant     = Color(0xFF242233),   // slightly lighter card
    onSurface          = Color(0xFFEDE8FF),   // light lavender text
    onSurfaceVariant   = Color(0xFF9B96B0),   // muted text
    outline            = Color(0xFF3A3650)    // subtle borders
)

private val SpendWiseShapes = Shapes(
    small  = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
    large  = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
)

@Composable
fun SpendWiseTheme(
    isDark: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (isDark) DarkColors else LightColors,
        typography  = Typography(),
        shapes      = SpendWiseShapes,
        content     = content
    )
}
