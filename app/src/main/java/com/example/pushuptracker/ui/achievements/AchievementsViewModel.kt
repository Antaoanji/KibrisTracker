package com.example.pushuptracker.ui.achievements

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pushuptracker.SettingsManager
import com.example.pushuptracker.model.Badge
import com.example.pushuptracker.model.BadgesRepo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class AchievementsUiState(
    val unlockedBadges: Set<String> = emptySet(),
    val allBadges: List<Badge> = emptyList()
)

class AchievementsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsManager = SettingsManager(application)

    val uiState: StateFlow<AchievementsUiState> = settingsManager.unlockedBadgesFlow.map {
        unlockedBadges ->
        AchievementsUiState(
            unlockedBadges = unlockedBadges,
            allBadges = BadgesRepo.allBadges
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AchievementsUiState()
    )
}
