package com.questlog.app.core.network.rawg

import com.questlog.app.core.model.Game
import com.questlog.app.core.model.GamePreview
import com.questlog.app.core.model.GameStatus
import java.util.UUID
import kotlinx.datetime.Clock

fun RawgGameDto.toDomain(): Game = Game(
    id = UUID.randomUUID().toString(),
    rawgId = id,
    title = name,
    coverUrl = backgroundImage,
    releaseYear = released?.take(4)?.toIntOrNull(),
    platforms = platforms?.map { it.platform.name } ?: emptyList(),
    rawgRating = rating,
    description = description?.cleanHtml(),
    status = GameStatus.WANT,
    personalRating = null,
    notes = null,
    addedAt = Clock.System.now(),
    updatedAt = Clock.System.now(),
    completedAt = null,
    proofTxSignature = null,
    proofAt = null,
    proofStatus = null,
    proofWalletAddress = null,
)

fun RawgGameDto.toPreview(): GamePreview = GamePreview(
    rawgId = id,
    title = name,
    coverUrl = backgroundImage,
    releaseYear = released?.take(4)?.toIntOrNull(),
    platforms = platforms?.map { it.platform.name } ?: emptyList(),
    rawgRating = rating,
    description = description?.cleanHtml()?.take(150),
)

private fun String.cleanHtml(): String = replace(Regex("<[^>]*>"), "")
    .replace(Regex("&[a-z]+;"), " ")
    .replace(Regex("\\s+"), " ")
    .trim()
    .take(500)
