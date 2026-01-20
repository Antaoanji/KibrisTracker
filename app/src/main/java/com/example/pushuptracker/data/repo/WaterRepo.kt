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
        
        val newRecord = if (currentRecord != null) {
            currentRecord.copy(
                value = currentRecord.value + amount,
                timestamp = System.currentTimeMillis()
            )
        } else {
            ActivityRecord(
                type = "water",
                value = amount,
                date = todayDateString
            )
        }
        
        waterDao.upsert(newRecord)
        return newRecord
    }

    suspend fun clear() {
        waterDao.clear()
    }
}
