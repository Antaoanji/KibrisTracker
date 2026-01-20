package com.example.pushuptracker.ui.stats

import android.graphics.Color
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val chartUiState by viewModel.chartUiState.collectAsStateWithLifecycle()
    val overallStats by viewModel.overallStats.collectAsStateWithLifecycle()
    val weeklyChange by viewModel.weeklyChange.collectAsStateWithLifecycle()
    val monthlyChange by viewModel.monthlyChange.collectAsStateWithLifecycle()
    val chartTimeSpan by viewModel.chartTimeSpan.collectAsStateWithLifecycle()
    val chartType by viewModel.chartType.collectAsStateWithLifecycle()
    
    val healthSessions by viewModel.healthSessions.collectAsStateWithLifecycle()
    val hasHealthPermissions by viewModel.hasHealthPermissions.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        Log.d("HealthConnect", "Permissions result: $grantedPermissions")
        viewModel.checkHealthPermissions()
    }

    Scaffold(containerColor = ComposeColor(0xFF1E1E1E)) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                OverallStatsCard(stats = overallStats, weeklyChange = weeklyChange, monthlyChange = monthlyChange)
            }
            
            item {
                PushupChartCard(chartUiState = chartUiState, chartTimeSpan = chartTimeSpan, chartType = chartType, viewModel = viewModel)
            }

            item {
                HealthConnectCard(
                    hasPermissions = hasHealthPermissions,
                    sessions = healthSessions,
                    onConnectClick = { 
                        val perms = viewModel.getHealthPermissions()
                        Log.d("HealthConnect", "Launching permissions with: $perms")
                        permissionLauncher.launch(perms) 
                    },
                    onRefreshClick = { viewModel.loadHealthData() }
                )
            }
        }
    }
}

@Composable
fun OverallStatsCard(stats: OverallStats, weeklyChange: ChangeStats, monthlyChange: ChangeStats) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ComposeColor(0xFF2C2C2E))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Genel İstatistikler", style = MaterialTheme.typography.headlineSmall, color = ComposeColor.White)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                StatItem(value = stats.totalPushups.toString(), label = "Toplam Şınav")
                StatItem(value = stats.totalWater.toString(), label = "Toplam Su (ml)")
                StatItem(value = stats.currentStreak.toString(), label = "Seri (Gün)")
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            // Total Calories Display
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ComposeColor.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Sync, contentDescription = null, tint = ComposeColor(0xFF00F5D4), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Toplam Yakılan: ${stats.totalCalories} kcal",
                    color = ComposeColor.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            ChangeStatsRow(label = "Haftalık Değişim", stats = weeklyChange)
            Spacer(modifier = Modifier.height(8.dp))
            ChangeStatsRow(label = "Aylık Değişim", stats = monthlyChange)
        }
    }
}

@Composable
fun HealthConnectCard(
    hasPermissions: Boolean,
    sessions: List<ExerciseSessionRecord>,
    onConnectClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ComposeColor(0xFF2C2C2E))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Health Connect Verileri", style = MaterialTheme.typography.headlineSmall, color = ComposeColor.White)
                if (hasPermissions) {
                    IconButton(onClick = onRefreshClick) {
                        Icon(Icons.Default.Sync, contentDescription = "Yenile", tint = ComposeColor(0xFF00F5D4))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            if (!hasPermissions) {
                Text("Xiaomi Band verileri (kalori vb.) için Health Connect'e bağlan.", color = ComposeColor.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onConnectClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ComposeColor(0xFF00F5D4), contentColor = ComposeColor.Black)
                ) {
                    Text("İzin Ver ve Bağlan")
                }
            } else {
                if (sessions.isEmpty()) {
                    Text("Son 24 saat içinde kaydedilmiş antrenman bulunamadı.", color = ComposeColor.Gray, fontSize = 14.sp)
                } else {
                    sessions.forEach { session ->
                        HealthSessionItem(session)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = ComposeColor.DarkGray)
                    }
                }
            }
        }
    }
}

