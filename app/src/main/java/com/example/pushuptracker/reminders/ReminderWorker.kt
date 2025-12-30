package com.example.pushuptracker.reminders

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class ReminderWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    companion object {
        const val KEY_REMINDER_TYPE = "reminder_type"
        const val TYPE_WORKOUT = "workout"
        const val TYPE_PUSHUP = "pushup"
        const val TYPE_WATER = "water"
    }

    override fun doWork(): Result {
        val reminderType = inputData.getString(KEY_REMINDER_TYPE)
            ?: return Result.failure()

        val title = when (reminderType) {
            TYPE_WORKOUT -> "Antrenman Zamanı!"
            TYPE_PUSHUP -> "Şınav Zamanı!"
            TYPE_WATER -> "Su Molası!"
            else -> "Hatırlatıcı"
        }
        
        val content = when (reminderType) {
            TYPE_WORKOUT -> "Bugünkü antrenmanını tamamlama zamanı geldi!"
            TYPE_PUSHUP -> "Bugünkü şınav hedefini unutma!"
            TYPE_WATER -> "Bir bardak su içmenin tam sırası."
            else -> ""
        }

        val notificationId = when (reminderType) {
            TYPE_WORKOUT -> NotificationHelper.WORKOUT_REMINDER_ID
            TYPE_PUSHUP -> NotificationHelper.PUSHUP_REMINDER_ID
            TYPE_WATER -> NotificationHelper.WATER_REMINDER_ID
            else -> -1
        }

        if (notificationId != -1) {
            NotificationHelper.createNotificationChannel(applicationContext, reminderType)
            NotificationHelper.sendReminderNotification(applicationContext, title, content, notificationId)
        }

        return Result.success()
    }
}
