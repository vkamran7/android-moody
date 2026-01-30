package com.example.moodtracker.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.moodtracker.domain.model.MoodSlot
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface MoodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: MoodEntryEntity)

    @Query("SELECT * FROM mood_entries WHERE date = :date AND slot = :slot LIMIT 1")
    suspend fun getEntry(date: LocalDate, slot: MoodSlot): MoodEntryEntity?

    @Query("SELECT * FROM mood_entries WHERE date = :date")
    fun observeDay(date: LocalDate): Flow<List<MoodEntryEntity>>

    @Query("SELECT * FROM mood_entries WHERE date BETWEEN :startInclusive AND :endInclusive ORDER BY date ASC")
    fun observeRange(startInclusive: LocalDate, endInclusive: LocalDate): Flow<List<MoodEntryEntity>>

    @Query("SELECT * FROM mood_entries ORDER BY date DESC, createdAt DESC LIMIT :limit")
    fun observeLast(limit: Int): Flow<List<MoodEntryEntity>>

    // Days with all 3 slots present.
    @Query(
        """
        SELECT date FROM mood_entries
        GROUP BY date
        HAVING COUNT(*) = 3
        ORDER BY date DESC
        """,
    )
    suspend fun getCompleteDaysDesc(): List<LocalDate>
}

