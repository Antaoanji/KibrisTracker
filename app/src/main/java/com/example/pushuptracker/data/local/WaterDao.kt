package com.example.pushuptracker.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.pushuptracker.model.ActivityRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterDao {

    @Upsert
    suspend fun upsert(record: ActivityRecord)

    @Query("SELECT * FROM activity_records WHERE date = :date AND type = 'water'")
    fun getRecordForDate(date: String): Flow<ActivityRecord?>

    @Query("SELECT * FROM activity_records WHERE type = 'water' ORDER BY date DESC")
    fun getAllRecords(): Flow<List<ActivityRecord>>

    @Query("DELETE FROM activity_records WHERE date = :date AND type = 'water'")
    suspend fun delete(date: String)

    @Query("DELETE FROM activity_records WHERE type = 'water'")
    suspend fun clear()
}
