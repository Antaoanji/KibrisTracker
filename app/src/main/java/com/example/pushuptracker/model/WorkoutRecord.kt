package com.example.pushuptracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_records")
data class WorkoutRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String, // format: "PROGRAMTYPE_WORKOUTTYPE" e.g. "MACHINE_WEIGHT_PUSH"
    val durationMinutes: Int,
    val date: String, // ISO date format "YYYY-MM-DD"
    val timestamp: Long = System.currentTimeMillis()
)
