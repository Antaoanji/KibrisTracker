package com.example.pushuptracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pushuptracker.SettingsManager
import com.example.pushuptracker.data.repo.PushupRepo
import com.example.pushuptracker.data.repo.WaterRepo
import com.example.pushuptracker.model.ActivityRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val pushupRepo: PushupRepo,
    private val waterRepo: WaterRepo,
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val today: String get() = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    private val yesterday: String get() = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)

    val currentStreak: Flow<Int> = pushupRepo.getAllRecords().map {
        calculateCurrentStreak(it.map { record -> record.date }.toSet())
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
            "pushups" -> pushupRepo.getAllRecords().map { it.sumOf { r -> r.value } }
            "water" -> waterRepo.getAllRecords().map { it.sumOf { r -> r.value } }
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
                    onGoalReached(newValue >= goal)
                }
                "water" -> {
                    val goal = settingsManager.dailyWaterGoalFlow.first()
                    val current = getTodayRecord(activityId).first()?.value ?: 0.0
                    val newValue = current + value
                    waterRepo.addWaterIntake(newValue.toInt())
                    onGoalReached(newValue >= goal)
                }
            }
        }
    }

    private fun calculateCurrentStreak(dates: Set<String>): Int {
        if (dates.isEmpty()) return 0
        var streak = 0
        var currentDate = LocalDate.now()

        if (dates.contains(currentDate.toString())) {
            streak++
            currentDate = currentDate.minusDays(1)
        } else if (dates.contains(currentDate.minusDays(1).toString())) {
            currentDate = currentDate.minusDays(1)
            streak++
        } else {
            return 0 // No streak if today or yesterday is missed
        }

        while (dates.contains(currentDate.toString())) {
            streak++
            currentDate = currentDate.minusDays(1)
        }
        return streak
    }
}
