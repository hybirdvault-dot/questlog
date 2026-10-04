package com.questlog.app.core.network.rawg

import com.google.gson.annotations.SerializedName

data class RawgSearchResponse(
    val results: List<RawgGameDto>,
)

data class RawgGameDto(
    val id: Int,
    val name: String,
    val released: String?,
    @SerializedName("background_image") val backgroundImage: String?,
    val rating: Double?,
    @SerializedName("description_raw") val description: String?,
    val platforms: List<RawgPlatformWrapper>?,
)

data class RawgPlatformWrapper(
    val platform: RawgPlatform,
)

data class RawgPlatform(
    val id: Int,
    val name: String,
)
