package com.example.pushuptracker.ui.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pushuptracker.SettingsManager
import com.example.pushuptracker.model.Badge
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class AchievementsUiState(
    val allBadges: List<Badge> = emptyList(),
    val unlockedBadges: Set<String> = emptySet()
)

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    settingsManager: SettingsManager
) : ViewModel() {

    private val allBadges = Badge.allBadges

    val uiState = combine(settingsManager.unlockedBadgesFlow) { (unlockedBadges) ->
        AchievementsUiState(
            allBadges = allBadges,
            unlockedBadges = unlockedBadges
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AchievementsUiState()
    )
}
