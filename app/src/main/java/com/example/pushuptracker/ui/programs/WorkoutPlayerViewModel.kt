package com.example.pushuptracker.ui.programs

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioManager
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pushuptracker.SettingsManager
import com.example.pushuptracker.audio.AudioCoach
import com.example.pushuptracker.audio.WorkoutService
import com.example.pushuptracker.data.local.WorkoutRecordDao
import com.example.pushuptracker.data.repo.PushupRepo
import com.example.pushuptracker.di.WorkoutHolder
import com.example.pushuptracker.gamification.GamificationManager
import com.example.pushuptracker.model.ActivityRecord
import com.example.pushuptracker.model.Exercise
import com.example.pushuptracker.model.WorkoutRecord
import com.example.pushuptracker.model.WorkoutSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

enum class ExerciseCategory {
    BARBELL, DUMBBELL, MACHINE, BODYWEIGHT
}

sealed class WorkoutState {
    data object Loading : WorkoutState()
    data class InProgress(
        val exercise: Exercise,
        val category: ExerciseCategory,
        val currentSet: Int,
        val totalExercises: Int,
        val currentExerciseIndex: Int,
        val coachSuggestion: String?,
        val remainingExerciseTime: Int? = null,
        val isTimerPaused: Boolean = false
    ) : WorkoutState()
    data class Resting(val nextExercise: Exercise, val nextSet: Int, val remainingTime: Int, val initialDuration: Int) : WorkoutState()
    data class Finished(
        val totalTimeMinutes: Int,
        val totalVolume: Double,
        val dominantDifficulty: String,
        val workoutTitle: String
    ) : WorkoutState()
}

