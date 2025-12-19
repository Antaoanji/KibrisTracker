package com.example.pushuptracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_records")
data class ActivityRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val type: String,       // e.g., "pushups", "water"
    val value: Double,      // e.g., 50.0 (reps), 250.0 (ml)
    val date: String        // e.g., "2024-10-28"
)
