package com.questlog.app.feature.capture

import com.questlog.app.core.model.Game
import com.questlog.app.core.model.GamePreview

sealed class CaptureUiState {
    object Idle : CaptureUiState()
    object Loading : CaptureUiState()
    data class Candidates(val candidates: List<GamePreview>) : CaptureUiState()
    data class Result(val game: Game) : CaptureUiState()
    data class Error(val message: String) : CaptureUiState()
}
