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
    primary = QuestlogViolet,
    onPrimary = Color.White,
    primaryContainer = QuestlogViolet.copy(alpha = 0.12f),
    onPrimaryContainer = QuestlogViolet,
    secondary = QuestlogCoral,
    onSecondary = Color.White,
    secondaryContainer = QuestlogCoral.copy(alpha = 0.12f),
    onSecondaryContainer = QuestlogCoral,
    background = QuestlogCream,
    onBackground = QuestlogCharcoal,
    surface = Color.White,
    onSurface = QuestlogCharcoal,
    surfaceVariant = QuestlogGray.copy(alpha = 0.08f),
    onSurfaceVariant = QuestlogGray,
    error = Color(0xFFEF4444),
    onError = Color.White,
    outline = QuestlogGray.copy(alpha = 0.3f),
)

val DarkColorScheme = darkColorScheme(
    primary = QuestlogVioletLight,
    onPrimary = QuestlogCharcoal,
    primaryContainer = QuestlogViolet.copy(alpha = 0.2f),
    onPrimaryContainer = QuestlogVioletLight,
    secondary = QuestlogCoralLight,
    onSecondary = QuestlogCharcoal,
    secondaryContainer = QuestlogCoral.copy(alpha = 0.2f),
    onSecondaryContainer = QuestlogCoralLight,
    background = QuestlogCharcoal,
    onBackground = QuestlogCream,
    surface = QuestlogDarkSurface,
    onSurface = QuestlogCream,
    surfaceVariant = QuestlogGray.copy(alpha = 0.15f),
    onSurfaceVariant = QuestlogGray,
    error = Color(0xFFF87171),
    onError = Color.White,
    outline = QuestlogGray.copy(alpha = 0.3f),
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
