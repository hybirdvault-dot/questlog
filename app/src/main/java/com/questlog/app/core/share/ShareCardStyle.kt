package com.questlog.app.core.share

import androidx.compose.ui.graphics.Color

data class CardStyle(
    val widthPx: Int,
    val heightPx: Int,
    val background: Color,
    val textColor: Color,
    val accentColor: Color,
)

object ShareCardStyle {
    val GameCard = CardStyle(
        widthPx = 1080,
        heightPx = 1080,
        background = Color(0xFF1E1712),
        textColor = Color(0xFFF5E9DC),
        accentColor = Color(0xFFE07856),
    )

    val StatsCard = CardStyle(
        widthPx = 1080,
        heightPx = 1920,
        background = Color(0xFF1E1712),
        textColor = Color(0xFFF5E9DC),
        accentColor = Color(0xFF7A9E7E),
    )
}
