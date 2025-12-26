package com.example.pushuptracker.di

import com.example.pushuptracker.model.Workout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutHolder @Inject constructor() {
    var workout: Workout? = null
    var currentExerciseIndex: Int = 0
    var currentSetIndex: Int = 1

    private val _currentDay = MutableStateFlow(1)
    val currentDay = _currentDay.asStateFlow()

    fun isWorkoutActive(): Boolean {
        return workout != null
    }

    fun startNextDay() {
        // Find the index of the start of the next day
        val nextDayIndex = workout?.exercises?.drop(currentExerciseIndex + 1)?.indexOfFirst { it.sets == 0 } ?: -1
        if (nextDayIndex != -1) {
            _currentDay.value++
            currentExerciseIndex += nextDayIndex + 1
            currentSetIndex = 1
        } else {
            // No more days found, treat as finished
            clearWorkout()
        }
    }


    fun clearWorkout() {
        workout = null
        currentExerciseIndex = 0
        currentSetIndex = 1
        _currentDay.value = 1
    }
}
