package com.example.pushuptracker.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pushuptracker.model.ActivityRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecord(record: ActivityRecord)

    @Query("SELECT * FROM activity_records WHERE type = :type ORDER BY date ASC")
    fun getRecordsByType(type: String): Flow<List<ActivityRecord>>

    @Query("SELECT date FROM activity_records WHERE type = :type AND value >= :goalValue")
    fun getGoalMetDates(type: String, goalValue: Double): Flow<List<String>>

    @Query("SELECT * FROM activity_records WHERE date = :date AND type = :type")
    fun getRecordByDateAndType(date: String, type: String): Flow<ActivityRecord?>

    @Query("SELECT SUM(value) FROM activity_records WHERE type = :type")
    fun getTotalValueByType(type: String): Flow<Double?>

    @Query("DELETE FROM activity_records WHERE date = :date AND type = :type")
    suspend fun deleteRecordByDateAndType(date: String, type: String)

    @Query("DELETE FROM activity_records WHERE type = :type")
    suspend fun clearRecordsByType(type: String)
}
