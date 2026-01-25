package com.example.pushuptracker.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "activity_records",
    indices = [Index(value = ["type", "date"], unique = true)]
)
data class ActivityRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,
    val value: Double,
    val date: String,
    val weightUsed: Double? = null,
    val note: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val difficulty: String = "medium",
    val barWeight: Double? = null,
    val plates: String? = null,
    val machineMode: String? = null, // "Light", "Medium", "Heavy"
    val machineLevel: Int? = null    // 0-10
)
