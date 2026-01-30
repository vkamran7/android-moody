package com.example.moodtracker.di

import android.content.Context
import androidx.room.Room
import com.example.moodtracker.data.db.MoodDatabase
import com.example.moodtracker.data.repo.MoodRepository
import com.example.moodtracker.domain.usecase.ComputeStreakUseCase
import com.example.moodtracker.domain.usecase.GetMonthMoodsUseCase
import com.example.moodtracker.domain.usecase.ObserveTodayUseCase
import com.example.moodtracker.domain.usecase.SaveMoodUseCase
import java.time.Clock

/**
 * Lightweight DI container (no Hilt) to keep the project simple and offline-friendly.
 */
class AppGraph(
    context: Context,
) {
    private val appContext = context.applicationContext

    val clock: Clock = Clock.systemDefaultZone()

    private val db: MoodDatabase = Room.databaseBuilder(appContext, MoodDatabase::class.java, "moodpulse.db")
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()

    private val repo: MoodRepository = MoodRepository(db.moodDao())

    val saveMoodUseCase: SaveMoodUseCase = SaveMoodUseCase(repo, clock)
    val observeTodayUseCase: ObserveTodayUseCase = ObserveTodayUseCase(repo, clock)
    val getMonthMoodsUseCase: GetMonthMoodsUseCase = GetMonthMoodsUseCase(repo)
    val computeStreakUseCase: ComputeStreakUseCase = ComputeStreakUseCase(repo, clock)
}

