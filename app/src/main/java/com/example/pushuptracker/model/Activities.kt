package com.example.pushuptracker.model

import androidx.compose.ui.Alignment
import com.example.pushuptracker.R

object Activities {

    val PUSHUPS = TrackableActivity(
        id = "pushups",
        name = "Şınav",
        imageRes = R.drawable.pushup_main,
        iconRes = R.drawable.logo,
        unit = "tekrar",
        alignment = Alignment.Center
    )

    val WATER = TrackableActivity(
        id = "water",
        name = "Su",
        imageRes = R.drawable.water_main,
        iconRes = R.drawable.logo,
        unit = "ml",
        alignment = Alignment.Center
    )

    val WALKING = TrackableActivity(
        id = "walking",
        name = "Japon Yürüyüşü",
        imageRes = R.drawable.walk_main,
        iconRes = R.drawable.logo,
        unit = "dk",
        alignment = Alignment.TopCenter // Yüzün görünmesi için yukarı hizalandı
    )

    val allActivities = listOf(PUSHUPS, WATER, WALKING)

}
