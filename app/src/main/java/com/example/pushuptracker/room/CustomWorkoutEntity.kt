package com.example.pushuptracker.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_workouts")
data class CustomWorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val programType: String, // "MACHINE_WEIGHT" or "CALISTHENICS_WEIGHT"
    val dayIndex: Int,       // 0 to 4
    val title: String
)

@Entity(tableName = "custom_exercises")
data class CustomExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutId: Long,
    val name: String,
    val searchKey: String,
    val sets: Int,
    val reps: String,
    val restTimeSeconds: Int,
    val description: String,
    val imageUrl: String = "",
    val videoUrl: String = "", // NEW: YouTube linki
    val orderIndex: Int
)
