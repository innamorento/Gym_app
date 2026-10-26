package com.gym.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = GymAccentGreen,
    onPrimary = GymDarkBackground,
    secondary = GymAccentBlue,
    onSecondary = GymDarkBackground,
    tertiary = GymAccentPurple,
    background = GymDarkBackground,
    onBackground = GymTextPrimary,
    surface = GymDarkSurface,
    onSurface = GymTextPrimary,
    surfaceVariant = GymDarkSurfaceVariant,
    onSurfaceVariant = GymTextSecondary,
    error = GymAccentRed
)

@Composable
fun GymTheme(
    darkTheme: Boolean = true, // По умолчанию всегда темная тема
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
