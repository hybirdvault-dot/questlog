package com.questlog.app.data.repository

import com.questlog.app.core.database.GameDao
import com.questlog.app.core.database.StatusCount
import com.questlog.app.core.database.toDomain
import com.questlog.app.core.database.toEntity
import com.questlog.app.core.model.Game
import com.questlog.app.core.model.GameStatus
import com.questlog.app.core.model.ProofStatus
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class GameRepository @Inject constructor(private val gameDao: GameDao) {

    fun observeGames(): Flow<List<Game>> =
        gameDao.observeGames().map { list -> list.map { it.toDomain() } }

    fun observeGamesByStatus(status: GameStatus): Flow<List<Game>> =
        gameDao.observeGamesByStatus(status).map { list -> list.map { it.toDomain() } }

    fun observeGame(id: String): Flow<Game?> =
        gameDao.observeGame(id).map { it?.toDomain() }

    suspend fun saveGame(game: Game) = gameDao.insertGame(game.toEntity())

    suspend fun updateGame(game: Game) =
        gameDao.updateGame(game.toEntity().copy(updatedAt = Clock.System.now()))

    suspend fun deleteGame(id: String) = gameDao.deleteGame(id)

    suspend fun deleteAll() = gameDao.deleteAll()

    suspend fun findByRawgId(rawgId: Int): Game? =
        gameDao.findByRawgId(rawgId)?.toDomain()

    suspend fun updateProof(
        id: String,
        signature: String?,
        proofAt: Instant?,
        proofStatus: ProofStatus?,
        walletAddress: String?,
    ) = gameDao.updateProof(id, signature, proofAt, proofStatus, walletAddress)

    fun countByStatus(): Flow<List<StatusCount>> = gameDao.countByStatus()
}
