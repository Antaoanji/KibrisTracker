package com.example.pushuptracker.model

import androidx.annotation.DrawableRes
import com.example.pushuptracker.R

data class Badge(
    val id: String,
    val title: String,
    @DrawableRes val iconRes: Int
) {
    companion object {
        val allBadges = listOf(
            Badge(id = "first_pushup", title = "İlk Şınav", iconRes = R.drawable.ic_launcher_foreground),
            Badge(id = "10_pushups_day", title = "10 Şınav/Gün", iconRes = R.drawable.ic_launcher_foreground),
            Badge(id = "50_pushups_day", title = "50 Şınav/Gün", iconRes = R.drawable.ic_launcher_foreground),
            Badge(id = "100_pushups_day", title = "100 Şınav/Gün", iconRes = R.drawable.ic_launcher_foreground),
            Badge(id = "7_day_streak", title = "7 Günlük Seri", iconRes = R.drawable.ic_launcher_foreground),
            Badge(id = "30_day_streak", title = "30 Günlük Seri", iconRes = R.drawable.ic_launcher_foreground)
        )
    }
}
