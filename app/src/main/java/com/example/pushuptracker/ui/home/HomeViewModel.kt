package com.example.pushuptracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pushuptracker.SettingsManager
import com.example.pushuptracker.UpdateInfo
import com.example.pushuptracker.UpdateManager
import com.example.pushuptracker.ai.PushupSensorManager
import com.example.pushuptracker.data.repo.PushupRepo
import com.example.pushuptracker.data.repo.WaterRepo
import com.example.pushuptracker.model.ActivityRecord
import com.example.pushuptracker.model.Streak
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val pushupRepo: PushupRepo,
    private val waterRepo: WaterRepo,
    private val settingsManager: SettingsManager,
    private val pushupSensorManager: PushupSensorManager,
    private val updateManager: UpdateManager // NEW: Added UpdateManager
) : ViewModel() {

    private val today: String get() = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    private val yesterday: String get() = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)

    // NEW: Update State
    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val updateInfo = _updateInfo.asStateFlow()

    init {
        viewModelScope.launch {
            checkAndResetStreaks()
            checkForUpdates() // NEW: Check for updates on startup
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

    private val pushupDataFlow = combine(
        pushupRepo.getRecordForDate(today),
        pushupRepo.getAllPushupRecords(),
        settingsManager.dailyGoalFlow
    ) { todayPushups, allPushups, dailyGoal ->
        Triple(todayPushups, allPushups, dailyGoal)
    }

    private val waterDataFlow = combine(
        waterRepo.getRecordForDate(today),
        waterRepo.getAllRecords(),
        settingsManager.dailyWaterGoalFlow
    ) { todayWater, allWater, dailyGoal ->
        Triple(todayWater, allWater, dailyGoal)
    }

    val homeScreenState = combine(
        pushupDataFlow,
        waterDataFlow,
        settingsManager.currentStreakFlow,
        settingsManager.lastWorkoutSummaryFlow
    ) { pushupData, waterData, workoutStreak, lastWorkoutSummary ->
        val (todayPushups, allPushups, dailyPushupGoal) = pushupData
        val (todayWater, allWater, dailyWaterGoal) = waterData

        val pushupStreak = calculateCurrentStreak(allPushups.map { it.date }.toSet())
        val waterStreak = calculateCurrentStreak(allWater.map { it.date }.toSet())

        val pushupStreakData = Streak(
            count = pushupStreak,
            isCompletedToday = (todayPushups?.value ?: 0.0) >= dailyPushupGoal,
            type = Streak.Type.PUSHUP
        )

        val waterStreakData = Streak(
            count = waterStreak,
            isCompletedToday = (todayWater?.value ?: 0.0) >= dailyWaterGoal,
            type = Streak.Type.WATER
        )

        val wasWorkoutCompletedToday = if (lastWorkoutSummary != null) {
            val lastWorkoutDate = Instant.ofEpochMilli(lastWorkoutSummary.timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            lastWorkoutDate == LocalDate.now()
        } else {
            false
        }

        val workoutStreakData = Streak(
            count = workoutStreak,
            isCompletedToday = wasWorkoutCompletedToday,
            type = Streak.Type.WORKOUT
        )

        HomeScreenState(
            streaks = listOf(pushupStreakData, waterStreakData, workoutStreakData)
        )

    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeScreenState())

    fun startAutoPushupCounting(onCountUpdate: (Int) -> Unit) {
        pushupSensorManager.startListening(onCountUpdate)
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
            val daysBetween = ChronoUnit.DAYS.between(lastWorkoutDate, LocalDate.now())

            if (daysBetween > 1) {
                settingsManager.saveCurrentStreak(0)
            }
        } else {
            settingsManager.saveCurrentStreak(0)
        }
    }

    fun getTodayRecord(activityId: String): Flow<ActivityRecord?> {
        return when (activityId) {
            "pushups" -> pushupRepo.getRecordForDate(today)
            "water" -> waterRepo.getRecordForDate(today)
            else -> flowOf(null)
        }
    }

    fun getYesterdayRecord(activityId: String): Flow<ActivityRecord?> {
        return when (activityId) {
            "pushups" -> pushupRepo.getRecordForDate(yesterday)
            "water" -> waterRepo.getRecordForDate(yesterday)
            else -> flowOf(null)
        }
    }

    fun getTotal(activityId: String): Flow<Double> {
        return when (activityId) {
            "pushups" -> pushupRepo.getAllPushupRecords().map { records -> records.sumOf { it.value } }
            "water" -> waterRepo.getAllRecords().map { records -> records.sumOf { it.value } }
            else -> flowOf(0.0)
        }
    }

    fun getDailyGoal(activityId: String): Flow<Int> {
        return when (activityId) {
            "pushups" -> settingsManager.dailyGoalFlow
            "water" -> settingsManager.dailyWaterGoalFlow
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
            }
        }
    }

    private fun calculateCurrentStreak(dates: Set<String>): Int {
        if (dates.isEmpty()) return 0
        var streak = 0
        var currentDate = LocalDate.now()

        if (!dates.contains(currentDate.toString())) {
            currentDate = currentDate.minusDays(1)
            if (!dates.contains(currentDate.toString())) {
                return 0
            }
        }

        while (dates.contains(currentDate.toString())) {
            streak++
            currentDate = currentDate.minusDays(1)
        }
        return streak
    }
}

data class HomeScreenState(
    val streaks: List<Streak> = emptyList()
)
