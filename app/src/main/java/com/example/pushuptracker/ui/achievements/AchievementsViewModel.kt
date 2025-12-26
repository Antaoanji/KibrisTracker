package com.example.pushuptracker.ui.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pushuptracker.SettingsManager
import com.example.pushuptracker.data.repo.PushupRepo
import com.example.pushuptracker.model.Badge
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AchievementsUiState(
    val badges: List<Badge> = emptyList(),
)

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val settingsManager: SettingsManager,
    private val pushupRepo: PushupRepo
) : ViewModel() {

    val uiState = combine(
        settingsManager.unlockedBadgesFlow,
        pushupRepo.getAllRecords(),
        settingsManager.currentStreakFlow
    ) { unlockedBadges, allPushups, currentStreak ->

        val totalPushups = allPushups.sumOf { it.value }.toInt()
        val maxInSingleSession = allPushups.maxOfOrNull { it.value }?.toInt() ?: 0

        val updatedBadges = Badge.allBadges.map { badge ->
            val isUnlocked = unlockedBadges.contains(badge.id)
            val progress = if (isUnlocked) 1f else {
                when (badge.id) {
                    "streak_3" -> (currentStreak / 3f)
                    "streak_7" -> (currentStreak / 7f)
                    "streak_14" -> (currentStreak / 14f)
                    "streak_30" -> (currentStreak / 30f)
                    "total_100" -> (totalPushups / 100f)
                    "total_500" -> (totalPushups / 500f)
                    "total_1000" -> (totalPushups / 1000f)
                    "total_5000" -> (totalPushups / 5000f)
                    "single_50" -> (maxInSingleSession / 50f)
                    else -> 0f
                }.coerceIn(0f, 1f)
            }
            badge.copy(progress = progress)
        }

        AchievementsUiState(
            badges = updatedBadges
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AchievementsUiState()
    )

    fun unlockBadge(badgeId: String) {
        viewModelScope.launch {
            settingsManager.unlockBadge(badgeId)
        }
    }
}
