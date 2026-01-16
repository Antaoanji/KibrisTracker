package com.example.pushuptracker.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.pushuptracker.data.local.PushupDao
import com.example.pushuptracker.data.local.WaterDao
import com.example.pushuptracker.model.ActivityRecord

@Database(
    entities = [ActivityRecord::class],
    version = 6, // Incremented version for the new 'note' column
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pushupDao(): PushupDao
    abstract fun waterDao(): WaterDao
}
