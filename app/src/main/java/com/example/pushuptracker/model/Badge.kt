package com.example.pushuptracker.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.pushuptracker.R

data class Badge(
    val id: String,
    @get:StringRes val title: Int,
    @get:StringRes val descriptionRes: Int,
    @get:DrawableRes val iconRes: Int,
    val progress: Float = 0f // 0.0 to 1.0
) {
    companion object {
        val allBadges = listOf(
            Badge("streak_3", R.string.badge_streak_3_title, R.string.badge_streak_3_desc, R.drawable.ic_launcher_foreground),
            Badge("streak_7", R.string.badge_streak_7_title, R.string.badge_streak_7_desc, R.drawable.ic_launcher_foreground),
            Badge("streak_14", R.string.badge_streak_14_title, R.string.badge_streak_14_desc, R.drawable.ic_launcher_foreground),
            Badge("streak_30", R.string.badge_streak_30_title, R.string.badge_streak_30_desc, R.drawable.ic_launcher_foreground),
            Badge("total_100", R.string.badge_total_100_title, R.string.badge_total_100_desc, R.drawable.ic_launcher_foreground),
            Badge("total_500", R.string.badge_total_500_title, R.string.badge_total_500_desc, R.drawable.ic_launcher_foreground),
            Badge("total_1000", R.string.badge_total_1000_title, R.string.badge_total_1000_desc, R.drawable.ic_launcher_foreground),
            Badge("total_5000", R.string.badge_total_5000_title, R.string.badge_total_5000_desc, R.drawable.ic_launcher_foreground),
            Badge("single_50", R.string.badge_single_50_title, R.string.badge_single_50_desc, R.drawable.ic_launcher_foreground)
        )
    }
}
