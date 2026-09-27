package com.questlog.app.feature.game

import com.questlog.app.core.model.Game

data class GameDetailUiState(
    val isLoading: Boolean = true,
    val game: Game? = null,
    val errorMessage: String? = null,
)
