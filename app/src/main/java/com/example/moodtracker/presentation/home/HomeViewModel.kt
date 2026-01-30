package com.example.moodtracker.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodtracker.domain.model.MoodOption
import com.example.moodtracker.domain.model.MoodSlot
import com.example.moodtracker.domain.model.defaultSlotForLocalTime
import com.example.moodtracker.domain.usecase.ObserveTodayUseCase
import com.example.moodtracker.domain.usecase.SaveMoodUseCase
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val date: LocalDate,
    val selectedSlot: MoodSlot,
    val todayBySlot: Map<MoodSlot, MoodOption?>,
    val isSaving: Boolean = false,
)

sealed interface HomeEvent {
    data class ShowMessage(val message: String) : HomeEvent
    data object Saved : HomeEvent
}

class HomeViewModel(
    private val clock: Clock,
    private val observeToday: ObserveTodayUseCase,
    private val saveMood: SaveMoodUseCase,
    private val resolveOption: (String) -> MoodOption?,
) : ViewModel() {
    private val today = LocalDate.now(clock)

    private val selectedSlot = MutableStateFlow(defaultSlotForLocalTime(LocalTime.now(clock)))
    private val isSaving = MutableStateFlow(false)
    private val events = MutableStateFlow<HomeEvent?>(null)

    val event: StateFlow<HomeEvent?> = events

    val uiState: StateFlow<HomeUiState> = combine(
        selectedSlot,
        observeToday(),
        isSaving,
    ) { slot, entries, saving ->
        val bySlot = entries.associate { it.slot to (resolveOption(it.moodId) ?: MoodOption(it.moodId, it.imageResName, it.moodId, it.colorArgb)) }
        HomeUiState(
            date = today,
            selectedSlot = slot,
            todayBySlot = MoodSlot.entries.associateWith { bySlot[it] },
            isSaving = saving,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState(today, selectedSlot.value, MoodSlot.entries.associateWith { null }))

    fun selectSlot(slot: MoodSlot) {
        selectedSlot.value = slot
    }

    fun consumeEvent() {
        events.value = null
    }

    fun save(option: MoodOption, allowReplace: Boolean) {
        viewModelScope.launch {
            val slot = selectedSlot.value
            val existing = uiState.value.todayBySlot[slot]
            if (existing != null && !allowReplace) {
                events.value = HomeEvent.ShowMessage("Already saved for ${slot.name}. View calendar or edit.")
                return@launch
            }
            isSaving.value = true
            runCatching {
                saveMood(date = today, slot = slot, option = option)
            }.onFailure {
                events.value = HomeEvent.ShowMessage("Couldn’t save. Try again.")
            }.onSuccess {
                events.value = HomeEvent.Saved
            }
            isSaving.value = false
        }
    }
}

