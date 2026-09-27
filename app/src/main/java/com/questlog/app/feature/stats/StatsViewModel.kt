package com.questlog.app.feature.stats

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

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val gameRepository: GameRepository,
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = gameRepository.countByStatus()
        .map { counts ->
            val total = counts.sumOf { it.count }
            val completed = counts.find { it.status == GameStatus.COMPLETED }?.count ?: 0
            val rate = if (total > 0) completed.toFloat() / total else 0f
            StatsUiState(
                isLoading = false,
                totalGames = total,
                completedGames = completed,
                completionRate = rate,
                gamesByStatus = counts.associate { it.status to it.count },
            )
        }
        .catch { e ->
            emit(
                StatsUiState(
                    isLoading = false,
                    errorMessage = e.message ?: "Could not load your stats",
                ),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StatsUiState(),
        )
}
