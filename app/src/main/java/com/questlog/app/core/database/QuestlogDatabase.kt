package com.questlog.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [GameEntity::class], version = 1, exportSchema = false)
@TypeConverters(GameConverters::class)
abstract class QuestlogDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
}
