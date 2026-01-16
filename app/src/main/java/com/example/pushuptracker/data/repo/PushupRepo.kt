package com.example.pushuptracker.data.repo

import com.example.pushuptracker.data.local.PushupDao
import com.example.pushuptracker.model.ActivityRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository to abstract data operations for all activities.
 * This class is the single source of truth for the ViewModels,
 * providing a clean API by exposing DAO functions directly.
 */
@Singleton
class PushupRepo @Inject constructor(private val pushupDao: PushupDao) {

    private val today: String get() = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    // General-purpose functions
    suspend fun insertRecord(record: ActivityRecord) = pushupDao.insertRecord(record)
    suspend fun clear() = pushupDao.clear()
    suspend fun delete(date: String) = pushupDao.delete(date)
    suspend fun deleteRecordForDateAndType(type: String, date: String) =
        pushupDao.deleteRecordForDateAndType(type, date)

    suspend fun addPushups(count: Double): ActivityRecord {
        val currentRecord = getRecordForDate(today).first()
        val newValue = (currentRecord?.value ?: 0.0) + count
        val newRecord = ActivityRecord(type = "pushup", value = newValue, date = today)
        insertRecord(newRecord)
        return newRecord
    }

    // Functions for Home & Achievements screens
    fun getAllRecords(): Flow<List<ActivityRecord>> = pushupDao.getAllRecords()
    fun getRecordForDate(date: String): Flow<ActivityRecord?> = pushupDao.getRecordForDate(date)

    // Functions for Workout Player
    suspend fun getLastRecordForExercise(exerciseType: String): ActivityRecord? =
        pushupDao.getLastRecordForExercise(exerciseType)

    // Functions for Stats screen
    fun getDistinctExerciseTypes(): Flow<List<String>> = pushupDao.getDistinctExerciseTypes()
    fun getAllRecordsForType(type: String): Flow<List<ActivityRecord>> =
        pushupDao.getAllRecordsForType(type)

    fun getAllPushupRecords(): Flow<List<ActivityRecord>> = pushupDao.getAllPushupRecords()
}
