package com.gym.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * Цветовая схема темы «Tactical Cyber Lime» (Material 3).
 */
private val CyberLimeDarkColorScheme = darkColorScheme(
    primary = GymAccentGreen,
    onPrimary = GymDarkBackground,
    primaryContainer = GymAccentGreenDim,
    onPrimaryContainer = GymAccentGreenVibrant,
    secondary = GymAccentBlue,
    onSecondary = GymDarkBackground,
    secondaryContainer = GymDarkSurfaceVariant,
    onSecondaryContainer = GymAccentBlue,
    tertiary = GymAccentPurple,
    onTertiary = GymDarkBackground,
    background = GymDarkBackground,
    onBackground = GymTextPrimary,
    surface = GymDarkSurface,
    onSurface = GymTextPrimary,
    surfaceVariant = GymDarkSurfaceVariant,
    onSurfaceVariant = GymTextSecondary,
    outline = GymSurfaceBorder,
    outlineVariant = GymSurfaceBorder.copy(alpha = 0.5f),
    error = GymAccentRed,
    onError = GymTextPrimary
)

@Composable
fun GymTheme(
    darkTheme: Boolean = true, // По умолчанию всегда высококонтрастная темная тема Cyber Lime
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CyberLimeDarkColorScheme,
        content = content
    )
}