@Composable
fun HealthSessionItem(session: ExerciseSessionRecord) {
    val startTime = session.startTime.atZone(ZoneId.systemDefault())
    val duration = Duration.between(session.startTime, session.endTime)
    val minutes = duration.toMinutes()
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = getExerciseName(session.exerciseType),
                color = ComposeColor.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = startTime.format(DateTimeFormatter.ofPattern("dd MMM, HH:mm")),
                color = ComposeColor.Gray,
                fontSize = 12.sp
            )
        }
        Text(
            text = "$minutes dk",
            color = ComposeColor(0xFF00F5D4),
            fontWeight = FontWeight.Bold
        )
    }
}

fun getExerciseName(type: Int): String {
    return when (type) {
        ExerciseSessionRecord.EXERCISE_TYPE_RUNNING -> "Koşu"
        ExerciseSessionRecord.EXERCISE_TYPE_WALKING -> "Yürüyüş"
        8 -> "Bisiklet" 
        74 -> "Yüzme"
        ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING -> "Ağırlık Antrenmanı"
        else -> "Diğer Egzersiz"
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = ComposeColor.White)
        Text(text = label, fontSize = 12.sp, color = ComposeColor.Gray)
    }
}

@Composable
fun ChangeStatsRow(label: String, stats: ChangeStats) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, modifier = Modifier.weight(1f), color = ComposeColor.White, fontSize = 16.sp)
        Row(modifier = Modifier.weight(2f), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Önceki\n${stats.previous}", color = ComposeColor.Gray, fontSize = 14.sp, textAlign = TextAlign.Center)
            Text("Şimdiki\n${stats.current}", color = ComposeColor.Gray, fontSize = 14.sp, textAlign = TextAlign.Center)
            val changeText = if (stats.change >= 0) "+${stats.change}" else "${stats.change}"
            val changeColor = if (stats.change >= 0) ComposeColor(0xFFE53935) else ComposeColor.Green
            Text("Değişim\n$changeText", color = changeColor, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun PushupChartCard(chartUiState: ChartUiState, chartTimeSpan: ChartTimeSpan, chartType: ChartType, viewModel: StatsViewModel) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ComposeColor(0xFF2C2C2E))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Gelişim Grafiği", style = MaterialTheme.typography.headlineSmall, color = ComposeColor.White)
            Spacer(modifier = Modifier.height(16.dp))
            ChartControls(chartTimeSpan = chartTimeSpan, chartType = chartType, onTimeSpanSelected = { viewModel.setChartTimeSpan(it) }, onTypeSelected = { viewModel.setChartType(it) })
            Spacer(modifier = Modifier.height(16.dp))
            StatsChart(chartUiState = chartUiState, timeSpan = chartTimeSpan, chartType = chartType, modifier = Modifier
                .fillMaxWidth()
                .height(250.dp))
        }
    }
}

@Composable
fun ChartControls(chartTimeSpan: ChartTimeSpan, chartType: ChartType, onTimeSpanSelected: (ChartTimeSpan) -> Unit, onTypeSelected: (ChartType) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        SegmentedButton(text = "Haftalık", isSelected = chartTimeSpan == ChartTimeSpan.WEEK, onClick = { onTimeSpanSelected(ChartTimeSpan.WEEK) })
        SegmentedButton(text = "Yıllık", isSelected = chartTimeSpan == ChartTimeSpan.YEAR, onClick = { onTimeSpanSelected(ChartTimeSpan.YEAR) })
        Spacer(modifier = Modifier.width(8.dp))
        SegmentedButton(text = "Çizgi", isSelected = chartType == ChartType.LINE, onClick = { onTypeSelected(ChartType.LINE) })
        SegmentedButton(text = "Bar", isSelected = chartType == ChartType.BAR, onClick = { onTypeSelected(ChartType.BAR) })
    }
}

@Composable
fun SegmentedButton(text: String, isSelected: Boolean, onClick: () -> Unit, enabled: Boolean = true) {
    val selectedColor = ComposeColor(0xFF00F5D4)
    val unselectedColor = ComposeColor.DarkGray
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) selectedColor else unselectedColor, contentColor = if (isSelected) ComposeColor.Black else ComposeColor.White),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        Text(text, fontSize = 12.sp)
    }
}


