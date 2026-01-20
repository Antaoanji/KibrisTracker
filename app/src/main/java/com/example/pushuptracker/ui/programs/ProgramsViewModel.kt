package com.example.pushuptracker.ui.programs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pushuptracker.SettingsManager
import com.example.pushuptracker.data.repo.CustomWorkoutRepository
import com.example.pushuptracker.di.WorkoutHolder
import com.example.pushuptracker.model.Workout
import com.example.pushuptracker.model.WorkoutSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ProgramType {
    MACHINE_WEIGHT, CALISTHENICS_WEIGHT
}

@HiltViewModel
class ProgramsViewModel @Inject constructor(
    private val settingsManager: SettingsManager,
    private val customWorkoutRepository: CustomWorkoutRepository,
    val workoutHolder: WorkoutHolder
) : ViewModel() {

    private val _selectedProgram = MutableStateFlow(ProgramType.MACHINE_WEIGHT)
    val selectedProgram = _selectedProgram.asStateFlow()

    // Antrenman detaylarını UI'da göstermek için State
    private val _workoutDetails = MutableStateFlow<Workout?>(null)
    val workoutDetails = _workoutDetails.asStateFlow()

    val uiState: StateFlow<ProgramScreenUiState> = combine(
        settingsManager.activeWorkoutCurrentDayFlow,
        settingsManager.lastWorkoutSummaryFlow,
        _selectedProgram
    ) { currentDay, summary, program ->
        ProgramScreenUiState(
            currentDay = currentDay,
            lastWorkoutSummary = summary,
            selectedProgram = program
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProgramScreenUiState(isLoading = false)
    )

    private val _eventState = MutableStateFlow(ProgramEventState())
    val eventState = _eventState.asStateFlow()

    fun selectProgram(type: ProgramType) {
        _selectedProgram.value = type
    }

    // Info butonuna tıklandığında veritabanından güncel hareketleri getirir
    fun loadWorkoutDetails(dayIndex: Int) {
        viewModelScope.launch {
            val programTypeStr = _selectedProgram.value.name
            val workoutId = customWorkoutRepository.ensureWorkoutExists(programTypeStr, dayIndex)
            
            customWorkoutRepository.getExercisesByWorkoutId(workoutId).collect { exercises ->
                val title = if (_selectedProgram.value == ProgramType.MACHINE_WEIGHT) "Makine + Ağırlık" else "Calisthenics + Ağırlık"
                _workoutDetails.value = Workout(title = "$title - Gün ${dayIndex + 1}", exercises = exercises)
            }
        }
    }

    // Düzenleme talebi geldiğinde ID'yi bulup navigasyonu tetiklemek için yardımcı fonksiyon
    fun onEditRequested(dayIndex: Int, onIdFound: (Long) -> Unit) {
        viewModelScope.launch {
            val workoutId = customWorkoutRepository.ensureWorkoutExists(_selectedProgram.value.name, dayIndex)
            onIdFound(workoutId)
        }
    }

    fun onDaySelected(dayIndex: Int) {
        viewModelScope.launch {
            _eventState.update { it.copy(isLoading = true) }
            val programTypeStr = _selectedProgram.value.name
            
            // Veritabanında bu günün kaydı var mı emin ol, yoksa kopyala (Sync)
            val workoutId = customWorkoutRepository.ensureWorkoutExists(programTypeStr, dayIndex)
            
            // Güncel egzersiz listesini al
            customWorkoutRepository.getExercisesByWorkoutId(workoutId).collect { exercises ->
                val title = if (_selectedProgram.value == ProgramType.MACHINE_WEIGHT) "Makine" else "Calisthenics"
                val workout = Workout(title = "$title - Gün ${dayIndex + 1}", exercises = exercises)
                
                workoutHolder.workout = workout
                workoutHolder.currentExerciseIndex = 0
                workoutHolder.currentSetIndex = 1
                
                _eventState.update { it.copy(navigateToPlayer = true, isLoading = false) }
            }
        }
    }

    fun resetProgramToDefault(dayIndex: Int) {
        viewModelScope.launch {
            customWorkoutRepository.resetToDefault(_selectedProgram.value.name, dayIndex)
            loadWorkoutDetails(dayIndex) // UI'ı güncelle
        }
    }

    fun onNavigationHandled() {
        _eventState.update { it.copy(navigateToPlayer = false) }
    }
}

data class ProgramScreenUiState(
    val isLoading: Boolean = false,
    val lastWorkoutSummary: WorkoutSummary? = null,
    val currentDay: Int = 1,
    val selectedProgram: ProgramType = ProgramType.MACHINE_WEIGHT
)

data class ProgramEventState(
    val navigateToPlayer: Boolean = false,
    val errorMessage: String? = null,
    val isLoading: Boolean = false
)
