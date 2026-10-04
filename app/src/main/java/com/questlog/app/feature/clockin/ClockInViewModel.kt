package com.questlog.app.feature.clockin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questlog.app.core.database.GameDao
import com.questlog.app.core.model.GameStatus
import com.questlog.app.data.repository.CheckInResult
import com.questlog.app.data.repository.ClockInRepository
import com.questlog.app.data.repository.StreakTier
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class ClockInUiState(
    val isLoading: Boolean = true,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalCheckIns: Int = 0,
    val tier: StreakTier = StreakTier.NONE,
    val gamesByStatus: Map<GameStatus, Int> = emptyMap(),
    val totalGames: Int = 0,
    val verifiedCompletions: Int = 0,
    val errorMessage: String? = null,
)

@HiltViewModel
class ClockInViewModel @Inject constructor(
    private val clockInRepository: ClockInRepository,
    gameDao: GameDao,
) : ViewModel() {

    val uiState: StateFlow<ClockInUiState> = combine(
        clockInRepository.streakState,
        gameDao.countByStatus(),
        gameDao.observeVerifiedCompletions(),
    ) { streak, counts, verified ->
        ClockInUiState(
            isLoading = false,
            currentStreak = streak.current,
            longestStreak = streak.longest,
            totalCheckIns = streak.total,
            tier = streak.tier,
            gamesByStatus = counts.associate { it.status to it.count },
            totalGames = counts.sumOf { it.count },
            verifiedCompletions = verified.size,
        )
    }
        .catch { e ->
            emit(ClockInUiState(isLoading = false, errorMessage = e.message ?: "Could not load your streak"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ClockInUiState(),
        )

    private val _checkInResult = MutableStateFlow<CheckInResult?>(null)
    val checkInResult: StateFlow<CheckInResult?> = _checkInResult.asStateFlow()

    fun checkIn() {
        val today = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
            .toEpochDays()
            .toLong()
        viewModelScope.launch {
            _checkInResult.value = clockInRepository.checkIn(today)
        }
    }

    fun consumeCheckInResult() {
        _checkInResult.value = null
    }
}
