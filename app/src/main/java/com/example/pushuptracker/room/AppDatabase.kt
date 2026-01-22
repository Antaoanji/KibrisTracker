package com.example.pushuptracker.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.pushuptracker.data.local.CustomWorkoutDao
import com.example.pushuptracker.data.local.PushupDao
import com.example.pushuptracker.data.local.WaterDao
import com.example.pushuptracker.model.ActivityRecord

@Database(
    entities = [
        ActivityRecord::class,
        CustomWorkoutEntity::class,
        CustomExerciseEntity::class
    ],
    version = 10,
    exportSchema = true
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pushupDao(): PushupDao
    abstract fun waterDao(): WaterDao
    abstract fun customWorkoutDao(): CustomWorkoutDao
}
