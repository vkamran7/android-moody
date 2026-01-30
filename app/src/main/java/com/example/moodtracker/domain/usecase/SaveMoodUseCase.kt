package com.example.moodtracker.domain.usecase

import com.example.moodtracker.data.repo.MoodRepository
import com.example.moodtracker.domain.model.MoodOption
import com.example.moodtracker.domain.model.MoodSlot
import java.time.Clock
import java.time.LocalDate

class SaveMoodUseCase(
    private val repo: MoodRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(date: LocalDate, slot: MoodSlot, option: MoodOption) {
        repo.upsert(date = date, slot = slot, option = option, createdAtMillis = clock.millis())
    }
}

