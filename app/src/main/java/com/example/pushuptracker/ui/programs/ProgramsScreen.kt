package com.example.pushuptracker.ui.programs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.pushuptracker.R
import com.example.pushuptracker.model.Workout
import com.example.pushuptracker.model.WorkoutSummary
import com.example.pushuptracker.navigation.Screen
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun ProgramsScreen(
    navController: NavController,
    viewModel: ProgramsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val eventState by viewModel.eventState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(eventState.errorMessage) {
        eventState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    if (eventState.showEquipmentDialog) {
        EquipmentSelectionDialog(
            onDismiss = { viewModel.dismissEquipmentDialog() },
            onConfirm = { equipments -> viewModel.generateWeeklyWorkoutPlan(equipments) }
        )
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        if (uiState.isLoading || eventState.isLoading) {
            LoadingState()
        } else if (uiState.structuredWorkout != null && eventState.displayingPlanDetails) {
            WorkoutPlanDisplay(
                workout = uiState.structuredWorkout!!,
                currentDay = uiState.currentDay,
                onStartClick = {
                    viewModel.onStartWorkoutClicked()
                    navController.navigate(Screen.WorkoutPlayer.route)
                },
                onBack = { viewModel.onPlanDetailsDismissed() }
            )
        } else {
            // Main Programs Screen Layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp), // Add spacing between cards
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Section
                LastWorkoutSummaryCard(
                    modifier = Modifier.weight(1f), // Make card take up space
                    summary = uiState.lastWorkoutSummary
                )
                
                // Middle Section
                GenerateWorkoutCard(
                    modifier = Modifier.weight(1f), // Make card take up space
                    onClick = { viewModel.showEquipmentDialog() })

                // Bottom Section
                ContinueWorkoutCard(
                    modifier = Modifier.weight(1f), // Make card take up space
                    uiState = uiState,
                    onClick = {
                        if (uiState.structuredWorkout != null) {
                            viewModel.onStartWorkoutClicked()
                            navController.navigate(Screen.WorkoutPlayer.route)
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Aktif antrenmanınız yok. Lütfen yeni bir antrenman oluşturun.")
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ContinueWorkoutCard(modifier: Modifier = Modifier, uiState: ProgramScreenUiState, onClick: () -> Unit) {
    val isEnabled = uiState.structuredWorkout != null
    val text = if (isEnabled) "${uiState.currentDay}. Gün Antrenmanına Devam Et" else "Aktif Antrenman Yok"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomStart) {
            Image(
                painter = painterResource(id = R.drawable.workout_resume),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter, // Aligns the image to the top
                alpha = if (isEnabled) 1.0f else 0.4f
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)),
                            startY = 300f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                if (isEnabled) {
                    Icon(
                        Icons.AutoMirrored.Filled.DirectionsRun,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingState() {
    val lottieComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading))

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(
            composition = lottieComposition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(150.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Antrenman planınız oluşturuluyor...",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LastWorkoutSummaryCard(modifier: Modifier = Modifier, summary: WorkoutSummary?) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val isEnabled = summary != null
            Image(
                painter = painterResource(id = R.drawable.workout_before),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
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
                            InfoChip(icon = Icons.Default.Timer, text = "${summary.totalTimeMinutes} dakika", color = Color.White)
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
fun GenerateWorkoutCard(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomStart) {
            Image(
                painter = painterResource(id = R.drawable.workout_main),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
             Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)),
                            startY = 300f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )
            Text(
                text = "Yeni Antrenman Oluştur",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun WorkoutPlanDisplay(workout: Workout, currentDay: Int, onStartClick: () -> Unit, onBack: () -> Unit) {
    Scaffold(
        topBar = { 
            Box(modifier = Modifier.padding(16.dp)){
                 Text(workout.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
        },
        bottomBar = {
            Column(modifier = Modifier.padding(16.dp)){
                Button(onClick = onStartClick, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Text("Hadi Başlayalım! ($currentDay. Gün)", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text("Geri")
                }
            }
        }
    ) {
        padding ->
         LazyColumn(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {
            items(workout.exercises) { exercise ->
                if (exercise.sets == 0) { // Day Header
                    Text(
                        exercise.name, 
                        style = MaterialTheme.typography.titleLarge, 
                        fontWeight = FontWeight.Bold, 
                        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                    )
                } else { // Exercise Item
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp), 
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        ) {}
                        Spacer(Modifier.size(12.dp))
                        Column {
                            Text(exercise.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${exercise.sets} set x ${exercise.reps} tekrar, ${exercise.restTimeSeconds} sn dinlenme",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, color: Color = MaterialTheme.colorScheme.secondary) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = color)
        Spacer(Modifier.size(6.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, color = color)
    }
}

@Composable
fun EquipmentSelectionDialog(onDismiss: () -> Unit, onConfirm: (List<String>) -> Unit) {
    val equipmentOptions = listOf("Vücut Ağırlığı", "Dumbbell", "Halter", "Direnç Bandı", "Makineler")
    val selectedEquipments = remember { mutableStateOf(setOf<String>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ekipmanlarınızı Seçin") },
        text = {
            Column {
                equipmentOptions.forEach { equipment ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                val currentSelection = selectedEquipments.value
                                selectedEquipments.value = if (currentSelection.contains(equipment)) currentSelection - equipment else currentSelection + equipment 
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        Checkbox(
                            checked = selectedEquipments.value.contains(equipment), 
                            onCheckedChange = { isChecked -> 
                                val currentSelection = selectedEquipments.value
                                selectedEquipments.value = if (isChecked) currentSelection + equipment else currentSelection - equipment 
                            }
                        )
                        Text(equipment, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedEquipments.value.toList()) }) { Text("Program Oluştur") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}
