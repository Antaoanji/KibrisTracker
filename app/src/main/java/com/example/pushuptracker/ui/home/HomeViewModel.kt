package com.example.pushuptracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pushuptracker.R
import com.example.pushuptracker.SettingsManager
import com.example.pushuptracker.data.repo.PushupRepo
import com.example.pushuptracker.data.repo.WaterRepo
import com.example.pushuptracker.model.ActivityRecord
import com.example.pushuptracker.model.Streak
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val today: String get() = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    private val yesterday: String get() = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)

    init {
        viewModelScope.launch {
            checkAndResetStreaks()
        }
    }

    private val pushupDataFlow = combine(
        pushupRepo.getRecordForDate(today),
        pushupRepo.getAllRecords(),
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
        settingsManager.lastWorkoutSummaryFlow // Added to check workout completion date
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
            isCompletedToday = wasWorkoutCompletedToday, // Fixed!
            type = Streak.Type.WORKOUT
        )

        HomeScreenState(
            streaks = listOf(pushupStreakData, waterStreakData, workoutStreakData)
        )

    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeScreenState())

    private suspend fun checkAndResetStreaks() {
        val lastWorkoutSummary = settingsManager.lastWorkoutSummaryFlow.first()
        if (lastWorkoutSummary != null) {
            val lastWorkoutDate = Instant.ofEpochMilli(lastWorkoutSummary.timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            val daysBetween = ChronoUnit.DAYS.between(lastWorkoutDate, LocalDate.now())
            // Reset if the last workout was not yesterday or today
            if (daysBetween > 1) {
                settingsManager.saveCurrentStreak(0)
            }
        } else {
            // If there's no workout history, streak must be 0.
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
            "pushups" -> pushupRepo.getAllRecords().map { records -> records.sumOf { it.value } }
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
                    val current = getTodayRecord(activityId).first()?.value ?: 0.0
                    val newValue = current + value
                    pushupRepo.addPushups(newValue.toInt())

                    // Check if goal was just reached
                    onGoalReached(current < goal && newValue >= goal)
                }
                "water" -> {
                    val goal = settingsManager.dailyWaterGoalFlow.first()
                    val current = getTodayRecord(activityId).first()?.value ?: 0.0
                    val newValue = current + value
                    waterRepo.addWaterIntake(newValue.toInt())

                    // Check if goal was just reached
                    onGoalReached(current < goal && newValue >= goal)
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
                return 0 // No streak if today or yesterday is missed
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
