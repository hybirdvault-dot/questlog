package com.questlog.app.core.model

import kotlinx.datetime.Instant

data class Game(
    val id: String,           // UUID string, stable identity
    val rawgId: Int?,         // nullable for manually added games
    val title: String,
    val coverUrl: String?,
    val releaseYear: Int?,
    val platforms: List<String>,
    val rawgRating: Double?,
    val description: String?, // clean text, NOT HTML
    val status: GameStatus,
    val personalRating: Int?, // 1-5 stars, null if not rated
    val notes: String?,
    val addedAt: Instant,
    val updatedAt: Instant,
    val completedAt: Instant?,
)
