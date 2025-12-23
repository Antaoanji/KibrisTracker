package com.example.pushuptracker.ui.programs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pushuptracker.di.WorkoutHolder
import com.example.pushuptracker.model.Exercise
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class WorkoutState {
    data object Loading : WorkoutState()
    data class InProgress(val exercise: Exercise, val currentSet: Int, val totalExercises: Int, val currentExerciseIndex: Int) : WorkoutState()
    data class Resting(val nextExercise: Exercise, val nextSet: Int, val remainingTime: Int, val initialDuration: Int) : WorkoutState()
    data object Finished : WorkoutState()
}

@HiltViewModel
class WorkoutPlayerViewModel @Inject constructor(
    private val workoutHolder: WorkoutHolder
) : ViewModel() {

    private val _workoutState = MutableStateFlow<WorkoutState>(WorkoutState.Loading)
    val workoutState = _workoutState.asStateFlow()

    private var timerJob: Job? = null
    private var currentExerciseIndex = 0
    private var currentSetIndex = 1

    init {
        startWorkout()
    }

    private fun startWorkout() {
        val workout = workoutHolder.workout
        if (workout == null || workout.exercises.isEmpty()) {
            _workoutState.value = WorkoutState.Finished
            return
        }
        _workoutState.value = WorkoutState.InProgress(workout.exercises[0], 1, workout.exercises.size, 1)
    }

    fun onSetFinished() {
        timerJob?.cancel() // Ensure any running timer is cancelled
        val workout = workoutHolder.workout ?: return
        val currentExercise = workout.exercises[currentExerciseIndex]

        if (currentSetIndex < currentExercise.sets) {
            // More sets for the current exercise
            currentSetIndex++
            startRest(currentExercise.restTimeSeconds, currentExercise, currentSetIndex)
        } else {
            // Move to the next exercise
            currentExerciseIndex++
            if (currentExerciseIndex < workout.exercises.size) {
                currentSetIndex = 1
                val nextExercise = workout.exercises[currentExerciseIndex]
                startRest(currentExercise.restTimeSeconds, nextExercise, currentSetIndex)
            } else {
                _workoutState.value = WorkoutState.Finished
            }
        }
    }

    private fun startRest(duration: Int, nextExercise: Exercise, nextSet: Int) {
        timerJob?.cancel() // Cancel any existing timer before starting a new one
        timerJob = viewModelScope.launch {
            var remainingTime = duration
            val initialDuration = if (duration > 0) duration else 1 // Avoid division by zero
            while (remainingTime > 0) {
                _workoutState.value = WorkoutState.Resting(nextExercise, nextSet, remainingTime, initialDuration)
                delay(1000)
                remainingTime--
            }
            onRestFinished()
        }
    }

    fun skipRest() {
        timerJob?.cancel()
        onRestFinished()
    }

    fun addRestTime() {
        val currentState = _workoutState.value
        if (currentState is WorkoutState.Resting) {
            // Cancel the current timer and start a new one with an adjusted duration.
            timerJob?.cancel()
            startRest(currentState.remainingTime + 30, currentState.nextExercise, currentState.nextSet)
        }
    }

    private fun onRestFinished() {
        val workout = workoutHolder.workout ?: return
        val exercise = workout.exercises[currentExerciseIndex]
        _workoutState.value = WorkoutState.InProgress(exercise, currentSetIndex, workout.exercises.size, currentExerciseIndex + 1)
    }
}
