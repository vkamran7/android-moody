package com.example.moodtracker.domain.model

import androidx.compose.ui.graphics.Color

data class MoodOption(
    val id: String,
    val emoji: String,
    val label: String,
    val colorArgb: Long,
    val score: Int? = null,
) {
    val color: Color get() = Color(colorArgb)
}

object MoodOptions {
    val all: List<MoodOption> = listOf(
        MoodOption(id = "good", emoji = "🌟", label = "Good", colorArgb = 0xFFFFD700, score = 5),
        MoodOption(id = "normal", emoji = "✨", label = "Normal", colorArgb = 0xFFB0BEC5, score = 3),
        MoodOption(id = "bad", emoji = "🌑", label = "Bad    ", colorArgb = 0xFF37474F, score = 1),
    )

    fun byId(id: String): MoodOption? = all.firstOrNull { it.id == id }
}