@Composable
fun StatsChart(chartUiState: ChartUiState, timeSpan: ChartTimeSpan, chartType: ChartType, modifier: Modifier = Modifier) {
    val chartColor = ComposeColor(0xFF00F5D4).toArgb()

    key(chartType) {
        AndroidView(
            modifier = modifier,
            factory = { context ->
                if (chartType == ChartType.BAR) {
                    BarChart(context).apply {
                        description.isEnabled = false
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.setDrawGridLines(false)
                        xAxis.textColor = Color.WHITE
                        xAxis.granularity = 1f
                        axisLeft.setDrawGridLines(true)
                        axisLeft.textColor = Color.WHITE
                        axisLeft.axisMinimum = 0f
                        axisRight.isEnabled = false
                        legend.isEnabled = false
                    }
                } else {
                    LineChart(context).apply {
                        description.isEnabled = false
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.setDrawGridLines(false)
                        xAxis.textColor = Color.WHITE
                        xAxis.granularity = 1f
                        axisLeft.setDrawGridLines(true)
                        axisLeft.textColor = Color.WHITE
                        axisLeft.axisMinimum = 0f
                        axisRight.isEnabled = false
                        legend.isEnabled = false
                    }
                }
            },
            update = { chart ->
                val today = LocalDate.now()
                val rawEntries = when (timeSpan) {
                    ChartTimeSpan.WEEK -> {
                        val startDate = today.minusDays(6)
                        (0..6).map {
                            val date = startDate.plusDays(it.toLong())
                            val record = chartUiState.records.find { r -> LocalDate.parse(r.date) == date }
                            Entry(it.toFloat(), record?.value?.toFloat() ?: 0f)
                        }
                    }
                    ChartTimeSpan.YEAR -> {
                        // Start 11 months ago to show a total of 12 months
                        val startDate = today.minusMonths(11).withDayOfMonth(1)
                        (0..11).map {
                            val targetMonth = startDate.plusMonths(it.toLong())
                            val monthValue = chartUiState.records.filter { r ->
                                val recordDate = LocalDate.parse(r.date)
                                recordDate.year == targetMonth.year && recordDate.month == targetMonth.month
                            }.sumOf { it.value }.toFloat()
                            Entry(it.toFloat(), monthValue)
                        }
                    }
                }

                if (chart is BarChart && chartType == ChartType.BAR) {
                    val barEntries = rawEntries.map { BarEntry(it.x, it.y) }
                    val dataSet = BarDataSet(barEntries, "").apply {
                        color = chartColor
                        setDrawValues(false)
                    }
                    chart.data = BarData(dataSet).apply { barWidth = 0.5f }
                } else if (chart is LineChart && chartType == ChartType.LINE) {
                    val dataSet = LineDataSet(rawEntries, "").apply {
                        color = chartColor
                        setCircleColor(chartColor)
                        lineWidth = 2f
                        circleRadius = 4f
                        setDrawFilled(true)
                        fillColor = chartColor
                        fillAlpha = 50
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                        setDrawValues(false)
                    }
                    chart.data = LineData(dataSet)
                }

                chart.xAxis.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val idx = value.toInt()
                        if (idx < 0 || idx >= rawEntries.size) return ""
                        return try {
                            when (timeSpan) {
                                ChartTimeSpan.WEEK -> {
                                    today.minusDays((6 - idx).toLong()).format(DateTimeFormatter.ofPattern("dd MMM"))
                                }
                                ChartTimeSpan.YEAR -> {
                                    today.minusMonths((11 - idx).toLong()).format(DateTimeFormatter.ofPattern("MMM"))
                                }
                            }
                        } catch (e: Exception) {
                            ""
                        }
                    }
                }

                chart.xAxis.labelCount = if (timeSpan == ChartTimeSpan.WEEK) 7 else 12
                chart.xAxis.axisMinimum = -0.5f
                chart.xAxis.axisMaximum = rawEntries.size.toFloat() - 0.5f
                
                chart.invalidate()
            }
        )
    }
}
