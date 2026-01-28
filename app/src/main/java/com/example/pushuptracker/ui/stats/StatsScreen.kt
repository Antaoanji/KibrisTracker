package com.example.pushuptracker.ui.stats

import android.graphics.Color
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pushuptracker.model.WorkoutRecord
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

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
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val heatmapData by viewModel.heatmapData.collectAsStateWithLifecycle()

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
                OverallStatsCard(
                    stats = overallStats,
                    weeklyChange = weeklyChange,
                    monthlyChange = monthlyChange,
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.loadHealthData() }
                )
            }

            item {
                WorkoutHeatmapCard(heatmapData = heatmapData)
            }
            
            item {
                PushupChartCard(chartUiState = chartUiState, chartTimeSpan = chartTimeSpan, chartType = chartType, viewModel = viewModel)
            }

            item {
                HealthConnectCard(
                    hasPermissions = hasHealthPermissions,
                    sessions = healthSessions,
                    isRefreshing = isRefreshing,
                    onConnectClick = {
                        try {
                            val perms = viewModel.getHealthPermissions()
                            permissionLauncher.launch(perms) 
                        } catch (e: Exception) {
                            Log.e("HealthConnect", "Failed to launch permissions", e)
                        }
                    },
                    onRefreshClick = { viewModel.loadHealthData() }
                )
            }
        }
    }
}

@Composable
fun WorkoutHeatmapCard(heatmapData: Map<String, String>) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ComposeColor(0xFF2C2C2E))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Antrenman Takvimi", style = MaterialTheme.typography.titleMedium, color = ComposeColor.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            WorkoutHeatmap(heatmapData = heatmapData)
            
            Spacer(modifier = Modifier.height(24.dp))
            HeatmapLegend()
        }
    }
}

@Composable
fun WorkoutHeatmap(heatmapData: Map<String, String>) {
    val today = LocalDate.now()
    // 12 hafta öncesinden başla (Pazartesiye sabitle)
    val startDay = today.minusWeeks(11).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    
    Row(modifier = Modifier.fillMaxWidth()) {
        BoxWithConstraints(modifier = Modifier.weight(1f)) {
            val weekCount = 12
            val spacing = 4.dp
            val cellSize = (maxWidth - (spacing * (weekCount))) / (weekCount + 1) // Gün etiketleri için +1 alan
            
            Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                // Gün etiketleri sütunu
                Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
                    val days = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")
                    days.forEach { day ->
                        Box(modifier = Modifier.size(cellSize), contentAlignment = Alignment.CenterStart) {
                            Text(day, color = ComposeColor.Gray, fontSize = 8.sp)
                        }
                    }
                }

                // Hafta sütunları
                for (week in 0 until weekCount) {
                    Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
                        for (day in 0 until 7) {
                            val currentDate = startDay.plusWeeks(week.toLong()).plusDays(day.toLong())
                            val dateStr = currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

                            // Logcat'teki 2026-01-28 gibi tarihleri yakalamak için doğrudan Map kontrolü
                            val workoutTitle = heatmapData[dateStr]
                            
                            HeatmapCell(
                                title = workoutTitle,
                                size = cellSize,
                                isToday = currentDate == today,
                                isFuture = currentDate.isAfter(today)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeatmapCell(title: String?, size: Dp, isToday: Boolean, isFuture: Boolean) {
    val color = when {
        title != null && title.contains("PUSH") -> ComposeColor(0xFFFF5722)
        title != null && title.contains("PULL") -> ComposeColor(0xFF2196F3)
        title != null && title.contains("LEGS") -> ComposeColor(0xFF4CAF50)
        title != null && title.contains("UPPER") -> ComposeColor(0xFF9C27B0)
        title != null && title.contains("LOWER") -> ComposeColor(0xFFFFC107)
        title != null -> ComposeColor(0xFF00F5D4) // Legacy veya Genel
        isFuture -> ComposeColor.Transparent
        else -> ComposeColor.White.copy(alpha = 0.05f) // Boş gün
    }

    val icon = when {
        title == null -> null
        title.contains("MACHINE") -> Icons.Default.FitnessCenter
        title.contains("CALISTHENICS") -> Icons.Default.AccessibilityNew
        else -> null
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(2.dp))
            .background(color)
            .then(
                if (isToday) Modifier.border(1.dp, ComposeColor.White.copy(alpha = 0.8f), RoundedCornerShape(2.dp))
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ComposeColor.White.copy(alpha = 0.9f),
                modifier = Modifier.size(size * 0.7f)
            )
        }
    }
}

@Composable
fun HeatmapLegend() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ComposeColor.Black.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: Types
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            LegendItem("Push", ComposeColor(0xFFFF5722))
            LegendItem("Pull", ComposeColor(0xFF2196F3))
            LegendItem("Legs", ComposeColor(0xFF4CAF50))
            LegendItem("Upper", ComposeColor(0xFF9C27B0))
            LegendItem("Lower", ComposeColor(0xFFFFC107))
        }
        
        HorizontalDivider(color = ComposeColor.White.copy(alpha = 0.05f))
        
        // Row 2: Modes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.FitnessCenter, null, tint = ComposeColor.Gray, modifier = Modifier.size(14.dp))
            Text(" Makine", color = ComposeColor.Gray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            
            Spacer(modifier = Modifier.width(24.dp))
            
            Icon(Icons.Default.AccessibilityNew, null, tint = ComposeColor.Gray, modifier = Modifier.size(14.dp))
            Text(" Cali", color = ComposeColor.Gray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            
            Spacer(modifier = Modifier.weight(1f))
            
            Box(modifier = Modifier.size(8.dp).background(ComposeColor(0xFF00F5D4).copy(alpha = 0.5f), RoundedCornerShape(1.dp)))
            Text(" Geçmiş", color = ComposeColor.Gray, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp))
        }
    }
}

