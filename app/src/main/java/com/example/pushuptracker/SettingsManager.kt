package com.example.pushuptracker

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.example.pushuptracker.model.WorkoutSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsManager @Inject constructor(private val dataStore: DataStore<Preferences>) {

    // --- User Profile ---
    private val ageKey = intPreferencesKey("user_age")
    val ageFlow: Flow<Int> = dataStore.data.map { it[ageKey] ?: 30 }
    suspend fun saveAge(age: Int) = dataStore.edit { it[ageKey] = age }

    private val genderKey = stringPreferencesKey("user_gender")
    val genderFlow: Flow<String> = dataStore.data.map { it[genderKey] ?: "Erkek" }
    suspend fun saveGender(gender: String) = dataStore.edit { it[genderKey] = gender }

    private val goalKey = stringPreferencesKey("user_goal")
    val goalFlow: Flow<String> = dataStore.data.map { it[goalKey] ?: "Direnç Kazanma" }
    suspend fun saveGoal(goal: String) = dataStore.edit { it[goalKey] = goal }

    private val workoutFrequencyKey = stringPreferencesKey("workout_frequency")
    val workoutFrequencyFlow: Flow<String> = dataStore.data.map { it[workoutFrequencyKey] ?: "Orta Seviye" }
    suspend fun saveWorkoutFrequency(frequency: String) = dataStore.edit { it[workoutFrequencyKey] = frequency }

    // --- Active Workout Plan ---
    private val activeWorkoutPlanKey = stringPreferencesKey("active_workout_plan")
    val activeWorkoutPlanFlow: Flow<String?> = dataStore.data.map { it[activeWorkoutPlanKey] }

    private val activeWorkoutCurrentDayKey = intPreferencesKey("active_workout_current_day")
    val activeWorkoutCurrentDayFlow: Flow<Int> = dataStore.data.map { it[activeWorkoutCurrentDayKey] ?: 1 }

    // En son seçilen program türü (MACHINE_WEIGHT veya CALISTHENICS_WEIGHT)
    private val lastSelectedProgramTypeKey = stringPreferencesKey("last_selected_program_type")
    val lastSelectedProgramTypeFlow: Flow<String> = dataStore.data.map { it[lastSelectedProgramTypeKey] ?: "MACHINE_WEIGHT" }

    suspend fun saveLastSelectedProgramType(type: String) {
        dataStore.edit { it[lastSelectedProgramTypeKey] = type }
    }

    suspend fun saveActiveWorkout(plan: String, day: Int) {
        dataStore.edit {
            it[activeWorkoutPlanKey] = plan
            it[activeWorkoutCurrentDayKey] = day
        }
    }

    suspend fun saveWorkoutCurrentDay(day: Int) {
        dataStore.edit { it[activeWorkoutCurrentDayKey] = day }
    }

    suspend fun clearActiveWorkout() {
        dataStore.edit {
            it.remove(activeWorkoutPlanKey)
            it.remove(activeWorkoutCurrentDayKey)
        }
    }

    // --- Workout Persistence (NEW) ---
    private val savedWorkoutTitleKey = stringPreferencesKey("saved_workout_title")
    private val savedExerciseIndexKey = intPreferencesKey("saved_exercise_index")
    private val savedSetIndexKey = intPreferencesKey("saved_set_index")

    val savedWorkoutStateFlow: Flow<Triple<String, Int, Int>?> = dataStore.data.map {
        val title = it[savedWorkoutTitleKey]
        val exIdx = it[savedExerciseIndexKey]
        val setIdx = it[savedSetIndexKey]
        if (title != null && exIdx != null && setIdx != null) {
            Triple(title, exIdx, setIdx)
        } else null
    }

    suspend fun saveWorkoutProgress(title: String, exerciseIndex: Int, setIndex: Int) {
        dataStore.edit {
            it[savedWorkoutTitleKey] = title
            it[savedExerciseIndexKey] = exerciseIndex
            it[savedSetIndexKey] = setIndex
        }
    }

    suspend fun clearSavedWorkoutProgress() {
        dataStore.edit {
            it.remove(savedWorkoutTitleKey)
            it.remove(savedExerciseIndexKey)
            it.remove(savedSetIndexKey)
        }
    }

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

    // --- Workout Reminder ---
    private val workoutReminderEnabledKey = booleanPreferencesKey("workout_reminder_enabled")
    val workoutReminderEnabledFlow: Flow<Boolean> = dataStore.data.map { it[workoutReminderEnabledKey] ?: false }

    private val workoutReminderTimeKey = stringPreferencesKey("workout_reminder_time")
    val workoutReminderTimeFlow: Flow<String> = dataStore.data.map { it[workoutReminderTimeKey] ?: "19:00" } // Default 19:00

    suspend fun saveWorkoutReminder(enabled: Boolean, time: String) {
        dataStore.edit {
            it[workoutReminderEnabledKey] = enabled
            it[workoutReminderTimeKey] = time
        }
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
