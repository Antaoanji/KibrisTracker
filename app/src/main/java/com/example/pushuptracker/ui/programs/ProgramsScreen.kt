package com.example.pushuptracker.ui.programs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.pushuptracker.model.Workout

@Composable
fun ProgramsScreen(
    navController: NavController, 
    viewModel: ProgramsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold {
            padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.isLoading) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (uiState.structuredWorkout != null) {
                item {
                    WorkoutPlanDisplay(
                        workout = uiState.structuredWorkout!!, 
                        onStartClick = {
                            viewModel.onStartWorkout(uiState.structuredWorkout!!)
                            navController.navigate("workout_player")
                        }
                    )
                }
            } else {
                // Display the selector directly in the LazyColumn
                item {
                    Text(
                        text = "Antrenman Programı Oluştur",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
                item {
                    Text(
                        text = "Yapay zekadan, profilinize özel bir antrenman programı oluşturmasını isteyin. Lütfen odaklanmak istediğiniz vücut bölgesini seçin.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                val bodyParts = listOf("Tüm Vücut", "Kol", "Göğüs", "Sırt", "Bacak", "Karın")
                items(bodyParts.chunked(2)) { rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        rowItems.forEach { bodyPart ->
                            BodyPartCard(bodyPart = bodyPart, onClick = { viewModel.generateWorkoutPlan(bodyPart) }, modifier = Modifier.weight(1f))
                        }
                        if (rowItems.size == 1) {
                            Box(modifier = Modifier.weight(1f)) {}
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyPartCard(bodyPart: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = bodyPart,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun WorkoutPlanDisplay(workout: Workout, onStartClick: () -> Unit) {
    Column {
        Text(
            text = workout.title,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        // You can display a summary of exercises here if you want
        workout.exercises.forEach {
            Text("• ${it.name}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 4.dp))
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onStartClick,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Antrenmanı Başlat", style = MaterialTheme.typography.titleMedium)
        }
    }
}
