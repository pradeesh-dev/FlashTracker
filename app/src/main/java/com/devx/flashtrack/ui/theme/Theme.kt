package com.devx.flashtrack.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary          = GreenIncome,
    onPrimary        = Color.Black,
    primaryContainer = Color(0xFF003D1A),
    secondary        = BlueCard,
    onSecondary      = Color.White,
    tertiary         = PurpleDebt,
    background       = DarkBackground,
    onBackground     = DarkTextPrimary,
    surface          = DarkSurface,
    onSurface        = DarkTextPrimary,
    surfaceVariant   = DarkSurface2,
    onSurfaceVariant = DarkTextSecondary,
    outline          = DarkBorder,
    error            = RedExpense,
    onError          = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary          = Color(0xFF00A846),
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFB7F5D3),
    secondary        = Color(0xFF1565C0),
    onSecondary      = Color.White,
    tertiary         = Color(0xFF7B1FA2),
    background       = LightBackground,
    onBackground     = LightTextPrimary,
    surface          = LightSurface,
    onSurface        = LightTextPrimary,
    surfaceVariant   = LightSurface2,
    onSurfaceVariant = LightTextSecondary,
    outline          = LightBorder,
    error            = Color(0xFFD32F2F),
    onError          = Color.White
)

// Composable to share "isDark" preference throughout app
val LocalIsDarkTheme = compositionLocalOf { true }

@Composable
fun FlashTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = FlashTrackTypography,
            content     = content
        )
    }
}
