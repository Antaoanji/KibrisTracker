package com.example.pushuptracker.gamification

import com.example.pushuptracker.SettingsManager
import com.example.pushuptracker.model.Activities
import com.example.pushuptracker.room.AppDao
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class GamificationManager(
    private val dao: AppDao,
    private val settingsManager: SettingsManager
) {

    suspend fun updateStreakAndCheckBadges() {
        val dailyGoal = settingsManager.dailyGoalFlow.first()

        // Directly fetch the dates when the push-up goal was met.
        val goalMetDateStrings = dao.getGoalMetDates(Activities.PUSHUPS.id, dailyGoal.toDouble()).first()
        val goalMetDates = goalMetDateStrings.map { LocalDate.parse(it) }.toSet()

        var streak = 0
        var checkDate = LocalDate.now()

        // If today's goal is not met, the streak can only be counted up to yesterday.
        if (!goalMetDates.contains(checkDate)) {
            checkDate = checkDate.minusDays(1)
        }

        // Now, count backwards from checkDate
        while (goalMetDates.contains(checkDate)) {
            streak++
            checkDate = checkDate.minusDays(1)
        }

        settingsManager.saveCurrentStreak(streak)

        // Check for Badges
        if (streak >= 7) {
            settingsManager.unlockBadge("streak_7_day")
        }
        // TODO: Add more badge checks here (e.g., for 30 days, 100 days)
    }
}
