package com.questlog.app.ui.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val AppShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
)

val LightColorScheme = lightColorScheme(
    primary = QuestlogTerracotta,
    onPrimary = Color.White,
    primaryContainer = QuestlogTerracotta.copy(alpha = 0.12f),
    onPrimaryContainer = QuestlogTerracotta,
    secondary = QuestlogSage,
    onSecondary = Color.White,
    secondaryContainer = QuestlogSage.copy(alpha = 0.12f),
    onSecondaryContainer = QuestlogSage,
    background = QuestlogCream,
    onBackground = QuestlogBrown,
    surface = Color.White,
    onSurface = QuestlogBrown,
    surfaceVariant = QuestlogWarmSurface,
    onSurfaceVariant = QuestlogSoftBrown,
    outline = QuestlogSoftBrown.copy(alpha = 0.3f),
)

val DarkColorScheme = darkColorScheme(
    primary = QuestlogTerracotta,
    onPrimary = Color.White,
    primaryContainer = QuestlogTerracotta.copy(alpha = 0.2f),
    onPrimaryContainer = QuestlogTerracotta,
    secondary = QuestlogSage,
    onSecondary = Color.White,
    secondaryContainer = QuestlogSage.copy(alpha = 0.2f),
    onSecondaryContainer = QuestlogSage,
    background = Color(0xFF1E1712),
    onBackground = Color(0xFFF5E9DC),
    surface = Color(0xFF2A211A),
    onSurface = Color(0xFFF5E9DC),
    surfaceVariant = Color(0xFF3A2E25),
    onSurfaceVariant = Color(0xFFD9C4B0),
    outline = QuestlogSoftBrown.copy(alpha = 0.5f),
)

@Composable
fun QuestlogTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = QuestlogTypography,
        shapes = AppShapes,
        content = content,
    )
}
