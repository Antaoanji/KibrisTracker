package com.example.pushuptracker.di

import com.example.pushuptracker.model.Workout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutHolder @Inject constructor() {
    // This holds the workout for the CURRENT DAY being played.
    var workout: Workout? = null
    var currentExerciseIndex: Int = 0
    var currentSetIndex: Int = 1

    // This holds the ENTIRE 7-day plan.
    val structuredWorkout = MutableStateFlow<Workout?>(null)

    private val _currentDay = MutableStateFlow(1)
    val currentDay = _currentDay.asStateFlow()

    fun isWorkoutActive(): Boolean {
        return workout != null
    }

    fun startNextDay() {
        if (_currentDay.value < 7) { // Assuming a 7-day plan
            _currentDay.value++
        }
        resetDailyProgress()
    }

    // Resets the progress for the day's workout, but keeps the overall plan.
    fun resetDailyProgress() {
        workout = null
        currentExerciseIndex = 0
        currentSetIndex = 1
    }

    // Clears everything, including the 7-day plan.
    fun clearWorkout() {
        workout = null
        structuredWorkout.value = null
        currentExerciseIndex = 0
        currentSetIndex = 1
        _currentDay.value = 1
    }
}
