package com.example.pushuptracker.ui.stats

import android.util.Log
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pushuptracker.data.HealthConnectManager
import com.example.pushuptracker.data.repo.PushupRepo
import com.example.pushuptracker.model.ActivityRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
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
    val totalCalories: Int = 0,
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

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    val exerciseList = pushupRepo.getDistinctExerciseTypes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("pushup"))

    init {
        checkHealthPermissions()
    }

    fun checkHealthPermissions() {
        viewModelScope.launch {
            try {
                _hasHealthPermissions.value = healthConnectManager.hasAllPermissions()
                if (_hasHealthPermissions.value) {
                    loadHealthData()
                }
            } catch (e: Exception) {
                Log.e("StatsViewModel", "Health Connect permission check failed", e)
                _hasHealthPermissions.value = false
            }
        }
    }

    fun loadHealthData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                // 1. Antrenman seanslarını oku (Sadece UI için)
                _healthSessions.value = healthConnectManager.readExerciseSessions()

                // 2. Son 30 günlük kalorileri oku ve DB'ye KAYDET
                val caloriesMap = healthConnectManager.readDailyCalories(
                    start = Instant.now().minusSeconds(30 * 24 * 60 * 60),
                    end = Instant.now()
                )

                caloriesMap.forEach { (date, calories) ->
                    if (calories > 0) {
                        pushupRepo.insertRecord(
                            ActivityRecord(
                                type = "calories",
                                value = calories,
                                date = date,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("StatsViewModel", "Failed to sync health data", e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun getHealthPermissions() = healthConnectManager.permissions

    val chartUiState = combine(
        selectedExercise,
        chartTimeSpan
    ) { exerciseType, _ -> exerciseType }.flatMapLatest { exerciseType ->
        pushupRepo.getRecordsByType(exerciseType).map { records ->
            val yAxisLabel = when(exerciseType) {
                "pushup" -> "Tekrar"
                "calories" -> "kcal"
                "water" -> "ml"
                else -> "Değer"
            }
            ChartUiState(records, yAxisLabel)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChartUiState())

    val overallStats = combine(
        pushupRepo.getAllRecords(),
        pushupRepo.getRecordsByType("calories")
    ) { records, calorieRecords ->
        val totalPushups = records.filter { it.type == "pushup" }.sumOf { it.value }.toInt()
        val totalWater = records.filter { it.type == "water" }.sumOf { it.value }.toInt()
        val totalCalories = calorieRecords.sumOf { it.value }.toInt()
        
        val pushupDates = records.filter { it.type == "pushup" && it.value > 0 }
            .mapNotNull { 
                try { LocalDate.parse(it.date) } catch (e: Exception) { null }
            }.distinct().sortedDescending()

        var currentStreak = 0
        if (pushupDates.isNotEmpty()) {
            val currentDate = LocalDate.now()
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
            }
        }
        OverallStats(
            totalPushups = totalPushups, 
            totalWater = totalWater, 
            totalCalories = totalCalories,
            currentStreak = currentStreak
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OverallStats())


    val weeklyChange = selectedExercise.flatMapLatest { exerciseType ->
        pushupRepo.getRecordsByType(exerciseType)
    }.map { records ->
        val today = LocalDate.now()
        val weekFields = WeekFields.of(Locale.getDefault())
        val startOfThisWeek = today.with(weekFields.dayOfWeek(), 1)
        val startOfLastWeek = startOfThisWeek.minusWeeks(1)

        val thisWeekValue = records.filter {
            try {
                val date = LocalDate.parse(it.date)
                !date.isBefore(startOfThisWeek) && date.isBefore(startOfThisWeek.plusWeeks(1))
            } catch (e: Exception) { false }
        }.sumOf { it.value }.toInt()

        val lastWeekValue = records.filter {
            try {
                val date = LocalDate.parse(it.date)
                !date.isBefore(startOfLastWeek) && date.isBefore(startOfThisWeek)
            } catch (e: Exception) { false }
        }.sumOf { it.value }.toInt()

        ChangeStats(previous = lastWeekValue, current = thisWeekValue)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChangeStats())

    val monthlyChange = selectedExercise.flatMapLatest { exerciseType ->
        pushupRepo.getRecordsByType(exerciseType)
    }.map { records ->
        val today = LocalDate.now()
        val startOfThisMonth = today.withDayOfMonth(1)
        val startOfLastMonth = startOfThisMonth.minusMonths(1)

        val thisMonthValue = records.filter {
            try {
                val date = LocalDate.parse(it.date)
                !date.isBefore(startOfThisMonth) && date.isBefore(startOfThisMonth.plusWeeks(1))
            } catch (e: Exception) { false }
        }.sumOf { it.value }.toInt()

        val lastMonthValue = records.filter {
            try {
                val date = LocalDate.parse(it.date)
                !date.isBefore(startOfLastMonth) && date.isBefore(startOfThisMonth)
            } catch (e: Exception) { false }
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
