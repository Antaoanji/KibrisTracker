package com.example.pushuptracker.ui.programs

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pushuptracker.SettingsManager
import com.example.pushuptracker.data.WorkoutData
import com.example.pushuptracker.data.repo.WorkoutPlanRepository
import com.example.pushuptracker.di.WorkoutHolder
import com.example.pushuptracker.model.Exercise
import com.example.pushuptracker.model.Workout
import com.example.pushuptracker.model.WorkoutSummary
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProgramsViewModel @Inject constructor(
    private val workoutPlanRepository: WorkoutPlanRepository,
    private val settingsManager: SettingsManager,
    val workoutHolder: WorkoutHolder
) : ViewModel() {

    private val gson = Gson()

    val uiState: StateFlow<ProgramScreenUiState> = combine(
        settingsManager.activeWorkoutPlanFlow,
        settingsManager.activeWorkoutCurrentDayFlow,
        settingsManager.lastWorkoutSummaryFlow
    ) { planJson, currentDay, summary ->

        val parsedWorkout = if (planJson != null) {
            parseWeeklyWorkoutJson(planJson)
        } else null

        // Update the holder with the parsed structure
        workoutHolder.structuredWorkout.value = parsedWorkout

        ProgramScreenUiState(
            structuredWorkout = parsedWorkout,
            currentDay = currentDay,
            lastWorkoutSummary = summary
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProgramScreenUiState(isLoading = true)
    )

    private val _eventState = MutableStateFlow(ProgramEventState())
    val eventState = _eventState.asStateFlow()

    fun onStartWorkoutClicked() {
        viewModelScope.launch {
            val currentDayIndex = uiState.value.currentDay - 1
            val pplulDay = WorkoutData.pplulProgram.getOrNull(currentDayIndex)
            
            if (pplulDay != null) {
                // Her zaman doğrudan WorkoutData'dan o günün antrenmanını alıyoruz
                // Bu, JSON ayrıştırma hatalarından etkilenmememizi sağlar.
                workoutHolder.workout = pplulDay
                workoutHolder.currentExerciseIndex = 0
                workoutHolder.currentSetIndex = 1
                
                onPlanDetailsDismissed()
            } else {
                // Eğer PPLUL içinde o gün yoksa (Örn: OFF günleri)
                Log.d("ProgramsViewModel", "Bu gün için antrenman bulunamadı.")
            }
        }
    }

    fun showEquipmentDialog() {
        generatePplulPlan()
    }

    fun dismissEquipmentDialog() {
        _eventState.update { it.copy(showEquipmentDialog = false) }
    }

    fun onPlanDetailsDismissed() {
        _eventState.update { it.copy(displayingPlanDetails = false) }
    }

    fun clearGeneratedWorkout() {
        viewModelScope.launch {
            settingsManager.clearActiveWorkout()
            workoutHolder.clearWorkout()
            _eventState.update { it.copy(displayingPlanDetails = false) }
        }
    }

    private fun generatePplulPlan() {
        viewModelScope.launch {
            _eventState.update { it.copy(isLoading = true) }
            
            val pplul = WorkoutData.pplulProgram
            val jsonPlan = JsonWorkoutPlan(
                programTitle = "Kibris PPLUL (5 Gün)",
                days = pplul.map { workout ->
                    JsonDay(
                        dayName = workout.title,
                        exercises = workout.exercises.map { ex ->
                            JsonExercise(
                                name = ex.name,
                                searchKey = ex.searchKey,
                                sets = ex.sets,
                                reps = ex.reps,
                                rest = ex.restTimeSeconds,
                                met = ex.metValue,
                                description = ex.description
                            )
                        }
                    )
                }
            )

            val jsonString = gson.toJson(jsonPlan)
            settingsManager.saveActiveWorkout(jsonString, 1)
            
            _eventState.update { 
                it.copy(isLoading = false, displayingPlanDetails = true) 
            }
        }
    }

    fun generateWeeklyWorkoutPlan(equipments: List<String>) {
        generatePplulPlan()
    }

    private suspend fun parseWeeklyWorkoutJson(jsonString: String): Workout? {
        return withContext(Dispatchers.Default) {
            try {
                val cleanJson = jsonString.replace("```json", "").replace("```", "").trim()
                val plan = gson.fromJson(cleanJson, JsonWorkoutPlan::class.java) ?: return@withContext null
                val allExercises = mutableListOf<Exercise>()

                plan.days.forEach { day ->
                    // Başlık satırı
                    allExercises.add(Exercise(
                        name = day.dayName,
                        sets = 0,
                        reps = "",
                        restTimeSeconds = 0,
                        description = "",
                        imageUrl = ""
                    ))

                    day.exercises.forEach { ex ->
                        allExercises.add(Exercise(
                            name = ex.name,
                            searchKey = ex.searchKey,
                            sets = ex.sets,
                            reps = ex.reps,
                            restTimeSeconds = ex.rest,
                            description = ex.description,
                            metValue = ex.met,
                            imageUrl = ""
                        ))
                    }
                }
                Workout(title = plan.programTitle, exercises = allExercises)
            } catch (e: Exception) {
                Log.e("ProgramsViewModel", "JSON Parse Error: ${e.message}", e)
                null
            }
        }
    }
}

data class JsonWorkoutPlan(val programTitle: String, val days: List<JsonDay>)
data class JsonDay(val dayName: String, val exercises: List<JsonExercise>)
data class JsonExercise(
    val name: String,
    val searchKey: String,
    val sets: Int,
    val reps: String,
    val rest: Int,
    val met: Double,
    val description: String
)

data class ProgramScreenUiState(
    val isLoading: Boolean = false,
    val structuredWorkout: Workout? = null,
    val lastWorkoutSummary: WorkoutSummary? = null,
    val currentDay: Int = 1
)

data class ProgramEventState(
    val showEquipmentDialog: Boolean = false,
    val displayingPlanDetails: Boolean = false,
    val errorMessage: String? = null,
    val isLoading: Boolean = false
)
