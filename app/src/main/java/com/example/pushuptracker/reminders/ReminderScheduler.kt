package com.example.pushuptracker.reminders

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(@param:ApplicationContext private val context: Context) {

    private val workManager = WorkManager.getInstance(context)

    private companion object {
        const val PUSHUP_REMINDER_TAG = "pushup_reminder_tag"
        const val WATER_REMINDER_TAG = "water_reminder_tag"
    }

    fun scheduleDailyPushupReminder(time: LocalTime) {
        val now = ZonedDateTime.now()
        var scheduleTime = now.with(time)

        if (now.isAfter(scheduleTime)) {
            scheduleTime = scheduleTime.plusDays(1)
        }

        val initialDelay = Duration.between(now, scheduleTime).toMillis()

        val inputData = workDataOf(ReminderWorker.KEY_REMINDER_TYPE to ReminderWorker.TYPE_PUSHUP)

        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag(PUSHUP_REMINDER_TAG)
            .build()

        workManager.enqueueUniquePeriodicWork(
            PUSHUP_REMINDER_TAG,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun cancelPushupReminder() {
        workManager.cancelUniqueWork(PUSHUP_REMINDER_TAG)
    }

    fun schedulePeriodicWaterReminder(repeatIntervalMinutes: Long) {
        val inputData = workDataOf(ReminderWorker.KEY_REMINDER_TYPE to ReminderWorker.TYPE_WATER)

        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
            repeatIntervalMinutes, TimeUnit.MINUTES
        )
            .setInputData(inputData)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WATER_REMINDER_TAG,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun cancelWaterReminder() {
        workManager.cancelUniqueWork(WATER_REMINDER_TAG)
    }
}
