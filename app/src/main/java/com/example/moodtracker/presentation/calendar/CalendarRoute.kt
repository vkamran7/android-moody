package com.example.moodtracker.presentation.calendar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moodtracker.MoodPulseApp
import com.example.moodtracker.domain.model.MoodEntry
import com.example.moodtracker.domain.model.MoodSlot
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarRoute(
    contentPadding: PaddingValues,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val graph = (context.applicationContext as MoodPulseApp).appGraph

    val vm: CalendarViewModel = viewModel(
        factory = SimpleVmFactory {
            CalendarViewModel(
                clock = graph.clock,
                getMonthMoods = graph.getMonthMoodsUseCase,
                computeStreak = graph.computeStreakUseCase,
            )
        },
    )

    val ui by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(ui.month) {
        vm.refreshStreak()
    }

    Surface(
        modifier = Modifier
            .padding(contentPadding)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            StatsRow(current = ui.streak.current, longest = ui.streak.longest)
            MonthHeader(month = ui.month, onPrev = vm::prevMonth, onNext = vm::nextMonth)
            WeekdayHeader()
            CalendarGrid(
                month = ui.month,
                today = ui.today,
                entries = ui.entries,
                onDayClick = { vm.selectDay(it) },
            )
        }
    }

    val selected = ui.selectedDay
    if (selected != null) {
        val dayEntries = ui.entries.filter { it.date == selected }.associateBy { it.slot }
        ModalBottomSheet(onDismissRequest = { vm.selectDay(null) }) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = selected.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                MoodSlot.entries.forEach { slot ->
                    val e = dayEntries[slot]
                    DaySlotRow(slot = slot, entry = e)
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun StatsRow(current: Int, longest: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Streak", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            StatChip(label = "Current", value = current.toString())
            StatChip(label = "Longest", value = longest.toString())
        }
    }
}

@Composable
private fun StatChip(label: String, value: String) {
    Column(
        modifier = Modifier
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun MonthHeader(month: YearMonth, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrev) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
        }
        Text(
            text = month.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + month.year,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
        }
    }
}

@Composable
private fun WeekdayHeader() {
    val days = remember {
        listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY,
        )
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        days.forEach { d ->
            Text(
                text = d.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    month: YearMonth,
    today: LocalDate,
    entries: List<MoodEntry>,
    onDayClick: (LocalDate) -> Unit,
) {
    val start = month.atDay(1)
    val daysInMonth = month.lengthOfMonth()
    val mondayBasedOffset = (start.dayOfWeek.value + 6) % 7 // Monday=0 .. Sunday=6
    val totalCells = ((mondayBasedOffset + daysInMonth + 6) / 7) * 7

    val entriesByDay: Map<LocalDate, List<MoodEntry>> = remember(entries, month) {
        entries.groupBy { it.date }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        var day = 1
        for (cell in 0 until totalCells step 7) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (i in 0 until 7) {
                    val idx = cell + i
                    val isInMonth = idx >= mondayBasedOffset && day <= daysInMonth
                    val date = if (isInMonth) month.atDay(day) else null
                    val dayEntries = date?.let { entriesByDay[it].orEmpty() }.orEmpty()

                    DayCell(
                        dayNumber = if (isInMonth) day.toString() else "",
                        isToday = date == today,
                        markers = dayEntries,
                        onClick = if (date != null) ({ onDayClick(date) }) else null,
                        modifier = Modifier.weight(1f),
                    )
                    if (isInMonth) day += 1
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    dayNumber: String,
    isToday: Boolean,
    markers: List<MoodEntry>,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val bg = when {
        isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(bg)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(10.dp)
            .height(56.dp),
    ) {
        Text(
            text = dayNumber,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            markers.sortedBy { it.slot.ordinal }.take(3).forEach { e ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(androidx.compose.ui.graphics.Color(e.colorArgb)),
                )
            }
        }
    }
}

@Composable
private fun DaySlotRow(slot: MoodSlot, entry: MoodEntry?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(slot.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        if (entry == null) {
            Text("Not set", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                val context = LocalContext.current
                val resId = context.resources.getIdentifier(entry.imageResName, "drawable", context.packageName)
                if (resId != 0) {
                    Image(
                        painter = painterResource(resId),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.size(48.dp)
                    )
                }
                Text(entry.localTimeString(), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private class SimpleVmFactory<T>(
    private val create: () -> T,
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <VM : androidx.lifecycle.ViewModel> create(modelClass: Class<VM>): VM = create() as VM
}

