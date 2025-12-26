package com.example.pushuptracker

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.example.pushuptracker.model.WorkoutSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsManager @Inject constructor(private val dataStore: DataStore<Preferences>) {

    // --- User Profile ---
    private val ageKey = intPreferencesKey("user_age")
    val ageFlow: Flow<Int> = dataStore.data.map { it[ageKey] ?: 30 }
    suspend fun saveAge(age: Int) = dataStore.edit { it[ageKey] = age }

    private val weightKey = intPreferencesKey("user_weight")
    val weightFlow: Flow<Int> = dataStore.data.map { it[weightKey] ?: 70 }
    suspend fun saveWeight(weight: Int) = dataStore.edit { it[weightKey] = weight }

    private val genderKey = stringPreferencesKey("user_gender")
    val genderFlow: Flow<String> = dataStore.data.map { it[genderKey] ?: "Erkek" }
    suspend fun saveGender(gender: String) = dataStore.edit { it[genderKey] = gender }

    private val goalKey = stringPreferencesKey("user_goal")
    val goalFlow: Flow<String> = dataStore.data.map { it[goalKey] ?: "Direnç Kazanma" }
    suspend fun saveGoal(goal: String) = dataStore.edit { it[goalKey] = goal }

    private val workoutFrequencyKey = stringPreferencesKey("workout_frequency")
    val workoutFrequencyFlow: Flow<String> = dataStore.data.map { it[workoutFrequencyKey] ?: "Orta Seviye" }
    suspend fun saveWorkoutFrequency(frequency: String) = dataStore.edit { it[workoutFrequencyKey] = frequency }

    // --- Last Completed Workout Summary ---
    private val lastWorkoutSummaryTitleKey = stringPreferencesKey("last_workout_summary_title")
    private val lastWorkoutSummaryTimeKey = intPreferencesKey("last_workout_summary_time")
    private val lastWorkoutSummaryCaloriesKey = intPreferencesKey("last_workout_summary_calories")
    private val lastWorkoutSummaryTimestampKey = longPreferencesKey("last_workout_summary_timestamp")

    val lastWorkoutSummaryFlow: Flow<WorkoutSummary?> = dataStore.data.map {
        val title = it[lastWorkoutSummaryTitleKey]
        val time = it[lastWorkoutSummaryTimeKey]
        val calories = it[lastWorkoutSummaryCaloriesKey]
        val timestamp = it[lastWorkoutSummaryTimestampKey]
        if (title != null && time != null && calories != null && timestamp != null) {
            WorkoutSummary(title, time, calories, timestamp)
        } else {
            null
        }
    }

    suspend fun saveLastWorkoutSummary(summary: WorkoutSummary) {
        dataStore.edit {
            it[lastWorkoutSummaryTitleKey] = summary.title
            it[lastWorkoutSummaryTimeKey] = summary.totalTimeMinutes
            it[lastWorkoutSummaryCaloriesKey] = summary.caloriesBurned
            it[lastWorkoutSummaryTimestampKey] = summary.timestamp
        }
    }

    // --- Goals ---
    private val dailyGoalKey = intPreferencesKey("daily_goal")
    val dailyGoalFlow: Flow<Int> = dataStore.data.map { it[dailyGoalKey] ?: 50 }

    suspend fun saveDailyGoal(goal: Int) {
        dataStore.edit { it[dailyGoalKey] = goal }
    }

    private val dailyWaterGoalKey = intPreferencesKey("daily_water_goal")
    val dailyWaterGoalFlow: Flow<Int> = dataStore.data.map { it[dailyWaterGoalKey] ?: 2000 }

    suspend fun saveDailyWaterGoal(goal: Int) {
        dataStore.edit { it[dailyWaterGoalKey] = goal }
    }

    // --- Push-up Reminder ---
    private val pushupReminderEnabledKey = booleanPreferencesKey("pushup_reminder_enabled")
    val pushupReminderEnabledFlow: Flow<Boolean> = dataStore.data.map { it[pushupReminderEnabledKey] ?: false }

    private val pushupReminderTimeKey = stringPreferencesKey("pushup_reminder_time")
    val pushupReminderTimeFlow: Flow<String> = dataStore.data.map { it[pushupReminderTimeKey] ?: "18:00" } // Default 18:00

    suspend fun savePushupReminder(enabled: Boolean, time: String) {
        dataStore.edit {
            it[pushupReminderEnabledKey] = enabled
            it[pushupReminderTimeKey] = time
        }
    }

    // --- Water Reminder ---
    private val waterReminderEnabledKey = booleanPreferencesKey("water_reminder_enabled")
    val waterReminderEnabledFlow: Flow<Boolean> = dataStore.data.map { it[waterReminderEnabledKey] ?: false }

    private val waterReminderFrequencyKey = intPreferencesKey("water_reminder_frequency_minutes")
    val waterReminderFrequencyFlow: Flow<Int> = dataStore.data.map { it[waterReminderFrequencyKey] ?: 120 } // Default 120 minutes (2 hours)

    suspend fun saveWaterReminder(enabled: Boolean, frequencyMinutes: Int) {
        dataStore.edit {
            it[waterReminderEnabledKey] = enabled
            it[waterReminderFrequencyKey] = frequencyMinutes
        }
    }

    // --- Gamification ---
    private val currentStreakKey = intPreferencesKey("current_streak")
    val currentStreakFlow: Flow<Int> = dataStore.data.map { it[currentStreakKey] ?: 0 }

    suspend fun saveCurrentStreak(streak: Int) {
        dataStore.edit { it[currentStreakKey] = streak }
    }

    suspend fun incrementCurrentStreak() {
        dataStore.edit { preferences ->
            val currentStreak = preferences[currentStreakKey] ?: 0
            preferences[currentStreakKey] = currentStreak + 1
        }
    }

    private val unlockedBadgesKey = stringSetPreferencesKey("unlocked_badges")
    val unlockedBadgesFlow: Flow<Set<String>> = dataStore.data.map { it[unlockedBadgesKey] ?: emptySet() }

    suspend fun unlockBadge(badgeId: String) {
        dataStore.edit {
            val currentBadges = it[unlockedBadgesKey] ?: emptySet()
            it[unlockedBadgesKey] = currentBadges + badgeId
        }
    }
    suspend fun clearAllData() {
        dataStore.edit { it.clear() }
    }
}
