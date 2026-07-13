package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Emerald80,
    secondary = SteelBlue80,
    tertiary = WarmAmber80,
    background = DarkBgStart,
    surface = DarkSurface,
    onPrimary = Color(0xFF003823),
    onSecondary = Color(0xFF003549),
    onTertiary = Color(0xFF492F00),
    onBackground = Color(0xFFE2E8F0),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = DarkCard,
    onSurfaceVariant = Color(0xFFCBD5E1)
)

private val LightColorScheme = lightColorScheme(
    primary = Emerald40,
    secondary = SteelBlue40,
    tertiary = WarmAmber40,
    background = LightBgStart,
    surface = LightSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF1E293B),
    surfaceVariant = LightCard,
    onSurfaceVariant = Color(0xFF475569)
)

@Composable
fun FitAICoachTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
