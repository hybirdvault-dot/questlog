package com.questlog.app.feature.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questlog.app.core.model.Game
import com.questlog.app.core.model.GamePreview
import com.questlog.app.core.network.rawg.RawgRepository
import com.questlog.app.data.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val rawgRepository: RawgRepository,
    private val gameRepository: GameRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DiscoverUiState>(DiscoverUiState.Idle)
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    private val _detailState = MutableStateFlow<Game?>(null)
    val detailState: StateFlow<Game?> = _detailState.asStateFlow()

    private val _addedToLibrary = MutableStateFlow(false)
    val addedToLibrary: StateFlow<Boolean> = _addedToLibrary.asStateFlow()

    private var searchJob: Job? = null

    fun search(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.value = DiscoverUiState.Idle
            return
        }
        searchJob = viewModelScope.launch {
            delay(350)
            _uiState.value = DiscoverUiState.Loading
            rawgRepository.search(query)
                .onSuccess { games ->
                    _uiState.value = if (games.isEmpty()) {
                        DiscoverUiState.Empty
                    } else {
                        DiscoverUiState.Success(games)
                    }
                }
                .onFailure { e ->
                    _uiState.value = DiscoverUiState.Error(e.message ?: "Search failed")
                }
        }
    }

    fun fetchDetail(game: GamePreview) {
        viewModelScope.launch {
            _detailState.value = null
            rawgRepository.getDetail(game.rawgId)
                .onSuccess { _detailState.value = it }
                .onFailure { /* stay on list */ }
        }
    }

    fun clearDetail() {
        _detailState.value = null
    }

    fun addToLibrary(game: Game) {
        viewModelScope.launch {
            val existing = game.rawgId?.let { gameRepository.findByRawgId(it) }
            if (existing == null) {
                gameRepository.saveGame(game)
            }
            _detailState.value = null
            _addedToLibrary.value = true
        }
    }

    fun resetAddedFlag() {
        _addedToLibrary.value = false
    }
}
