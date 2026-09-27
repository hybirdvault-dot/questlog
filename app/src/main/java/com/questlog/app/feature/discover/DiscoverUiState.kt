package com.questlog.app.feature.discover

import com.questlog.app.core.model.GamePreview

sealed class DiscoverUiState {
    object Idle : DiscoverUiState()
    object Loading : DiscoverUiState()
    data class Success(val games: List<GamePreview>) : DiscoverUiState()
    object Empty : DiscoverUiState()
    data class Error(val message: String) : DiscoverUiState()
}
