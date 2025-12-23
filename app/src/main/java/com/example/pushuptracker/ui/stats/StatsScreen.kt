package com.example.pushuptracker.ui.stats

import android.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pushuptracker.R
import com.example.pushuptracker.model.ActivityRecord
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val chartRecords by viewModel.chartRecords.collectAsStateWithLifecycle(emptyList())
    val overallStats by viewModel.overallStats.collectAsStateWithLifecycle()
    val chartTimeSpan by viewModel.chartTimeSpan.collectAsStateWithLifecycle()
    val chartType by viewModel.chartType.collectAsStateWithLifecycle()
    val editState by viewModel.editDialogState.collectAsStateWithLifecycle()

    Scaffold {
            padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OverallStatsCard(stats = overallStats)
            }
            item {
                PushupChartCard(
                    records = chartRecords,
                    timeSpan = chartTimeSpan,
                    chartType = chartType,
                    onTimeSpanSelected = { viewModel.setChartTimeSpan(it) },
                    onChartTypeSelected = { viewModel.setChartType(it) },
                    onEntrySelected = { date -> viewModel.onChartEntrySelected(date) }
                )
            }
        }

        editState?.let { record ->
            EditRecordDialog(
                record = record,
                onDismiss = { viewModel.onDismissEditDialog() },
                onSave = { newValue -> viewModel.updateRecordForDate(record.date, newValue) }
            )
        }
    }
}

