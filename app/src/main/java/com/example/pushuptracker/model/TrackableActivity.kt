package com.example.pushuptracker.model

import androidx.annotation.DrawableRes

data class TrackableActivity(
    val id: String,
    val name: String,
    @DrawableRes val imageRes: Int,
    @DrawableRes val iconRes: Int,
    val unit: String
)
