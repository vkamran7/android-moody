package com.example.moodtracker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [MoodEntryEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class MoodDatabase : RoomDatabase() {
    abstract fun moodDao(): MoodDao
}

