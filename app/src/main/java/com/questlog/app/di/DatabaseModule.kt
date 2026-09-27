package com.questlog.app.di

import android.content.Context
import androidx.room.Room
import com.questlog.app.core.database.GameDao
import com.questlog.app.core.database.QuestlogDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): QuestlogDatabase =
        Room.databaseBuilder(context, QuestlogDatabase::class.java, "questlog.db")
            .build()

    @Provides
    @Singleton
    fun provideGameDao(db: QuestlogDatabase): GameDao = db.gameDao()
}
