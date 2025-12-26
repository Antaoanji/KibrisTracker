package com.example.pushuptracker.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pushuptracker.data.repo.PushupRepo
import com.example.pushuptracker.model.ActivityRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import javax.inject.Inject

enum class ChartTimeSpan { WEEK, YEAR }
enum class ChartType { LINE, BAR }

data class ChartUiState(
    val records: List<ActivityRecord> = emptyList()
)

data class OverallStats(
    val totalPushups: Int = 0,
    val totalWater: Int = 0,
    val currentStreak: Int = 0,
    val thisWeekTotalPushups: Int = 0,
    val lastWeekTotalPushups: Int = 0,
    val thisMonthTotalPushups: Int = 0,
    val lastMonthTotalPushups: Int = 0
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val pushupRepo: PushupRepo
) : ViewModel() {

    val chartTimeSpan = MutableStateFlow(ChartTimeSpan.WEEK)
    val chartType = MutableStateFlow(ChartType.LINE)
    val editDialogState = MutableStateFlow<ActivityRecord?>(null)

    val chartUiState = combine(
        pushupRepo.getAllRecords(),
        chartTimeSpan
    ) { allRecords, timeSpan ->
        val records = if (timeSpan == ChartTimeSpan.WEEK) {
            val startOfWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val weekDates = (0..6).map { startOfWeek.plusDays(it.toLong()) }
            weekDates.map {
                allRecords.find { record -> LocalDate.parse(record.date) == it } ?: ActivityRecord("pushup", 0.0, it.toString())
            }
        } else {
            val startOfYear = LocalDate.now().withDayOfYear(1)
            (0..11).map { 
                val month = startOfYear.plusMonths(it.toLong())
                val monthlyTotal = allRecords.filter { record ->
                    val recordDate = LocalDate.parse(record.date)
                    recordDate.month == month.month && recordDate.year == month.year
                }.sumOf { it.value }
                ActivityRecord("pushup", monthlyTotal, month.toString())
            }
        }
        ChartUiState(records)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChartUiState())

    val overallStats = pushupRepo.getAllRecords().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()).combine(chartTimeSpan) { records, timeSpan ->
        val today = LocalDate.now()
        val totalPushups = records.sumOf { it.value }.toInt()

        val thisWeekTotalPushups = records.filter {
            val recordDate = LocalDate.parse(it.date)
            recordDate.isAfter(today.minusWeeks(1))
        }.sumOf { it.value }.toInt()

        val lastWeekTotalPushups = records.filter {
            val recordDate = LocalDate.parse(it.date)
            recordDate.isAfter(today.minusWeeks(2)) && recordDate.isBefore(today.minusWeeks(1))
        }.sumOf { it.value }.toInt()

        val thisMonthTotalPushups = records.filter {
            val recordDate = LocalDate.parse(it.date)
            recordDate.isAfter(today.minusMonths(1))
        }.sumOf { it.value }.toInt()

        val lastMonthTotalPushups = records.filter {
            val recordDate = LocalDate.parse(it.date)
            recordDate.isAfter(today.minusMonths(2)) && recordDate.isBefore(today.minusMonths(1))
        }.sumOf { it.value }.toInt()

        OverallStats(
            totalPushups = totalPushups,
            thisWeekTotalPushups = thisWeekTotalPushups,
            lastWeekTotalPushups = lastWeekTotalPushups,
            thisMonthTotalPushups = thisMonthTotalPushups,
            lastMonthTotalPushups = lastMonthTotalPushups
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OverallStats())

    fun setChartTimeSpan(timeSpan: ChartTimeSpan) {
        chartTimeSpan.value = timeSpan
    }

    fun setChartType(chartType: ChartType) {
        this.chartType.value = chartType
    }

    fun onChartEntrySelected(record: ActivityRecord) {
        if(record.value > 0) editDialogState.value = record
    }

    fun onDismissEditDialog() {
        editDialogState.value = null
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
