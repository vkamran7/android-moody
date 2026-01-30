package com.example.moodtracker.data.db

import androidx.room.TypeConverter
import com.example.moodtracker.domain.model.MoodSlot
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun localDateToString(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun stringToLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun slotToString(slot: MoodSlot?): String? = slot?.name

    @TypeConverter
    fun stringToSlot(value: String?): MoodSlot? = value?.let(MoodSlot::valueOf)
}

