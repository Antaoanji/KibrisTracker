package com.example.pushuptracker.ui.programs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
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
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    if (uiState.showEquipmentDialog) {
        EquipmentSelectionDialog(
            onDismiss = { viewModel.dismissEquipmentDialog() },
            onConfirm = { equipments -> viewModel.generateWeeklyWorkoutPlan(equipments) }
        )
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        if (uiState.isLoading) {
            LoadingState()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceAround,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (uiState.structuredWorkout != null) {
                    WorkoutPlanDisplay(
                        workout = uiState.structuredWorkout!!,
                        currentDay = uiState.currentDay,
                        onStartClick = {
                            viewModel.onStartWorkoutClicked()
                            navController.navigate(Screen.WorkoutPlayer.route)
                        },
                        onBack = { viewModel.clearGeneratedWorkout() }
                    )
                } else {
                    if (uiState.lastWorkoutSummary == null) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("İlk antrenmanına başla!", style = MaterialTheme.typography.titleLarge)
                            }
                        }
                    } else {
                        LastWorkoutSummaryCard(summary = uiState.lastWorkoutSummary!!)
                    }
                }
                GenerateWorkoutCard(onClick = { viewModel.showEquipmentDialog() })

                if (uiState.structuredWorkout == null) {
                    OutlinedButton(
                        onClick = { navController.navigate(Screen.WorkoutPlayer.route) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.currentDay > 1
                    ) {
                        val buttonText = if (uiState.currentDay > 1) {
                            "${uiState.currentDay}. Güne Devam Et"
                        } else {
                            "Aktif Antrenmanınız Yok"
                        }
                        Text(buttonText)
                    }
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
fun LastWorkoutSummaryCard(summary: WorkoutSummary) {
    val date = Instant.ofEpochMilli(summary.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
    val today = LocalDate.now()
    val dateText = when (ChronoUnit.DAYS.between(date, today)) {
        0L -> "Bugün"
        1L -> "Dün"
        else -> date.format(DateTimeFormatter.ofPattern("dd MMMM"))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Tamamlandı", tint = MaterialTheme.colorScheme.tertiary)
                Spacer(Modifier.size(8.dp))
                Text("Son Antrenman", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.weight(1f))
                Text(dateText, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(16.dp))
            Text(summary.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                InfoChip(icon = Icons.Default.Timer, text = "${summary.totalTimeMinutes} dakika")
                InfoChip(icon = Icons.Default.LocalFireDepartment, text = "≈ ${summary.caloriesBurned} kcal")
            }
        }
    }
}

@Composable
fun GenerateWorkoutCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column {
            Image(
                painter = painterResource(id = R.drawable.workout_main),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Text("Yeni Antrenman Oluştur", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun WorkoutPlanDisplay(workout: Workout, currentDay: Int, onStartClick: () -> Unit, onBack: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(workout.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            workout.exercises.forEach { exercise ->
                if (exercise.sets == 0) { // Day Header
                    Text(
                        exercise.name, 
                        style = MaterialTheme.typography.titleLarge, 
                        fontWeight = FontWeight.Bold, 
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
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
            Spacer(Modifier.height(24.dp))
            Button(onClick = onStartClick, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text("$currentDay. Gün Antrenmanını Başlat", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.size(8.dp))
                Text("Yeni Plan Oluştur")
            }
        }
    }
}

@Composable
fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.size(6.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge)
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
