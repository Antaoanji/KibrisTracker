package com.example.pushuptracker.audio

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.pushuptracker.MainActivity
import com.example.pushuptracker.R
import com.example.pushuptracker.data.repo.PushupRepo
import com.example.pushuptracker.model.ActivityRecord
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class WorkoutService : Service() {

    @Inject
    lateinit var audioCoach: AudioCoach
    
    @Inject
    lateinit var pushupRepo: PushupRepo

    private val binder = WorkoutBinder()
    private val notificationId = 1001
    private val channelId = "workout_channel"
    
    private var timerJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var wakeLock: PowerManager.WakeLock? = null

    private val _timerState = MutableStateFlow<TimerState?>(null)
    val timerState = _timerState.asStateFlow()

    private val _walkingState = MutableStateFlow<WalkingState?>(null)
    val walkingState = _walkingState.asStateFlow()

    data class TimerState(val remainingSeconds: Int, val isPaused: Boolean, val label: String)
    data class WalkingState(val remainingSeconds: Int, val isPaused: Boolean, val isFastMode: Boolean)

    inner class WorkoutBinder : Binder() {
        fun getService(): WorkoutService = this@WorkoutService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val isWalking = intent?.getBooleanExtra("is_walking", false) ?: false
        val initialSeconds = intent?.getIntExtra("seconds", 0) ?: 0
        val label = intent?.getStringExtra("label") ?: "Antrenman"

        when (action) {
            "START_WALKING" -> startWalkingTimer()
            "START_TIMER" -> startGeneralTimer(initialSeconds, label)
            "PAUSE" -> pauseActive()
            "RESUME" -> resumeActive()
            "STOP" -> stopAll(saveProgress = true) // BİTİR butonu
            "CANCEL" -> stopAll(saveProgress = false) // İPTAL ET butonu
            else -> {
                val title = intent?.getStringExtra("title") ?: "Antrenman Devam Ediyor"
                val content = intent?.getStringExtra("content") ?: ""
                updateNotification(title, content, true)
            }
        }
        
        return START_STICKY
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "KibrisTracker:WorkoutWakeLock")
        wakeLock?.acquire()
    }

    private fun startGeneralTimer(seconds: Int, label: String) {
        _timerState.value = TimerState(seconds, false, label)
        _walkingState.value = null
        runGeneralTimer()
    }

    private fun runGeneralTimer() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive) {
                val current = _timerState.value ?: break
                if (current.isPaused) { delay(500); continue }
                if (current.remainingSeconds <= 0) break

                val newTime = current.remainingSeconds - 1
                _timerState.value = current.copy(remainingSeconds = newTime)
                
                withContext(Dispatchers.Main) {
                    updateNotification(current.label, "$newTime saniye kaldı", true)
                }

                if (newTime <= 3 && newTime > 0) {
                    withContext(Dispatchers.Main) { audioCoach.playCountdown() }
                }
                delay(1000)
            }
            if (_timerState.value?.remainingSeconds ?: 0 <= 0) {
                withContext(Dispatchers.Main) { audioCoach.announceExercise("Süre bitti!") }
            }
        }
    }

    private fun startWalkingTimer() {
        _walkingState.value = WalkingState(33 * 60, false, false)
        _timerState.value = null
        runWalkingTimer()
    }

    private fun runWalkingTimer() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive) {
                val current = _walkingState.value ?: break
                if (current.isPaused) { delay(500); continue }
                if (current.remainingSeconds <= 0) break

                val elapsed = (33 * 60) - current.remainingSeconds
                val isFast = (elapsed / 180) % 2 != 0
                val newTime = current.remainingSeconds - 1

                _walkingState.value = current.copy(remainingSeconds = newTime, isFastMode = isFast)
                
                val timeStr = String.format("%02d:%02d", newTime / 60, newTime % 60)
                withContext(Dispatchers.Main) {
                    updateNotification("Japon Yürüyüşü: $timeStr", if (isFast) "🔥 HIZLI TEMPO" else "🍃 YAVAŞ TEMPO", true)
                }

                if (newTime > 0 && newTime % 180 == 0) {
                    withContext(Dispatchers.Main) {
                        audioCoach.announceExercise(if (isFast) "Hızlanma zamanı!" else "Yavaşla ve nefeslen.")
                    }
                }
                delay(1000)
            }
            if (_walkingState.value?.remainingSeconds ?: 0 <= 0) {
                saveWalkingRecord(33.0) 
                withContext(Dispatchers.Main) {
                    audioCoach.announceExercise("Tebrikler! Japon Yürüyüşü tamamlandı.")
                }
                stopAll(saveProgress = false) // Already saved
            }
        }
    }

    private suspend fun saveWalkingRecord(minutes: Double) {
        if (minutes < 1.0) return // 1 dakikadan az yürüyüşleri kaydetme
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val record = ActivityRecord(
            type = "walking",
            value = minutes,
            date = today,
            timestamp = System.currentTimeMillis()
        )
        pushupRepo.insertRecord(record)
    }

    private fun pauseActive() {
        _timerState.value = _timerState.value?.copy(isPaused = true)
        _walkingState.value = _walkingState.value?.copy(isPaused = true)
        updateNotification("Duraklatıldı", "Devam etmek için dokunun", false)
    }

    private fun resumeActive() {
        _timerState.value = _timerState.value?.copy(isPaused = false)
        _walkingState.value = _walkingState.value?.copy(isPaused = false)
        if (_timerState.value != null) runGeneralTimer() else if (_walkingState.value != null) runWalkingTimer()
    }

    private fun stopAll(saveProgress: Boolean) {
        val currentWalking = _walkingState.value
        if (saveProgress && currentWalking != null) {
            val totalSeconds = 33 * 60
            val walkedSeconds = totalSeconds - currentWalking.remainingSeconds
            val walkedMinutes = walkedSeconds / 60.0
            serviceScope.launch {
                saveWalkingRecord(walkedMinutes)
            }
        }

        _timerState.value = null
        _walkingState.value = null
        timerJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun updateNotification(title: String, content: String, isRunning: Boolean) {
        val notification = createNotificationBuilder(title, content, isRunning).build()
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(notificationId, notification)
        
        if (isRunning) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                startForeground(notificationId, notification)
            }
        }
    }

    private fun createNotificationBuilder(title: String, content: String, isRunning: Boolean): NotificationCompat.Builder {
        val intent = Intent(this, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val pauseIntent = Intent(this, WorkoutService::class.java).apply { action = if (isRunning) "PAUSE" else "RESUME" }
        val stopIntent = Intent(this, WorkoutService::class.java).apply { action = "STOP" }

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setContentText(content)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)
            .addAction(R.drawable.logo, if (isRunning) "DURAKLAT" else "DEVAM ET", PendingIntent.getService(this, 2, pauseIntent, PendingIntent.FLAG_IMMUTABLE))
            .addAction(R.drawable.logo, "BİTİR", PendingIntent.getService(this, 1, stopIntent, PendingIntent.FLAG_IMMUTABLE))
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(channelId, "Antrenman Takibi", NotificationManager.IMPORTANCE_HIGH)
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        timerJob?.cancel()
        if (wakeLock?.isHeld == true) wakeLock?.release()
        super.onDestroy()
    }
}
