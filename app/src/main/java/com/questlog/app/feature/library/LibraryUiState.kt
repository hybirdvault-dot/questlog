package com.questlog.app.feature.library

import com.questlog.app.core.model.Game
import com.questlog.app.core.model.GameStatus

data class LibraryUiState(
    val isLoading: Boolean = true,
    val games: List<Game> = emptyList(),
    val selectedStatus: GameStatus? = null, // null = All
    val errorMessage: String? = null,
)
