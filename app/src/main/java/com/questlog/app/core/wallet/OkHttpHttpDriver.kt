package com.questlog.app.core.wallet

import com.solana.networking.HttpNetworkDriver
import com.solana.networking.HttpRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class OkHttpHttpDriver(
    private val callFactory: Call.Factory,
) : HttpNetworkDriver {

    override suspend fun makeHttpRequest(request: HttpRequest): String = withContext(Dispatchers.IO) {
        val body = if (request.method.equals("GET", ignoreCase = true)) {
            null
        } else {
            request.body?.toRequestBody(JSON_MEDIA_TYPE)
        }
        val httpRequest = Request.Builder()
            .url(request.url)
            .method(request.method, body)
            .apply {
                request.properties.forEach { (name, value) -> header(name, value) }
            }
            .build()

        callFactory.newCall(httpRequest).execute().use { response ->
            response.body?.string().orEmpty()
        }
    }

    private companion object {
        val JSON_MEDIA_TYPE: MediaType? = "application/json".toMediaTypeOrNull()
    }
}
