package com.questlog.app.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questlog.app.core.model.GameStatus
import com.questlog.app.data.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val gameRepository: GameRepository,
) : ViewModel() {

    private val _selectedStatus = MutableStateFlow<GameStatus?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<LibraryUiState> = _selectedStatus
        .flatMapLatest { status ->
            val gamesFlow = if (status == null) {
                gameRepository.observeGames()
            } else {
                gameRepository.observeGamesByStatus(status)
            }
            gamesFlow.map { games ->
                LibraryUiState(
                    isLoading = false,
                    games = games,
                    selectedStatus = status,
                )
            }
        }
        .catch { e ->
            emit(
                LibraryUiState(
                    isLoading = false,
                    errorMessage = e.message ?: "Could not load your library",
                ),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LibraryUiState(),
        )

    fun selectStatus(status: GameStatus?) {
        _selectedStatus.value = status
    }

    fun deleteGame(id: String) {
        viewModelScope.launch {
            gameRepository.deleteGame(id)
        }
    }
}
