package com.example.moodtracker.domain.usecase

import com.example.moodtracker.data.repo.MoodRepository
import com.example.moodtracker.domain.model.MoodEntry
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

class ObserveTodayUseCase(
    private val repo: MoodRepository,
    private val clock: Clock,
) {
    operator fun invoke(): Flow<List<MoodEntry>> {
        val today = LocalDate.now(clock)
        return repo.observeDay(today)
    }
}

