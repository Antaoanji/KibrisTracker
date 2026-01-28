package com.example.pushuptracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pushuptracker.model.WorkoutRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: WorkoutRecord)

    @Query("SELECT * FROM workout_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<WorkoutRecord>>

    @Query("DELETE FROM workout_records")
    suspend fun clear()
}
