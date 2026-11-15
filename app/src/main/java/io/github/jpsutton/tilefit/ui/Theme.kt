package io.github.jpsutton.tilefit.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Light = lightColorScheme(
    primary = Color(0xFF2F5BD8),
    onPrimary = Color.White,
    secondary = Color(0xFFE08A1E),
    tertiary = Color(0xFFD9467A),
    secondaryContainer = Color(0xFFDCE4FA),
    onSecondaryContainer = Color(0xFF0E1E4D),
    background = Color(0xFFF5F6FB),
    surface = Color(0xFFF5F6FB),
    surfaceVariant = Color(0xFFE3E6F2),
)

private val Dark = darkColorScheme(
    primary = Color(0xFF8CA8FF),
    onPrimary = Color(0xFF0A1B52),
    secondary = Color(0xFFFFB454),
    tertiary = Color(0xFFFF8FB3),
    secondaryContainer = Color(0xFF26335F),
    onSecondaryContainer = Color(0xFFDCE4FA),
    background = Color(0xFF111423),
    surface = Color(0xFF111423),
    surfaceVariant = Color(0xFF1F2440),
)

@Composable
fun TilefitTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) Dark else Light, content = content)
}
