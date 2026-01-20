package com.example.pushuptracker.ui.programs

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pushuptracker.BuildConfig
import com.example.pushuptracker.SettingsManager
import com.example.pushuptracker.audio.AudioCoach
import com.example.pushuptracker.data.repo.PushupRepo
import com.example.pushuptracker.di.WorkoutHolder
import com.example.pushuptracker.model.ActivityRecord
import com.example.pushuptracker.model.Exercise
import com.example.pushuptracker.model.WorkoutSummary
import com.google.ai.client.generativeai.GenerativeModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

sealed class WorkoutState {
    data object Loading : WorkoutState()
    data class InProgress(
        val exercise: Exercise,
        val currentSet: Int,
        val totalExercises: Int,
        val currentExerciseIndex: Int,
        val historyHint: String?,
        val coachSuggestion: String?,
        val remainingExerciseTime: Int? = null // New: for timed exercises
    ) : WorkoutState()
    data class Resting(val nextExercise: Exercise, val nextSet: Int, val remainingTime: Int, val initialDuration: Int) : WorkoutState()
    data class Finished(
        val totalTimeMinutes: Int, 
        val totalVolume: Double,
        val dominantDifficulty: String
    ) : WorkoutState()
}

@HiltViewModel
class WorkoutPlayerViewModel @Inject constructor(
    private val workoutHolder: WorkoutHolder,
    private val settingsManager: SettingsManager,
    private val audioCoach: AudioCoach,
    private val pushupRepo: PushupRepo
) : ViewModel() {

    private val _workoutState = MutableStateFlow<WorkoutState>(WorkoutState.Loading)
    val workoutState = _workoutState.asStateFlow()
    
    private val _isSwapping = MutableStateFlow(false)
    val isSwapping = _isSwapping.asStateFlow()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    private var timerJob: Job? = null
    private var exerciseTimerJob: Job? = null
    private var startTimeMillis: Long = 0L
    private var sessionVolume: Double = 0.0
    private val sessionDifficulties = mutableListOf<String>()

    init {
        loadStateAndStart()
    }

    private fun loadStateAndStart() = viewModelScope.launch {
        sessionVolume = 0.0
        sessionDifficulties.clear()
        val workout = workoutHolder.workout
        if (workout == null || workout.exercises.isEmpty() || workoutHolder.currentExerciseIndex >= workout.exercises.size) {
            finishWorkout(true)
            return@launch
        }

        startTimeMillis = System.currentTimeMillis()
        val exercise = workout.exercises[workoutHolder.currentExerciseIndex]
        updateStateWithHistory(exercise, workoutHolder.currentSetIndex)
    }

    private fun isWarmup(exercise: Exercise): Boolean {
        val warmupKeywords = listOf(
            "Isınma", "Çevirme", "Döndürme", "Jumping", "Kedi-Deve", 
            "Scapular", "Duvarda", "Leg Swing", "Pull Apart", 
            "Bodyweight Squat", "Melek", "Stretch", "Hang", "Statik"
        )
        return warmupKeywords.any { exercise.name.contains(it, ignoreCase = true) } || 
               exercise.reps.contains("Saniye") || 
               exercise.reps.contains("Dakika")
    }

    fun onSetFinished(weightUsed: Double?, difficulty: String, note: String? = null) = viewModelScope.launch {
        timerJob?.cancel()
        exerciseTimerJob?.cancel()
        val workout = workoutHolder.workout ?: return@launch
        val currentExercise = workout.exercises[workoutHolder.currentExerciseIndex]
        
        val repsString = currentExercise.reps
        val reps = if (repsString == "MAX" || repsString.contains("Dakika") || repsString.contains("Saniye")) 20 else repsString.split('-').mapNotNull { it.trim().toIntOrNull() }.average().toInt()

        val key = if (currentExercise.searchKey.isNotBlank()) currentExercise.searchKey else currentExercise.name
        val record = ActivityRecord(
            type = key,
            value = reps.toDouble(),
            date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
            weightUsed = weightUsed,
            note = note,
            timestamp = System.currentTimeMillis(),
            difficulty = difficulty
        )
        pushupRepo.insertRecord(record)

        sessionVolume += (weightUsed ?: 0.0) * reps
        sessionDifficulties.add(difficulty)

        val shouldSkipRest = isWarmup(currentExercise)

        if (workoutHolder.currentSetIndex < currentExercise.sets) {
            workoutHolder.currentSetIndex++
            if (shouldSkipRest) {
                onRestFinished()
            } else {
                startRest(currentExercise.restTimeSeconds, currentExercise, workoutHolder.currentSetIndex)
            }
        } else {
            workoutHolder.currentExerciseIndex++
            if (workoutHolder.currentExerciseIndex < workout.exercises.size) {
                workoutHolder.currentSetIndex = 1
                val nextExercise = workout.exercises[workoutHolder.currentExerciseIndex]
                if (shouldSkipRest) {
                    onRestFinished()
                } else {
                    startRest(currentExercise.restTimeSeconds, nextExercise, 1)
                }
            } else {
                finishWorkout(false)
            }
        }
    }

    fun swapCurrentExercise() = viewModelScope.launch {
        if (_isSwapping.value) return@launch
        val state = _workoutState.value
        if (state !is WorkoutState.InProgress) return@launch

        _isSwapping.value = true
        try {
            val currentExercise = state.exercise
            val prompt = "I cannot do '${currentExercise.name}'. Suggest ONE alternative exercise name targeting the same muscle group. Output ONLY the exercise name in English, no extra text."

            val response = generativeModel.generateContent(prompt)
            val newExerciseName = response.text?.trim()

            if (!newExerciseName.isNullOrBlank()) {
                val newSearchKey = newExerciseName.lowercase().replace(" ", "-")
                val workout = workoutHolder.workout ?: return@launch
                val exerciseIndex = workoutHolder.currentExerciseIndex
                val updatedExercises = workout.exercises.toMutableList()
                val originalExercise = updatedExercises[exerciseIndex]

                updatedExercises[exerciseIndex] = originalExercise.copy(
                    name = newExerciseName,
                    searchKey = newSearchKey
                )
                workoutHolder.workout = workout.copy(exercises = updatedExercises)
                updateStateWithHistory(updatedExercises[exerciseIndex], workoutHolder.currentSetIndex)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isSwapping.value = false
        }
    }

    private fun getDominantDifficulty(): String {
        if (sessionDifficulties.isEmpty()) return "Dengeli"
        val dominant = sessionDifficulties.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key
        return when (dominant) {
            "easy" -> "Rahat"
            "medium" -> "Dengeli"
            "hard" -> "Zorlu"
            else -> "Dengeli"
        }
    }

    private fun finishWorkout(isPlanFinished: Boolean) {
        val totalTimeMillis = System.currentTimeMillis() - startTimeMillis
        val totalTimeMinutes = (totalTimeMillis / 60000).toInt()

        _workoutState.value = WorkoutState.Finished(
            totalTimeMinutes,
            sessionVolume,
            getDominantDifficulty()
        )

        viewModelScope.launch {
            workoutHolder.workout?.title?.let {
                val summary = WorkoutSummary(
                    title = it,
                    totalTimeMinutes = totalTimeMinutes,
                    caloriesBurned = 0,
                    timestamp = System.currentTimeMillis()
                )
                settingsManager.saveLastWorkoutSummary(summary)
            }

            if (isPlanFinished || workoutHolder.currentDay.value >= 7) {
                settingsManager.clearActiveWorkout()
                workoutHolder.clearWorkout()
                settingsManager.incrementCurrentStreak()
            } else {
                val nextDay = workoutHolder.currentDay.value + 1
                settingsManager.saveWorkoutCurrentDay(nextDay)
                workoutHolder.startNextDay()
                settingsManager.incrementCurrentStreak()
            }
        }
    }

    private fun startRest(duration: Int, nextExercise: Exercise, nextSet: Int) {
        timerJob?.cancel()
        exerciseTimerJob?.cancel()
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

    private fun onRestFinished() = viewModelScope.launch {
        val workout = workoutHolder.workout ?: return@launch
        if (workoutHolder.currentExerciseIndex >= workout.exercises.size) {
            finishWorkout(true)
            return@launch
        }

        val exercise = workout.exercises[workoutHolder.currentExerciseIndex]
        updateStateWithHistory(exercise, workoutHolder.currentSetIndex)
    }

    private suspend fun updateStateWithHistory(exercise: Exercise, setIndex: Int) {
        val key = if (exercise.searchKey.isNotBlank()) exercise.searchKey else exercise.name
        val lastRecord = pushupRepo.getLastRecordForExercise(key)

        val hint = lastRecord?.let {
            val reps = it.value.toInt()
            val weight = it.weightUsed?.toInt()
            val note = it.note
            val weightPart = if (weight != null && weight > 0) " @ $weight kg" else ""
            val notePart = if (!note.isNullOrBlank()) " ($note)" else ""
            "Geçen Sefer: $reps Tekrar$weightPart$notePart"
        }
        
        val coachSuggestion = lastRecord?.let {
            when (it.difficulty) {
                "easy" -> "Geçen sefer kolaydı. Bugün ağırlığı biraz artırabilirsin! 🚀"
                "medium" -> "Formun iyiydi. Aynı ağırlıkla tekniğini koru. 👍"
                "hard" -> "Geçen sefer zorlandın. Bugün ağırlığı artırma. 🛡️"
                else -> null
            }
        } ?: "İlk kez yapıyorsun, başarılar!"

        val workout = workoutHolder.workout!!
        
        // Handle Timed Exercises
        val repsText = exercise.reps
        val initialTime = when {
            repsText.contains("Dakika") -> {
                val mins = repsText.split(" ")[0].toIntOrNull() ?: 1
                mins * 60
            }
            repsText.contains("Saniye") -> {
                repsText.split(" ")[0].toIntOrNull() ?: 30
            }
            else -> null
        }
        
        _workoutState.value = WorkoutState.InProgress(
            exercise,
            setIndex,
            workout.exercises.size,
            workoutHolder.currentExerciseIndex + 1,
            historyHint = hint,
            coachSuggestion = coachSuggestion,
            remainingExerciseTime = initialTime
        )

        if (initialTime != null) {
            startExerciseTimer(initialTime)
        }
    }

    private fun startExerciseTimer(seconds: Int) {
        exerciseTimerJob?.cancel()
        exerciseTimerJob = viewModelScope.launch {
            var time = seconds
            while (time > 0) {
                val currentState = _workoutState.value
                if (currentState is WorkoutState.InProgress) {
                    _workoutState.value = currentState.copy(remainingExerciseTime = time)
                }
                delay(1000)
                time--
            }
            // Auto finish when time is up
            if (time <= 0) {
                audioCoach.announceExercise("Süre bitti!")
                delay(1000) // Give a second for the announcement/ui to settle
                onSetFinished(null, "medium", "Otomatik Tamamlandı")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioCoach.shutdown()
        timerJob?.cancel()
        exerciseTimerJob?.cancel()
    }
}
