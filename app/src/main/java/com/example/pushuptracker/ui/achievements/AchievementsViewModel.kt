package com.example.pushuptracker.ui.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pushuptracker.SettingsManager
import com.example.pushuptracker.data.HealthConnectManager
import com.example.pushuptracker.data.repo.PushupRepo
import com.example.pushuptracker.model.ActivityRecord
import com.example.pushuptracker.model.Badge
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

data class AchievementsUiState(
    val badges: List<Badge> = emptyList()
)

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val settingsManager: SettingsManager,
    private val pushupRepo: PushupRepo,
    private val healthConnectManager: HealthConnectManager
) : ViewModel() {

    private val _totalHealthCalories = MutableStateFlow(0.0)

    init {
        fetchHealthData()
    }

    private fun fetchHealthData() {
        viewModelScope.launch {
            if (healthConnectManager.hasAllPermissions()) {
                val calories = healthConnectManager.readTotalCalories(
                    start = Instant.now().minusSeconds(30 * 24 * 60 * 60),
                    end = Instant.now()
                )
                _totalHealthCalories.value = calories
            }
        }
    }

    val uiState = combine(
        settingsManager.unlockedBadgesFlow,
        pushupRepo.getAllRecords(),
        settingsManager.currentStreakFlow,
        _totalHealthCalories
    ) { unlockedBadges, allRecords, currentStreak, healthCalories ->

        val totalPushups = allRecords.filter { it.type == "pushup" }.sumOf { it.value }
        val maxPushupSet = allRecords.filter { it.type == "pushup" }.maxOfOrNull { it.value } ?: 0.0
        val walkingRecords = allRecords.filter { it.type == "walking" }
        val totalWalkMin = walkingRecords.sumOf { it.value }
        val waterDays = allRecords.filter { it.type == "water" }.map { it.date }.distinct().size
        val workouts = allRecords.filter { it.type == "workout_complete" }.size

        val totalTonage = allRecords.sumOf { (it.weightUsed ?: 0.0) * it.value }
        val maxWorkoutTonage = allRecords.groupBy { it.date }.map { entry ->
            entry.value.sumOf { (it.weightUsed ?: 0.0) * it.value }
        }.maxOfOrNull { it } ?: 0.0

        val actualCalories = healthCalories 

        val uniqueExercises = allRecords.map { it.type }.distinct().size
        val musclesCovered = checkMuscleGroups(allRecords.map { it.type }.toSet())

        val totalDips = allRecords.filter { it.type.contains("Dips", ignoreCase = true) }.sumOf { it.value }
        val totalPullups = allRecords.filter { it.type.contains("Pull-Up", ignoreCase = true) || it.type.contains("Barfiks", ignoreCase = true) }.sumOf { it.value }
        val totalSquats = allRecords.filter { it.type.contains("Squat", ignoreCase = true) }.sumOf { it.value }
        val totalPlankSec = allRecords.filter { it.type.contains("Plank", ignoreCase = true) }.sumOf { it.value }
        val maxPushupsPerDay = allRecords.filter { it.type == "pushup" }.groupBy { it.date }.map { it.value.sumOf { r -> r.value } }.maxOfOrNull { it } ?: 0.0

        val updatedBadges = Badge.allBadges.map { badge ->
            val isUnlocked = unlockedBadges.contains(badge.id)
            val progress = if (isUnlocked) 1f else {
                calculateProgress(
                    id = badge.id, 
                    streak = currentStreak, 
                    pushups = totalPushups, 
                    maxPushup = maxPushupSet, 
                    walkMin = totalWalkMin, 
                    walkSessions = walkingRecords.size,
                    waterDays = waterDays, 
                    workouts = workouts,
                    tonage = totalTonage,
                    maxWorkoutTonage = maxWorkoutTonage,
                    calories = actualCalories,
                    uniqueExercises = uniqueExercises,
                    muscles = musclesCovered,
                    dips = totalDips,
                    pullups = totalPullups,
                    squats = totalSquats,
                    plankMin = totalPlankSec / 60.0,
                    maxPushDay = maxPushupsPerDay,
                    allRecords = allRecords
                )
            }
            badge.copy(progress = progress.coerceIn(0f, 1f))
        }

        AchievementsUiState(badges = updatedBadges)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AchievementsUiState())

    private fun calculateProgress(
        id: String, streak: Int, pushups: Double, maxPushup: Double, walkMin: Double, walkSessions: Int,
        waterDays: Int, workouts: Int, tonage: Double, maxWorkoutTonage: Double, calories: Double,
        uniqueExercises: Int, muscles: Int, dips: Double, pullups: Double, squats: Double,
        plankMin: Double, maxPushDay: Double,
        allRecords: List<ActivityRecord>
    ): Float {
        return when (id) {
            "streak_3" -> streak / 3f; "streak_7" -> streak / 7f; "streak_14" -> streak / 14f; "streak_30" -> streak / 30f
            "streak_60" -> streak / 60f; "streak_90" -> streak / 90f; "streak_180" -> streak / 180f; "streak_365" -> streak / 365f
            "total_100" -> pushups.toFloat() / 100f; "total_500" -> pushups.toFloat() / 500f; "total_1000" -> pushups.toFloat() / 1000f
            "total_5000" -> pushups.toFloat() / 5000f; "total_10000" -> pushups.toFloat() / 10000f; "total_25000" -> pushups.toFloat() / 25000f
            "total_50000" -> pushups.toFloat() / 50000f; "single_50" -> maxPushup.toFloat() / 50f; "single_100" -> maxPushup.toFloat() / 100f
            "walk_1" -> walkSessions.toFloat() / 1f; "walk_10" -> walkSessions.toFloat() / 10f; "walk_50" -> walkSessions.toFloat() / 50f
            "walk_100" -> walkSessions.toFloat() / 100f; "walk_33" -> 0f; "walk_1000" -> walkMin.toFloat() / 1000f
            "walk_5000" -> walkMin.toFloat() / 5000f; "walk_morning" -> 0f; "walk_night" -> 0f
            "ton_1" -> tonage.toFloat() / 1000f; "ton_10" -> tonage.toFloat() / 10000f
            "ton_50" -> tonage.toFloat() / 50000f; "ton_100" -> tonage.toFloat() / 100000f
            "whale" -> maxWorkoutTonage.toFloat() / 5000f
            "cal_1000" -> calories.toFloat() / 1000f; "cal_10000" -> calories.toFloat() / 10000f
            "cal_50000" -> calories.toFloat() / 50000f; "cal_100000" -> calories.toFloat() / 100000f
            "explore_5" -> uniqueExercises / 5f; "explore_15" -> uniqueExercises / 15f; "explore_30" -> uniqueExercises / 30f
            "body_engineer" -> muscles / 5f
            "master_dips" -> dips.toFloat() / 500f; "master_pullup" -> pullups.toFloat() / 250f
            "master_squat" -> squats.toFloat() / 2000f; "master_plank" -> plankMin.toFloat() / 60f
            "master_pushup_200" -> maxPushDay.toFloat() / 200f
            "early_bird" -> if (allRecords.any { isHourInRange(it.timestamp, 4, 7) }) 1f else 0f
            "night_owl" -> if (allRecords.any { isHourInRange(it.timestamp, 23, 3) }) 1f else 0f
            "lunch_warrior" -> if (allRecords.any { isHourInRange(it.timestamp, 12, 14) }) 1f else 0f
            "new_year" -> if (allRecords.any { isNewYear(it.timestamp) }) 1f else 0f
            "water_1" -> waterDays / 1f; "water_7" -> waterDays / 7f; "water_30" -> waterDays / 30f; "water_100" -> waterDays / 100f
            "cali_100" -> workouts / 100f; "weight_100" -> workouts / 100f; "consistency_100" -> workouts / 100f
            "level_bronze" -> 0f; "level_silver" -> 0f; "level_gold" -> 0f; "level_platinum" -> 0f; "legend" -> 0f
            else -> 0f
        }
    }

    private fun checkMuscleGroups(types: Set<String>): Int {
        var count = 0
        if (types.any { it.contains("Chest", true) || it.contains("Push", true) }) count++
        if (types.any { it.contains("Back", true) || it.contains("Pull", true) }) count++
        if (types.any { it.contains("Leg", true) || it.contains("Squat", true) }) count++
        if (types.any { it.contains("Arm", true) || it.contains("Curl", true) }) count++
        if (types.any { it.contains("Core", true) || it.contains("Plank", true) }) count++
        return count
    }

    private fun isHourInRange(ts: Long, start: Int, end: Int): Boolean {
        if (ts <= 0) return false
        val h = Instant.ofEpochMilli(ts).atZone(ZoneId.systemDefault()).hour
        return if (start <= end) h in start..end else h >= start || h <= end
    }

    private fun isNewYear(ts: Long): Boolean {
        if (ts <= 0) return false
        val date = Instant.ofEpochMilli(ts).atZone(ZoneId.systemDefault()).toLocalDate()
        return date.monthValue == 1 && date.dayOfMonth == 1
    }

    fun unlockBadge(badgeId: String) {
        viewModelScope.launch { settingsManager.unlockBadge(badgeId) }
    }
}
