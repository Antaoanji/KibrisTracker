package com.example.pushuptracker.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.pushuptracker.model.ActivityRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface PushupDao {

    @Upsert
    suspend fun upsert(record: ActivityRecord)

    @Query("SELECT * FROM activity_records WHERE date = :date AND type = 'pushup'")
    fun getRecordForDate(date: String): Flow<ActivityRecord?>

    @Query("SELECT * FROM activity_records WHERE type = 'pushup' ORDER BY date DESC")
    fun getAllRecords(): Flow<List<ActivityRecord>>

    @Query("DELETE FROM activity_records WHERE date = :date AND type = 'pushup'")
    suspend fun delete(date: String)

    @Query("DELETE FROM activity_records WHERE type = 'pushup'")
    suspend fun clear()
}
