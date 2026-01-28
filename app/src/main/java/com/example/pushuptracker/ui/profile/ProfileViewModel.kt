package com.example.pushuptracker.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pushuptracker.SettingsManager
import com.example.pushuptracker.data.local.WorkoutRecordDao
import com.example.pushuptracker.data.repo.PushupRepo
import com.example.pushuptracker.data.repo.WaterRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val settingsManager: SettingsManager,
    private val pushupRepo: PushupRepo,
    private val waterRepo: WaterRepo,
    private val workoutRecordDao: WorkoutRecordDao
) : ViewModel() {

    val age: Flow<Int> = settingsManager.ageFlow
    val gender: Flow<String> = settingsManager.genderFlow
    val goal: Flow<String> = settingsManager.goalFlow
    val workoutFrequency: Flow<String> = settingsManager.workoutFrequencyFlow
    val dailyGoal: Flow<Int> = settingsManager.dailyGoalFlow
    val dailyWaterGoal: Flow<Int> = settingsManager.dailyWaterGoalFlow
    val workoutReminderEnabled: Flow<Boolean> = settingsManager.workoutReminderEnabledFlow
    val workoutReminderTime: Flow<String> = settingsManager.workoutReminderTimeFlow
    val pushupReminderEnabled: Flow<Boolean> = settingsManager.pushupReminderEnabledFlow
    val pushupReminderTime: Flow<String> = settingsManager.pushupReminderTimeFlow
    val waterReminderEnabled: Flow<Boolean> = settingsManager.waterReminderEnabledFlow
    val waterReminderFrequency: Flow<Int> = settingsManager.waterReminderFrequencyFlow

    fun saveAge(age: Int) = viewModelScope.launch { settingsManager.saveAge(age) }
    fun saveGender(gender: String) = viewModelScope.launch { settingsManager.saveGender(gender) }
    fun saveGoal(goal: String) = viewModelScope.launch { settingsManager.saveGoal(goal) }
    fun saveWorkoutFrequency(frequency: String) = viewModelScope.launch { settingsManager.saveWorkoutFrequency(frequency) }
    fun saveDailyGoal(goal: Int) = viewModelScope.launch { settingsManager.saveDailyGoal(goal) }
    fun saveDailyWaterGoal(goal: Int) = viewModelScope.launch { settingsManager.saveDailyWaterGoal(goal) }

    fun setWorkoutReminder(enabled: Boolean, time: LocalTime?) = viewModelScope.launch {
        val timeString = time?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: settingsManager.workoutReminderTimeFlow.first()
        settingsManager.saveWorkoutReminder(enabled, timeString)
    }

    fun setPushupReminder(enabled: Boolean, time: LocalTime?) = viewModelScope.launch {
        val timeString = time?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: settingsManager.pushupReminderTimeFlow.first()
        settingsManager.savePushupReminder(enabled, timeString)
    }

    fun setWaterReminder(enabled: Boolean, frequency: Int?) = viewModelScope.launch {
        val frequencyMinutes = frequency ?: settingsManager.waterReminderFrequencyFlow.first()
        settingsManager.saveWaterReminder(enabled, frequencyMinutes)
    }

    fun resetAllUserData() = viewModelScope.launch {
        settingsManager.clearAllData()
        pushupRepo.clear()
        waterRepo.clear()
        workoutRecordDao.clear() // Heatmap verilerini de sıfırla
    }
}
