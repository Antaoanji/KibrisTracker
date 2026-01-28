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
import com.example.pushuptracker.gamification.GamificationManager
import com.example.pushuptracker.model.ActivityRecord
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class WorkoutService : Service() {

    @Inject
    lateinit var audioCoach: AudioCoach
    
    @Inject
    lateinit var pushupRepo: PushupRepo

    @Inject
    lateinit var gamificationManager: GamificationManager

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

    private val _lymphaticState = MutableStateFlow<LymphaticState?>(null)
    val lymphaticState = _lymphaticState.asStateFlow()

    data class TimerState(val remainingSeconds: Int, val isPaused: Boolean, val label: String)
    data class WalkingState(val remainingSeconds: Int, val isPaused: Boolean, val isFastMode: Boolean)
    data class LymphaticState(
        val currentMovementIndex: Int,
        val remainingSeconds: Int,
        val isPaused: Boolean,
        val movementName: String,
        val movementDescription: String,
        val isPreparing: Boolean = false
    )

    private val lymphaticMovements = listOf(
        Pair("Lymphatic Hopping", "Topuklarını yere hafifçe vurarak, tüm vücudunu titret."),
        Pair("Armpit Tapping", "Sol ve sağ koltuk altına hafifçe vur."),
        Pair("Arm Swing", "Kollarını öne ve arkaya serbestçe salla."),
        Pair("Trunk Twists", "Gövdeni sağa sola döndür, kollar serbest."),
        Pair("Body Wave", "Omurganla akışkan bir dalgalanma yap."),
        Pair("Spinal Twist", "Kollar sırta ve göğüse çarpsın."),
        Pair("Marches", "Dizlerini karnına doğru çekerek yürü.")
    )

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
        val initialSeconds = intent?.getIntExtra("seconds", 0) ?: 0
        val label = intent?.getStringExtra("label") ?: "Antrenman"

        when (action) {
            "START_WALKING" -> startWalkingTimer()
            "START_LYMPHATIC" -> startLymphaticTimer()
            "SKIP_LYMPHATIC" -> skipLymphaticMovement()
            "START_TIMER" -> startGeneralTimer(initialSeconds, label)
            "PAUSE" -> pauseActive()
            "RESUME" -> resumeActive()
            "STOP" -> stopAll(saveProgress = true)
            "CANCEL" -> stopAll(saveProgress = false)
            else -> {
                val title = intent?.getStringExtra("title") ?: "Antrenman Devam Ediyor"
                val content = intent?.getStringExtra("content") ?: ""
                updateNotification(title, content, true)
            }
        }
        
        return START_NOT_STICKY
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "KibrisTracker:WorkoutWakeLock")
        wakeLock?.acquire()
    }

    private fun startGeneralTimer(seconds: Int, label: String) {
        _timerState.value = TimerState(seconds, false, label)
        _walkingState.value = null
        _lymphaticState.value = null
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
                updateNotification(current.label, "$newTime saniye kaldı", true)

                if (newTime == 3) serviceScope.launch { audioCoach.playCountdown() }
                delay(1000)
            }
            if (_timerState.value?.remainingSeconds ?: 0 <= 0) {
                audioCoach.announceExercise("Süre bitti!")
            }
        }
    }

    private fun startWalkingTimer() {
        _walkingState.value = WalkingState(33 * 60, false, false)
        _timerState.value = null
        _lymphaticState.value = null
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
                val timeStr = String.format(Locale.getDefault(), "%02d:%02d", newTime / 60, newTime % 60)
                updateNotification("Japon Yürüyüşü: $timeStr", if (isFast) "🔥 HIZLI TEMPO" else "🍃 YAVAŞ TEMPO", true)

                if (newTime > 0 && newTime % 180 == 0) {
                    audioCoach.announceExercise(if (isFast) "Hızlanma zamanı!" else "Yavaşla ve nefeslen.")
                }
                delay(1000)
            }
            if (_walkingState.value?.remainingSeconds ?: 0 <= 0) {
                saveRecord("walking", 33.0) 
                audioCoach.announceExercise("Tebrikler! Japon Yürüyüşü tamamlandı.")
                stopAll(saveProgress = false)
            }
        }
    }

    private fun startLymphaticTimer() {
        val first = lymphaticMovements[0]
        _lymphaticState.value = LymphaticState(0, 60, false, first.first, first.second, isPreparing = true)
        _timerState.value = null
        _walkingState.value = null
        runLymphaticTimer()
    }

    private fun skipLymphaticMovement() {
        val current = _lymphaticState.value ?: return
        val nextIndex = current.currentMovementIndex + 1
        if (nextIndex < lymphaticMovements.size) {
            val nextMovement = lymphaticMovements[nextIndex]
            _lymphaticState.value = current.copy(
                currentMovementIndex = nextIndex,
                remainingSeconds = 60,
                movementName = nextMovement.first,
                movementDescription = nextMovement.second,
                isPreparing = true
            )
            runLymphaticTimer() 
        } else {
            stopAll(saveProgress = true)
        }
    }

    private fun runLymphaticTimer() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive) {
                val current = _lymphaticState.value ?: break
                if (current.isPaused) { delay(500); continue }

                if (current.isPreparing) {
                    audioCoach.announceExercise("Sıradaki hareket: ${current.movementName}")
                    delay(2500) 
                    audioCoach.announceExercise(current.movementDescription)
                    _lymphaticState.value = _lymphaticState.value!!.copy(isPreparing = false)
                    continue
                }
                
                if (current.remainingSeconds <= 0) {
                    val nextIndex = current.currentMovementIndex + 1
                    if (nextIndex < lymphaticMovements.size) {
                        val nextMovement = lymphaticMovements[nextIndex]
                        _lymphaticState.value = current.copy(
                            currentMovementIndex = nextIndex,
                            remainingSeconds = 60,
                            movementName = nextMovement.first,
                            movementDescription = nextMovement.second,
                            isPreparing = true
                        )
                        continue
                    } else {
                        break
                    }
                }

                val newTime = _lymphaticState.value!!.remainingSeconds - 1
                _lymphaticState.value = _lymphaticState.value!!.copy(remainingSeconds = newTime)
                updateNotification("Lenfatik: ${_lymphaticState.value!!.movementName}", "$newTime saniye kaldı", true)
                
                if (newTime == 30) audioCoach.announceExercise("Son 30 saniye")
                if (newTime == 15) audioCoach.announceExercise("Son 15 saniye")
                
                if (newTime == 3) serviceScope.launch { audioCoach.playCountdown() }
                delay(1000)
            }
            if (_lymphaticState.value != null && _lymphaticState.value!!.currentMovementIndex >= lymphaticMovements.size - 1) {
                saveRecord("lymphatic", 7.0)
                audioCoach.announceExercise("Tebrikler! Lenfatik egzersiz tamamlandı.")
                stopAll(saveProgress = false)
            }
        }
    }

    private suspend fun saveRecord(type: String, minutes: Double) {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val record = ActivityRecord(
            type = type,
            value = minutes,
            date = today,
            timestamp = System.currentTimeMillis()
        )
        pushupRepo.insertRecord(record)
        gamificationManager.checkAndUnlockAchievements()
    }

    private fun pauseActive() {
        _timerState.value = _timerState.value?.copy(isPaused = true)
        _walkingState.value = _walkingState.value?.copy(isPaused = true)
        _lymphaticState.value = _lymphaticState.value?.copy(isPaused = true)
        updateNotification("Duraklatıldı", "Devam etmek için dokunun", false)
    }

    private fun resumeActive() {
        _timerState.value = _timerState.value?.copy(isPaused = false)
        _walkingState.value = _walkingState.value?.copy(isPaused = false)
        _lymphaticState.value = _lymphaticState.value?.copy(isPaused = false)
        
        if (_timerState.value != null) runGeneralTimer() 
        else if (_walkingState.value != null) runWalkingTimer()
        else if (_lymphaticState.value != null) runLymphaticTimer()
    }

    private fun stopAll(saveProgress: Boolean) {
        val currentWalking = _walkingState.value
        if (saveProgress && currentWalking != null) {
            val walkedMinutes = ((33 * 60) - currentWalking.remainingSeconds) / 60.0
            serviceScope.launch { saveRecord("walking", walkedMinutes) }
        }
        
        val currentLymphatic = _lymphaticState.value
        if (saveProgress && currentLymphatic != null) {
            val completedMinutes = (currentLymphatic.currentMovementIndex + 1).toDouble()
            serviceScope.launch { saveRecord("lymphatic", completedMinutes) }
        }

        _timerState.value = null
        _walkingState.value = null
        _lymphaticState.value = null
        timerJob?.cancel()
        
        // Bildirimi kesin olarak temizle
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(notificationId)
        
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun isAppInForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        val packageName = packageName
        for (appProcess in appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && 
                appProcess.processName == packageName) {
                return true
            }
        }
        return false
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

        val priority = if (isRunning && isAppInForeground()) NotificationCompat.PRIORITY_LOW else NotificationCompat.PRIORITY_MAX

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setContentText(content)
            .setOngoing(isRunning)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setPriority(priority)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)
            .addAction(R.drawable.logo, if (isRunning) "DURAKLAT" else "DEVAM ET", PendingIntent.getService(this, 2, pauseIntent, PendingIntent.FLAG_IMMUTABLE))
            .addAction(R.drawable.logo, "BİTİR", PendingIntent.getService(this, 1, stopIntent, PendingIntent.FLAG_IMMUTABLE))
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(channelId, "Antrenman Takibi", NotificationManager.IMPORTANCE_DEFAULT)
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        channel.setSound(null, null)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        timerJob?.cancel()
        serviceScope.cancel()
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(notificationId)
        stopForeground(STOP_FOREGROUND_REMOVE)
        if (wakeLock?.isHeld == true) wakeLock?.release()
        super.onDestroy()
    }
}