@HiltViewModel
class WorkoutPlayerViewModel @Inject constructor(
    application: Application,
    private val workoutHolder: WorkoutHolder,
    private val settingsManager: SettingsManager,
    private val audioCoach: AudioCoach,
    private val pushupRepo: PushupRepo,
    private val workoutRecordDao: WorkoutRecordDao,
    private val gamificationManager: GamificationManager
) : AndroidViewModel(application) {

    private val _workoutState = MutableStateFlow<WorkoutState>(WorkoutState.Loading)
    val workoutState = _workoutState.asStateFlow()

    private val _selectedBarWeight = MutableStateFlow(0.0)
    val selectedBarWeight = _selectedBarWeight.asStateFlow()

    private val _selectedPlates = MutableStateFlow<List<Double>>(emptyList())
    val selectedPlates = _selectedPlates.asStateFlow()

    private val _totalCalculatedWeight = MutableStateFlow(0.0)
    val totalCalculatedWeight = _totalCalculatedWeight.asStateFlow()

    private val _machineMode = MutableStateFlow("Medium")
    val machineMode = _machineMode.asStateFlow()

    private val _machineLevel = MutableStateFlow(5)
    val machineLevel = _machineLevel.asStateFlow()

    private var workoutService: WorkoutService? = null
    private var serviceConnection: ServiceConnection? = null
    private var serviceJob: Job? = null

    private var startTimeMillis: Long = 0L
    private var sessionVolume: Double = 0.0
    private val sessionDifficulties = mutableListOf<String>()

    init {
        bindToService()
        loadStateAndStart()
    }

    private fun bindToService() {
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as WorkoutService.WorkoutBinder
                workoutService = binder.getService()
                observeServiceTimer()
            }
            override fun onServiceDisconnected(name: ComponentName?) {
                workoutService = null
            }
        }
        val intent = Intent(getApplication(), WorkoutService::class.java)
        getApplication<Application>().bindService(intent, serviceConnection!!, Context.BIND_AUTO_CREATE)
    }

    private fun observeServiceTimer() {
        serviceJob?.cancel()
        serviceJob = viewModelScope.launch {
            workoutService?.timerState?.collect { timerState ->
                if (timerState == null) return@collect

                val currentState = _workoutState.value
                when (currentState) {
                    is WorkoutState.Resting -> {
                        if (timerState.remainingSeconds <= 0) {
                            onRestFinished()
                        } else {
                            _workoutState.value = currentState.copy(remainingTime = timerState.remainingSeconds)
                        }
                    }
                    is WorkoutState.InProgress -> {
                        if (timerState.isWorkoutTimer) {
                            if (timerState.remainingSeconds <= 0) {
                                audioCoach.announceExercise("Süre bitti!")
                                delay(1000)
                                onSetFinished("medium", "Otomatik Tamamlandı", null)
                            } else {
                                _workoutState.value = currentState.copy(
                                    remainingExerciseTime = timerState.remainingSeconds,
                                    isTimerPaused = timerState.isPaused
                                )
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    private fun loadStateAndStart() = viewModelScope.launch {
        sessionVolume = 0.0
        sessionDifficulties.clear()
        
        val savedState = settingsManager.savedWorkoutStateFlow.first()
        val currentWorkout = workoutHolder.workout

        if (savedState != null && currentWorkout != null && savedState.first == currentWorkout.title) {
            workoutHolder.currentExerciseIndex = savedState.second
            workoutHolder.currentSetIndex = savedState.third
        }

        val workout = workoutHolder.workout
        if (workout == null || workout.exercises.isEmpty() || workoutHolder.currentExerciseIndex >= workout.exercises.size) {
            finishWorkout(true)
            return@launch
        }

        startTimeMillis = System.currentTimeMillis()
        updateStateWithHistory(workout.exercises[workoutHolder.currentExerciseIndex], workoutHolder.currentSetIndex)
    }

    private fun saveCurrentProgress() {
        viewModelScope.launch {
            val title = workoutHolder.workout?.title ?: return@launch
            settingsManager.saveWorkoutProgress(
                title = title,
                exerciseIndex = workoutHolder.currentExerciseIndex,
                setIndex = workoutHolder.currentSetIndex
            )
        }
    }

    fun selectBar(weight: Double) {
        _selectedBarWeight.value = weight
        calculateTotal()
    }

    fun addPlate(plateWeight: Double, count: Int = 1) {
        val current = _selectedPlates.value.toMutableList()
        repeat(count) { current.add(plateWeight) }
        _selectedPlates.value = current
        calculateTotal()
    }

    fun removePlateAt(index: Int) {
        val current = _selectedPlates.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _selectedPlates.value = current
            calculateTotal()
        }
    }

    fun setMachineMode(mode: String) {
        _machineMode.value = mode
    }

    fun setMachineLevel(level: Int) {
        _machineLevel.value = level
    }

    private fun calculateTotal(currentCategory: ExerciseCategory? = null) {
        val bar = _selectedBarWeight.value
        val platesSum = _selectedPlates.value.sum()
        _totalCalculatedWeight.value = bar + platesSum
    }

    private fun getCategory(exerciseName: String): ExerciseCategory {
        val name = exerciseName.lowercase()
        return when {
            listOf("machine", "cable", "pulldown", "pushdown", "pec deck", "fly", "extension", "leg curl", "row", "preacher").any { name.contains(it) } && !name.contains("dumbbell") && !name.contains("barbell") -> ExerciseCategory.MACHINE
            listOf("barbell", "deadlift", "squat", "overhead press", "bent over row", "curl").any { name.contains(it) } && !name.contains("dumbbell") && !name.contains("goblet") && !name.contains("bodyweight") -> ExerciseCategory.BARBELL
            listOf("dumbbell", "dumble", "lunge", "goblet", "skullcrusher", "hammer", "floor press").any { name.contains(it) } -> ExerciseCategory.DUMBBELL
            else -> ExerciseCategory.BODYWEIGHT
        }
    }

    fun onSetFinished(difficulty: String, note: String? = null, actualReps: Int? = null) = viewModelScope.launch {
        val workout = workoutHolder.workout ?: return@launch
        val currentExercise = workout.exercises[workoutHolder.currentExerciseIndex]
        val category = getCategory(currentExercise.name)

        val calculatedReps: Double = try {
            when {
                actualReps != null && actualReps > 0 -> actualReps.toDouble()
                currentExercise.reps.contains("MAX", ignoreCase = true) ||
                        currentExercise.reps.contains("Dakika", ignoreCase = true) ||
                        currentExercise.reps.contains("Saniye", ignoreCase = true) ||
                        currentExercise.reps.contains("sn", ignoreCase = true) -> 20.0
                else -> {
                    val numbers = currentExercise.reps.split('-').mapNotNull { part ->
                        part.filter { it.isDigit() || it == '.' }.toDoubleOrNull()
                    }
                    if (numbers.isNotEmpty()) {
                        val avg = numbers.average()
                        if (avg.isNaN() || avg <= 0) 10.0 else avg
                    } else 10.0
                }
            }
        } catch (e: Exception) { 10.0 }

        val key = if (currentExercise.searchKey.isNotBlank()) currentExercise.searchKey else currentExercise.name
        val finalWeight = if (category == ExerciseCategory.MACHINE) 0.0 else _totalCalculatedWeight.value
        val platesString = _selectedPlates.value.joinToString(",")

        val record = ActivityRecord(
            type = key,
            value = calculatedReps,
            date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
            weightUsed = finalWeight,
            note = note,
            timestamp = System.currentTimeMillis(),
            difficulty = difficulty,
            barWeight = if (category == ExerciseCategory.MACHINE) null else _selectedBarWeight.value,
            plates = platesString,
            machineMode = if (category == ExerciseCategory.MACHINE) _machineMode.value else null,
            machineLevel = if (category == ExerciseCategory.MACHINE) _machineLevel.value else null
        )

        try { pushupRepo.insertRecord(record) } catch (e: Exception) { Log.e("WorkoutPlayerVM", "Room Error: ${e.message}") }

        sessionVolume += (finalWeight ?: 0.0) * calculatedReps
        sessionDifficulties.add(difficulty)

        if (workoutHolder.currentSetIndex < currentExercise.sets) {
            workoutHolder.currentSetIndex++
            saveCurrentProgress()
            startRest(currentExercise.restTimeSeconds, currentExercise, workoutHolder.currentSetIndex)
        } else {
            workoutHolder.currentExerciseIndex++
            if (workoutHolder.currentExerciseIndex < workout.exercises.size) {
                workoutHolder.currentSetIndex = 1
                saveCurrentProgress()
                val nextExercise = workout.exercises[workoutHolder.currentExerciseIndex]
                startRest(currentExercise.restTimeSeconds, nextExercise, 1)
            } else {
                finishWorkout(false)
            }
        }
    }

    fun pauseWorkout() {
        val currentState = _workoutState.value
        if (currentState is WorkoutState.InProgress && currentState.remainingExerciseTime != null) {
            if (!currentState.isTimerPaused) toggleExerciseTimer()
        }
    }

    fun resumeWorkout() {
        val currentState = _workoutState.value
        if (currentState is WorkoutState.InProgress && currentState.remainingExerciseTime != null) {
            if (currentState.isTimerPaused) toggleExerciseTimer()
        }
    }

    private fun getDominantDifficulty(): String {
        val dominant = sessionDifficulties.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key
        return when (dominant) {
            "easy" -> "Rahat"
            "hard" -> "Zorlu"
            else -> "Dengeli"
        }
    }

    fun moveToNextExercise() = viewModelScope.launch {
        val workout = workoutHolder.workout ?: return@launch
        if (workoutHolder.currentExerciseIndex < workout.exercises.size - 1) {
            workoutHolder.currentExerciseIndex++; workoutHolder.currentSetIndex = 1
            saveCurrentProgress()
            updateStateWithHistory(workout.exercises[workoutHolder.currentExerciseIndex], 1)
        } else { finishWorkout(false) }
    }

    fun moveToPreviousExercise() = viewModelScope.launch {
        val workout = workoutHolder.workout ?: return@launch
        if (workoutHolder.currentExerciseIndex > 0) {
            workoutHolder.currentExerciseIndex--; workoutHolder.currentSetIndex = 1
            saveCurrentProgress()
            updateStateWithHistory(workout.exercises[workoutHolder.currentExerciseIndex], 1)
        }
    }

    fun restartExerciseTimer() {
        val currentState = _workoutState.value
        if (currentState is WorkoutState.InProgress) {
            val initialTime = parseExerciseTime(currentState.exercise.reps)
            if (initialTime != null) {
                startExerciseTimer(initialTime)
            }
        }
    }

    private fun parseExerciseTime(repsText: String): Int? {
        return when {
            repsText.contains("Dakika", ignoreCase = true) -> (repsText.filter { it.isDigit() }.toIntOrNull() ?: 1) * 60
            repsText.contains("Saniye", ignoreCase = true) -> repsText.filter { it.isDigit() }.toIntOrNull() ?: 30
            repsText.contains("sn", ignoreCase = true) -> repsText.filter { it.isDigit() }.toIntOrNull() ?: 30
            else -> null
        }
    }

    fun toggleExerciseTimer() {
        val intent = Intent(getApplication(), WorkoutService::class.java).apply {
            action = if (_workoutState.value.let { it is WorkoutState.InProgress && it.isTimerPaused }) "RESUME" else "PAUSE"
        }
        getApplication<Application>().startService(intent)
    }

    private fun finishWorkout(isPlanFinished: Boolean) {
        val stopIntent = Intent(getApplication(), WorkoutService::class.java).apply { action = "STOP" }
        getApplication<Application>().startService(stopIntent)

        val totalTimeMinutes = ((System.currentTimeMillis() - startTimeMillis) / 60000).toInt()
        val workoutTitle = workoutHolder.workout?.title ?: "CUSTOM"
        _workoutState.value = WorkoutState.Finished(totalTimeMinutes, sessionVolume, getDominantDifficulty(), workoutTitle)
        
        viewModelScope.launch {
            settingsManager.clearSavedWorkoutProgress()
            val summary = WorkoutSummary(workoutTitle, totalTimeMinutes, 0, System.currentTimeMillis())
            settingsManager.saveLastWorkoutSummary(summary)
            
            val todayDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            // WorkoutRecord parametre uyuşmazlığı giderildi
            workoutRecordDao.insertRecord(
                WorkoutRecord(
                    id = 0, 
                    title = workoutTitle, 
                    durationMinutes = totalTimeMinutes, 
                    date = todayDate, 
                    timestamp = System.currentTimeMillis()
                )
            )
            
            pushupRepo.insertRecord(
                ActivityRecord(
                    type = workoutTitle, 
                    value = 1.0, 
                    date = todayDate, 
                    timestamp = System.currentTimeMillis(), 
                    note = "Workout Completed"
                )
            )

            if (isPlanFinished || workoutHolder.currentDay.value >= 7) {
                settingsManager.clearActiveWorkout(); workoutHolder.clearWorkout()
            } else {
                settingsManager.saveWorkoutCurrentDay(workoutHolder.currentDay.value + 1)
                workoutHolder.startNextDay()
            }
            settingsManager.incrementCurrentStreak()
            gamificationManager.checkAndUnlockAchievements()
        }
    }

    private fun startRest(duration: Int, nextExercise: Exercise, nextSet: Int) {
        audioCoach.announceExercise("Dinlen, $duration saniye. Sıradaki: ${nextExercise.name}, $nextSet. set.")

        val workout = workoutHolder.workout
        val total = workout?.exercises?.size ?: 0
        val currentIdx = workoutHolder.currentExerciseIndex + 1
        val title = "($currentIdx/$total) Dinlenme"
        
        _workoutState.value = WorkoutState.Resting(nextExercise, nextSet, duration, duration)

        val intent = Intent(getApplication(), WorkoutService::class.java).apply {
            action = "START_TIMER"
            putExtra("seconds", duration)
            putExtra("label", title)
        }
        getApplication<Application>().startService(intent)
    }

    fun skipRest() {
        val intent = Intent(getApplication(), WorkoutService::class.java).apply { action = "STOP" }
        getApplication<Application>().startService(intent)
        onRestFinished()
    }

    fun addRestTime() {
        val currentState = _workoutState.value
        if (currentState is WorkoutState.Resting) {
            val newTime = currentState.remainingTime + 15
            _workoutState.value = currentState.copy(remainingTime = newTime, initialDuration = newTime)
            val intent = Intent(getApplication(), WorkoutService::class.java).apply {
                action = "START_TIMER"
                putExtra("seconds", newTime)
                putExtra("label", "Dinlenme")
            }
            getApplication<Application>().startService(intent)
        }
    }

    private fun onRestFinished() = viewModelScope.launch {
        val workout = workoutHolder.workout ?: return@launch
        if (workoutHolder.currentExerciseIndex >= workout.exercises.size) { finishWorkout(true); return@launch }
        updateStateWithHistory(workout.exercises[workoutHolder.currentExerciseIndex], workoutHolder.currentSetIndex)
    }

    private suspend fun updateStateWithHistory(exercise: Exercise, setIndex: Int) {
        val key = if (exercise.searchKey.isNotBlank()) exercise.searchKey else exercise.name
        val lastRecord = pushupRepo.getLastRecordForExercise(key)
        val category = getCategory(exercise.name)

        lastRecord?.let {
            _selectedBarWeight.value = it.barWeight ?: if (category == ExerciseCategory.DUMBBELL) 2.0 else if (category == ExerciseCategory.BODYWEIGHT) 0.0 else 10.0
            _selectedPlates.value = it.plates?.split(",")?.mapNotNull { p -> p.toDoubleOrNull() } ?: emptyList()
            _machineMode.value = it.machineMode ?: "Medium"
            _machineLevel.value = it.machineLevel ?: 5
            calculateTotal(category)
        } ?: run {
            _selectedBarWeight.value = when (category) { ExerciseCategory.DUMBBELL -> 2.0; ExerciseCategory.BODYWEIGHT -> 0.0; else -> 10.0 }
            _selectedPlates.value = emptyList()
            _machineMode.value = "Medium"; _machineLevel.value = 5; calculateTotal(category)
        }

        audioCoach.announceExercise("${exercise.name}. $setIndex. set. Hedef ${if (exercise.reps.contains("MAX", true)) "Maksimum tekrar" else "${exercise.reps} tekrar"}")

        val initialTime = parseExerciseTime(exercise.reps)
        _workoutState.value = WorkoutState.InProgress(exercise, category, setIndex, workoutHolder.workout!!.exercises.size, workoutHolder.currentExerciseIndex + 1, null, initialTime, false)

        val workoutTotal = workoutHolder.workout?.exercises?.size ?: 0
        val currentExIdx = workoutHolder.currentExerciseIndex + 1
        
        if (initialTime != null) {
            startExerciseTimer(initialTime)
        } else {
            updateServiceUI("($currentExIdx/$workoutTotal) ${exercise.name}", "$setIndex. Set yapılıyor")
        }
    }

    private fun startExerciseTimer(seconds: Int) {
        val total = workoutHolder.workout?.exercises?.size ?: 0
        val currentIdx = workoutHolder.currentExerciseIndex + 1
        val exName = (workoutState.value as? WorkoutState.InProgress)?.exercise?.name ?: "Antrenman"

        val intent = Intent(getApplication(), WorkoutService::class.java).apply {
            action = "START_WORKOUT_TIMER"
            putExtra("seconds", seconds)
            putExtra("label", "($currentIdx/$total) $exName")
        }
        getApplication<Application>().startService(intent)
    }

    private fun updateServiceUI(title: String, content: String) {
        val intent = Intent(getApplication(), WorkoutService::class.java).apply {
            action = "UPDATE_UI"
            putExtra("title", title)
            putExtra("content", content)
        }
        getApplication<Application>().startService(intent)
    }

    override fun onCleared() {
        serviceConnection?.let { getApplication<Application>().unbindService(it) }
        serviceJob?.cancel()
        super.onCleared()
    }
}
