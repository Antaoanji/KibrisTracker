package com.example.pushuptracker.gamification

import com.example.pushuptracker.SettingsManager
import com.example.pushuptracker.data.repo.PushupRepo
import com.example.pushuptracker.data.repo.WaterRepo
import com.example.pushuptracker.model.ActivityRecord
import com.example.pushuptracker.model.Badge
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import java.time.LocalTime
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GamificationManager @Inject constructor(
    private val settingsManager: SettingsManager,
    private val pushupRepo: PushupRepo,
    private val waterRepo: WaterRepo
) {

    private val _newBadgeUnlocked = MutableSharedFlow<Badge>(replay = 0)
    val newBadgeUnlocked = _newBadgeUnlocked.asSharedFlow()

    suspend fun checkAndUnlockAchievements() {
        val unlockedBadges = settingsManager.unlockedBadgesFlow.first()
        val allPushups = pushupRepo.getAllPushupRecords().first()
        val allWater = waterRepo.getAllRecords().first()
        val currentStreak = settingsManager.currentStreakFlow.first()
        val lastWorkout = settingsManager.lastWorkoutSummaryFlow.first()

        // 1. STREAK CHECK
        checkStreak(currentStreak, unlockedBadges)

        // 2. PUSHUP VOLUME CHECK
        checkPushupVolume(allPushups, unlockedBadges)

        // 3. WATER CHECK
        checkWaterGoals(allWater, unlockedBadges)

        // 4. DISCIPLINE CHECK (CALI & MACHINE)
        checkDiscipline(allPushups, lastWorkout?.title ?: "", unlockedBadges)

        // 5. SPECIAL CONDITIONS (TIME, etc.)
        checkSpecialConditions(unlockedBadges)
        
        // 6. META ACHIEVEMENTS (Badge counts)
        checkMetaAchievements(unlockedBadges.size, unlockedBadges)
    }

    private suspend fun unlock(badgeId: String, unlocked: Set<String>) {
        if (!unlocked.contains(badgeId)) {
            settingsManager.unlockBadge(badgeId)
            // Bul ve UI'a fırlat
            Badge.allBadges.find { it.id == badgeId }?.let {
                _newBadgeUnlocked.emit(it)
            }
        }
    }

    private suspend fun checkStreak(streak: Int, unlocked: Set<String>) {
        val streakTiers = mapOf(
            3 to "streak_3", 7 to "streak_7", 14 to "streak_14", 30 to "streak_30",
            60 to "streak_60", 90 to "streak_90", 180 to "streak_180", 365 to "streak_365"
        )
        streakTiers.forEach { (days, id) ->
            if (streak >= days) unlock(id, unlocked)
        }
    }

    private suspend fun checkPushupVolume(records: List<ActivityRecord>, unlocked: Set<String>) {
        val totalPushups = records.sumOf { it.value }
        val volumeTiers = mapOf(
            100.0 to "total_100", 500.0 to "total_500", 1000.0 to "total_1000",
            5000.0 to "total_5000", 10000.0 to "total_10000", 25000.0 to "total_25000", 50000.0 to "total_50000"
        )
        volumeTiers.forEach { (count, id) ->
            if (totalPushups >= count) unlock(id, unlocked)
        }

        val maxSingleSet = records.maxByOrNull { it.value }?.value ?: 0.0
        if (maxSingleSet >= 50) unlock("single_50", unlocked)
        if (maxSingleSet >= 100) unlock("single_100", unlocked)
    }

    private suspend fun checkWaterGoals(records: List<ActivityRecord>, unlocked: Set<String>) {
        val waterGoalMetCount = records.size
        if (waterGoalMetCount >= 1) unlock("water_1", unlocked)
        if (waterGoalMetCount >= 7) unlock("water_7", unlocked)
        if (waterGoalMetCount >= 30) unlock("water_30", unlocked)
        if (waterGoalMetCount >= 100) unlock("water_100", unlocked)
    }

    private suspend fun checkDiscipline(records: List<ActivityRecord>, lastTitle: String, unlocked: Set<String>) {
        if (lastTitle.contains("Calisthenics", ignoreCase = true)) unlock("cali_1", unlocked)
        if (lastTitle.contains("Makine", ignoreCase = true)) unlock("weight_1", unlocked)

        val totalDips = records.filter { it.type.contains("Dips", ignoreCase = true) }.sumOf { it.value }
        if (totalDips >= 500) unlock("cali_dips", unlocked)

        val totalPullups = records.filter { it.type.contains("Pull-Up", ignoreCase = true) || it.type.contains("Barfiks", ignoreCase = true) }.sumOf { it.value }
        if (totalPullups >= 250) unlock("cali_pullup", unlocked)
    }

    private suspend fun checkSpecialConditions(unlocked: Set<String>) {
        val now = LocalTime.now()
        if (now.isBefore(LocalTime.of(7, 0))) unlock("early_bird", unlocked)
        if (now.isAfter(LocalTime.of(23, 0))) unlock("night_owl", unlocked)

        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) unlock("weekend_warrior", unlocked)
    }

    private suspend fun checkMetaAchievements(count: Int, unlocked: Set<String>) {
        if (count >= 10) unlock("level_up", unlocked)
        if (count >= 25) unlock("badge_hunter", unlocked)
        if (count >= 50) unlock("legend", unlocked)
    }
}
