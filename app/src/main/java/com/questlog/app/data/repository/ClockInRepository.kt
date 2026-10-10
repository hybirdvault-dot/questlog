package com.questlog.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
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
    data class StreakReset(val newStreak: Int) : CheckInResult()
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

    val lastCheckInEpochDay: Flow<Long?> =
        context.clockInDataStore.data.map { it[LAST_CHECK_IN_EPOCH_DAY] }

    val checkInDays: Flow<Set<Long>> =
        context.clockInDataStore.data.map { parseDaySet(it[CHECK_IN_DAYS]) }

    suspend fun checkIn(todayEpochDay: Long): CheckInResult {
        var result: CheckInResult = CheckInResult.AlreadyCheckedIn(0)
        context.clockInDataStore.edit { preferences ->
            val storedStreak = preferences[STREAK] ?: 0
            val storedLongest = preferences[LONGEST_STREAK] ?: 0
            val storedTotal = preferences[TOTAL_CHECK_INS] ?: 0
            val lastCheckInDay = preferences[LAST_CHECK_IN_EPOCH_DAY]

            val newStreak: Int
            val isReset: Boolean
            if (lastCheckInDay == null) {
                newStreak = 1
                isReset = false
            } else {
                val gap = todayEpochDay - lastCheckInDay
                when {
                    gap == 0L -> {
                        result = CheckInResult.AlreadyCheckedIn(storedStreak)
                        return@edit
                    }

                    gap == 1L -> {
                        newStreak = storedStreak + 1
                        isReset = false
                    }

                    gap >= 2L -> {
                        newStreak = 1
                        isReset = storedStreak > 1
                    }

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

            val days = parseDaySet(preferences[CHECK_IN_DAYS]).toMutableSet()
            days.add(todayEpochDay)
            preferences[CHECK_IN_DAYS] = encodeDaySet(days)

            result = if (isReset) {
                CheckInResult.StreakReset(newStreak)
            } else {
                CheckInResult.CheckedIn(newStreak, StreakTier.forStreak(newStreak))
            }
        }
        return result
    }

    private companion object {
        val STREAK = intPreferencesKey("streak")
        val LONGEST_STREAK = intPreferencesKey("longest_streak")
        val LAST_CHECK_IN_EPOCH_DAY = longPreferencesKey("last_check_in_epoch_day")
        val TOTAL_CHECK_INS = intPreferencesKey("total_check_ins")
        val CHECK_IN_DAYS = stringPreferencesKey("check_in_days")

        const val MAX_CHECK_IN_DAYS = 365

        fun parseDaySet(raw: String?): Set<Long> {
            if (raw.isNullOrEmpty()) return emptySet()
            return raw.split(',')
                .mapNotNull { it.trim().toLongOrNull() }
                .toSet()
        }

        fun encodeDaySet(days: Set<Long>): String =
            days.sortedDescending()
                .take(MAX_CHECK_IN_DAYS)
                .sorted()
                .joinToString(",")
    }
}
