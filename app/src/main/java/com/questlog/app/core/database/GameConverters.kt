package com.questlog.app.core.database

import androidx.room.TypeConverter
import com.questlog.app.core.model.GameStatus
import com.questlog.app.core.model.ProofStatus
import kotlinx.datetime.Instant

class GameConverters {

    @TypeConverter
    fun longToInstant(value: Long?): Instant? =
        value?.let(Instant::fromEpochMilliseconds)

    @TypeConverter
    fun instantToLong(instant: Instant?): Long? =
        instant?.toEpochMilliseconds()

    @TypeConverter
    fun fromStringList(value: List<String>): String = value.joinToString("|||")

    @TypeConverter
    fun toStringList(value: String): List<String> =
        if (value.isEmpty()) emptyList() else value.split("|||")

    @TypeConverter
    fun gameStatusToString(status: GameStatus): String = status.name

    @TypeConverter
    fun stringToGameStatus(value: String): GameStatus = GameStatus.valueOf(value)

    @TypeConverter
    fun proofStatusToString(status: ProofStatus?): String? = status?.name

    @TypeConverter
    fun stringToProofStatus(value: String?): ProofStatus? = value?.let(ProofStatus::valueOf)
}
