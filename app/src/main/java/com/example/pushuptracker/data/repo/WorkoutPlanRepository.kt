package com.example.pushuptracker.data.repo

import com.example.pushuptracker.data.WorkoutData
import com.example.pushuptracker.model.Workout
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutPlanRepository @Inject constructor() {

    /**
     * Returns the pre-defined Machine + Weight program.
     */
    fun getMachineWeightProgram(): List<Workout> {
        return WorkoutData.machineWeightProgram
    }

    /**
     * Returns a specific day's workout from the default program.
     */
    fun getWorkoutForDay(index: Int): Workout? {
        return WorkoutData.machineWeightProgram.getOrNull(index)
    }
}
