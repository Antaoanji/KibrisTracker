package com.example.pushuptracker.data

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    val permissions = setOf(
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class)
    )

    suspend fun hasAllPermissions(): Boolean {
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        return granted.containsAll(permissions)
    }

    suspend fun readExerciseSessions(
        start: Instant = Instant.now().minusSeconds(24 * 60 * 60),
        end: Instant = Instant.now()
    ): List<ExerciseSessionRecord> {
        return try {
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    recordType = ExerciseSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )
            response.records
        } catch (e: Exception) {
            Log.e("HealthConnect", "Error reading sessions", e)
            emptyList()
        }
    }

    suspend fun readTotalCalories(
        start: Instant,
        end: Instant
    ): Double {
        return try {
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    recordType = ActiveCaloriesBurnedRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )
            response.records.sumOf { it.energy.inKilocalories }
        } catch (e: Exception) {
            Log.e("HealthConnect", "Error reading calories", e)
            0.0
        }
    }

    /**
     * Verileri gün gün gruplayarak okur. Senkronizasyon için idealdir.
     */
    suspend fun readDailyCalories(
        start: Instant,
        end: Instant
    ): Map<String, Double> {
        return try {
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    recordType = ActiveCaloriesBurnedRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )

            response.records.groupBy {
                LocalDate.ofInstant(it.startTime, ZoneId.systemDefault())
                    .format(DateTimeFormatter.ISO_LOCAL_DATE)
            }.mapValues { entry ->
                entry.value.sumOf { it.energy.inKilocalories }
            }
        } catch (e: Exception) {
            Log.e("HealthConnect", "Error reading daily calories", e)
            emptyMap()
        }
    }
}
