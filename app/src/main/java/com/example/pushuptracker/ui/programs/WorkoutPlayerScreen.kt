package com.example.pushuptracker.ui.programs

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.pushuptracker.R
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

    // Determine the background image based on the current state
    val currentExerciseImage = when (val s = state) {
        is WorkoutState.InProgress -> s.exercise.imageUrl
        is WorkoutState.Resting -> s.nextExercise.imageUrl
        else -> null
    }

    Scaffold(
        containerColor = Color.Transparent, // Make scaffold background transparent
        topBar = {
            TopAppBar(
                title = {
                    val titleText = when (val currentState = state) {
                        is WorkoutState.InProgress -> "${currentState.exercise.name} (${currentState.currentExerciseIndex}/${currentState.totalExercises})"
                        is WorkoutState.Resting -> "Dinlenme"
                        is WorkoutState.Finished -> "Antrenman Tamamlandı"
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.4f),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) {
        padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Blurred Background
            if (currentExerciseImage != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(currentExerciseImage).build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().blur(32.dp),
                    contentScale = ContentScale.Crop
                )
                 // Dark overlay
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)))
            }

            // Main Content
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
                    is WorkoutState.Finished -> FinishedScreen(
                        totalTimeMinutes = currentState.totalTimeMinutes,
                        caloriesBurned = currentState.caloriesBurned,
                        onFinish = onNavigateUp
                    )
                }
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
        // Added a semi-transparent background to the image for better visibility
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(exercise.imageUrl).build(),
            contentDescription = exercise.name,
            modifier = Modifier.size(250.dp).background(Color.White.copy(alpha=0.1f), CircleShape).padding(16.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(Modifier.height(24.dp))
        Text(text = "$set. Set", style = MaterialTheme.typography.displaySmall.copy(color = Color.White, shadow = Shadow(Color.Black, blurRadius = 8f)))
        Text(text = "Hedef: ${exercise.reps}", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, shadow = Shadow(Color.Black, blurRadius = 8f)))
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
        Text("Dinlen", style = MaterialTheme.typography.displayMedium.copy(color = Color.White, shadow = Shadow(Color.Black, blurRadius = 8f)))
        Spacer(Modifier.height(16.dp))
        // Giant countdown timer
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(280.dp)) {
            CircularProgressIndicator(
                progress = { progress }, 
                modifier = Modifier.fillMaxSize(), 
                strokeWidth = 20.dp, 
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
            Text("$restTime", style = MaterialTheme.typography.displayLarge.copy(fontSize = 100.sp, color = Color.White, shadow = Shadow(Color.Black, blurRadius = 12f)))
        }
        Spacer(Modifier.height(16.dp))
        Text("Sıradaki: $nextExerciseName", style = MaterialTheme.typography.headlineSmall.copy(color = Color.White, shadow = Shadow(Color.Black, blurRadius = 8f)))
        Spacer(Modifier.height(32.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = onSkip, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                Text("Atla")
            }
            Button(onClick = onAddTime, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)) {
                Text("+15 sn Ekle") // Reduced added time for balance
            }
        }
    }
}

@Composable
fun FinishedScreen(totalTimeMinutes: Int, caloriesBurned: Int, onFinish: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.padding(16.dp)) {
        Text("Tebrikler!", style = MaterialTheme.typography.displayMedium.copy(color = Color.White, shadow = Shadow(Color.Black, blurRadius = 8f)))
        Text("Antrenmanı başarıyla tamamladın.", style = MaterialTheme.typography.titleMedium.copy(color = Color.White.copy(alpha = 0.8f)))
        Spacer(Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Süre", style = MaterialTheme.typography.titleMedium.copy(color = Color.White.copy(alpha = 0.8f)))
                Text("$totalTimeMinutes dk", style = MaterialTheme.typography.displaySmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Yakılan Kalori", style = MaterialTheme.typography.titleMedium.copy(color = Color.White.copy(alpha = 0.8f)))
                Text("≈ $caloriesBurned kcal", style = MaterialTheme.typography.displaySmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
            }
        }

        Spacer(Modifier.height(48.dp))
        Button(onClick = onFinish, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("Bitti", style = MaterialTheme.typography.titleLarge)
        }
    }
}
