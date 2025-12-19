package com.example.pushuptracker

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {

    // --- Goals ---
    private val dailyGoalKey = intPreferencesKey("daily_goal")
    val dailyGoalFlow: Flow<Int> = context.dataStore.data
        .map { it[dailyGoalKey] ?: 50 }

    suspend fun saveDailyGoal(goal: Int) {
        context.dataStore.edit { it[dailyGoalKey] = goal }
    }

    private val dailyWaterGoalKey = intPreferencesKey("daily_water_goal")
    val dailyWaterGoalFlow: Flow<Int> = context.dataStore.data
        .map { it[dailyWaterGoalKey] ?: 2000 }

    suspend fun saveDailyWaterGoal(goal: Int) {
        context.dataStore.edit { it[dailyWaterGoalKey] = goal }
    }

    // --- Push-up Reminder ---
    private val pushupReminderEnabledKey = booleanPreferencesKey("pushup_reminder_enabled")
    val pushupReminderEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { it[pushupReminderEnabledKey] ?: false }

    private val pushupReminderTimeKey = stringPreferencesKey("pushup_reminder_time")
    val pushupReminderTimeFlow: Flow<String> = context.dataStore.data
        .map { it[pushupReminderTimeKey] ?: "18:00" } // Default 18:00

    suspend fun savePushupReminder(enabled: Boolean, time: String) {
        context.dataStore.edit {
            it[pushupReminderEnabledKey] = enabled
            it[pushupReminderTimeKey] = time
        }
    }

    // --- Water Reminder ---
    private val waterReminderEnabledKey = booleanPreferencesKey("water_reminder_enabled")
    val waterReminderEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { it[waterReminderEnabledKey] ?: false }

    private val waterReminderFrequencyKey = intPreferencesKey("water_reminder_frequency_minutes")
    val waterReminderFrequencyFlow: Flow<Int> = context.dataStore.data
        .map { it[waterReminderFrequencyKey] ?: 120 } // Default 120 minutes (2 hours)

    suspend fun saveWaterReminder(enabled: Boolean, frequencyMinutes: Int) {
        context.dataStore.edit {
            it[waterReminderEnabledKey] = enabled
            it[waterReminderFrequencyKey] = frequencyMinutes
        }
    }

    // --- Gamification ---
    private val currentStreakKey = intPreferencesKey("current_streak")
    val currentStreakFlow: Flow<Int> = context.dataStore.data
        .map { it[currentStreakKey] ?: 0 }

    suspend fun saveCurrentStreak(streak: Int) {
        context.dataStore.edit { it[currentStreakKey] = streak }
    }

    private val unlockedBadgesKey = stringSetPreferencesKey("unlocked_badges")
    val unlockedBadgesFlow: Flow<Set<String>> = context.dataStore.data
        .map { it[unlockedBadgesKey] ?: emptySet() }

    suspend fun unlockBadge(badgeId: String) {
        context.dataStore.edit {
            val currentBadges = it[unlockedBadgesKey] ?: emptySet()
            it[unlockedBadgesKey] = currentBadges + badgeId
        }
    }
}
