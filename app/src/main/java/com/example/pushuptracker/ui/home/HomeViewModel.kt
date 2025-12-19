package com.example.pushuptracker.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pushuptracker.SettingsManager
import com.example.pushuptracker.audio.SoundPlayer
import com.example.pushuptracker.gamification.GamificationManager
import com.example.pushuptracker.model.Activities
import com.example.pushuptracker.model.ActivityRecord
import com.example.pushuptracker.room.AppDao
import com.example.pushuptracker.room.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val dao: AppDao
    private val settingsManager: SettingsManager
    private val soundPlayer: SoundPlayer
    private val gamificationManager: GamificationManager

    val currentStreak: Flow<Int>

    init {
        val context = application.applicationContext
        dao = AppDatabase.get(context).dao()
        settingsManager = SettingsManager(context)
        gamificationManager = GamificationManager(dao, settingsManager)
        soundPlayer = SoundPlayer(context)
        viewModelScope.launch {
            soundPlayer.loadSound(com.example.pushuptracker.R.raw.level_up)
            soundPlayer.loadSound(com.example.pushuptracker.R.raw.level_up2)
            soundPlayer.loadSound(com.example.pushuptracker.R.raw.goal_complete)
        }
        currentStreak = settingsManager.currentStreakFlow

        updateStreak()
    }

    fun updateStreak() {
        viewModelScope.launch {
            gamificationManager.updateStreakAndCheckBadges()
        }
    }

    fun getTodayRecord(activityId: String) = dao.getRecordByDateAndType(LocalDate.now().toString(), activityId)
    fun getYesterdayRecord(activityId: String) = dao.getRecordByDateAndType(LocalDate.now().minusDays(1).toString(), activityId)
    fun getTotal(activityId: String) = dao.getTotalValueByType(activityId)

    fun getDailyGoal(activityId: String): Flow<Int> {
        return if (activityId == Activities.PUSHUPS.id) {
            settingsManager.dailyGoalFlow
        } else {
            settingsManager.dailyWaterGoalFlow
        }
    }

    fun addRecord(activityId: String, value: Double, callback: (goalReached: Boolean) -> Unit) {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            val existingRecord = dao.getRecordByDateAndType(today, activityId).first()
            val currentTotal = existingRecord?.value ?: 0.0
            val newTotal = currentTotal + value

            val dailyGoal = getDailyGoal(activityId).first()

            var goalReached = false
            if (newTotal >= dailyGoal && currentTotal < dailyGoal) {
                soundPlayer.playSound(com.example.pushuptracker.R.raw.goal_complete)
                if (activityId == Activities.PUSHUPS.id) goalReached = true
            } else if (currentTotal >= dailyGoal) {
                soundPlayer.playSound(com.example.pushuptracker.R.raw.level_up2)
            } else {
                soundPlayer.playSound(com.example.pushuptracker.R.raw.level_up)
            }

            dao.upsertRecord(ActivityRecord(id = existingRecord?.id ?: 0, type = activityId, value = newTotal, date = today))
            updateStreak() // Update streak after adding a record
            callback(goalReached)
        }
    }

    override fun onCleared() {
        soundPlayer.release()
        super.onCleared()
    }
}
