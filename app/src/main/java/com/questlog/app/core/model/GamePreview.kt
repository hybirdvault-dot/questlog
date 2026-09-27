package com.questlog.app.core.model

data class GamePreview(
    val rawgId: Int,
    val title: String,
    val coverUrl: String?,
    val releaseYear: Int?,
    val platforms: List<String>,
    val rawgRating: Double?,
    val description: String?, // short snippet, 2 lines max
)
