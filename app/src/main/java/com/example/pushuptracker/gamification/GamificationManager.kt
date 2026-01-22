package com.example.pushuptracker.gamification

import com.example.pushuptracker.SettingsManager
import com.example.pushuptracker.data.HealthConnectManager
import com.example.pushuptracker.data.repo.PushupRepo
import com.example.pushuptracker.data.repo.WaterRepo
import com.example.pushuptracker.model.ActivityRecord
import com.example.pushuptracker.model.Badge
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GamificationManager @Inject constructor(
    private val settingsManager: SettingsManager,
    private val pushupRepo: PushupRepo,
    private val waterRepo: WaterRepo,
    private val healthConnectManager: HealthConnectManager
) {

    private val _newBadgeUnlocked = MutableSharedFlow<Badge>(replay = 0)
    val newBadgeUnlocked = _newBadgeUnlocked.asSharedFlow()

    suspend fun checkAndUnlockAchievements() {
        val unlocked = settingsManager.unlockedBadgesFlow.first()
        val allRecords = pushupRepo.getAllRecords().first()
        val allWater = waterRepo.getAllRecords().first()
        val streak = settingsManager.currentStreakFlow.first()
        
        // HEALTH CONNECT'TEN GERÇEK KALORİYİ ÇEK (Son 30 gün verisi)
        val healthCalories = if (healthConnectManager.hasAllPermissions()) {
            healthConnectManager.readTotalCalories(
                start = Instant.now().minusSeconds(30 * 24 * 60 * 60),
                end = Instant.now()
            )
        } else 0.0

        // 1. STREAKS
        val streakTiers = mapOf(3 to "streak_3", 7 to "streak_7", 14 to "streak_14", 30 to "streak_30", 60 to "streak_60", 90 to "streak_90", 180 to "streak_180", 365 to "streak_365")
        streakTiers.forEach { (d, id) -> if (streak >= d) unlock(id, unlocked) }

        // 2. VOLUME (PUSHUPS)
        val totalPushups = allRecords.filter { it.type == "pushup" }.sumOf { it.value }
        val volTiers = mapOf(100.0 to "total_100", 500.0 to "total_500", 1000.0 to "total_1000", 5000.0 to "total_5000", 10000.0 to "total_10000", 25000.0 to "total_25000", 50000.0 to "total_50000")
        volTiers.forEach { (c, id) -> if (totalPushups >= c) unlock(id, unlocked) }

        // 3. TONAGE (WEIGHTS)
        val totalTonage = allRecords.sumOf { (it.weightUsed ?: 0.0) * it.value }
        if (totalTonage >= 1000) unlock("ton_1", unlocked)
        if (totalTonage >= 10000) unlock("ton_10", unlocked)
        if (totalTonage >= 50000) unlock("ton_50", unlocked)
        if (totalTonage >= 100000) unlock("ton_100", unlocked)

        // 4. CALORIES (XIAOMI BAND VERİSİ)
        if (healthCalories >= 1000) unlock("cal_1000", unlocked)
        if (healthCalories >= 10000) unlock("cal_10000", unlocked)
        if (healthCalories >= 50000) unlock("cal_50000", unlocked)
        if (healthCalories >= 100000) unlock("cal_100000", unlocked)

        // 5. EXPLORATION
        val uniqueEx = allRecords.map { it.type }.distinct().size
        if (uniqueEx >= 5) unlock("explore_5", unlocked)
        if (uniqueEx >= 15) unlock("explore_15", unlocked)
        if (uniqueEx >= 30) unlock("explore_30", unlocked)

        // 6. MASTERY
        val totalDips = allRecords.filter { it.type.contains("Dips", true) }.sumOf { it.value }
        if (totalDips >= 500) unlock("master_dips", unlocked)
        val totalPull = allRecords.filter { it.type.contains("Pull-Up", true) || it.type.contains("Barfiks", true) }.sumOf { it.value }
        if (totalPull >= 250) unlock("master_pullup", unlocked)
        val totalSquat = allRecords.filter { it.type.contains("Squat", true) }.sumOf { it.value }
        if (totalSquat >= 2000) unlock("master_squat", unlocked)

        // 7. TIME & SPECIAL
        val now = LocalTime.now()
        if (now.isBefore(LocalTime.of(7, 0))) unlock("early_bird", unlocked)
        if (now.isAfter(LocalTime.of(23, 0))) unlock("night_owl", unlocked)
        
        // 8. WATER & DISCIPLINE
        val uniqueWater = allWater.map { it.date }.distinct().size
        if (uniqueWater >= 30) unlock("water_30", unlocked)
        
        // 9. META
        val count = unlocked.size
        if (count >= 10) unlock("level_bronze", unlocked)
        if (count >= 30) unlock("level_silver", unlocked)
        if (count >= 60) unlock("level_gold", unlocked)
        if (count >= 90) unlock("level_platinum", unlocked)
        if (count >= 100) unlock("legend", unlocked)
    }

    private suspend fun unlock(id: String, unlocked: Set<String>) {
        if (!unlocked.contains(id)) {
            settingsManager.unlockBadge(badgeId = id)
            Badge.allBadges.find { it.id == id }?.let { _newBadgeUnlocked.emit(it) }
        }
    }
}
