package com.example.pushuptracker.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pushuptracker.SettingsManager
import com.example.pushuptracker.model.Activities
import com.example.pushuptracker.model.ActivityRecord
import com.example.pushuptracker.room.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

data class OverallStats(
    val totalPushups: Int = 0,
    val totalWater: Int = 0,
    val currentStreak: Int = 0,
    val todayPushups: Int = 0,
    val yesterdayPushups: Int = 0,
    val thisWeekTotalPushups: Int = 0,
    val lastWeekTotalPushups: Int = 0,
    val thisMonthTotalPushups: Int = 0,
    val lastMonthTotalPushups: Int = 0,
)

enum class ChartTimeSpan { WEEK, YEAR }
enum class ChartType { LINE, BAR }

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.get(application).dao()
    private val settingsManager = SettingsManager(application)
    private val today = LocalDate.now()

    // --- Chart display options ---
    private val _chartTimeSpan = MutableStateFlow(ChartTimeSpan.WEEK)
    val chartTimeSpan: StateFlow<ChartTimeSpan> = _chartTimeSpan.asStateFlow()

    private val _chartType = MutableStateFlow(ChartType.LINE)
    val chartType: StateFlow<ChartType> = _chartType.asStateFlow()

    // --- Edit Dialog State ---
    private val _editDialogState = MutableStateFlow<ActivityRecord?>(null)
    val editDialogState: StateFlow<ActivityRecord?> = _editDialogState.asStateFlow()

    // --- Data Flows ---
    private val allPushupRecords: StateFlow<List<ActivityRecord>> = dao.getRecordsByType(Activities.PUSHUPS.id)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered, grouped, and padded records for the chart
    val chartRecords: StateFlow<List<ActivityRecord>> = combine(
        allPushupRecords,
        _chartTimeSpan
    ) { records, timeSpan ->
        when (timeSpan) {
            ChartTimeSpan.WEEK -> {
                val recordsMap = records.associateBy { LocalDate.parse(it.date) }
                val weekFields = WeekFields.of(Locale.getDefault())
                val startOfWeek = today.with(weekFields.dayOfWeek(), 1)
                (0L..6L).map { startOfWeek.plusDays(it) }
                    .map {
                        date ->
                        recordsMap[date] ?: ActivityRecord(type = Activities.PUSHUPS.id, value = 0.0, date = date.toString())
                    }
            }
            ChartTimeSpan.YEAR -> {
                val recordsByMonth = records.groupBy { LocalDate.parse(it.date).withDayOfMonth(1) }
                val startOfYear = today.withDayOfYear(1)
                (0..11).map { i ->
                    val monthStartDate = startOfYear.plusMonths(i.toLong())
                    val totalForMonth = recordsByMonth[monthStartDate]?.sumOf { it.value } ?: 0.0
                    ActivityRecord(
                        type = Activities.PUSHUPS.id,
                        value = totalForMonth,
                        date = monthStartDate.toString()
                    )
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    val overallStats: StateFlow<OverallStats> = combine(
        allPushupRecords, // Use all records to calculate all totals
        dao.getTotalValueByType(Activities.WATER.id),
        settingsManager.currentStreakFlow
    ) { pushupRecords, totalWater, streak ->
        val weekFields = WeekFields.of(Locale.getDefault())
        val startOfThisWeek = today.with(weekFields.dayOfWeek(), 1)
        val startOfLastWeek = startOfThisWeek.minusWeeks(1)

        val startOfThisMonth = today.withDayOfMonth(1)
        val startOfLastMonth = startOfThisMonth.minusMonths(1)

        OverallStats(
            totalPushups = pushupRecords.sumOf { it.value }.toInt(),
            totalWater = totalWater?.toInt() ?: 0,
            currentStreak = streak,
            todayPushups = pushupRecords.find { it.date == today.toString() }?.value?.toInt() ?: 0,
            yesterdayPushups = pushupRecords.find { it.date == today.minusDays(1).toString() }?.value?.toInt() ?: 0,
            thisWeekTotalPushups = pushupRecords.filter { LocalDate.parse(it.date) >= startOfThisWeek }.sumOf { it.value }.toInt(),
            lastWeekTotalPushups = pushupRecords.filter { val date = LocalDate.parse(it.date); date >= startOfLastWeek && date < startOfThisWeek }.sumOf { it.value }.toInt(),
            thisMonthTotalPushups = pushupRecords.filter { val date = LocalDate.parse(it.date); date.month == today.month && date.year == today.year }.sumOf { it.value }.toInt(),
            lastMonthTotalPushups = pushupRecords.filter { val date = LocalDate.parse(it.date); date.month == startOfLastMonth.month && date.year == startOfLastMonth.year }.sumOf { it.value }.toInt()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OverallStats())

    // --- Public Actions ---
    fun setChartTimeSpan(timeSpan: ChartTimeSpan) {
        _chartTimeSpan.value = timeSpan
    }

    fun setChartType(type: ChartType) {
        _chartType.value = type
    }

    fun onChartEntrySelected(date: LocalDate) {
        if (_chartTimeSpan.value == ChartTimeSpan.YEAR) return

        viewModelScope.launch {
            val record = allPushupRecords.first().find { it.date == date.toString() }
            _editDialogState.value = record ?: ActivityRecord(type = Activities.PUSHUPS.id, value = 0.0, date = date.toString())
        }
    }

    fun onDismissEditDialog() {
        _editDialogState.value = null
    }

    fun updateRecordForDate(date: String, newValue: Double) {
        viewModelScope.launch {
            if (newValue <= 0) {
                dao.deleteRecordByDateAndType(date, Activities.PUSHUPS.id)
            } else {
                val existingRecord = allPushupRecords.first().find { it.date == date }
                val recordToUpsert = existingRecord?.copy(value = newValue) ?: ActivityRecord(type = Activities.PUSHUPS.id, value = newValue, date = date)
                dao.upsertRecord(recordToUpsert)
            }
            onDismissEditDialog()
        }
    }
}
