package com.example.pushuptracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pushuptracker.model.ActivityRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface PushupDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: ActivityRecord)

    @Query("SELECT * FROM activity_records WHERE type = 'pushup' AND date = :date")
    fun getRecordForDate(date: String): Flow<ActivityRecord?>

    @Query("SELECT * FROM activity_records WHERE type = 'pushup' ORDER BY date DESC")
    fun getAllRecords(): Flow<List<ActivityRecord>>
    
    @Query("DELETE FROM activity_records WHERE type = 'pushup' AND date = :date")
    suspend fun delete(date: String)
}
