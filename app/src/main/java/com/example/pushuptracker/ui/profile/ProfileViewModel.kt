package com.example.pushuptracker.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pushuptracker.SettingsManager
import com.example.pushuptracker.reminders.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application
    private val settingsManager = SettingsManager(application)

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
                ReminderScheduler.scheduleDailyPushupReminder(app, LocalTime.parse(newTime))
            } else {
                ReminderScheduler.cancelPushupReminder(app)
            }
        }
    }

    fun setWaterReminder(enabled: Boolean, frequencyMinutes: Int? = null) {
        viewModelScope.launch {
            val newFrequency = frequencyMinutes ?: waterReminderFrequency.value
            settingsManager.saveWaterReminder(enabled, newFrequency)
            if (enabled) {
                ReminderScheduler.schedulePeriodicWaterReminder(app, newFrequency.toLong())
            } else {
                ReminderScheduler.cancelWaterReminder(app)
            }
        }
    }
}
