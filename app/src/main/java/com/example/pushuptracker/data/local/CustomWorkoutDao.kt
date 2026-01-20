package com.example.pushuptracker.data.local

import androidx.room.*
import com.example.pushuptracker.room.CustomExerciseEntity
import com.example.pushuptracker.room.CustomWorkoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomWorkoutDao {

    @Query("SELECT * FROM custom_workouts WHERE programType = :programType AND dayIndex = :dayIndex")
    suspend fun getWorkout(programType: String, dayIndex: Int): CustomWorkoutEntity?

    @Query("SELECT * FROM custom_exercises WHERE workoutId = :workoutId ORDER BY orderIndex ASC")
    fun getExercisesForWorkout(workoutId: Long): Flow<List<CustomExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: CustomWorkoutEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<CustomExerciseEntity>)

    @Update
    suspend fun updateExercise(exercise: CustomExerciseEntity)

    @Delete
    suspend fun deleteExercise(exercise: CustomExerciseEntity)

    @Query("DELETE FROM custom_exercises WHERE workoutId = :workoutId")
    suspend fun deleteExercisesForWorkout(workoutId: Long)

    @Transaction
    suspend fun resetWorkout(programType: String, dayIndex: Int, newTitle: String, newExercises: List<CustomExerciseEntity>) {
        val workout = getWorkout(programType, dayIndex)
        val id = workout?.id ?: insertWorkout(CustomWorkoutEntity(programType = programType, dayIndex = dayIndex, title = newTitle))
        deleteExercisesForWorkout(id)
        insertExercises(newExercises.map { it.copy(workoutId = id) })
    }
}
