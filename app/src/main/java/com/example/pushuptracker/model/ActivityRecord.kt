package com.example.pushuptracker.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "activity_records",
    indices = [Index(value = ["type", "date"], unique = true)] // Bir günde bir tipte sadece bir kayıt olabilir
)
data class ActivityRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // e.g., "pushup", "water", or exercise searchKey
    val value: Double, // reps or quantity
    val date: String, // Using ISO-8601 format "YYYY-8601"
    val weightUsed: Double? = null,
    val note: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val difficulty: String = "medium" // RPE: easy, medium, hard
)
