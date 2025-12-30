package com.example.pushuptracker.ui.programs

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pushuptracker.SettingsManager
import com.example.pushuptracker.audio.AudioCoach
import com.example.pushuptracker.di.WorkoutHolder
import com.example.pushuptracker.model.Exercise
import com.example.pushuptracker.model.WorkoutSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class WorkoutState {
    data object Loading : WorkoutState()
    data class InProgress(val exercise: Exercise, val currentSet: Int, val totalExercises: Int, val currentExerciseIndex: Int) : WorkoutState()
    data class Resting(val nextExercise: Exercise, val nextSet: Int, val remainingTime: Int, val initialDuration: Int) : WorkoutState()
    data class Finished(val totalTimeMinutes: Int, val caloriesBurned: Int) : WorkoutState()
}

@HiltViewModel
class WorkoutPlayerViewModel @Inject constructor(
    private val workoutHolder: WorkoutHolder,
    private val settingsManager: SettingsManager,
    private val audioCoach: AudioCoach
) : ViewModel() {

    private val _workoutState = MutableStateFlow<WorkoutState>(WorkoutState.Loading)
    val workoutState = _workoutState.asStateFlow()

    private var timerJob: Job? = null
    private var startTimeMillis: Long = 0L
    private var totalCaloriesBurned: Double = 0.0

    init {
        loadStateAndStart()
    }

    private fun loadStateAndStart() {
        val workout = workoutHolder.workout
        if (workout == null || workout.exercises.isEmpty() || workoutHolder.currentExerciseIndex >= workout.exercises.size) {
            finishWorkout(true) // Entire plan finished
            return
        }

        // Start timer at the beginning of every workout session
        startTimeMillis = System.currentTimeMillis()

        val exercise = workout.exercises[workoutHolder.currentExerciseIndex]
        _workoutState.value = WorkoutState.InProgress(
            exercise = exercise,
            currentSet = workoutHolder.currentSetIndex,
            totalExercises = workout.exercises.size,
            currentExerciseIndex = workoutHolder.currentExerciseIndex + 1
        )
    }

    fun onSetFinished() = viewModelScope.launch {
        timerJob?.cancel()
        val workout = workoutHolder.workout ?: return@launch
        val currentExercise = workout.exercises[workoutHolder.currentExerciseIndex]

        calculateCaloriesForSet(currentExercise)

        if (workoutHolder.currentSetIndex < currentExercise.sets) {
            workoutHolder.currentSetIndex++
            startRest(currentExercise.restTimeSeconds, currentExercise, workoutHolder.currentSetIndex)
        } else {
            workoutHolder.currentExerciseIndex++
            if (workoutHolder.currentExerciseIndex < workout.exercises.size) {
                workoutHolder.currentSetIndex = 1
                val nextExercise = workout.exercises[workoutHolder.currentExerciseIndex]
                startRest(currentExercise.restTimeSeconds, nextExercise, 1)
            } else {
                finishWorkout(false) // Day finished, not whole plan
            }
        }
    }

    private suspend fun calculateCaloriesForSet(exercise: Exercise) {
        val repsAsInt = exercise.reps.toIntOrNull() ?: 10
        val estimatedSetDurationSeconds = (repsAsInt * 3) // Assuming 3 seconds per rep
        val userWeight = settingsManager.weightFlow.first()
        val caloriesForSet = (exercise.metValue * 3.5 * userWeight) / 200 * (estimatedSetDurationSeconds / 60.0)
        totalCaloriesBurned += caloriesForSet
    }

    private fun finishWorkout(isPlanFinished: Boolean) {
        val totalTimeMillis = System.currentTimeMillis() - startTimeMillis
        val totalTimeMinutes = (totalTimeMillis / 60000).toInt()
        val finalCaloriesBurned = totalCaloriesBurned.toInt()

        _workoutState.value = WorkoutState.Finished(totalTimeMinutes, finalCaloriesBurned)
        audioCoach.announceExercise("Antrenman tamamlandı!")

        viewModelScope.launch {
            // Save workout summary
            workoutHolder.workout?.title?.let {
                val summary = WorkoutSummary(
                    title = it,
                    totalTimeMinutes = totalTimeMinutes,
                    caloriesBurned = finalCaloriesBurned,
                    timestamp = System.currentTimeMillis()
                )
                settingsManager.saveLastWorkoutSummary(summary)
            }

            if (isPlanFinished || workoutHolder.currentDay.value >= 7) {
                // The whole 7-day plan is finished
                settingsManager.clearActiveWorkout()
                workoutHolder.clearWorkout()
                 settingsManager.incrementCurrentStreak() // Increment streak on the last day too
            } else {
                // Only a day is finished, advance to the next day
                val nextDay = workoutHolder.currentDay.value + 1
                settingsManager.saveWorkoutCurrentDay(nextDay)
                workoutHolder.startNextDay() // This will also reset daily progress in the holder
                settingsManager.incrementCurrentStreak()
            }
        }
    }

    private fun startRest(duration: Int, nextExercise: Exercise, nextSet: Int) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var remainingTime = duration
            val initialDuration = if (duration > 0) duration else 1
            var countdownStarted = false
            while (remainingTime > 0) {
                _workoutState.value = WorkoutState.Resting(nextExercise, nextSet, remainingTime, initialDuration)
                if (remainingTime <= 3 && !countdownStarted) {
                    countdownStarted = true
                    audioCoach.playCountdown()
                }
                delay(1000)
                remainingTime--
            }
            onRestFinished()
        }
    }

    fun skipRest() {
        timerJob?.cancel()
        audioCoach.stop()
        onRestFinished()
    }

    fun addRestTime() {
        val currentState = _workoutState.value
        if (currentState is WorkoutState.Resting) {
            timerJob?.cancel()
            audioCoach.stop()
            startRest(currentState.remainingTime + 15, currentState.nextExercise, currentState.nextSet)
        }
    }

    private fun onRestFinished() {
        val workout = workoutHolder.workout ?: return
        if (workoutHolder.currentExerciseIndex >= workout.exercises.size) {
            finishWorkout(true) // This now means the entire plan is finished
            return
        }

        val exercise = workout.exercises[workoutHolder.currentExerciseIndex]
        _workoutState.value = WorkoutState.InProgress(
            exercise,
            workoutHolder.currentSetIndex,
            workout.exercises.size,
            workoutHolder.currentExerciseIndex + 1
        )
    }

    override fun onCleared() {
        super.onCleared()
        audioCoach.shutdown()
        timerJob?.cancel()
    }
}
