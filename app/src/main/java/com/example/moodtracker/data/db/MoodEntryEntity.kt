package com.example.moodtracker.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.example.moodtracker.domain.model.MoodSlot
import java.time.LocalDate

@Entity(
    tableName = "mood_entries",
    primaryKeys = ["date", "slot"],
    indices = [
        Index(value = ["date"]),
        Index(value = ["slot"]),
    ],
)
data class MoodEntryEntity(
    val date: LocalDate,
    val slot: MoodSlot,
    val moodId: String,
    @ColumnInfo(name = "emoji") val imageResName: String,
    val colorArgb: Long,
    val createdAt: Long,
)