@Composable
fun LegendItem(label: String, color: ComposeColor) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, color = ComposeColor.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun OverallStatsCard(
    stats: OverallStats,
    weeklyChange: ChangeStats,
    monthlyChange: ChangeStats,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
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
                Text("Genel İstatistikler", style = MaterialTheme.typography.headlineSmall, color = ComposeColor.White)

                val rotation by animateFloatAsState(
                    targetValue = if (isRefreshing) 360f else 0f,
                    animationSpec = if (isRefreshing) {
                        infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Restart)
                    } else {
                        tween(0)
                    }, label = "sync_rotation"
                )

                IconButton(onClick = onRefresh) {
                    Icon(
                        Icons.Default.Sync,
                        contentDescription = "Senkronize Et",
                        tint = ComposeColor(0xFF00F5D4),
                        modifier = Modifier.rotate(rotation)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Stats Grid
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    StatItem(value = stats.totalWorkouts.toString(), label = "Antrenman")
                    StatItem(value = stats.totalPushups.toString(), label = "Şınav")
                    StatItem(value = stats.currentStreak.toString(), label = "Seri (Gün)")
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    StatItem(value = stats.totalWalkingMinutes.toString(), label = "Yürüyüş (dk)")
                    StatItem(value = stats.totalLymphaticCount.toString(), label = "Lenfatik")
                    StatItem(value = stats.totalWater.toString(), label = "Su (ml)")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
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
                    text = "Xiaomi Band Kalori: ${stats.totalCalories} kcal",
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
    isRefreshing: Boolean,
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
                Text("Xiaomi Band Verileri", style = MaterialTheme.typography.headlineSmall, color = ComposeColor.White)
                if (hasPermissions) {
                    IconButton(onClick = onRefreshClick) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Yenile",
                            tint = ComposeColor(0xFF00F5D4),
                            modifier = Modifier.rotate(if (isRefreshing) 180f else 0f)
                        )
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

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onRefreshClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ComposeColor(0xFF2C2C2E), contentColor = ComposeColor(0xFF00F5D4)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ComposeColor(0xFF00F5D4))
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = ComposeColor(0xFF00F5D4), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Xiaomi Verilerini Senkronize Et")
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
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ComposeColor.White)
        Text(text = label, fontSize = 11.sp, color = ComposeColor.Gray)
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
            
            if (chartUiState.records.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(250.dp), contentAlignment = Alignment.Center) {
                    Text("Henüz veri bulunmuyor.", color = ComposeColor.Gray)
                }
            } else {
                StatsChart(chartUiState = chartUiState, timeSpan = chartTimeSpan, chartType = chartType, modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp))
            }
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
                val rawEntries = try {
                    when (timeSpan) {
                        ChartTimeSpan.WEEK -> {
                            val startDate = today.minusDays(6)
                            (0..6).map {
                                val date = startDate.plusDays(it.toLong())
                                val record = chartUiState.records.find { r -> 
                                    try { LocalDate.parse(r.date) == date } catch(e: Exception) { false }
                                }
                                Entry(it.toFloat(), record?.value?.toFloat() ?: 0f)
                            }
                        }
                        ChartTimeSpan.YEAR -> {
                            val startDate = today.minusMonths(11).withDayOfMonth(1)
                            (0..11).map {
                                val targetMonth = startDate.plusMonths(it.toLong())
                                val monthValue = chartUiState.records.filter { r ->
                                    try {
                                        val recordDate = LocalDate.parse(r.date)
                                        recordDate.year == targetMonth.year && recordDate.month == targetMonth.month
                                    } catch(e: Exception) { false }
                                }.sumOf { it.value }.toFloat()
                                Entry(it.toFloat(), monthValue)
                            }
                        }
                    }
                } catch (e: Exception) {
                    emptyList<Entry>()
                }

                if (rawEntries.isEmpty()) return@AndroidView

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
                                    today.minusDays((rawEntries.size - 1 - idx).toLong()).format(DateTimeFormatter.ofPattern("dd MMM"))
                                }
                                ChartTimeSpan.YEAR -> {
                                    today.minusMonths((rawEntries.size - 1 - idx).toLong()).format(DateTimeFormatter.ofPattern("MMM"))
                                }
                            }
                        } catch (e: Exception) { "" }
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
