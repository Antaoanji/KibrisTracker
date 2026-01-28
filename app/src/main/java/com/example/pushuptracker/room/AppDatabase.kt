package com.example.pushuptracker.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.pushuptracker.data.local.CustomWorkoutDao
import com.example.pushuptracker.data.local.PushupDao
import com.example.pushuptracker.data.local.WaterDao
import com.example.pushuptracker.data.local.WorkoutRecordDao
import com.example.pushuptracker.model.ActivityRecord
import com.example.pushuptracker.model.WorkoutRecord

@Database(
    entities = [
        ActivityRecord::class,
        CustomWorkoutEntity::class,
        CustomExerciseEntity::class,
        WorkoutRecord::class
    ],
    version = 11,
    exportSchema = true
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pushupDao(): PushupDao
    abstract fun waterDao(): WaterDao
    abstract fun customWorkoutDao(): CustomWorkoutDao
    abstract fun workoutRecordDao(): WorkoutRecordDao
}
