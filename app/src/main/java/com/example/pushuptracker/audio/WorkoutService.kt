package com.example.pushuptracker.audio

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.pushuptracker.MainActivity
import com.example.pushuptracker.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WorkoutService : Service() {

    @Inject
    lateinit var audioCoach: AudioCoach

    private val binder = WorkoutBinder()
    private val notificationId = 1001
    private val channelId = "workout_channel"

    inner class WorkoutBinder : Binder() {
        fun getService(): WorkoutService = this@WorkoutService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val title = intent?.getStringExtra("title") ?: "Antrenman Devam Ediyor"
        val content = intent?.getStringExtra("content") ?: ""
        
        val notification = createNotification(title, content)

        // Android 14 (API 34) ve sonrası için servis tipini açıkça belirtmek zorunludur.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(notificationId, notification)
        }
        
        return START_NOT_STICKY
    }

    fun updateNotification(title: String, content: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, createNotification(title, content))
    }

    private fun createNotification(title: String, content: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP // Uygulama zaten açıksa yeni pencere açmaz
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Öncelik artırıldı
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Kilit ekranında görünürlük
            .build()
    }

    private fun createNotificationChannel() {
        // Önem derecesini DEFAULT yaparak bildirimin görünür olmasını sağlıyoruz
        val channel = NotificationChannel(
            channelId,
            "Antrenman Takibi",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "Aktif antrenman süresini ve hareketleri gösterir."
        channel.setShowBadge(false) // Uygulama ikonu üzerinde sayı gösterme

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }
}
