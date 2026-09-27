package com.questlog.app.core.network.rawg

import com.questlog.app.core.model.Game
import com.questlog.app.core.model.GamePreview
import javax.inject.Inject

class RawgRepository @Inject constructor(private val api: RawgApi) {

    suspend fun search(query: String): Result<List<GamePreview>> =
        try {
            Result.success(api.searchGames(query).results.map { it.toPreview() })
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun getDetail(rawgId: Int): Result<Game> =
        try {
            Result.success(api.getGameDetail(rawgId).toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
}
