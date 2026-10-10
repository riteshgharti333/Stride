package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BluePrimaryDark,
    onPrimary = Color.White,
    primaryContainer = BlueContainerDark,
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = Color(0xFF60A5FA),
    onSecondary = Color(0xFF0F172A),
    tertiary = RedAccentDark,
    onTertiary = Color.White,
    tertiaryContainer = RedContainerDark,
    onTertiaryContainer = Color(0xFFFEE2E2),
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkOutline,
    outlineVariant = Color(0xFF1E293B)
)

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = BlueContainer,
    onPrimaryContainer = Color(0xFF1E40AF),
    secondary = BlueSecondary,
    onSecondary = Color.White,
    tertiary = RedAccent,
    onTertiary = Color.White,
    tertiaryContainer = RedContainer,
    onTertiaryContainer = Color(0xFF991B1B),
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightOutline,
    outlineVariant = Color(0xFFF1F5F9)
)

@Composable
fun StrideTheme(
    themePreference: String = "LIGHT",
    content: @Composable () -> Unit
) {
    val isDark = when (themePreference) {
        "LIGHT" -> false
        "DARK" -> true
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
