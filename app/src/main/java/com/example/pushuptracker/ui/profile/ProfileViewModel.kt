package com.example.pushuptracker.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pushuptracker.SettingsManager
import com.example.pushuptracker.reminders.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val settingsManager: SettingsManager,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    // User Profile
    val age = settingsManager.ageFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 30)
    val weight = settingsManager.weightFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 70)
    val gender = settingsManager.genderFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Male")
    val goal = settingsManager.goalFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Direnç Kazanma")
    val workoutFrequency = settingsManager.workoutFrequencyFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Orta Seviye")

    // Goals
    val dailyGoal = settingsManager.dailyGoalFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 50)

    val dailyWaterGoal = settingsManager.dailyWaterGoalFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2000)

    // Reminders
    val pushupReminderEnabled = settingsManager.pushupReminderEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val pushupReminderTime = settingsManager.pushupReminderTimeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "18:00")

    val waterReminderEnabled = settingsManager.waterReminderEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val waterReminderFrequency = settingsManager.waterReminderFrequencyFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 120)

    fun saveAge(age: Int) = viewModelScope.launch { settingsManager.saveAge(age) }
    fun saveWeight(weight: Int) = viewModelScope.launch { settingsManager.saveWeight(weight) }
    fun saveGender(gender: String) = viewModelScope.launch { settingsManager.saveGender(gender) }
    fun saveGoal(goal: String) = viewModelScope.launch { settingsManager.saveGoal(goal) }
    fun saveWorkoutFrequency(frequency: String) = viewModelScope.launch { settingsManager.saveWorkoutFrequency(frequency) }

    fun saveDailyGoal(newGoal: Int) {
        viewModelScope.launch {
            settingsManager.saveDailyGoal(newGoal)
        }
    }

    fun saveDailyWaterGoal(newGoal: Int) {
        viewModelScope.launch {
            settingsManager.saveDailyWaterGoal(newGoal)
        }
    }

    fun setPushupReminder(enabled: Boolean, time: LocalTime? = null) {
        viewModelScope.launch {
            val newTime = time?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: pushupReminderTime.value
            settingsManager.savePushupReminder(enabled, newTime)
            if (enabled) {
                reminderScheduler.scheduleDailyPushupReminder(LocalTime.parse(newTime))
            } else {
                reminderScheduler.cancelPushupReminder()
            }
        }
    }

    fun setWaterReminder(enabled: Boolean, frequencyMinutes: Int? = null) {
        viewModelScope.launch {
            val newFrequency = frequencyMinutes ?: waterReminderFrequency.value
            settingsManager.saveWaterReminder(enabled, newFrequency)
            if (enabled) {
                reminderScheduler.schedulePeriodicWaterReminder(newFrequency.toLong())
            } else {
                reminderScheduler.cancelWaterReminder()
            }
        }
    }
}
