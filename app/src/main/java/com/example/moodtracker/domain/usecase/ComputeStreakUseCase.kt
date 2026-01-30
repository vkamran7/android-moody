package com.example.moodtracker.domain.usecase

import com.example.moodtracker.data.repo.MoodRepository
import java.time.Clock
import java.time.LocalDate

data class StreakStats(
    val current: Int,
    val longest: Int,
)

class ComputeStreakUseCase(
    private val repo: MoodRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(): StreakStats {
        // We define "current streak" as consecutive complete days ending at the most recent complete day
        // (today if complete; otherwise yesterday / last complete day).
        val completeDaysDesc = repo.getCompleteDaysDesc()
        if (completeDaysDesc.isEmpty()) return StreakStats(current = 0, longest = 0)

        val completeSet = completeDaysDesc.toHashSet()
        val today = LocalDate.now(clock)
        val endDay = if (completeSet.contains(today)) today else today.minusDays(1)

        var cur = 0
        var d = endDay
        while (completeSet.contains(d)) {
            cur += 1
            d = d.minusDays(1)
        }

        val completeAsc = completeDaysDesc.asReversed()
        var longest = 1
        var run = 1
        for (i in 1 until completeAsc.size) {
            val prev = completeAsc[i - 1]
            val next = completeAsc[i]
            if (prev.plusDays(1) == next) {
                run += 1
            } else {
                longest = maxOf(longest, run)
                run = 1
            }
        }
        longest = maxOf(longest, run)

        return StreakStats(current = cur, longest = longest)
    }
}

