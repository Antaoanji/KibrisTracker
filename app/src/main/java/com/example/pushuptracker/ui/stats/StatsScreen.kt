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
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val chartUiState by viewModel.chartUiState.collectAsStateWithLifecycle()
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
                    records = chartUiState.records,
                    timeSpan = chartTimeSpan,
                    chartType = chartType,
                    onTimeSpanSelected = { viewModel.setChartTimeSpan(it) },
                    onChartTypeSelected = { viewModel.setChartType(it) },
                    onEntrySelected = { record -> viewModel.onChartEntrySelected(record) }
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
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Genel İstatistikler", style = MaterialTheme.typography.titleLarge)

            // Main Stats
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                StatItem(value = stats.totalPushups.toString(), label = "Toplam Şınav")
                StatItem(value = stats.totalWater.toString(), label = "Toplam Su (ml)")
                StatItem(value = stats.currentStreak.toString(), label = "Seri (Gün)")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Comparisons
            ComparisonRow(label = "Haftalık Değişim", current = stats.thisWeekTotalPushups, previous = stats.lastWeekTotalPushups)
            ComparisonRow(label = "Aylık Değişim", current = stats.thisMonthTotalPushups, previous = stats.lastMonthTotalPushups)
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
fun ComparisonRow(label: String, current: Int, previous: Int) {
    val difference = current - previous
    val diffText = when {
        difference > 0 -> "+${abs(difference)}"
        difference < 0 -> "-${abs(difference)}"
        else -> "-"
    }
    val diffColor = when {
        difference > 0 -> MaterialTheme.colorScheme.tertiary
        difference < 0 -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Önceki", style = MaterialTheme.typography.bodySmall)
                Text(previous.toString(), style = MaterialTheme.typography.titleMedium)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Şimdiki", style = MaterialTheme.typography.bodySmall)
                Text(current.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Değişim", style = MaterialTheme.typography.bodySmall)
                Text(diffText, style = MaterialTheme.typography.titleMedium, color = diffColor, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PushupChartCard(
    records: List<ActivityRecord>,
    timeSpan: ChartTimeSpan,
    chartType: ChartType,
    onTimeSpanSelected: (ChartTimeSpan) -> Unit,
    onChartTypeSelected: (ChartType) -> Unit,
    onEntrySelected: (ActivityRecord) -> Unit
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

            key(timeSpan, chartType, records) { // Recreate the chart when data changes
                if (records.isEmpty() || records.all { it.value == 0.0 }) {
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

class IndexToDateValueFormatter(private val records: List<ActivityRecord>, private val timeSpan: ChartTimeSpan) : ValueFormatter() {
    private val weekFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("tr"))
    private val yearFormatter = DateTimeFormatter.ofPattern("MMM", Locale.forLanguageTag("tr"))

    override fun getFormattedValue(value: Float): String {
        val index = value.toInt()
        return if (index >= 0 && index < records.size) {
            val date = LocalDate.parse(records[index].date)
            if (timeSpan == ChartTimeSpan.WEEK) date.format(weekFormatter) else date.month.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("tr"))
        } else {
            ""
        }
    }
}

val yAxisValueFormatter = object : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return value.roundToInt().toString()
    }
}


@Composable
fun LineChart(records: List<ActivityRecord>, timeSpan: ChartTimeSpan, onEntrySelected: (ActivityRecord) -> Unit) {
    val chartColor = MaterialTheme.colorScheme.primary.toArgb()

    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                setDrawGridBackground(false)
                legend.isEnabled = false

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    textColor = chartColor
                    axisLineColor = chartColor
                    granularity = 1f
                }
                axisLeft.apply {
                    textColor = chartColor
                    axisLineColor = chartColor
                    setDrawGridLines(true)
                    granularity = 5f
                    valueFormatter = yAxisValueFormatter
                    axisMinimum = 0f
                }
                axisRight.isEnabled = false
            }
        },
        update = { chart ->
            val entries = records.mapIndexed { index, record -> 
                Entry(index.toFloat(), record.value.toFloat())
            }
            val dataSet = LineDataSet(entries, "Push-ups").apply {
                color = chartColor
                valueTextColor = Color.TRANSPARENT
                setCircleColor(chartColor)
                circleHoleColor = chartColor
                lineWidth = 2f
            }
            
            chart.xAxis.valueFormatter = IndexToDateValueFormatter(records, timeSpan)
            chart.xAxis.axisMinimum = -0.5f
            chart.xAxis.axisMaximum = records.size - 0.5f
            chart.data = LineData(dataSet)

            chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    e?.let {
                        val index = it.x.toInt()
                        if (index >= 0 && index < records.size) {
                            onEntrySelected(records[index])
                        }
                    }
                }
                override fun onNothingSelected() {}
            })

            chart.invalidate()
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun BarChart(records: List<ActivityRecord>, timeSpan: ChartTimeSpan, onEntrySelected: (ActivityRecord) -> Unit) {
    val chartColor = MaterialTheme.colorScheme.primary.toArgb()

    AndroidView(
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                setDrawGridBackground(false)
                legend.isEnabled = false

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    textColor = chartColor
                    axisLineColor = chartColor
                    granularity = 1f
                }
                axisLeft.apply {
                    textColor = chartColor
                    axisLineColor = chartColor
                    setDrawGridLines(true)
                    granularity = 5f
                    valueFormatter = yAxisValueFormatter
                    axisMinimum = 0f
                }
                axisRight.isEnabled = false
            }
        },
        update = { chart ->
            val entries = records.mapIndexed { index, record -> 
                BarEntry(index.toFloat(), record.value.toFloat())
            }
            val dataSet = BarDataSet(entries, "Push-ups").apply {
                color = chartColor
                valueTextColor = Color.TRANSPARENT
            }

            chart.xAxis.valueFormatter = IndexToDateValueFormatter(records, timeSpan)
            val data = BarData(dataSet)
            data.barWidth = 0.5f
            chart.data = data

            chart.xAxis.axisMinimum = -0.5f
            chart.xAxis.axisMaximum = (records.size - 1) + 0.5f

            chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    e?.let {
                        val index = it.x.toInt()
                        if (index >= 0 && index < records.size) {
                            onEntrySelected(records[index])
                        }
                    }
                }
                override fun onNothingSelected() {}
            })

            chart.invalidate()
        },
        modifier = Modifier.fillMaxSize()
    )
}

fun String.toLocalDate(): LocalDate = LocalDate.parse(this)