@Composable
fun EditRecordDialog(record: ActivityRecord, onDismiss: () -> Unit, onSave: (Double) -> Unit) {
    var input by remember { mutableStateOf(record.value.toInt().toString()) }
    val displayDate = remember(record.date) {
        try {
            LocalDate.parse(record.date).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        } catch (_: Exception) {
            record.date // Fallback to original date if parsing fails
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Değeri Düzenle ($displayDate)") },
        text = {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text(text = "Şınav Sayısı") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = {
                val newValue = input.toDoubleOrNull() ?: 0.0
                onSave(newValue)
            }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}


@Composable
fun OverallStatsCard(stats: OverallStats) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(text = "Genel İstatistikler", style = MaterialTheme.typography.titleLarge)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                StatItem(value = stats.totalPushups.toString(), label = "Toplam Şınav")
                StatItem(value = stats.totalWater.toString(), label = "Toplam Su (ml)")
                StatItem(value = stats.currentStreak.toString(), label = "Seri (Gün)")
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    StatItem(value = stats.todayPushups.toString(), label = "Bugün")
                    StatItem(value = stats.yesterdayPushups.toString(), label = "Dün")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    StatItem(value = stats.thisWeekTotalPushups.toString(), label = "Bu Hafta")
                    StatItem(value = stats.lastWeekTotalPushups.toString(), label = "Geçen Hafta")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    StatItem(value = stats.thisMonthTotalPushups.toString(), label = "Bu Ay")
                    StatItem(value = stats.lastMonthTotalPushups.toString(), label = "Geçen Ay")
                }
            }
        }
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun PushupChartCard(
    records: List<ActivityRecord>,
    timeSpan: ChartTimeSpan,
    chartType: ChartType,
    onTimeSpanSelected: (ChartTimeSpan) -> Unit,
    onChartTypeSelected: (ChartType) -> Unit,
    onEntrySelected: (LocalDate) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Şınav Grafiği", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

            // Time Span Selector
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                ToggleButton(text = "Haftalık", selected = timeSpan == ChartTimeSpan.WEEK) { onTimeSpanSelected(ChartTimeSpan.WEEK) }
                Spacer(modifier = Modifier.padding(4.dp))
                ToggleButton(text = "Yıllık", selected = timeSpan == ChartTimeSpan.YEAR) { onTimeSpanSelected(ChartTimeSpan.YEAR) }
            }
            Spacer(Modifier.height(8.dp))

            // Chart Type Selector
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                ToggleButton(text = "Çizgi", selected = chartType == ChartType.LINE) { onChartTypeSelected(ChartType.LINE) }
                Spacer(modifier = Modifier.padding(4.dp))
                ToggleButton(text = "Bar", selected = chartType == ChartType.BAR) { onChartTypeSelected(ChartType.BAR) }
            }

            key(timeSpan, chartType) { // Recreate the chart when timeSpan or chartType changes
                if (records.all { it.value == 0.0 }) {
                    Box(modifier = Modifier.height(250.dp).padding(top = 16.dp), contentAlignment = Alignment.Center) {
                        Text(text = stringResource(id = R.string.no_stats_to_display_in_chart))
                    }
                } else {
                    Box(modifier = Modifier.height(250.dp).padding(top = 16.dp)) {
                        if (chartType == ChartType.LINE) {
                            LineChart(records = records, timeSpan = timeSpan, onEntrySelected = onEntrySelected)
                        } else {
                            BarChart(records = records, timeSpan = timeSpan, onEntrySelected = onEntrySelected)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ToggleButton(text: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    ) {
        Text(text)
    }
}

class DynamicDateAxisFormatter : ValueFormatter() {
    private var currentPattern = "d MMM"
    private var locale = Locale.forLanguageTag("tr")

    fun setFormat(timeSpan: ChartTimeSpan) {
        currentPattern = if (timeSpan == ChartTimeSpan.WEEK) "d MMM" else "MMM"
    }

    override fun getFormattedValue(value: Float): String {
        return try {
            val date = LocalDate.ofEpochDay(value.toLong())
            val formatter = DateTimeFormatter.ofPattern(currentPattern, locale)
            date.format(formatter)
        } catch (_: Exception) {
            ""
        }
    }
}

val yAxisValueFormatter = object : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return value.roundToInt().toString()
    }
}

fun createChartValueSelectedListener(onEntrySelected: (LocalDate) -> Unit) = object : OnChartValueSelectedListener {
    override fun onValueSelected(e: Entry?, h: Highlight?) {
        e?.let { onEntrySelected(LocalDate.ofEpochDay(it.x.toLong())) }
    }
    override fun onNothingSelected() {}
}

@Composable
fun LineChart(records: List<ActivityRecord>, timeSpan: ChartTimeSpan, onEntrySelected: (LocalDate) -> Unit) {
    val chartColor = MaterialTheme.colorScheme.primary.toArgb()
    val today = LocalDate.now()

    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                setDrawGridBackground(false)
                legend.isEnabled = false
                setOnChartValueSelectedListener(createChartValueSelectedListener(onEntrySelected))

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    textColor = chartColor
                    axisLineColor = chartColor
                    valueFormatter = DynamicDateAxisFormatter()
                    granularity = 1f
                    labelCount = if (timeSpan == ChartTimeSpan.WEEK) 7 else 12
                }
                axisLeft.apply {
                    textColor = chartColor
                    axisLineColor = chartColor
                    setDrawGridLines(true)
                    granularity = 5f
                    valueFormatter = yAxisValueFormatter
                    axisMinimum = 0f // Start Y-axis at 0
                }
                axisRight.isEnabled = false
            }
        },
        update = { chart ->
            (chart.xAxis.valueFormatter as DynamicDateAxisFormatter).setFormat(timeSpan)

            val entries = records.map { Entry(it.date.toLocalDate().toEpochDay().toFloat(), it.value.toFloat()) }
            val dataSet = LineDataSet(entries, "Push-ups").apply {
                color = chartColor
                valueTextColor = Color.TRANSPARENT
                setCircleColor(chartColor)
                circleHoleColor = chartColor
                lineWidth = 2f
            }

            val weekFields = WeekFields.of(Locale.getDefault())
            if (timeSpan == ChartTimeSpan.WEEK) {
                chart.xAxis.axisMinimum = today.with(weekFields.dayOfWeek(), 1).toEpochDay().toFloat()
                chart.xAxis.axisMaximum = today.with(weekFields.dayOfWeek(), 7).toEpochDay().toFloat()
            } else {
                val startOfYear = today.withDayOfYear(1)
                chart.xAxis.axisMinimum = startOfYear.toEpochDay().toFloat()
                chart.xAxis.axisMaximum = startOfYear.plusYears(1).minusDays(1).toEpochDay().toFloat()
            }

            chart.axisLeft.axisMaximum = (entries.maxOfOrNull { it.y }?.plus(10f)) ?: 50f

            chart.data = LineData(dataSet)
            chart.invalidate()
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun BarChart(records: List<ActivityRecord>, timeSpan: ChartTimeSpan, onEntrySelected: (LocalDate) -> Unit) {
    val chartColor = MaterialTheme.colorScheme.primary.toArgb()
    val today = LocalDate.now()

    AndroidView(
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                setDrawGridBackground(false)
                legend.isEnabled = false
                setOnChartValueSelectedListener(createChartValueSelectedListener(onEntrySelected))

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    textColor = chartColor
                    axisLineColor = chartColor
                    valueFormatter = DynamicDateAxisFormatter()
                    granularity = 1f
                    labelCount = if (timeSpan == ChartTimeSpan.WEEK) 7 else 12
                }
                axisLeft.apply {
                    textColor = chartColor
                    axisLineColor = chartColor
                    setDrawGridLines(true)
                    granularity = 5f
                    valueFormatter = yAxisValueFormatter
                    axisMinimum = 0f // Start Y-axis at 0
                }
                axisRight.isEnabled = false
            }
        },
        update = { chart ->
            (chart.xAxis.valueFormatter as DynamicDateAxisFormatter).setFormat(timeSpan)

            val entries = records.map { BarEntry(it.date.toLocalDate().toEpochDay().toFloat(), it.value.toFloat()) }
            val dataSet = BarDataSet(entries, "Push-ups").apply {
                color = chartColor
                valueTextColor = Color.TRANSPARENT
            }
            
            val data = BarData(dataSet)
            data.barWidth = if(timeSpan == ChartTimeSpan.WEEK) 0.5f else 20f
            chart.data = data

            val weekFields = WeekFields.of(Locale.getDefault())
            if (timeSpan == ChartTimeSpan.WEEK) {
                chart.xAxis.axisMinimum = today.with(weekFields.dayOfWeek(), 1).toEpochDay().toFloat()
                chart.xAxis.axisMaximum = today.with(weekFields.dayOfWeek(), 7).toEpochDay().toFloat()
            } else {
                val startOfYear = today.withDayOfYear(1)
                chart.xAxis.axisMinimum = startOfYear.toEpochDay().toFloat()
                chart.xAxis.axisMaximum = startOfYear.plusYears(1).minusDays(1).toEpochDay().toFloat()
            }

            chart.axisLeft.axisMaximum = (entries.maxOfOrNull { it.y }?.plus(10f)) ?: 50f

            chart.invalidate()
        },
        modifier = Modifier.fillMaxSize()
    )
}

fun String.toLocalDate(): LocalDate = LocalDate.parse(this)
