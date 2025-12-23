package com.example.pushuptracker.ui.programs

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.example.pushuptracker.model.Exercise
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutPlayerScreen(onNavigateUp: () -> Unit, viewModel: WorkoutPlayerViewModel = hiltViewModel()) {
    val state by viewModel.workoutState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedExerciseForInfo by remember { mutableStateOf<Exercise?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val titleText = when (val currentState = state) {
                        is WorkoutState.InProgress -> "${currentState.exercise.name} (${currentState.currentExerciseIndex}/${currentState.totalExercises})"
                        is WorkoutState.Resting -> "Dinlenme"
                        else -> "Antrenman"
                    }
                    Text(titleText)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                     if (state is WorkoutState.InProgress) {
                         IconButton(onClick = { 
                             selectedExerciseForInfo = (state as WorkoutState.InProgress).exercise
                             showBottomSheet = true 
                        }) {
                            Icon(Icons.Default.Info, contentDescription = "Egzersiz Bilgisi")
                        }
                     }
                }
            )
        }
    ) {
        padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when (val currentState = state) {
                is WorkoutState.Loading -> CircularProgressIndicator()
                is WorkoutState.InProgress -> ExerciseScreen(exercise = currentState.exercise, set = currentState.currentSet, onSetFinished = { viewModel.onSetFinished() })
                is WorkoutState.Resting -> RestScreen(
                    restTime = currentState.remainingTime,
                    initialDuration = currentState.initialDuration,
                    nextExerciseName = currentState.nextExercise.name,
                    onSkip = { viewModel.skipRest() },
                    onAddTime = { viewModel.addRestTime() }
                )
                is WorkoutState.Finished -> FinishedScreen(onFinish = onNavigateUp)
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(selectedExerciseForInfo?.name ?: "", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(16.dp))
                    Text(selectedExerciseForInfo?.description ?: "Açıklama bulunamadı.", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(32.dp))
                    Button(onClick = { scope.launch { sheetState.hide() }.invokeOnCompletion { showBottomSheet = false } }) {
                        Text("Anladım")
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseScreen(exercise: Exercise, set: Int, onSetFinished: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.padding(16.dp)) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(exercise.gifUrl)
                .decoderFactory(ImageDecoderDecoder.Factory()) // Important for GIFs
                .build(),
            contentDescription = exercise.name,
            modifier = Modifier.size(250.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(Modifier.height(24.dp))
        Text(text = "$set. Set", style = MaterialTheme.typography.displaySmall)
        Text(text = "Hedef: ${exercise.reps}", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))
        Button(onClick = onSetFinished, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("Seti Bitir", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
fun RestScreen(restTime: Int, initialDuration: Int, nextExerciseName: String, onSkip: () -> Unit, onAddTime: () -> Unit) {
    val progress by animateFloatAsState(
        targetValue = if (initialDuration > 0) restTime.toFloat() / initialDuration.toFloat() else 0f,
        label = "Rest Timer Progress"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.padding(16.dp)) {
        Text("Dinlen", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(16.dp))
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(250.dp)) {
            CircularProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxSize(), strokeWidth = 16.dp)
            Text("$restTime", style = MaterialTheme.typography.displayLarge)
        }
        Spacer(Modifier.height(16.dp))
        Text("Sıradaki: $nextExerciseName", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(32.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = onSkip, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                Text("Atla")
            }
            Button(onClick = onAddTime, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)) {
                Text("+30 sn Ekle")
            }
        }
    }
}

@Composable
fun FinishedScreen(onFinish: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Antrenman Bitti!", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onFinish, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("Harika!", style = MaterialTheme.typography.titleLarge)
        }
    }
}
