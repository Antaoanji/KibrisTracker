package com.example.pushuptracker.reminders

import android.content.Context
import androidx.work.*
import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    private const val PUSHUP_REMINDER_TAG = "pushup_reminder_tag"
    private const val WATER_REMINDER_TAG = "water_reminder_tag"

    fun scheduleDailyPushupReminder(context: Context, time: LocalTime) {
        val now = ZonedDateTime.now()
        var scheduleTime = now.with(time)

        // If the time is already past for today, schedule it for tomorrow
        if (now.isAfter(scheduleTime)) {
            scheduleTime = scheduleTime.plusDays(1)
        }

        val initialDelay = Duration.between(now, scheduleTime).toMillis()

        val inputData = workDataOf(ReminderWorker.KEY_REMINDER_TYPE to ReminderWorker.TYPE_PUSHUP)

        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag(PUSHUP_REMINDER_TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            PUSHUP_REMINDER_TAG,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun cancelPushupReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(PUSHUP_REMINDER_TAG)
    }

    fun schedulePeriodicWaterReminder(context: Context, repeatIntervalMinutes: Long) {
        val inputData = workDataOf(ReminderWorker.KEY_REMINDER_TYPE to ReminderWorker.TYPE_WATER)

        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
            repeatIntervalMinutes, TimeUnit.MINUTES
        )
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WATER_REMINDER_TAG,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun cancelWaterReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WATER_REMINDER_TAG)
    }
}
