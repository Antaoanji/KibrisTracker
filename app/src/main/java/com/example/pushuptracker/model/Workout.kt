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
    val sets: Int,
    val reps: String, // e.g., "10-12" or "Maksimum"
    val restTimeSeconds: Int,
    val description: String, // How to do it, tips etc.
    val gifUrl: String // URL for the animated GIF
) : Parcelable
