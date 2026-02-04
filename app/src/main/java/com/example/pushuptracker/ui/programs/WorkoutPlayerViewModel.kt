package com.example.pushuptracker.ui.programs

import android.app.Application
import android.content.Context
import android.content.Intent
import android.media.AudioManager
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

    private val audioManager = application.getSystemService(Context.AUDIO_SERVICE) as AudioManager

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

    val newBadgeUnlocked = gamificationManager.newBadgeUnlocked

    private var timerJob: Job? = null
    private var exerciseTimerJob: Job? = null
    private var stateUpdateJob: Job? = null
    private var startTimeMillis: Long = 0L
    private var sessionVolume: Double = 0.0
    private val sessionDifficulties = mutableListOf<String>()

    init {
        loadStateAndStart()
    }

    private fun loadStateAndStart() = viewModelScope.launch {
        sessionVolume = 0.0
        sessionDifficulties.clear()
        
        // Önce kaydedilmiş bir ilerleme var mı kontrol et (Persistence)
        val savedState = settingsManager.savedWorkoutStateFlow.first()
        val currentWorkout = workoutHolder.workout

        if (savedState != null && currentWorkout != null && savedState.first == currentWorkout.title) {
            // Eğer kaydedilen antrenman şu an açılan ile aynıysa, oradan devam et
            workoutHolder.currentExerciseIndex = savedState.second
            workoutHolder.currentSetIndex = savedState.third
            Log.d("WorkoutPlayerVM", "Restoring progress: Ex ${savedState.second}, Set ${savedState.third}")
        }

        val workout = workoutHolder.workout
        if (workout == null || workout.exercises.isEmpty() || workoutHolder.currentExerciseIndex >= workout.exercises.size) {
            finishWorkout(true)
            return@launch
        }

        startTimeMillis = System.currentTimeMillis()
        val exercise = workout.exercises[workoutHolder.currentExerciseIndex]
        updateStateWithHistory(exercise, workoutHolder.currentSetIndex)
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
        timerJob?.cancel()
        exerciseTimerJob?.cancel()
        stateUpdateJob?.cancel()

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

        try {
            pushupRepo.insertRecord(record)
        } catch (e: Exception) {
            Log.e("WorkoutPlayerVM", "Room Error: ${e.message}")
        }

        sessionVolume += (finalWeight ?: 0.0) * calculatedReps
        sessionDifficulties.add(difficulty)

        if (workoutHolder.currentSetIndex < currentExercise.sets) {
            workoutHolder.currentSetIndex++
            saveCurrentProgress() // Set tamamlandı, kaydet
            startRest(currentExercise.restTimeSeconds, currentExercise, workoutHolder.currentSetIndex)
        } else {
            workoutHolder.currentExerciseIndex++
            if (workoutHolder.currentExerciseIndex < workout.exercises.size) {
                workoutHolder.currentSetIndex = 1
                saveCurrentProgress() // Egzersiz değişti, kaydet
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
        timerJob?.cancel(); exerciseTimerJob?.cancel(); stateUpdateJob?.cancel()
        val workout = workoutHolder.workout ?: return@launch
        if (workoutHolder.currentExerciseIndex < workout.exercises.size - 1) {
            workoutHolder.currentExerciseIndex++; workoutHolder.currentSetIndex = 1
            saveCurrentProgress()
            updateStateWithHistory(workout.exercises[workoutHolder.currentExerciseIndex], 1)
        } else { finishWorkout(false) }
    }

    fun moveToPreviousExercise() = viewModelScope.launch {
        timerJob?.cancel(); exerciseTimerJob?.cancel(); stateUpdateJob?.cancel()
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
                _workoutState.value = currentState.copy(remainingExerciseTime = initialTime, isTimerPaused = false)
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
        val currentState = _workoutState.value
        if (currentState is WorkoutState.InProgress && currentState.remainingExerciseTime != null) {
            val newPausedState = !currentState.isTimerPaused
            _workoutState.value = currentState.copy(isTimerPaused = newPausedState)
            if (newPausedState) {
                exerciseTimerJob?.cancel()
                updateNotification("Duraklatıldı: ${currentState.exercise.name}", "${currentState.remainingExerciseTime} sn kaldı")
            } else { startExerciseTimer(currentState.remainingExerciseTime!!) }
        }
    }

    private fun finishWorkout(isPlanFinished: Boolean) {
        val stopIntent = Intent(getApplication(), WorkoutService::class.java).apply { action = "STOP" }
        getApplication<Application>().startService(stopIntent)

        val totalTimeMinutes = ((System.currentTimeMillis() - startTimeMillis) / 60000).toInt()
        val workoutTitle = workoutHolder.workout?.title ?: "CUSTOM"
        _workoutState.value = WorkoutState.Finished(totalTimeMinutes, sessionVolume, getDominantDifficulty(), workoutTitle)
        
        viewModelScope.launch {
            // Antrenman bitti, ilerlemeyi temizle
            settingsManager.clearSavedWorkoutProgress()

            val summary = WorkoutSummary(workoutTitle, totalTimeMinutes, 0, System.currentTimeMillis())
            settingsManager.saveLastWorkoutSummary(summary)
            
            val todayDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            workoutRecordDao.insertRecord(
                WorkoutRecord(
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
        timerJob?.cancel(); exerciseTimerJob?.cancel()
        audioCoach.announceExercise("Dinlen, $duration saniye. Sıradaki: ${nextExercise.name}, $nextSet. set.")

        val workout = workoutHolder.workout
        val total = workout?.exercises?.size ?: 0
        val currentIdx = workoutHolder.currentExerciseIndex + 1
        val title = "($currentIdx/$total) Dinlenme: $duration sn"
        val content = "Sıradaki: ${nextExercise.name} ($nextSet. Set)"
        
        updateNotification(title, content)
        
        timerJob = viewModelScope.launch {
            var remainingTime = duration
            while (remainingTime > 0) {
                _workoutState.value = WorkoutState.Resting(nextExercise, nextSet, remainingTime, duration)
                val liveTitle = "($currentIdx/$total) Dinlenme: $remainingTime sn"
                updateNotification(liveTitle, content)
                if (remainingTime == 3) { launch { audioCoach.playCountdown() } }
                delay(1000); remainingTime--
            }
            onRestFinished()
        }
    }

    fun skipRest() { timerJob?.cancel(); audioCoach.stop(); onRestFinished() }
    fun addRestTime() {
        val currentState = _workoutState.value
        if (currentState is WorkoutState.Resting) {
            timerJob?.cancel(); audioCoach.stop()
            startRest(currentState.remainingTime + 15, currentState.nextExercise, currentState.nextSet)
        }
    }

    private fun onRestFinished() = viewModelScope.launch {
        val workout = workoutHolder.workout ?: return@launch
        if (workoutHolder.currentExerciseIndex >= workout.exercises.size) { finishWorkout(true); return@launch }
        updateStateWithHistory(workout.exercises[workoutHolder.currentExerciseIndex], workoutHolder.currentSetIndex)
    }

    private suspend fun updateStateWithHistory(exercise: Exercise, setIndex: Int) {
        stateUpdateJob?.cancel()
        val key = if (exercise.searchKey.isNotBlank()) exercise.searchKey else exercise.name
        val lastRecord = pushupRepo.getLastRecordForExercise(key)
        val category = getCategory(exercise.name)

        lastRecord?.let {
            _selectedBarWeight.value = it.barWeight ?: if (category == ExerciseCategory.DUMBBELL) 2.0 else if (category == ExerciseCategory.BODYWEIGHT) 0.0 else 10.0
            val plateList = it.plates?.split(",")?.mapNotNull { p -> p.toDoubleOrNull() } ?: emptyList()
            _selectedPlates.value = plateList
            _machineMode.value = it.machineMode ?: "Medium"
            _machineLevel.value = it.machineLevel ?: 5
            calculateTotal(category)
        } ?: run {
            _selectedBarWeight.value = when (category) {
                ExerciseCategory.DUMBBELL -> 2.0
                ExerciseCategory.BODYWEIGHT -> 0.0
                else -> 10.0
            }
            _selectedPlates.value = emptyList()
            _machineMode.value = "Medium"
            _machineLevel.value = 5
            calculateTotal(category)
        }

        val coachSuggestion = lastRecord?.let {
            when (it.difficulty) {
                "easy" -> "Geçen sefer kolaydı. Seviyeyi biraz artırabilirsin! 🚀"
                "medium" -> "Formun iyiydi. Tekniğini koru. 👍"
                "hard" -> "Geçen sefer zorlandın. Aynı seviyede kal. 🛡️"
                else -> null
            }
        } ?: "İlk kez yapıyorsun, başarılar!"

        val repsAnons = if (exercise.reps.contains("MAX", true)) "Maksimum tekrar" else "${exercise.reps} tekrar"

        val weightAnons = if (setIndex == 1) {
            lastRecord?.let {
                if (category == ExerciseCategory.MACHINE) "Geçen sefer ${it.machineMode} modunda seviye ${it.machineLevel} yapmıştın."
                else if ((it.weightUsed ?: 0.0) > 0) "Geçen sefer ${it.weightUsed} kilogram ile yapmıştın."
                else null
            } ?: ""
        } else ""

        audioCoach.announceExercise("${exercise.name}. $setIndex. set. Hedef $repsAnons. $weightAnons")

        val initialTime = parseExerciseTime(exercise.reps)

        _workoutState.value = WorkoutState.InProgress(exercise, category, setIndex, workoutHolder.workout!!.exercises.size, workoutHolder.currentExerciseIndex + 1,
            coachSuggestion, initialTime, false
        )

        val workoutTotal = workoutHolder.workout?.exercises?.size ?: 0
        val currentExIdx = workoutHolder.currentExerciseIndex + 1
        
        if (initialTime != null) {
            updateNotification("($currentExIdx/$workoutTotal) ${exercise.name}", "Hazırlanılıyor...")
            stateUpdateJob = viewModelScope.launch { delay(2500); val cur = _workoutState.value; if (cur is WorkoutState.InProgress) { _workoutState.value = cur.copy(); startExerciseTimer(initialTime) } }
        } else {
            updateNotification("($currentExIdx/$workoutTotal) ${exercise.name}", "$setIndex. Set yapılıyor")
        }
    }

    private fun startExerciseTimer(seconds: Int) {
        exerciseTimerJob?.cancel()
        exerciseTimerJob = viewModelScope.launch {
            var time = seconds
            val total = workoutHolder.workout?.exercises?.size ?: 0
            val currentIdx = workoutHolder.currentExerciseIndex + 1
            val exName = _workoutState.value.let { if (it is WorkoutState.InProgress) it.exercise.name else "Antrenman" }
            
            while (time > 0) {
                val cur = _workoutState.value
                if (cur is WorkoutState.InProgress) {
                    if (cur.isTimerPaused) break
                    _workoutState.value = cur.copy(remainingExerciseTime = time)
                    updateNotification("($currentIdx/$total) $exName", "$time sn kaldı")
                    if (time == 3) { launch { audioCoach.playCountdown() } }
                }
                delay(1000); time--
            }
            if (time <= 0) { audioCoach.announceExercise("Süre bitti!"); delay(1000); onSetFinished("medium", "Otomatik Tamamlandı", null) }
        }
    }

    private fun updateNotification(title: String, content: String) {
        val intent = Intent(getApplication(), WorkoutService::class.java).apply {
            putExtra("title", title)
            putExtra("content", content)
        }
        getApplication<Application>().startForegroundService(intent)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        exerciseTimerJob?.cancel()
        stateUpdateJob?.cancel()
    }
}
