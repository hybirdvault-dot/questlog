package com.questlog.app.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.questlog.app.core.model.Game
import com.questlog.app.core.model.GameStatus
import com.questlog.app.core.model.ProofStatus
import kotlinx.datetime.Instant

@Entity(
    tableName = "games",
    indices = [
        Index(value = ["rawgId"], unique = true),
        Index(value = ["status"]),
        Index(value = ["updatedAt"]),
    ],
)
data class GameEntity(
    @PrimaryKey val id: String,
    val rawgId: Int?,
    val title: String,
    val coverUrl: String?,
    val releaseYear: Int?,
    val platforms: List<String>,  // stored via TypeConverter
    val rawgRating: Double?,
    val description: String?,
    val status: GameStatus,       // stored via TypeConverter
    val personalRating: Int?,
    val notes: String?,
    val addedAt: Instant,
    val updatedAt: Instant,
    val completedAt: Instant?,
    val proofTxSignature: String?,
    val proofAt: Instant?,
    val proofStatus: ProofStatus?,
    val proofWalletAddress: String?,
)

fun GameEntity.toDomain(): Game = Game(
    id = id,
    rawgId = rawgId,
    title = title,
    coverUrl = coverUrl,
    releaseYear = releaseYear,
    platforms = platforms,
    rawgRating = rawgRating,
    description = description,
    status = status,
    personalRating = personalRating,
    notes = notes,
    addedAt = addedAt,
    updatedAt = updatedAt,
    completedAt = completedAt,
    proofTxSignature = proofTxSignature,
    proofAt = proofAt,
    proofStatus = proofStatus,
    proofWalletAddress = proofWalletAddress,
)

fun Game.toEntity(): GameEntity = GameEntity(
    id = id,
    rawgId = rawgId,
    title = title,
    coverUrl = coverUrl,
    releaseYear = releaseYear,
    platforms = platforms,
    rawgRating = rawgRating,
    description = description,
    status = status,
    personalRating = personalRating,
    notes = notes,
    addedAt = addedAt,
    updatedAt = updatedAt,
    completedAt = completedAt,
    proofTxSignature = proofTxSignature,
    proofAt = proofAt,
    proofStatus = proofStatus,
    proofWalletAddress = proofWalletAddress,
)
