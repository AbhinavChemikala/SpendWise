package com.yourapp.spendwise.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

internal const val THEME_MODE_SYSTEM = "system"
internal const val THEME_MODE_LIGHT = "light"
internal const val THEME_MODE_DARK = "dark"
internal const val THEME_MODE_DARK_AMOLED = "dark_amoled"
internal const val THEME_MODE_DARK_OCEAN = "dark_ocean"
internal const val THEME_MODE_DARK_FOREST = "dark_forest"

internal val DarkThemeModes = setOf(
    THEME_MODE_DARK,
    THEME_MODE_DARK_AMOLED,
    THEME_MODE_DARK_OCEAN,
    THEME_MODE_DARK_FOREST
)

internal fun normalizeThemeMode(mode: String): String {
    return when (mode) {
        THEME_MODE_SYSTEM,
        THEME_MODE_LIGHT,
        THEME_MODE_DARK,
        THEME_MODE_DARK_AMOLED,
        THEME_MODE_DARK_OCEAN,
        THEME_MODE_DARK_FOREST -> mode
        else -> THEME_MODE_SYSTEM
    }
}

// ── Light palette (warm cream + violet) ───────────────────────────────────────
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

// ── Default Dark palette (deep navy-violet) ────────────────────────────────────
// Matches the reference screenshots: very dark navy background,
// slightly lighter card surfaces, violet-purple primary accent.
private val DarkColors = darkColorScheme(
    primary            = Color(0xFF7B5CF6),   // violet-purple accent
    onPrimary          = Color(0xFF1A0060),
    primaryContainer   = Color(0xFF2D1B69),
    onPrimaryContainer = Color(0xFFCDB9FF),
    secondary          = Color(0xFF24B6D3),   // teal accent
    onSecondary        = Color(0xFF003E4A),
    background         = Color(0xFF0D0D1A),   // very dark navy
    surface            = Color(0xFF16162A),   // dark card surface
    surfaceVariant     = Color(0xFF1E1E35),   // slightly lighter variant
    onSurface          = Color(0xFFEDE8FF),   // off-white lavender text
    onSurfaceVariant   = Color(0xFF8B86A0),   // muted lavender grey
    outline            = Color(0xFF2E2C45),   // subtle separator
    error              = Color(0xFFFF577A),
    onError            = Color.White
)

private val AmoledDarkColors = darkColorScheme(
    primary            = Color(0xFF9B7FFF),
    onPrimary          = Color(0xFF120044),
    secondary          = Color(0xFF00D5FF),
    background         = Color.Black,
    surface            = Color(0xFF050505),
    surfaceVariant     = Color(0xFF111111),
    onSurface          = Color(0xFFF5F3FF),
    onSurfaceVariant   = Color(0xFFAAA6B8),
    outline            = Color(0xFF2A2A2A)
)

private val OceanDarkColors = darkColorScheme(
    primary            = Color(0xFF4CC9F0),
    onPrimary          = Color(0xFF001F2B),
    secondary          = Color(0xFF2DD4BF),
    background         = Color(0xFF06111F),
    surface            = Color(0xFF0B1A2B),
    surfaceVariant     = Color(0xFF14263B),
    onSurface          = Color(0xFFE6F4FF),
    onSurfaceVariant   = Color(0xFFA4B8C8),
    outline            = Color(0xFF2C4A62)
)

private val ForestDarkColors = darkColorScheme(
    primary            = Color(0xFF5FE08E),
    onPrimary          = Color(0xFF00210D),
    secondary          = Color(0xFFE1B866),
    background         = Color(0xFF07140E),
    surface            = Color(0xFF101D16),
    surfaceVariant     = Color(0xFF1A2D22),
    onSurface          = Color(0xFFEAF7EC),
    onSurfaceVariant   = Color(0xFFA9BAAE),
    outline            = Color(0xFF324D3B)
)

private val SpendWiseShapes = Shapes(
    small  = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
    large  = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
)

@Composable
fun SpendWiseTheme(
    isDark: Boolean = false,
    themeMode: String = THEME_MODE_DARK,
    content: @Composable () -> Unit
) {
    val colors = if (!isDark) {
        LightColors
    } else {
        when (normalizeThemeMode(themeMode)) {
            THEME_MODE_DARK_AMOLED -> AmoledDarkColors
            THEME_MODE_DARK_OCEAN -> OceanDarkColors
            THEME_MODE_DARK_FOREST -> ForestDarkColors
            else -> DarkColors
        }
    }
    MaterialTheme(
        colorScheme = colors,
        typography  = Typography(),
        shapes      = SpendWiseShapes,
        content     = content
    )
}
