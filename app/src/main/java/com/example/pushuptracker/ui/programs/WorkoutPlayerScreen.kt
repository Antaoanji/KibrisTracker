package com.example.pushuptracker.ui.programs

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.pushuptracker.model.Exercise
import com.example.pushuptracker.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutPlayerScreen(navController: NavController, viewModel: WorkoutPlayerViewModel = hiltViewModel()) {
    val state by viewModel.workoutState.collectAsStateWithLifecycle()
    val isSwapping by viewModel.isSwapping.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedExerciseForInfo by remember { mutableStateOf<Exercise?>(null) }

    val currentExerciseImage = when (val s = state) {
        is WorkoutState.InProgress -> s.exercise.imageUrl
        is WorkoutState.Resting -> s.nextExercise.imageUrl
        else -> null
    }

    Scaffold(
        containerColor = Color.Transparent,
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
                    IconButton(onClick = { navController.navigateUp() }) {
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
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (currentExerciseImage != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(currentExerciseImage).build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().blur(32.dp),
                    contentScale = ContentScale.Crop
                )
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)))
            }

            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                when (val currentState = state) {
                    is WorkoutState.Loading -> CircularProgressIndicator()
                    is WorkoutState.InProgress -> ExerciseScreen(
                        exercise = currentState.exercise,
                        set = currentState.currentSet,
                        historyHint = currentState.historyHint,
                        coachSuggestion = currentState.coachSuggestion,
                        isSwapping = isSwapping,
                        onSwap = { viewModel.swapCurrentExercise() },
                        onSetFinished = { weight, difficulty, note -> viewModel.onSetFinished(weight, difficulty, note) }
                    )
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
                        totalVolume = currentState.totalVolume,
                        dominantDifficulty = currentState.dominantDifficulty,
                        onNavigateToPrograms = { 
                            navController.navigate(Screen.Programs.route) {
                                popUpTo(Screen.Programs.route) { inclusive = true }
                            }
                        },
                        onNavigateToHome = { 
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                         }
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
fun ExerciseScreen(
    exercise: Exercise,
    set: Int,
    historyHint: String?,
    coachSuggestion: String?,
    isSwapping: Boolean,
    onSwap: () -> Unit,
    onSetFinished: (weightUsed: Double?, difficulty: String, note: String?) -> Unit
) {
    val scope = rememberCoroutineScope()
    var buttonClicked by remember(exercise.searchKey, set) { mutableStateOf(false) }
    
    // Parse weight and note from historyHint if available
    val initialWeight = remember(historyHint) {
        historyHint?.substringAfter("@ ", "")?.substringBefore(" kg") ?: ""
    }
    val initialNote = remember(historyHint) {
        historyHint?.substringAfter("(", "")?.substringBefore(")") ?: ""
    }

    var weightInput by remember(exercise.searchKey) { mutableStateOf(initialWeight) }
    var noteInput by remember(exercise.searchKey) { mutableStateOf(initialNote) }

    val oneRepMax = remember(weightInput, exercise.reps) {
        val weight = weightInput.toDoubleOrNull() ?: 0.0
        val reps = exercise.reps.split("-").first().toIntOrNull() ?: 10
        if (weight > 0) {
            (weight * (1 + (reps.toDouble() / 30.0))).roundToInt()
        } else {
            0
        }
    }

    val initialProgress = remember(exercise.sets, set) { if (exercise.sets > 0) (set - 1).toFloat() / exercise.sets.toFloat() else 0f }
    val finalProgress = remember(exercise.sets, set) { if (exercise.sets > 0) set.toFloat() / exercise.sets.toFloat() else 0f }

    val animatedProgress by animateFloatAsState(
        targetValue = if (buttonClicked) finalProgress else initialProgress,
        animationSpec = tween(durationMillis = 750),
        label = "Set Progress Animation"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val gradientBrush = remember(exercise.sets, primaryColor) {
        val colors = if (exercise.sets > 1) {
            val baseColors = (0 until exercise.sets).map { i ->
                lerp(Color(0xFFE91E63), primaryColor, i.toFloat() / exercise.sets)
            }
            baseColors + baseColors.first()
        } else {
            listOf(primaryColor, primaryColor)
        }
        Brush.sweepGradient(colors = colors)
    }

    var isPulsing by remember { mutableStateOf(false) }
    val pulseScale by animateFloatAsState(
        targetValue = if (isPulsing) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "Pulse Animation",
        finishedListener = { isPulsing = false }
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Spacer(Modifier.weight(1f))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(250.dp)
                .scale(pulseScale)
        ) {
            val strokeWidth = 16.dp
            val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx())
                )
                drawArc(
                    brush = gradientBrush,
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$set. Set",
                        style = MaterialTheme.typography.displaySmall.copy(
                            color = Color.White,
                            shadow = Shadow(Color.Black, blurRadius = 8f)
                        )
                    )
                    if (isSwapping) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp).padding(start = 8.dp), strokeWidth = 2.dp)
                    } else {
                        IconButton(onClick = onSwap) {
                            Icon(Icons.Default.Refresh, contentDescription = "Egzersizi Değiştir", tint = Color.White.copy(alpha = 0.7f))
                        }
                    }
                }
                Text(
                    text = "Hedef: ${exercise.reps}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White.copy(alpha = 0.8f),
                        shadow = Shadow(Color.Black, blurRadius = 8f)
                    )
                )
            }
        }
        
        Spacer(Modifier.weight(1f))

        if (historyHint != null) {
            Text(
                text = historyHint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        
        if (coachSuggestion != null) {
            Text(
                text = "💡 $coachSuggestion",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                 modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = weightInput,
                onValueChange = { weightInput = it },
                label = { Text("Ağırlık (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = noteInput,
                onValueChange = { noteInput = it },
                label = { Text("Not (Plaka/Ayar)") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        if (oneRepMax > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "✨ Tahmini 1RM: $oneRepMax kg",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        Spacer(Modifier.height(16.dp))

        // Note: Weight is no longer strictly required if a note is provided, but at least one should be present or we can allow empty.
        // Let's keep it enabled if user wants to just record reps.
        val canFinish = !buttonClicked

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (!buttonClicked) {
                        buttonClicked = true
                        scope.launch { 
                            delay(800L)
                            onSetFinished(weightInput.toDoubleOrNull(), "easy", noteInput)
                        }
                    }
                },
                enabled = canFinish,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                modifier = Modifier.weight(1f)
            ) {
                Text("Kolay")
            }
            Button(
                onClick = {
                    if (!buttonClicked) {
                        buttonClicked = true
                        scope.launch { 
                            delay(800L)
                            onSetFinished(weightInput.toDoubleOrNull(), "medium", noteInput)
                        }
                    }
                },
                enabled = canFinish,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                modifier = Modifier.weight(1f)
            ) {
                Text("Orta")
            }
            Button(
                onClick = {
                    if (!buttonClicked) {
                        buttonClicked = true
                        scope.launch { 
                            delay(800L)
                            onSetFinished(weightInput.toDoubleOrNull(), "hard", noteInput)
                        }
                    }
                },
                enabled = canFinish,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                modifier = Modifier.weight(1f)
            ) {
                Text("Zor")
            }
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
                Text("+15 sn Ekle")
            }
        }
    }
}

@Composable
fun FinishedScreen(
    totalTimeMinutes: Int, 
    caloriesBurned: Int,
    totalVolume: Double,
    dominantDifficulty: String,
    onNavigateToPrograms: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        Text("Tebrikler!", style = MaterialTheme.typography.displayMedium.copy(color = Color.White, shadow = Shadow(Color.Black, blurRadius = 8f)))
        Text("Antrenman Karnen", style = MaterialTheme.typography.titleLarge.copy(color = Color.White.copy(alpha = 0.8f)))
        Spacer(Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            InfoCard(title = "Süre", value = "$totalTimeMinutes dk", modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.padding(8.dp))
            InfoCard(title = "Yakılan Kalori", value = "≈ $caloriesBurned kcal", modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            val volumeText = if (totalVolume > 1000) "${(totalVolume / 1000).toInt()} Ton" else "${totalVolume.toInt()} kg"
            InfoCard(title = "Toplam Yük", value = volumeText, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.padding(8.dp))
            InfoCard(title = "Hissiyat", value = dominantDifficulty, modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(48.dp))
        
        Button(onClick = onNavigateToPrograms, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("Programa Dön", style = MaterialTheme.typography.titleLarge)
        }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onNavigateToHome, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("Ana Sayfa", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
fun InfoCard(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium.copy(color = Color.White.copy(alpha = 0.8f)))
        Text(text = value, style = MaterialTheme.typography.displaySmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
    }
}
