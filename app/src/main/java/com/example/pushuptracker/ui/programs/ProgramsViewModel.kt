package com.example.pushuptracker.ui.programs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pushuptracker.SettingsManager
import com.example.pushuptracker.data.HealthConnectManager
import com.example.pushuptracker.data.repo.CustomWorkoutRepository
import com.example.pushuptracker.data.repo.PushupRepo
import com.example.pushuptracker.di.WorkoutHolder
import com.example.pushuptracker.model.Workout
import com.example.pushuptracker.model.WorkoutSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import javax.inject.Inject

enum class ProgramType {
    MACHINE_WEIGHT, CALISTHENICS_WEIGHT
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProgramsViewModel @Inject constructor(
    private val settingsManager: SettingsManager,
    private val customWorkoutRepository: CustomWorkoutRepository,
    private val pushupRepo: PushupRepo,
    private val healthConnectManager: HealthConnectManager,
    val workoutHolder: WorkoutHolder
) : ViewModel() {

    private val _selectedProgram = MutableStateFlow(ProgramType.MACHINE_WEIGHT)
    val selectedProgram = _selectedProgram.asStateFlow()

    private val _workoutDetails = MutableStateFlow<Workout?>(null)
    val workoutDetails = _workoutDetails.asStateFlow()

    // Haftalık tamamlanan antrenmanları takip et
    private val completedWorkoutsThisWeek: Flow<Set<String>> = pushupRepo.getAllRecords().map { records ->
        val monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        records.filter { 
            try {
                val date = LocalDate.parse(it.date)
                !date.isBefore(monday) 
            } catch (e: Exception) { false }
        }.map { it.type.uppercase(Locale.getDefault()) }.toSet() // Hepsi büyük harf yapıldı
    }

    // Health Connect'ten gelen son antrenman kalorisini tutan akış
    private val lastWorkoutRealCalories = settingsManager.lastWorkoutSummaryFlow.flatMapLatest { summary ->
        if (summary == null) return@flatMapLatest flowOf(0)
        
        flow {
            if (healthConnectManager.hasAllPermissions()) {
                val endTime = Instant.ofEpochMilli(summary.timestamp)
                val startTime = endTime.minusSeconds(summary.totalTimeMinutes.toLong() * 60)
                val calories = healthConnectManager.readTotalCalories(startTime, endTime)
                emit(calories.toInt())
            } else {
                emit(summary.caloriesBurned)
            }
        }
    }

    val uiState: StateFlow<ProgramScreenUiState> = combine(
        settingsManager.activeWorkoutCurrentDayFlow,
        settingsManager.lastWorkoutSummaryFlow,
        _selectedProgram,
        completedWorkoutsThisWeek,
        lastWorkoutRealCalories
    ) { currentDay, summary, program, completedTitles, realCalories ->
        ProgramScreenUiState(
            currentDay = currentDay,
            lastWorkoutSummary = summary?.copy(caloriesBurned = realCalories),
            selectedProgram = program,
            completedWorkoutTypes = completedTitles // Artık büyük harf setleri geliyor
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

    fun handleVoiceCommand(programType: String?, dayName: String?) {
        viewModelScope.launch {
            val type = when {
                programType?.contains("calisthenics", ignoreCase = true) == true || 
                programType?.contains("kendi", ignoreCase = true) == true -> ProgramType.CALISTHENICS_WEIGHT
                else -> ProgramType.MACHINE_WEIGHT
            }
            _selectedProgram.value = type

            val dayIndex = when {
                dayName?.contains("pazartesi", ignoreCase = true) == true -> 0
                dayName?.contains("salı", ignoreCase = true) == true -> 1
                dayName?.contains("çarşamba", ignoreCase = true) == true -> 2
                dayName?.contains("cuma", ignoreCase = true) == true -> 3
                dayName?.contains("cumartesi", ignoreCase = true) == true -> 4
                dayName?.contains("bugün", ignoreCase = true) == true -> getTodayIndex()
                else -> getTodayIndex()
            }

            if (dayIndex != -1) {
                onDaySelected(dayIndex)
            }
        }
    }

    private fun getTodayIndex(): Int {
        return when (LocalDate.now().dayOfWeek) {
            DayOfWeek.MONDAY -> 0
            DayOfWeek.TUESDAY -> 1
            DayOfWeek.WEDNESDAY -> 2
            DayOfWeek.FRIDAY -> 3
            DayOfWeek.SATURDAY -> 4
            else -> -1
        }
    }

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
            
            val workoutId = customWorkoutRepository.ensureWorkoutExists(programTypeStr, dayIndex)
            
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
            loadWorkoutDetails(dayIndex)
        }
    }

    fun resetPrograms(types: List<ProgramType>) {
        viewModelScope.launch {
            _eventState.update { it.copy(isLoading = true) }
            types.forEach { type ->
                customWorkoutRepository.resetAllToDefault(type.name)
            }
            _eventState.update { it.copy(isLoading = false) }
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
    val selectedProgram: ProgramType = ProgramType.MACHINE_WEIGHT,
    val completedWorkoutTypes: Set<String> = emptySet()
)

data class ProgramEventState(
    val navigateToPlayer: Boolean = false,
    val errorMessage: String? = null,
    val isLoading: Boolean = false
)
