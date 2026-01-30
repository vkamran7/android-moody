package com.example.moodtracker.presentation.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodtracker.domain.model.MoodEntry
import com.example.moodtracker.domain.usecase.ComputeStreakUseCase
import com.example.moodtracker.domain.usecase.GetMonthMoodsUseCase
import com.example.moodtracker.domain.usecase.StreakStats
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CalendarUiState(
    val month: YearMonth,
    val today: LocalDate,
    val entries: List<MoodEntry>,
    val selectedDay: LocalDate? = null,
    val streak: StreakStats = StreakStats(current = 0, longest = 0),
)

class CalendarViewModel(
    private val clock: Clock,
    private val getMonthMoods: GetMonthMoodsUseCase,
    private val computeStreak: ComputeStreakUseCase,
) : ViewModel() {
    private val month = MutableStateFlow(YearMonth.now(clock))
    private val selectedDay = MutableStateFlow<LocalDate?>(null)
    private val streak = MutableStateFlow(StreakStats(0, 0))

    @OptIn(ExperimentalCoroutinesApi::class)
    private val entriesForMonth: StateFlow<List<MoodEntry>> =
        month.flatMapLatest { m -> getMonthMoods(m) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState: StateFlow<CalendarUiState> = combine(
        month,
        entriesForMonth,
        selectedDay,
        streak,
    ) { m, entries, selected, s ->
        CalendarUiState(
            month = m,
            today = LocalDate.now(clock),
            entries = entries,
            selectedDay = selected,
            streak = s,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarUiState(YearMonth.now(clock), LocalDate.now(clock), emptyList()))

    init {
        refreshStreak()
    }

    fun prevMonth() {
        month.value = month.value.minusMonths(1)
    }

    fun nextMonth() {
        month.value = month.value.plusMonths(1)
    }

    fun selectDay(day: LocalDate?) {
        selectedDay.value = day
    }

    fun refreshStreak() {
        viewModelScope.launch {
            streak.value = computeStreak()
        }
    }
}

