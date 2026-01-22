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
    suspend fun insertRecord(record: ActivityRecord)

    @Query("SELECT * FROM activity_records WHERE date = :date AND type = 'pushup'")
    fun getRecordForDate(date: String): Flow<ActivityRecord?>

    @Query("SELECT * FROM activity_records WHERE date = :date AND type = :type")
    fun getRecordByDateAndType(date: String, type: String): Flow<ActivityRecord?>

    @Query("SELECT * FROM activity_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<ActivityRecord>>
    
    @Query("SELECT * FROM activity_records WHERE type = :exerciseType ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastRecordForExercise(exerciseType: String): ActivityRecord?

    @Query("SELECT DISTINCT type FROM activity_records")
    fun getDistinctExerciseTypes(): Flow<List<String>>

    @Query("SELECT * FROM activity_records WHERE type = :type ORDER BY timestamp ASC")
    fun getRecordsByType(type: String): Flow<List<ActivityRecord>>

    @Query("SELECT * FROM activity_records WHERE type = 'pushup'")
    fun getAllPushupRecords(): Flow<List<ActivityRecord>>

    @Query("DELETE FROM activity_records WHERE date = :date AND type = 'pushup'")
    suspend fun delete(date: String)

    @Query("DELETE FROM activity_records WHERE type = :type AND date = :date")
    suspend fun deleteRecordForDateAndType(type: String, date: String)

    @Query("DELETE FROM activity_records")
    suspend fun clear()
}
