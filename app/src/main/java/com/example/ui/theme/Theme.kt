package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
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

private val LightColorScheme = DarkColorScheme // Liquid Glass is designed as an immersive dark theme

@Composable
fun NixeonTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  NixeonTheme(content = content)
}
