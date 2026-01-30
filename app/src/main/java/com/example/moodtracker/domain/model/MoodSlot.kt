package com.example.moodtracker.domain.model

import java.time.LocalTime

enum class MoodSlot {
    Morning,
    Afternoon,
    Night,
}

fun defaultSlotForLocalTime(time: LocalTime): MoodSlot {
    // Spec:
    // 05:00–11:59 -> Morning
    // 12:00–17:59 -> Afternoon
    // 18:00–04:59 -> Night
    return when {
        !time.isBefore(LocalTime.of(5, 0)) && time.isBefore(LocalTime.NOON) -> MoodSlot.Morning
        !time.isBefore(LocalTime.NOON) && time.isBefore(LocalTime.of(18, 0)) -> MoodSlot.Afternoon
        else -> MoodSlot.Night
    }
}

