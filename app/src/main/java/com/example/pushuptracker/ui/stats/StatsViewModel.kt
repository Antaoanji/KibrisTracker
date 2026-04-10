package com.example.pushuptracker.ui.stats

import android.util.Log
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pushuptracker.data.HealthConnectManager
import com.example.pushuptracker.data.local.WorkoutRecordDao
import com.example.pushuptracker.data.repo.PushupRepo
import com.example.pushuptracker.gamification.StreakManager
import com.example.pushuptracker.model.ActivityRecord
import com.example.pushuptracker.model.WorkoutRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
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
    val totalCalories: Int = 0,
    val weeklyCalories: Int = 0,
    val totalWorkouts: Int = 0,
    val totalWalkingMinutes: Int = 0,
    val totalLymphaticCount: Int = 0,
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
    private val workoutRecordDao: WorkoutRecordDao,
    private val healthConnectManager: HealthConnectManager,
    private val streakManager: StreakManager
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

    // Heatmap verisi - Arka planda işleniyor (Performans Düzeltmesi)
    val heatmapData: StateFlow<Map<String, String>> = combine(
        workoutRecordDao.getAllRecords(),
        pushupRepo.getAllRecords()
    ) { workoutRecords, activityRecords ->
        val dataMap = mutableMapOf<String, String>()

        // 1. Önce ActivityRecords üzerinden genel antrenmanları tara
        activityRecords.forEach { record ->
            val type = record.type.uppercase()
            if (type.contains("PUSH") || type.contains("PULL") || type.contains("LEGS") ||
                type.contains("UPPER") || type.contains("LOWER") || type == "WORKOUT_COMPLETED") {
                dataMap[record.date] = record.type 
            }
        }

        // 2. WorkoutRecords tablosundaki özel başlıklar her zaman önceliklidir
        workoutRecords.forEach { record ->
            dataMap[record.date] = record.title
        }

        dataMap
    }
    .flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

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
                // 1. Egzersiz seanslarını her zaman güncel tut (Küçük veri)
                _healthSessions.value = healthConnectManager.readExerciseSessions()

                // 2. Akıllı Senkronizasyon: En son hangi günün kalorisini kaydetmiştik?
                val lastSavedDateStr = pushupRepo.getLastCalorieRecordDate()
                val startTime = if (lastSavedDateStr != null) {
                    try {
                        // Kaydedilen son günün bir sonrasından başla
                        LocalDate.parse(lastSavedDateStr).plusDays(1)
                            .atStartOfDay(ZoneId.systemDefault()).toInstant()
                    } catch (e: Exception) {
                        Instant.now().minusSeconds(30 * 24 * 60 * 60)
                    }
                } else {
                    Instant.now().minusSeconds(30 * 24 * 60 * 60)
                }

                val endTime = Instant.now()

                // Sadece çekilmesi gereken bir aralık varsa Health Connect'e git
                if (startTime.isBefore(endTime)) {
                    val caloriesMap = healthConnectManager.readDailyCalories(start = startTime, end = endTime)

                    val recordsToInsert = caloriesMap.map { (date, calories) ->
                        ActivityRecord(
                            type = "calories",
                            value = calories,
                            date = date,
                            timestamp = System.currentTimeMillis()
                        )
                    }.filter { it.value > 0 }

                    if (recordsToInsert.isNotEmpty()) {
                        pushupRepo.insertRecords(recordsToInsert)
                        Log.d("StatsViewModel", "${recordsToInsert.size} yeni kalori kaydı senkronize edildi.")
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
                "walking" -> "dk"
                "lymphatic" -> "Tekrar"
                else -> "Değer"
            }
            ChartUiState(records, yAxisLabel)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChartUiState())

    val overallStats = combine(
        pushupRepo.getAllRecords(),
        pushupRepo.getRecordsByType("calories"),
        workoutRecordDao.getAllRecords(),
        streakManager.getStreaksFlow()
    ) { records, calorieRecords, workoutRecords, streaks ->
        val totalPushups = records.filter { it.type == "pushup" }.sumOf { it.value }.toInt()
        val totalWater = records.filter { it.type == "water" }.sumOf { it.value }.toInt()
        
        // Kalori Hesaplamaları
        val totalCalories = calorieRecords.sumOf { it.value }.toInt()
        
        val today = LocalDate.now()
        val weekFields = WeekFields.of(Locale.getDefault())
        val startOfThisWeek = today.with(weekFields.dayOfWeek(), 1)
        
        val weeklyCalories = calorieRecords.filter {
            val date = try { LocalDate.parse(it.date) } catch (e: Exception) { null }
            date != null && !date.isBefore(startOfThisWeek)
        }.sumOf { it.value }.toInt()
        
        val totalWorkouts = workoutRecords.size
        val totalWalkingMinutes = records.filter { it.type == "walking" }.sumOf { it.value }.toInt()
        val totalLymphaticCount = records.filter { it.type == "lymphatic" }.size

        val currentStreak = streaks.find { it.type == com.example.pushuptracker.model.Streak.Type.WORKOUT }?.count ?: 0

        OverallStats(
            totalPushups = totalPushups, 
            totalWater = totalWater, 
            totalCalories = totalCalories,
            weeklyCalories = weeklyCalories,
            totalWorkouts = totalWorkouts,
            totalWalkingMinutes = totalWalkingMinutes,
            totalLymphaticCount = totalLymphaticCount,
            currentStreak = currentStreak
        )
    }
    .flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OverallStats())

    val weeklyChange = workoutRecordDao.getAllRecords().map { records ->
        val today = LocalDate.now()
        val weekFields = WeekFields.of(Locale.getDefault())
        val startOfThisWeek = today.with(weekFields.dayOfWeek(), 1)
        val startOfLastWeek = startOfThisWeek.minusWeeks(1)

        val thisWeekCount = records.filter {
            val date = try { LocalDate.parse(it.date) } catch (e: Exception) { null }
            date != null && !date.isBefore(startOfThisWeek) && date.isBefore(startOfThisWeek.plusWeeks(1))
        }.size

        val lastWeekCount = records.filter {
            val date = try { LocalDate.parse(it.date) } catch (e: Exception) { null }
            date != null && !date.isBefore(startOfLastWeek) && date.isBefore(startOfThisWeek)
        }.size

        ChangeStats(previous = lastWeekCount, current = thisWeekCount)
    }
    .flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChangeStats())

    val monthlyChange = workoutRecordDao.getAllRecords().map { records ->
        val today = LocalDate.now()
        val startOfThisMonth = today.withDayOfMonth(1)
        val startOfLastMonth = startOfThisMonth.minusMonths(1)

        val thisMonthCount = records.filter {
            val date = try { LocalDate.parse(it.date) } catch (e: Exception) { null }
            date != null && !date.isBefore(startOfThisMonth) && date.isBefore(startOfThisMonth.plusWeeks(1))
        }.size

        val lastMonthCount = records.filter {
            val date = try { LocalDate.parse(it.date) } catch (e: Exception) { null }
            date != null && !date.isBefore(startOfLastMonth) && date.isBefore(startOfThisMonth)
        }.size

        ChangeStats(previous = lastMonthCount, current = thisMonthCount)
    }
    .flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChangeStats())


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
