package com.questlog.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.clockInDataStore by preferencesDataStore(name = "clock_in")

enum class StreakTier {
    NONE,
    WARM,
    FLAMEKEEPER,
    INFERNO,
    LEGEND;

    companion object {
        fun forStreak(streak: Int): StreakTier = when {
            streak >= 30 -> LEGEND
            streak >= 14 -> INFERNO
            streak >= 7 -> FLAMEKEEPER
            streak >= 3 -> WARM
            else -> NONE
        }
    }
}

data class StreakState(
    val current: Int,
    val longest: Int,
    val total: Int,
    val tier: StreakTier,
)

sealed class CheckInResult {
    data class CheckedIn(val newStreak: Int, val tier: StreakTier) : CheckInResult()
    data class AlreadyCheckedIn(val currentStreak: Int) : CheckInResult()
}

@Singleton
class ClockInRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    val streakState: Flow<StreakState> = context.clockInDataStore.data.map { preferences ->
        val current = preferences[STREAK] ?: 0
        StreakState(
            current = current,
            longest = preferences[LONGEST_STREAK] ?: 0,
            total = preferences[TOTAL_CHECK_INS] ?: 0,
            tier = StreakTier.forStreak(current),
        )
    }

    suspend fun checkIn(todayEpochDay: Long): CheckInResult {
        var result: CheckInResult = CheckInResult.AlreadyCheckedIn(0)
        context.clockInDataStore.edit { preferences ->
            val storedStreak = preferences[STREAK] ?: 0
            val storedLongest = preferences[LONGEST_STREAK] ?: 0
            val storedTotal = preferences[TOTAL_CHECK_INS] ?: 0
            val lastCheckInDay = preferences[LAST_CHECK_IN_EPOCH_DAY]

            val newStreak = if (lastCheckInDay == null) {
                1
            } else {
                val gap = todayEpochDay - lastCheckInDay
                when {
                    gap == 0L -> {
                        result = CheckInResult.AlreadyCheckedIn(storedStreak)
                        return@edit
                    }

                    gap == 1L -> storedStreak + 1

                    gap >= 2L -> 1

                    else -> {
                        result = CheckInResult.AlreadyCheckedIn(storedStreak)
                        return@edit
                    }
                }
            }

            preferences[STREAK] = newStreak
            preferences[LAST_CHECK_IN_EPOCH_DAY] = todayEpochDay
            preferences[LONGEST_STREAK] = maxOf(storedLongest, newStreak)
            preferences[TOTAL_CHECK_INS] = storedTotal + 1

            result = CheckInResult.CheckedIn(newStreak, StreakTier.forStreak(newStreak))
        }
        return result
    }

    private companion object {
        val STREAK = intPreferencesKey("streak")
        val LONGEST_STREAK = intPreferencesKey("longest_streak")
        val LAST_CHECK_IN_EPOCH_DAY = longPreferencesKey("last_check_in_epoch_day")
        val TOTAL_CHECK_INS = intPreferencesKey("total_check_ins")
    }
}
