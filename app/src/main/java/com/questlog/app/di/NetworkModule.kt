package com.questlog.app.di

import android.util.Log
import com.questlog.app.BuildConfig
import com.questlog.app.core.network.rawg.RawgApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.Call
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun okHttpCallFactory(): Call.Factory {
        val builder = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(RedactingHttpLogger())
        }
        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpCallFactory: Call.Factory): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.rawg.io/api/")
            .callFactory(okHttpCallFactory)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideRawgApi(retrofit: Retrofit): RawgApi =
        retrofit.create(RawgApi::class.java)

    private class RedactingHttpLogger : Interceptor {

        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val safeUrl = request.url.redacted()
            Log.d(TAG, "--> ${request.method} $safeUrl")
            val response = chain.proceed(request)
            Log.d(TAG, "<-- ${response.code} $safeUrl")
            return response
        }

        private fun HttpUrl.redacted(): String {
            if (queryParameter(API_KEY_QUERY) == null) return toString()
            return newBuilder()
                .removeAllQueryParameters(API_KEY_QUERY)
                .addQueryParameter(API_KEY_QUERY, REDACTED)
                .build()
                .toString()
        }

        private companion object {
            const val TAG = "QuestlogHttp"
            const val API_KEY_QUERY = "key"
            const val REDACTED = "REDACTED"
        }
    }
}
