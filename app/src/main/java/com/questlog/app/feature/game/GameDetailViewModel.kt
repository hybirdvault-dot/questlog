package com.questlog.app.feature.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questlog.app.core.model.GameStatus
import com.questlog.app.data.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

@HiltViewModel
class GameDetailViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val gameId: String = savedStateHandle["gameId"] ?: ""

    val uiState: StateFlow<GameDetailUiState> = gameRepository.observeGame(gameId)
        .map { game -> GameDetailUiState(isLoading = false, game = game) }
        .catch { e ->
            emit(
                GameDetailUiState(
                    isLoading = false,
                    errorMessage = e.message ?: "Could not load this game",
                ),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GameDetailUiState(),
        )

    fun updateStatus(status: GameStatus) {
        val game = uiState.value.game ?: return
        viewModelScope.launch {
            val updated = game.copy(
                status = status,
                updatedAt = Clock.System.now(),
                completedAt = if (status == GameStatus.COMPLETED) {
                    Clock.System.now()
                } else {
                    game.completedAt
                },
            )
            gameRepository.updateGame(updated)
        }
    }

    fun updateRating(rating: Int?) {
        val game = uiState.value.game ?: return
        viewModelScope.launch {
            gameRepository.updateGame(
                game.copy(personalRating = rating, updatedAt = Clock.System.now()),
            )
        }
    }

    fun updateNotes(notes: String) {
        val game = uiState.value.game ?: return
        viewModelScope.launch {
            gameRepository.updateGame(
                game.copy(notes = notes, updatedAt = Clock.System.now()),
            )
        }
    }

    fun deleteGame() {
        viewModelScope.launch {
            gameRepository.deleteGame(gameId)
        }
    }
}
