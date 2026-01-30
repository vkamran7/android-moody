package com.example.moodtracker.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class MoodEntry(
    val date: LocalDate,
    val slot: MoodSlot,
    val moodId: String,
    val emoji: String,
    val colorArgb: Long,
    val createdAtEpochMillis: Long,
) {
    fun localTimeString(zoneId: ZoneId = ZoneId.systemDefault()): String {
        val t = Instant.ofEpochMilli(createdAtEpochMillis).atZone(zoneId).toLocalTime()
        return "%02d:%02d".format(t.hour, t.minute)
    }
}

