package com.example.pushuptracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pushuptracker.model.ActivityRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: ActivityRecord)

    @Query("SELECT * FROM activity_records WHERE type = 'water' AND date = :date")
    fun getRecordForDate(date: String): Flow<ActivityRecord?>

    @Query("SELECT * FROM activity_records WHERE type = 'water' ORDER BY date DESC")
    fun getAllRecords(): Flow<List<ActivityRecord>>
}
