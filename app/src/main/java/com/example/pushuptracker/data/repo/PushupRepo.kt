package com.example.pushuptracker.data.repo

import com.example.pushuptracker.data.local.PushupDao
import com.example.pushuptracker.model.ActivityRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PushupRepo @Inject constructor(private val pushupDao: PushupDao) {

    private val today: String get() = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    suspend fun insertRecord(record: ActivityRecord) = pushupDao.insertRecord(record)
    suspend fun clear() = pushupDao.clear()
    suspend fun delete(date: String) = pushupDao.delete(date)
    suspend fun deleteRecordForDateAndType(type: String, date: String) =
        pushupDao.deleteRecordForDateAndType(type, date)

    suspend fun addPushups(count: Double): ActivityRecord {
        val currentRecord = getRecordForDate(today).first()
        
        val newRecord = if (currentRecord != null) {
            currentRecord.copy(
                value = currentRecord.value + count,
                timestamp = System.currentTimeMillis()
            )
        } else {
            ActivityRecord(type = "pushup", value = count, date = today)
        }
        
        insertRecord(newRecord)
        return newRecord
    }

    fun getAllRecords(): Flow<List<ActivityRecord>> = pushupDao.getAllRecords()
    fun getRecordForDate(date: String): Flow<ActivityRecord?> = pushupDao.getRecordForDate(date)

    fun getRecordByDateAndType(date: String, type: String): Flow<ActivityRecord?> = 
        pushupDao.getRecordByDateAndType(date, type)

    // Doğru isimlendirme: DAO ile uyumlu
    fun getRecordsByType(type: String): Flow<List<ActivityRecord>> = 
        pushupDao.getRecordsByType(type)

    suspend fun getLastRecordForExercise(exerciseType: String): ActivityRecord? =
        pushupDao.getLastRecordForExercise(exerciseType)

    fun getDistinctExerciseTypes(): Flow<List<String>> = pushupDao.getDistinctExerciseTypes()

    fun getAllPushupRecords(): Flow<List<ActivityRecord>> = pushupDao.getAllPushupRecords()
}
