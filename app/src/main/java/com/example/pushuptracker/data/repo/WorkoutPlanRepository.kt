package com.example.pushuptracker.data.repo

import com.example.pushuptracker.data.WorkoutData
import com.example.pushuptracker.model.Workout
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutPlanRepository @Inject constructor() {

    /**
     * Returns the pre-defined PPLUL program.
     */
    fun getPplulProgram(): List<Workout> {
        return WorkoutData.pplulProgram
    }

    /**
     * Returns a specific day's workout from the PPLUL program.
     * index 0: Push, 1: Pull, 2: Legs, 3: Upper, 4: Lower
     */
    fun getWorkoutForDay(index: Int): Workout? {
        return WorkoutData.pplulProgram.getOrNull(index)
    }
}
