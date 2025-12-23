package com.example.pushuptracker.model

import androidx.room.Entity

@Entity(tableName = "activity_records", primaryKeys = ["date", "type"])
data class ActivityRecord(
    val type: String, // e.g., "pushup", "water"
    val value: Double,
    val date: String // Using ISO-8601 format "YYYY-MM-DD"
)
