package com.example.pushuptracker.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pushuptracker.data.repo.PushupRepo
import com.example.pushuptracker.data.repo.WaterRepo
import com.example.pushuptracker.model.ActivityRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val pushupRepo: PushupRepo,
    private val waterRepo: WaterRepo
) : ViewModel() {

    private val _chartTimeSpan = MutableStateFlow(ChartTimeSpan.WEEK)
    val chartTimeSpan = _chartTimeSpan.asStateFlow()

    private val _chartType = MutableStateFlow(ChartType.BAR)
    val chartType = _chartType.asStateFlow()

    private val _editDialogState = MutableStateFlow<ActivityRecord?>(null)
    val editDialogState = _editDialogState.asStateFlow()

    val chartRecords: Flow<List<ActivityRecord>> = combine(
        chartTimeSpan, pushupRepo.getAllRecords()
    ) { timeSpan, records ->
        val today = LocalDate.now()
        val startDate = when (timeSpan) {
            ChartTimeSpan.WEEK -> today.minusDays(6)
            ChartTimeSpan.YEAR -> today.withDayOfYear(1)
        }
        records.filter { !LocalDate.parse(it.date).isBefore(startDate) }
    }

    val overallStats: StateFlow<OverallStats> = combine(
        pushupRepo.getAllRecords(),
        waterRepo.getAllRecords(),
    ) { pushups, water ->
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        val pushupsByDate = pushups.associate { it.date to it.value.toInt() }

        val totalPushups = pushups.sumOf { it.value }.toInt()
        val totalWater = water.sumOf { it.value }.toInt()
        val todayPushups = pushupsByDate[today.toString()] ?: 0
        val yesterdayPushups = pushupsByDate[yesterday.toString()] ?: 0

        val currentStreak = calculateCurrentStreak(pushupsByDate.keys)

        OverallStats(
            totalPushups = totalPushups,
            totalWater = totalWater,
            currentStreak = currentStreak,
            todayPushups = todayPushups,
            yesterdayPushups = yesterdayPushups,
            thisWeekTotalPushups = 0, // Placeholder
            lastWeekTotalPushups = 0, // Placeholder
            thisMonthTotalPushups = 0, // Placeholder
            lastMonthTotalPushups = 0 // Placeholder
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OverallStats())

    private fun calculateCurrentStreak(dates: Set<String>): Int {
        if (dates.isEmpty()) return 0

        var streak = 0
        var currentDate = LocalDate.now()

        // Start streak calculation from today or yesterday
        if (dates.contains(currentDate.toString())) {
            streak++
        } else {
            currentDate = currentDate.minusDays(1)
            if (dates.contains(currentDate.toString())) {
                streak++
            } else {
                return 0 // No streak if neither today nor yesterday has a record
            }
        }

        // Continue counting backwards
        while (dates.contains(currentDate.minusDays(1).toString())) {
            streak++
            currentDate = currentDate.minusDays(1)
        }
        return streak
    }


    fun setChartTimeSpan(timeSpan: ChartTimeSpan) {
        _chartTimeSpan.value = timeSpan
    }

    fun setChartType(chartType: ChartType) {
        _chartType.value = chartType
    }

    fun onChartEntrySelected(date: LocalDate) {
        viewModelScope.launch {
            val records = chartRecords.first()
            _editDialogState.value = records.find { it.date == date.toString() }
                ?: ActivityRecord("pushup", 0.0, date.toString()) // Create empty record for adding new one
        }
    }

    fun onDismissEditDialog() {
        _editDialogState.value = null
    }

    fun updateRecordForDate(date: String, newValue: Double) {
        viewModelScope.launch {
            if (newValue > 0) {
                pushupRepo.updatePushupsForDate(date, newValue.toInt())
            } else {
                pushupRepo.deletePushupsForDate(date)
            }
            onDismissEditDialog()
        }
    }
}

enum class ChartTimeSpan { WEEK, YEAR }
enum class ChartType { LINE, BAR }

data class OverallStats(
    val totalPushups: Int = 0,
    val totalWater: Int = 0,
    val currentStreak: Int = 0,
    val todayPushups: Int = 0,
    val yesterdayPushups: Int = 0,
    val thisWeekTotalPushups: Int = 0,
    val lastWeekTotalPushups: Int = 0,
    val thisMonthTotalPushups: Int = 0,
    val lastMonthTotalPushups: Int = 0
)
