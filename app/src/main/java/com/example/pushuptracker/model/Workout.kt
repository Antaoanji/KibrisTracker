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
    val reps: String, // e.g., "10-12" or "Maksimum"
    val restTimeSeconds: Int,
    val description: String, // How to do it, tips etc.
    val imageUrl: String, // URL for the static image
    val metValue: Double = 3.0 // Metabolic Equivalent of Task - Default to a moderate value
) : Parcelable


@Parcelize
data class WorkoutSummary(
    val title: String,
    val totalTimeMinutes: Int,
    val caloriesBurned: Int,
    val timestamp: Long
) : Parcelable
