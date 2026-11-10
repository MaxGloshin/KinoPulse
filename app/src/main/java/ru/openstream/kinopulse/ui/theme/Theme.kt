package ru.openstream.kinopulse.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColorScheme(
    primary = Color(0xFFFF754C),
    onPrimary = Color(0xFF121216),
    secondary = Color(0xFF28C7FA),
    background = Color(0xFF0E1015),
    surface = Color(0xFF171A21),
    surfaceVariant = Color(0xFF222631),
    onBackground = Color(0xFFF3F4F6),
    onSurface = Color(0xFFF3F4F6),
    onSurfaceVariant = Color(0xFF9CA3AF)
)

@Composable
fun KinoPulseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorPalette,
        content = content
    )
}
