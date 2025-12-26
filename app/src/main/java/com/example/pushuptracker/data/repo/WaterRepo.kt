package com.example.pushuptracker.data.repo

import com.example.pushuptracker.data.local.WaterDao
import com.example.pushuptracker.model.ActivityRecord
import kotlinx.coroutines.flow.Flow
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
