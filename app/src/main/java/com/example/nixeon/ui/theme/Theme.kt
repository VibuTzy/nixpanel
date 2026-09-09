package com.example.nixeon.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = NixeonCyan,
    onPrimary = NixeonObsidian,
    secondary = NixeonViolet,
    onSecondary = NixeonObsidian,
    tertiary = NixeonEmerald,
    onTertiary = NixeonObsidian,
    background = NixeonObsidian,
    onBackground = NixeonTextPrimary,
    surface = NixeonSurface,
    onSurface = NixeonTextPrimary,
    surfaceVariant = NixeonSurfaceDeep,
    onSurfaceVariant = NixeonTextSecondary,
    error = NixeonCrimson,
    onError = NixeonObsidian
)

@Composable
fun NixeonTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
