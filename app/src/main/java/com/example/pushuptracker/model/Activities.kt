package com.example.pushuptracker.model

import com.example.pushuptracker.R

object Activities {

    val PUSHUPS = TrackableActivity(
        id = "pushups",
        name = "Şınav",
        imageRes = R.drawable.pushup_main,
        iconRes = R.drawable.ic_fire_workout, // Corrected to a valid drawable
        unit = "tekrar"
    )

    val WATER = TrackableActivity(
        id = "water",
        name = "Su",
        imageRes = R.drawable.water_main,
        iconRes = R.drawable.ic_fire_water, // Corrected to a valid drawable
        unit = "ml"
    )

    val allActivities = listOf(PUSHUPS, WATER)

}
