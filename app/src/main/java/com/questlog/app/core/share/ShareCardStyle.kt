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
        background = Color(0xFF1A1A2E),
        textColor = Color(0xFFFAF8F5),
        accentColor = Color(0xFF7C3AED),
    )

    val StatsCard = CardStyle(
        widthPx = 1080,
        heightPx = 1920,
        background = Color(0xFF1A1A2E),
        textColor = Color(0xFFFAF8F5),
        accentColor = Color(0xFFF97316),
    )
}
