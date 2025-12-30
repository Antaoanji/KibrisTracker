package com.example.pushuptracker.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.pushuptracker.R

object NotificationHelper {

    private const val WORKOUT_CHANNEL_ID = "workout_reminder_channel"
    private const val PUSHUP_CHANNEL_ID = "pushup_reminder_channel"
    private const val WATER_CHANNEL_ID = "water_reminder_channel"

    const val WORKOUT_REMINDER_ID = 1
    const val PUSHUP_REMINDER_ID = 2
    const val WATER_REMINDER_ID = 3

    fun createNotificationChannel(context: Context, reminderType: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val (channelId, channelName) = when (reminderType) {
                ReminderWorker.TYPE_WORKOUT -> WORKOUT_CHANNEL_ID to "Antrenman Hatırlatıcıları"
                ReminderWorker.TYPE_PUSHUP -> PUSHUP_CHANNEL_ID to "Şınav Hatırlatıcıları"
                ReminderWorker.TYPE_WATER -> WATER_CHANNEL_ID to "Su Hatırlatıcıları"
                else -> return
            }

            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendReminderNotification(context: Context, title: String, content: String, notificationId: Int) {
        val channelId = when (notificationId) {
            WORKOUT_REMINDER_ID -> WORKOUT_CHANNEL_ID
            PUSHUP_REMINDER_ID -> PUSHUP_CHANNEL_ID
            WATER_REMINDER_ID -> WATER_CHANNEL_ID
            else -> return
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }
}
