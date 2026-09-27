package com.questlog.app.feature.stats

import com.questlog.app.core.model.GameStatus

data class StatsUiState(
    val isLoading: Boolean = true,
    val totalGames: Int = 0,
    val completedGames: Int = 0,
    val completionRate: Float = 0f,
    val gamesByStatus: Map<GameStatus, Int> = emptyMap(),
    val errorMessage: String? = null,
)
