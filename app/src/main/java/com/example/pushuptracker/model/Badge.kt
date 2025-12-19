package com.example.pushuptracker.model

import androidx.annotation.DrawableRes
import com.example.pushuptracker.R

// Represents a single badge that can be earned
data class Badge(
    val id: String,
    val title: String,
    val description: String,
    @DrawableRes val iconRes: Int,
)

// A static repository of all possible badges in the app
object BadgesRepo {
    val allBadges = listOf(
        // --- Push-up Badges ---
        Badge(
            id = "pushups_50",
            title = "Acemi Şınavcı",
            description = "Tek seferde 50 şınav çek.",
            iconRes = R.drawable.ic_launcher_foreground // Placeholder Icon
        ),
        Badge(
            id = "pushups_100",
            title = "Şınav Ustası",
            description = "Tek seferde 100 şınav çek.",
            iconRes = R.drawable.ic_launcher_foreground // Placeholder Icon
        ),
        // --- Water Badges ---
        Badge(
            id = "water_3000ml",
            title = "Su Perisi",
            description = "Bir günde 3000ml su iç.",
            iconRes = R.drawable.ic_launcher_foreground // Placeholder Icon
        ),
        // --- Streak Badges ---
        Badge(
            id = "streak_7_day",
            title = "Azim Abidesi",
            description = "7 gün üst üste hedefe ulaş.",
            iconRes = R.drawable.ic_launcher_foreground // Placeholder Icon
        ),
        Badge(
            id = "streak_30_day",
            title = "Alışkanlık Kazanan",
            description = "30 gün üst üste hedefe ulaş.",
            iconRes = R.drawable.ic_launcher_foreground // Placeholder Icon
        ),
        // --- Program Badges ---
        Badge(
            id = "program_complete_1",
            title = "İlk Adım",
            description = "İlk antrenman programını tamamla.",
            iconRes = R.drawable.ic_launcher_foreground // Placeholder Icon
        ),
    )
}
