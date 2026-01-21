package com.example.pushuptracker.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Workout(
    val title: String,
    val exercises: List<Exercise>
) : Parcelable

@Parcelize
data class Exercise(
    val name: String,
    val searchKey: String = "",
    val sets: Int,
    val reps: String,
    val restTimeSeconds: Int,
    val description: String,
    val imageUrl: String = "",
    val videoUrl: String = "", // NEW: YouTube linki için
    val metValue: Double = 3.0
) : Parcelable


@Parcelize
data class WorkoutSummary(
    val title: String,
    val totalTimeMinutes: Int,
    val caloriesBurned: Int,
    val timestamp: Long
) : Parcelable
