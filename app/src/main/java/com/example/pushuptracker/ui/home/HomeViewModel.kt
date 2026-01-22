package com.example.pushuptracker.ui.home

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pushuptracker.SettingsManager
import com.example.pushuptracker.UpdateInfo
import com.example.pushuptracker.UpdateManager
import com.example.pushuptracker.ai.PushupSensorManager
import com.example.pushuptracker.audio.WorkoutService
import com.example.pushuptracker.data.repo.PushupRepo
import com.example.pushuptracker.data.repo.WaterRepo
import com.example.pushuptracker.model.ActivityRecord
import com.example.pushuptracker.model.Streak
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val updateManager: UpdateManager
) : AndroidViewModel(application) {

    private val today: String get() = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    private val yesterday: String get() = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)

    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val updateInfo = _updateInfo.asStateFlow()

    init {
        viewModelScope.launch {
            checkAndResetStreaks()
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

    private val walkingDataFlow = combine(
        pushupRepo.getRecordByDateAndType(today, "walking"),
        pushupRepo.getRecordsByType("walking")
    ) { todayWalking, allWalking ->
        Pair(todayWalking, allWalking)
    }

    val homeScreenState = combine(
        pushupDataFlow,
        waterDataFlow,
        walkingDataFlow,
        settingsManager.currentStreakFlow,
        settingsManager.lastWorkoutSummaryFlow
    ) { pushupData, waterData, walkingData, workoutStreak, lastWorkoutSummary ->
        val (todayPushups, allPushups, dailyPushupGoal) = pushupData
        val (todayWater, allWater, dailyWaterGoal) = waterData
        val (todayWalking, allWalking) = walkingData

        val achievedPushupDates = allPushups.filter { it.value >= dailyPushupGoal }.map { it.date }.toSet()
        val achievedWaterDates = allWater.filter { it.value >= dailyWaterGoal }.map { it.date }.toSet()
        val achievedWalkingDates = allWalking.filter { it.value >= 33.0 }.map { it.date }.toSet()

        val pushupStreakData = Streak(
            count = calculateCurrentStreak(achievedPushupDates, isDaily = true),
            isCompletedToday = (todayPushups?.value ?: 0.0) >= dailyPushupGoal,
            type = Streak.Type.PUSHUP
        )

        val waterStreakData = Streak(
            count = calculateCurrentStreak(achievedWaterDates, isDaily = true),
            isCompletedToday = (todayWater?.value ?: 0.0) >= dailyWaterGoal,
            type = Streak.Type.WATER
        )

        val walkingStreakData = Streak(
            count = calculateCurrentStreak(achievedWalkingDates, isDaily = true),
            isCompletedToday = (todayWalking?.value ?: 0.0) >= 33.0,
            type = Streak.Type.WALKING
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
            streaks = listOf(pushupStreakData, waterStreakData, walkingStreakData, workoutStreakData)
        )

    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeScreenState())

    fun startWalkingWorkout() {
        val intent = Intent(getApplication(), WorkoutService::class.java).apply {
            action = "START_WALKING"
            putExtra("is_walking", true)
            putExtra("label", "Japon Yürüyüşü")
        }
        getApplication<Application>().startForegroundService(intent)
    }

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
            else -> flowOf(null)
        }
    }

    fun getYesterdayRecord(activityId: String): Flow<ActivityRecord?> {
        return when (activityId) {
            "pushups" -> pushupRepo.getRecordForDate(yesterday)
            "water" -> waterRepo.getRecordForDate(yesterday)
            "walking" -> pushupRepo.getRecordByDateAndType(yesterday, "walking")
            else -> flowOf(null)
        }
    }

    fun getTotal(activityId: String): Flow<Double> {
        return when (activityId) {
            "pushups" -> pushupRepo.getAllPushupRecords().map { records -> records.sumOf { it.value } }
            "water" -> waterRepo.getAllRecords().map { records -> records.sumOf { it.value } }
            "walking" -> pushupRepo.getRecordsByType("walking").map { list -> list.sumOf { it.value } }
            else -> flowOf(0.0)
        }
    }

    fun getDailyGoal(activityId: String): Flow<Int> {
        return when (activityId) {
            "pushups" -> settingsManager.dailyGoalFlow
            "water" -> settingsManager.dailyWaterGoalFlow
            "walking" -> flowOf(33)
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

    private fun calculateCurrentStreak(dates: Set<String>, isDaily: Boolean): Int {
        if (dates.isEmpty()) return 0
        var streak = 0
        var currentDate = LocalDate.now()
        if (!dates.contains(currentDate.toString())) {
            currentDate = currentDate.minusDays(1)
            if (!dates.contains(currentDate.toString())) {
                if (!isDaily) {
                    val yesterdayDay = currentDate.dayOfWeek
                    if (yesterdayDay == DayOfWeek.THURSDAY || yesterdayDay == DayOfWeek.SUNDAY) {
                        currentDate = currentDate.minusDays(1)
                    }
                }
                if (!dates.contains(currentDate.toString())) return 0
            }
        }
        while (dates.contains(currentDate.toString()) || (!isDaily && isRestDay(currentDate))) {
            if (dates.contains(currentDate.toString())) {
                streak++
            }
            currentDate = currentDate.minusDays(1)
        }
        return streak
    }

    private fun isRestDay(date: LocalDate): Boolean {
        val day = date.dayOfWeek
        return day == DayOfWeek.THURSDAY || day == DayOfWeek.SUNDAY
    }
}

data class HomeScreenState(
    val streaks: List<Streak> = emptyList()
)
