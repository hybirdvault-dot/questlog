package com.questlog.app.core.network.rawg

import com.questlog.app.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RawgApi {

    @GET("games")
    suspend fun searchGames(
        @Query("search") query: String,
        @Query("page_size") pageSize: Int = 20,
        @Query("key") apiKey: String = BuildConfig.RAWG_API_KEY,
    ): RawgSearchResponse

    @GET("games/{id}")
    suspend fun getGameDetail(
        @Path("id") id: Int,
        @Query("key") apiKey: String = BuildConfig.RAWG_API_KEY,
    ): RawgGameDto
}
