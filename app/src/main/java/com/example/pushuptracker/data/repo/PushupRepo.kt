package com.example.pushuptracker.data.repo

import com.example.pushuptracker.data.local.PushupDao
import com.example.pushuptracker.model.ActivityRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PushupRepo @Inject constructor(private val pushupDao: PushupDao) {

    private val todayDateString: String
        get() = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    fun getPushupsForToday(): Flow<Int> {
        return pushupDao.getRecordForDate(todayDateString)
            .map { it?.value?.toInt() ?: 0 }
    }

    fun getRecordForDate(date: String): Flow<ActivityRecord?> {
        return pushupDao.getRecordForDate(date)
    }
    
    fun getAllRecords(): Flow<List<ActivityRecord>> {
        return pushupDao.getAllRecords()
    }

    suspend fun addPushups(count: Int) {
        val todayRecord = ActivityRecord(
            type = "pushup",
            value = count.toDouble(),
            date = todayDateString
        )
        pushupDao.upsert(todayRecord)
    }

    suspend fun updatePushupsForDate(date: String, count: Int) {
        val record = ActivityRecord(
            type = "pushup",
            value = count.toDouble(),
            date = date
        )
        pushupDao.upsert(record)
    }

    suspend fun deletePushupsForDate(date: String) {
        pushupDao.delete(date)
    }
}
