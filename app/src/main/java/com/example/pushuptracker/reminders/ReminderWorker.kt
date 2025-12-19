package com.example.pushuptracker.reminders

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class ReminderWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    companion object {
        const val KEY_REMINDER_TYPE = "reminder_type"
        const val TYPE_PUSHUP = "pushup"
        const val TYPE_WATER = "water"
    }

    override fun doWork(): Result {
        val reminderType = inputData.getString(KEY_REMINDER_TYPE)
            ?: return Result.failure()

        val title = if (reminderType == TYPE_PUSHUP) "Şınav Zamanı!" else "Su Molası!"
        val content = if (reminderType == TYPE_PUSHUP) "Bugünkü şınav hedefini unutma!" else "Bir bardak su içmenin tam sırası."

        NotificationHelper.createNotificationChannel(applicationContext)
        NotificationHelper.sendReminderNotification(applicationContext, title, content)

        return Result.success()
    }
}
