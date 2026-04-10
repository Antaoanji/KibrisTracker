package com.example.pushuptracker.data

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Instant
import java.time.temporal.ChronoUnit

@HiltWorker
class HealthConnectWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val healthConnectManager: HealthConnectManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            if (healthConnectManager.hasAllPermissions()) {
                val end = Instant.now()
                val start = end.minus(7, ChronoUnit.DAYS)
                
                // Read and sync calories or sessions if needed
                // Currently just checking permissions and reading as a "warm-up"
                // In a real app, we might save this to a local DB cache
                healthConnectManager.readTotalCalories(start, end)
                healthConnectManager.readExerciseSessions(start, end)
                
                Result.success()
            } else {
                Result.failure()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
