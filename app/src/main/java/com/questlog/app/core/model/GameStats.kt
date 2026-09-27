package com.questlog.app.core.model

data class GameStats(
    val totalGames: Int,
    val completedGames: Int,
    val completionRate: Float, // 0.0 to 1.0
    val gamesByStatus: Map<GameStatus, Int>,
    val monthlyActivity: Map<String, Int>, // "2026-01" -> count
)
