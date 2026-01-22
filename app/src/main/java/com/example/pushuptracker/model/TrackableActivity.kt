package com.example.pushuptracker.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.Alignment

data class TrackableActivity(
    val id: String,
    val name: String,
    @field:DrawableRes val imageRes: Int,
    @field:DrawableRes val iconRes: Int,
    val unit: String,
    val alignment: Alignment = Alignment.Center // NEW: Hizalama parametresi
)
