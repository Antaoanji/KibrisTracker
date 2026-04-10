package com.example.pushuptracker.ui.home

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pushuptracker.SettingsManager
import com.example.pushuptracker.UpdateInfo
import com.example.pushuptracker.UpdateManager
import com.example.pushuptracker.audio.WorkoutService
import com.example.pushuptracker.data.repo.PushupRepo
import com.example.pushuptracker.data.repo.WaterRepo
import com.example.pushuptracker.gamification.GamificationManager
import com.example.pushuptracker.gamification.StreakManager
import com.example.pushuptracker.model.ActivityRecord
import com.example.pushuptracker.model.Streak
import com.example.pushuptracker.model.WorkoutSummary
import com.example.pushuptracker.sensor.PushupSensorManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val pushupRepo: PushupRepo,
    private val waterRepo: WaterRepo,
    private val settingsManager: SettingsManager,
    private val pushupSensorManager: PushupSensorManager,
    private val updateManager: UpdateManager,
    private val gamificationManager: GamificationManager,
    private val streakManager: StreakManager
) : AndroidViewModel(application) {

    private val today: String get() = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    private val yesterday: String get() = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)

    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val updateInfo = _updateInfo.asStateFlow()

    private val _remainingPushups = settingsManager.remainingPushupsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val remainingPushups = _remainingPushups

    private var lastPushupUpdate: Double? = null
    private var lastPushupFromPool: Boolean = false

    private val _eventFlow = MutableSharedFlow<HomeEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    sealed class HomeEvent {
        data class ShowUndoSnackbar(val message: String) : HomeEvent()
        object TriggerHaptic : HomeEvent()
    }

    init {
        viewModelScope.launch {
            streakManager.checkAndResetWorkoutStreak()
            checkForUpdates()
        }
    }

    private fun checkForUpdates() {
        viewModelScope.launch {
            val info = updateManager.checkForUpdates()
            _updateInfo.value = info
        }
    }

    fun onUpdateConfirmed() {
        _updateInfo.value?.let {
            updateManager.openDownloadPage(it.downloadUrl)
        }
        _updateInfo.value = null
    }

    fun onUpdateDismissed() {
        _updateInfo.value = null
    }

    val homeScreenState = streakManager.getStreaksFlow()
        .map { streaks -> HomeScreenState(streaks = streaks) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeScreenState())

    fun startWalkingWorkout() {
        val intent = Intent(getApplication(), WorkoutService::class.java).apply {
            action = "START_WALKING"
            putExtra("is_walking", true)
            putExtra("label", "Japon Yürüyüşü")
        }
        getApplication<Application>().startForegroundService(intent)
    }

    fun startLymphaticWorkout() {
        val intent = Intent(getApplication(), WorkoutService::class.java).apply {
            action = "START_LYMPHATIC"
            putExtra("label", "Çin Lenfatik Egzersizi")
        }
        getApplication<Application>().startForegroundService(intent)
    }

    fun startAutoPushupCounting(onCountUpdate: (Int) -> Unit) {
        var lastCount = 0
        pushupSensorManager.startListening { count ->
            val delta = count - lastCount
            if (delta > 0) {
                completePushup(delta)
                lastCount = count
                onCountUpdate(count)
            }
        }
    }

    fun completePushup(count: Int = 1) {
        viewModelScope.launch {
            val currentRem = remainingPushups.first()
            if (currentRem > 0) {
                settingsManager.saveRemainingPushups((currentRem - count.toDouble()).coerceAtLeast(0.0))
                lastPushupFromPool = true
            } else {
                lastPushupFromPool = false
            }
            pushupRepo.addPushups(count.toDouble())
            lastPushupUpdate = count.toDouble()
            
            _eventFlow.emit(HomeEvent.TriggerHaptic)
            _eventFlow.emit(HomeEvent.ShowUndoSnackbar("$count şınav tamamlandı"))
            
            gamificationManager.checkAndUnlockAchievements()
        }
    }

    fun undoLastPushup() {
        viewModelScope.launch {
            val count = lastPushupUpdate ?: return@launch
            if (lastPushupFromPool) {
                val currentRem = remainingPushups.first()
                settingsManager.saveRemainingPushups(currentRem + count)
            }
            pushupRepo.addPushups(-count)
            lastPushupUpdate = null
        }
    }

    fun addRemainingPushups(value: Double) {
        viewModelScope.launch {
            val currentRem = remainingPushups.first()
            settingsManager.saveRemainingPushups(currentRem + value)
            _eventFlow.emit(HomeEvent.TriggerHaptic)
        }
    }

    fun stopAutoPushupCounting() {
        pushupSensorManager.stopListening()
    }

    private suspend fun checkAndResetStreaks() {
        val lastWorkoutSummary = settingsManager.lastWorkoutSummaryFlow.first()
        if (lastWorkoutSummary != null) {
            val lastWorkoutDate = Instant.ofEpochMilli(lastWorkoutSummary.timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            val todayDate = LocalDate.now()
            val daysBetween = ChronoUnit.DAYS.between(lastWorkoutDate, todayDate)
            if (daysBetween > 1) {
                var checkDate = lastWorkoutDate.plusDays(1)
                var isStreakBroken = false
                while(checkDate.isBefore(todayDate)) {
                    val day = checkDate.dayOfWeek
                    val isRestDay = day == DayOfWeek.THURSDAY || day == DayOfWeek.SUNDAY
                    if (!isRestDay) {
                        isStreakBroken = true
                        break
                    }
                    checkDate = checkDate.plusDays(1)
                }
                if (isStreakBroken) {
                    settingsManager.saveCurrentStreak(0)
                }
            }
        } else {
            settingsManager.saveCurrentStreak(0)
        }
    }

    fun getTodayRecord(activityId: String): Flow<ActivityRecord?> {
        return when (activityId) {
            "pushups" -> pushupRepo.getRecordForDate(today)
            "water" -> waterRepo.getRecordForDate(today)
            "walking" -> pushupRepo.getRecordByDateAndType(today, "walking")
            "lymphatic" -> pushupRepo.getRecordByDateAndType(today, "lymphatic")
            else -> flowOf(null)
        }
    }

    fun getYesterdayRecord(activityId: String): Flow<ActivityRecord?> {
        return when (activityId) {
            "pushups" -> pushupRepo.getRecordForDate(yesterday)
            "water" -> waterRepo.getRecordForDate(yesterday)
            "walking" -> pushupRepo.getRecordByDateAndType(yesterday, "walking")
            "lymphatic" -> pushupRepo.getRecordByDateAndType(yesterday, "lymphatic")
            else -> flowOf(null)
        }
    }

    fun getTotal(activityId: String): Flow<Double> {
        return when (activityId) {
            "pushups" -> pushupRepo.getAllPushupRecords().map { records -> records.sumOf { it.value } }
            "water" -> waterRepo.getAllRecords().map { records -> records.sumOf { it.value } }
            "walking" -> pushupRepo.getRecordsByType("walking").map { list -> list.sumOf { it.value } }
            "lymphatic" -> pushupRepo.getRecordsByType("lymphatic").map { list -> list.sumOf { it.value } }
            else -> flowOf(0.0)
        }
    }

    fun getDailyGoal(activityId: String): Flow<Int> {
        return when (activityId) {
            "pushups" -> settingsManager.dailyGoalFlow
            "water" -> settingsManager.dailyWaterGoalFlow
            "walking" -> flowOf(33)
            "lymphatic" -> flowOf(7)
            else -> flowOf(0)
        }
    }

    fun addRecord(activityId: String, value: Double, onGoalReached: (Boolean) -> Unit) {
        viewModelScope.launch {
            when (activityId) {
                "pushups" -> {
                    val goal = settingsManager.dailyGoalFlow.first()
                    val currentRecord = pushupRepo.getRecordForDate(today).first()
                    val wasGoalReachedBefore = (currentRecord?.value ?: 0.0) >= goal
                    val newRecord = pushupRepo.addPushups(value)
                    val isGoalReachedNow = newRecord.value >= goal
                    onGoalReached(!wasGoalReachedBefore && isGoalReachedNow)
                }
                "water" -> {
                    val goal = settingsManager.dailyWaterGoalFlow.first()
                    val currentRecord = waterRepo.getRecordForDate(today).first()
                    val wasGoalReachedBefore = (currentRecord?.value ?: 0.0) >= goal
                    val newRecord = waterRepo.addWater(value)
                    val isGoalReachedNow = newRecord.value >= goal
                    onGoalReached(!wasGoalReachedBefore && isGoalReachedNow)
                }
                "lymphatic" -> {
                    val record = ActivityRecord(type = "lymphatic", value = value, date = today, timestamp = System.currentTimeMillis())
                    pushupRepo.insertRecord(record)
                    onGoalReached(value >= 7.0)
                }
            }
            gamificationManager.checkAndUnlockAchievements()
        }
    }
}

data class HomeScreenState(
    val streaks: List<Streak> = emptyList()
)
