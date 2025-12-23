package com.example.pushuptracker.model

import androidx.annotation.DrawableRes

data class TrackableActivity(
    val id: String,
    val name: String,
    @field:DrawableRes val imageRes: Int,
    @field:DrawableRes val iconRes: Int,
    val unit: String
)
