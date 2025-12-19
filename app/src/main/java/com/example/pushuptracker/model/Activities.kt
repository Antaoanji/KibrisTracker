package com.example.pushuptracker.model

import com.example.pushuptracker.R

object Activities {

    val PUSHUPS = TrackableActivity(
        id = "pushups",
        name = "Şınav",
        imageRes = R.drawable.pushup_main,
        iconRes = R.drawable.ic_pushup,
        unit = "tekrar"
    )

    val WATER = TrackableActivity(
        id = "water",
        name = "Su",
        imageRes = R.drawable.water_main,
        iconRes = R.drawable.ic_water,
        unit = "ml"
    )

    val allActivities = listOf(PUSHUPS, WATER)

}
