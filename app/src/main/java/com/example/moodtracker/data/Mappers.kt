package com.example.moodtracker.data

import com.example.moodtracker.data.db.MoodEntryEntity
import com.example.moodtracker.domain.model.MoodEntry

fun MoodEntryEntity.toDomain(): MoodEntry = MoodEntry(
    date = date,
    slot = slot,
    moodId = moodId,
    imageResName = imageResName,
    colorArgb = colorArgb,
    createdAtEpochMillis = createdAt,
)

