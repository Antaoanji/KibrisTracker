package com.example.pushuptracker.ui.stats

import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pushuptracker.data.HealthConnectManager
import com.example.pushuptracker.data.repo.PushupRepo
import com.example.pushuptracker.model.ActivityRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject

enum class ChartTimeSpan { WEEK, YEAR }
enum class ChartType { LINE, BAR }

data class ChartUiState(
    val records: List<ActivityRecord> = emptyList(),
    val yAxisLabel: String = "Tekrar"
)

data class OverallStats(
    val totalPushups: Int = 0,
    val totalWater: Int = 0,
    val currentStreak: Int = 0
)

data class ChangeStats(
    val previous: Int = 0,
    val current: Int = 0
) {
    val change: Int
        get() = current - previous
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StatsViewModel @Inject constructor(
    private val pushupRepo: PushupRepo,
    private val healthConnectManager: HealthConnectManager
) : ViewModel() {

    val chartTimeSpan = MutableStateFlow(ChartTimeSpan.WEEK)
    val chartType = MutableStateFlow(ChartType.BAR)
    val editDialogState = MutableStateFlow<ActivityRecord?>(null)

    private val _selectedExercise = MutableStateFlow("pushup")
    val selectedExercise = _selectedExercise.asStateFlow()

    private val _healthSessions = MutableStateFlow<List<ExerciseSessionRecord>>(emptyList())
    val healthSessions = _healthSessions.asStateFlow()

    private val _hasHealthPermissions = MutableStateFlow(false)
    val hasHealthPermissions = _hasHealthPermissions.asStateFlow()

    val exerciseList = pushupRepo.getDistinctExerciseTypes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("pushup"))

    init {
        checkHealthPermissions()
    }

    fun checkHealthPermissions() {
        viewModelScope.launch {
            _hasHealthPermissions.value = healthConnectManager.hasAllPermissions()
            if (_hasHealthPermissions.value) {
                loadHealthSessions()
            }
        }
    }

    fun loadHealthSessions() {
        viewModelScope.launch {
            _healthSessions.value = healthConnectManager.readExerciseSessions()
        }
    }

    fun getHealthPermissions() = healthConnectManager.permissions

    val chartUiState = combine(
        selectedExercise,
        chartTimeSpan
    ) { exerciseType, timeSpan ->
        Pair(exerciseType, timeSpan)
    }.flatMapLatest { (exerciseType, timeSpan) ->
        pushupRepo.getAllRecordsForType(exerciseType).map {
            val yAxisLabel = if (exerciseType == "pushup") "Tekrar" else "Ağırlık (kg)"
            ChartUiState(it, yAxisLabel)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChartUiState())

    val overallStats = pushupRepo.getAllRecords().map { records ->
        val totalPushups = records.filter { it.type == "pushup" }.sumOf { it.value }.toInt()
        val totalWater = records.filter { it.type == "water" }.sumOf { it.value }.toInt()
        val pushupDates = records.filter { it.type == "pushup" && it.value > 0 }
            .map { LocalDate.parse(it.date) }.distinct().sortedDescending()

        var currentStreak = 0
        if (pushupDates.isNotEmpty()) {
            var currentDate = LocalDate.now()
            if (pushupDates.first().isEqual(currentDate) || pushupDates.first().isEqual(currentDate.minusDays(1))) {
                currentStreak = 1
                var lastDate = pushupDates.first()

                for (i in 1 until pushupDates.size) {
                    val date = pushupDates[i]
                    if (lastDate.minusDays(1).isEqual(date)) {
                        currentStreak++
                        lastDate = date
                    } else {
                        break
                    }
                }
                if (!pushupDates.contains(LocalDate.now()) && !pushupDates.contains(LocalDate.now().minusDays(1))) {
                    currentStreak = 0
                }
            }
        }
        OverallStats(totalPushups = totalPushups, totalWater = totalWater, currentStreak = currentStreak)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OverallStats())


    val weeklyChange = selectedExercise.flatMapLatest { exerciseType ->
        pushupRepo.getAllRecordsForType(exerciseType)
    }.map { records ->
        val today = LocalDate.now()
        val weekFields = WeekFields.of(Locale.getDefault())
        val startOfThisWeek = today.with(weekFields.dayOfWeek(), 1)
        val startOfLastWeek = startOfThisWeek.minusWeeks(1)

        val thisWeekValue = records.filter {
            val date = LocalDate.parse(it.date)
            !date.isBefore(startOfThisWeek) && date.isBefore(startOfThisWeek.plusWeeks(1))
        }.sumOf { it.value }.toInt()

        val lastWeekValue = records.filter {
            val date = LocalDate.parse(it.date)
            !date.isBefore(startOfLastWeek) && date.isBefore(startOfThisWeek)
        }.sumOf { it.value }.toInt()

        ChangeStats(previous = lastWeekValue, current = thisWeekValue)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChangeStats())

    val monthlyChange = selectedExercise.flatMapLatest { exerciseType ->
        pushupRepo.getAllRecordsForType(exerciseType)
    }.map { records ->
        val today = LocalDate.now()
        val startOfThisMonth = today.withDayOfMonth(1)
        val startOfLastMonth = startOfThisMonth.minusMonths(1)

        val thisMonthValue = records.filter {
            val date = LocalDate.parse(it.date)
            !date.isBefore(startOfThisMonth) && date.isBefore(startOfThisMonth.plusMonths(1))
        }.sumOf { it.value }.toInt()

        val lastMonthValue = records.filter {
            val date = LocalDate.parse(it.date)
            !date.isBefore(startOfLastMonth) && date.isBefore(startOfThisMonth)
        }.sumOf { it.value }.toInt()

        ChangeStats(previous = lastMonthValue, current = thisMonthValue)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChangeStats())


    fun onExerciseSelected(exercise: String) {
        _selectedExercise.value = exercise
    }

    fun setChartTimeSpan(timeSpan: ChartTimeSpan) {
        chartTimeSpan.value = timeSpan
    }

    fun setChartType(type: ChartType) {
        chartType.value = type
    }

    fun onChartEntrySelected(record: ActivityRecord) {
        if (record.value > 0) editDialogState.value = record
    }

    fun onDismissEditDialog() {
        editDialogState.value = null
    }

    fun updateRecordForDate(date: String, newValue: Double) {
        viewModelScope.launch {
            if (newValue > 0) {
                val record =
                    ActivityRecord(type = selectedExercise.value, value = newValue, date = date)
                pushupRepo.insertRecord(record)
            } else {
                pushupRepo.deleteRecordForDateAndType(selectedExercise.value, date)
            }
            onDismissEditDialog()
        }
    }
}
