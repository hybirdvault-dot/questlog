package com.questlog.app.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.questlog.app.core.model.GameStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {

    @Query("SELECT * FROM games ORDER BY addedAt DESC")
    fun observeGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE status = :status ORDER BY addedAt DESC")
    fun observeGamesByStatus(status: GameStatus): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE id = :id")
    fun observeGame(id: String): Flow<GameEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity)

    @Update
    suspend fun updateGame(game: GameEntity)

    @Query("DELETE FROM games WHERE id = :id")
    suspend fun deleteGame(id: String)

    @Query("DELETE FROM games")
    suspend fun deleteAll()

    @Query("SELECT status, COUNT(*) AS count FROM games GROUP BY status")
    fun countByStatus(): Flow<List<StatusCount>>

    @Query("SELECT * FROM games WHERE rawgId = :rawgId LIMIT 1")
    suspend fun findByRawgId(rawgId: Int): GameEntity?
}
