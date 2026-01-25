package com.example.pushuptracker.data.repo

import com.example.pushuptracker.data.WorkoutData
import com.example.pushuptracker.data.local.CustomWorkoutDao
import com.example.pushuptracker.model.Exercise
import com.example.pushuptracker.model.Workout
import com.example.pushuptracker.room.CustomExerciseEntity
import com.example.pushuptracker.room.CustomWorkoutEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomWorkoutRepository @Inject constructor(
    private val customWorkoutDao: CustomWorkoutDao
) {

    suspend fun getWorkout(programType: String, dayIndex: Int): Workout? {
        val workoutEntity = customWorkoutDao.getWorkout(programType, dayIndex)
        if (workoutEntity == null) {
            return syncDefaultWorkout(programType, dayIndex)
        }
        return null
    }

    fun getExercisesByWorkoutId(workoutId: Long): Flow<List<Exercise>> {
        return customWorkoutDao.getExercisesForWorkout(workoutId).map { entities ->
            entities.map { it.toModel() }
        }
    }

    suspend fun ensureWorkoutExists(programType: String, dayIndex: Int): Long {
        val existing = customWorkoutDao.getWorkout(programType, dayIndex)
        if (existing != null) return existing.id

        val defaultList = if (programType == "MACHINE_WEIGHT") WorkoutData.machineWeightProgram else WorkoutData.calisthenicsProgram
        val defaultWorkout = defaultList.getOrNull(dayIndex) ?: return -1

        val workoutId = customWorkoutDao.insertWorkout(
            CustomWorkoutEntity(programType = programType, dayIndex = dayIndex, title = defaultWorkout.title)
        )

        val exerciseEntities = defaultWorkout.exercises.mapIndexed { index, ex ->
            ex.toEntity(workoutId, index)
        }
        customWorkoutDao.insertExercises(exerciseEntities)
        return workoutId
    }

    suspend fun resetToDefault(programType: String, dayIndex: Int) {
        val defaultList = if (programType == "MACHINE_WEIGHT") WorkoutData.machineWeightProgram else WorkoutData.calisthenicsProgram
        val defaultWorkout = defaultList.getOrNull(dayIndex) ?: return

        val exerciseEntities = defaultWorkout.exercises.mapIndexed { index, ex ->
            ex.toEntity(0, index)
        }
        customWorkoutDao.resetWorkout(programType, dayIndex, defaultWorkout.title, exerciseEntities)
    }

    // NEW: Reset all days for a program type
    suspend fun resetAllToDefault(programType: String) {
        val defaultList = if (programType == "MACHINE_WEIGHT") WorkoutData.machineWeightProgram else WorkoutData.calisthenicsProgram
        defaultList.forEachIndexed { index, workout ->
            val exerciseEntities = workout.exercises.mapIndexed { exIndex, ex ->
                ex.toEntity(0, exIndex)
            }
            customWorkoutDao.resetWorkout(programType, index, workout.title, exerciseEntities)
        }
    }

    private suspend fun syncDefaultWorkout(programType: String, dayIndex: Int): Workout? {
        val defaultList = if (programType == "MACHINE_WEIGHT") WorkoutData.machineWeightProgram else WorkoutData.calisthenicsProgram
        val defaultWorkout = defaultList.getOrNull(dayIndex) ?: return null

        val workoutId = customWorkoutDao.insertWorkout(
            CustomWorkoutEntity(programType = programType, dayIndex = dayIndex, title = defaultWorkout.title)
        )

        val exerciseEntities = defaultWorkout.exercises.mapIndexed { index, ex ->
            ex.toEntity(workoutId, index)
        }
        customWorkoutDao.insertExercises(exerciseEntities)
        
        return defaultWorkout
    }

    private fun Exercise.toEntity(workoutId: Long, order: Int) = CustomExerciseEntity(
        workoutId = workoutId,
        name = name,
        searchKey = searchKey,
        sets = sets,
        reps = reps,
        restTimeSeconds = restTimeSeconds,
        description = description,
        imageUrl = imageUrl,
        videoUrl = videoUrl,
        orderIndex = order
    )

    private fun CustomExerciseEntity.toModel() = Exercise(
        name = name,
        searchKey = searchKey,
        sets = sets,
        reps = reps,
        restTimeSeconds = restTimeSeconds,
        description = description,
        imageUrl = imageUrl,
        videoUrl = videoUrl
    )
}
