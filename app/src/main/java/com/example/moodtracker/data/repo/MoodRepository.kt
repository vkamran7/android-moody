package com.example.moodtracker.data.repo

import com.example.moodtracker.data.db.MoodDao
import com.example.moodtracker.data.db.MoodEntryEntity
import com.example.moodtracker.data.toDomain
import com.example.moodtracker.domain.model.MoodEntry
import com.example.moodtracker.domain.model.MoodOption
import com.example.moodtracker.domain.model.MoodSlot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class MoodRepository(
    private val dao: MoodDao,
) {
    suspend fun upsert(date: LocalDate, slot: MoodSlot, option: MoodOption, createdAtMillis: Long) {
        dao.upsert(
            MoodEntryEntity(
                date = date,
                slot = slot,
                moodId = option.id,
                imageResName = option.imageResName,
                colorArgb = option.colorArgb,
                createdAt = createdAtMillis,
            ),
        )
    }

    suspend fun getEntry(date: LocalDate, slot: MoodSlot): MoodEntry? = dao.getEntry(date, slot)?.toDomain()

    fun observeDay(date: LocalDate): Flow<List<MoodEntry>> =
        dao.observeDay(date).map { list -> list.map { it.toDomain() } }

    fun observeRange(startInclusive: LocalDate, endInclusive: LocalDate): Flow<List<MoodEntry>> =
        dao.observeRange(startInclusive, endInclusive).map { list -> list.map { it.toDomain() } }

    fun observeLast(limit: Int): Flow<List<MoodEntry>> =
        dao.observeLast(limit).map { list -> list.map { it.toDomain() } }

    suspend fun getCompleteDaysDesc(): List<LocalDate> = dao.getCompleteDaysDesc()
}

