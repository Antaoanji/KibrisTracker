package com.example.pushuptracker.ui.programs

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.pushuptracker.R
import com.example.pushuptracker.data.WorkoutData
import com.example.pushuptracker.model.Workout
import com.example.pushuptracker.model.WorkoutSummary
import com.example.pushuptracker.navigation.Screen
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramsScreen(
    navController: NavController,
    viewModel: ProgramsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val eventState by viewModel.eventState.collectAsStateWithLifecycle()
    val workoutDetails by viewModel.workoutDetails.collectAsStateWithLifecycle()

    var showInfoSheet by remember { mutableStateOf(false) }
    var selectedDayIndex by remember { mutableIntStateOf(-1) }
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(eventState.navigateToPlayer) {
        if (eventState.navigateToPlayer) {
            navController.navigate(Screen.WorkoutPlayer.route)
            viewModel.onNavigationHandled()
        }
    }

    val currentDayOfWeek = LocalDate.now().dayOfWeek
    val todayIndex = when (currentDayOfWeek) {
        DayOfWeek.MONDAY -> 0
        DayOfWeek.TUESDAY -> 1
        DayOfWeek.WEDNESDAY -> 2
        DayOfWeek.FRIDAY -> 3
        DayOfWeek.SATURDAY -> 4
        else -> -1
    }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item { LastWorkoutSummaryCard(summary = uiState.lastWorkoutSummary) }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Antrenman Programın",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ProgramSelectCard(
                            title = "Makine + Ağırlık",
                            imageRes = R.drawable.workout_resume,
                            isSelected = uiState.selectedProgram == ProgramType.MACHINE_WEIGHT,
                            alignment = Alignment.TopCenter,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.selectProgram(ProgramType.MACHINE_WEIGHT) }
                        )
                        ProgramSelectCard(
                            title = "Calisthenics + Ağırlık",
                            imageRes = R.drawable.workout_before,
                            isSelected = uiState.selectedProgram == ProgramType.CALISTHENICS_WEIGHT,
                            alignment = Alignment.Center,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.selectProgram(ProgramType.CALISTHENICS_WEIGHT) }
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Haftalık Akış",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            val days = listOf("Pazartesi (PUSH)", "Salı (PULL)", "Çarşamba (LEGS)", "Cuma (UPPER)", "Cumartesi (LOWER)")
            itemsIndexed(days) { index, dayName ->
                DayWorkoutCard(
                    dayName = dayName,
                    isToday = index == todayIndex,
                    onInfoClick = {
                        selectedDayIndex = index
                        viewModel.loadWorkoutDetails(index)
                        showInfoSheet = true
                    },
                    onStartClick = { viewModel.onDaySelected(index) }
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        if (showInfoSheet && workoutDetails != null) {
            ModalBottomSheet(
                onDismissRequest = { showInfoSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                WorkoutInfoContent(
                    workout = workoutDetails!!,
                    onResetToDefault = { viewModel.resetProgramToDefault(selectedDayIndex) },
                    navController = navController,
                    viewModel = viewModel,
                    dayIndex = selectedDayIndex,
                    onCloseSheet = { showInfoSheet = false }
                )
            }
        }
    }
}

@Composable
fun WorkoutInfoContent(
    workout: Workout, 
    onResetToDefault: () -> Unit,
    navController: NavController,
    viewModel: ProgramsViewModel,
    dayIndex: Int,
    onCloseSheet: () -> Unit
) {
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = workout.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onResetToDefault) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Varsayılana Dön", style = MaterialTheme.typography.labelMedium)
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        LazyColumn(
            modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(workout.exercises) { _, exercise ->
                val isWarmup = exercise.name.contains("Isınma", ignoreCase = true) || 
                               exercise.reps.contains("Dakika") ||
                               exercise.reps.contains("Saniye")

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isWarmup) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = exercise.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            if (isWarmup) {
                                Text("Isınma Hareketi", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "${exercise.sets} Set", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text(text = exercise.reps, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        Button(
            onClick = {
                viewModel.onEditRequested(dayIndex) { workoutId ->
                    onCloseSheet()
                    navController.navigate(Screen.WorkoutEditor.createRoute(workoutId))
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Edit, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("PROGRAMI DÜZENLE", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ProgramSelectCard(
    title: String,
    imageRes: Int,
    isSelected: Boolean,
    alignment: Alignment = Alignment.Center,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(if (isSelected) 8.dp else 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alignment = alignment,
                alpha = if (isSelected) 1f else 0.6f
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        )
                    )
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
            )
        }
    }
}

@Composable
fun DayWorkoutCard(dayName: String, isToday: Boolean, onInfoClick: () -> Unit, onStartClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val borderModifier = if (isToday) {
        Modifier.border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = alpha), RoundedCornerShape(16.dp))
    } else Modifier

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .then(borderModifier)
            .shadow(if (isToday) 8.dp else 0.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val split = dayName.split(" (")
                Text(
                    text = split[0],
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Bold,
                    color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                if (split.size > 1) {
                    Text(
                        text = split[1].replace(")", ""),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onInfoClick) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Detaylar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                
                Spacer(Modifier.width(4.dp))
                
                Button(
                    onClick = onStartClick,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("BAŞLAT", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun LastWorkoutSummaryCard(summary: WorkoutSummary?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val isEnabled = summary != null
            Image(
                painter = painterResource(id = R.drawable.workout_main),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
                alpha = if (isEnabled) 1.0f else 0.4f
            )
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
            )
            if (summary != null) {
                 val date = Instant.ofEpochMilli(summary.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
                val today = LocalDate.now()
                val dateText = when (ChronoUnit.DAYS.between(date, today)) {
                    0L -> "Bugün"
                    1L -> "Dün"
                    else -> date.format(DateTimeFormatter.ofPattern("dd MMMM"))
                }

                Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Tamamlandı", tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.size(8.dp))
                        Text("Son Antrenman", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(Modifier.weight(1f))
                        Text(dateText, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                    }
                    Column {
                        Text(summary.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            InfoChip(icon = Icons.Default.Timer, text = "${summary.totalTimeMinutes} dk", color = Color.White)
                            InfoChip(icon = Icons.Default.LocalFireDepartment, text = "≈ ${summary.caloriesBurned} kcal", color = Color.White)
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Henüz bir antrenman tamamlamadın.", 
                        style = MaterialTheme.typography.titleLarge, 
                        textAlign = TextAlign.Center, 
                        modifier = Modifier.padding(16.dp),
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = color)
        Spacer(Modifier.size(4.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = color)
    }
}
