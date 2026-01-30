package com.example.moodtracker.domain.usecase

import com.example.moodtracker.data.repo.MoodRepository
import com.example.moodtracker.domain.model.MoodEntry
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow

class GetMonthMoodsUseCase(
    private val repo: MoodRepository,
) {
    operator fun invoke(month: YearMonth): Flow<List<MoodEntry>> {
        val start = month.atDay(1)
        val end = month.atEndOfMonth()
        return repo.observeRange(startInclusive = start, endInclusive = end)
    }
}

