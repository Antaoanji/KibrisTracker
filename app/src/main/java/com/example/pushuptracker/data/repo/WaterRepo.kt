package com.example.pushuptracker.data.repo

import com.example.pushuptracker.data.local.WaterDao
import com.example.pushuptracker.model.ActivityRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WaterRepo @Inject constructor(private val waterDao: WaterDao) {

    private val todayDateString: String
        get() = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    fun getRecordForDate(date: String): Flow<ActivityRecord?> {
        return waterDao.getRecordForDate(date)
    }

    fun getAllRecords(): Flow<List<ActivityRecord>> {
        return waterDao.getAllRecords()
    }

    suspend fun addWater(amount: Double): ActivityRecord {
        val currentRecord = getRecordForDate(todayDateString).first()
        val newValue = (currentRecord?.value ?: 0.0) + amount
        val newRecord = ActivityRecord(
            type = "water",
            value = newValue,
            date = todayDateString
        )
        waterDao.upsert(newRecord)
        return newRecord
    }

    suspend fun addWaterIntake(amount: Int) {
        val todayRecord = ActivityRecord(
            type = "water",
            value = amount.toDouble(),
            date = todayDateString
        )
        waterDao.upsert(todayRecord)
    }

    suspend fun clear() {
        waterDao.clear()
    }
}
