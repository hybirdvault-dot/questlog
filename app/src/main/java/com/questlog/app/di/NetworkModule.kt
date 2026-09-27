package com.questlog.app.di

import com.questlog.app.BuildConfig
import com.questlog.app.core.network.rawg.RawgApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun okHttpCallFactory(): Call.Factory =
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    if (BuildConfig.DEBUG) {
                        setLevel(HttpLoggingInterceptor.Level.BODY)
                    }
                },
            )
            .build()

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
}
