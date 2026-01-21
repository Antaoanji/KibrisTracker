package com.example.pushuptracker.ui.programs

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.pushuptracker.model.Badge
import com.example.pushuptracker.model.Exercise
import com.example.pushuptracker.navigation.Screen
import com.example.pushuptracker.util.YoutubeUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutPlayerScreen(
    navController: NavController, 
    viewModel: WorkoutPlayerViewModel = hiltViewModel()
) {
    val state by viewModel.workoutState.collectAsStateWithLifecycle()
    val isSwapping by viewModel.isSwapping.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedExerciseForInfo by remember { mutableStateOf<Exercise?>(null) }

    // --- CELEBRATION STATE ---
    var activeBadge by remember { mutableStateOf<Badge?>(null) }
    
    LaunchedEffect(Unit) {
        viewModel.newBadgeUnlocked.collect { badge ->
            activeBadge = badge
            delay(5000)
            activeBadge = null
        }
    }

    val currentView = LocalView.current
    DisposableEffect(Unit) {
        currentView.keepScreenOn = true
        onDispose {
            currentView.keepScreenOn = false
        }
    }

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
                    Text(
                        text = titleText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    if (state is WorkoutState.InProgress) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.moveToPreviousExercise() }) {
                                Icon(Icons.Default.SkipPrevious, contentDescription = "Önceki")
                            }
                            IconButton(onClick = { viewModel.moveToNextExercise() }) {
                                Icon(Icons.Default.SkipNext, contentDescription = "Sonraki")
                            }
                            IconButton(onClick = {
                                selectedExerciseForInfo = (state as WorkoutState.InProgress).exercise
                                showBottomSheet = true
                            }) {
                                Icon(Icons.Default.Info, contentDescription = "Bilgi")
                            }
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
                        remainingTime = currentState.remainingExerciseTime,
                        isTimerPaused = currentState.isTimerPaused,
                        isSwapping = isSwapping,
                        onSwap = { viewModel.swapCurrentExercise() },
                        onRestartTimer = { viewModel.restartExerciseTimer() },
                        onToggleTimer = { viewModel.toggleExerciseTimer() },
                        onSetFinished = { weight, difficulty, note, reps ->
                            viewModel.onSetFinished(weight, difficulty, note, reps)
                        }
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

            AnimatedVisibility(
                visible = activeBadge != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 80.dp).padding(horizontal = 16.dp)
            ) {
                activeBadge?.let { badge ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(
                                    "Başarım Kazandın!",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    stringResource(badge.title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    stringResource(badge.descriptionRes),
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showBottomSheet && selectedExerciseForInfo != null) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = selectedExerciseForInfo?.name ?: "",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(Modifier.height(16.dp))

                    val videoId = YoutubeUtils.getYoutubeId(selectedExerciseForInfo?.videoUrl)
                    if (videoId.isNotEmpty()) {
                        YoutubePlayerComposable(videoId = videoId)
                        Spacer(Modifier.height(16.dp))
                    }

                    Text(
                        text = "Nasıl Yapılır?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = selectedExerciseForInfo?.description ?: "Açıklama bulunamadı.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Spacer(Modifier.height(32.dp))
                    
                    Button(
                        onClick = { scope.launch { sheetState.hide() }.invokeOnCompletion { showBottomSheet = false } },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Anladım")
                    }
                    Spacer(Modifier.height(16.dp))
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
    remainingTime: Int?,
    isTimerPaused: Boolean,
    isSwapping: Boolean,
    onSwap: () -> Unit,
    onRestartTimer: () -> Unit,
    onToggleTimer: () -> Unit,
    onSetFinished: (weightUsed: Double?, difficulty: String, note: String?, actualReps: Int?) -> Unit
) {
    val scope = rememberCoroutineScope()
    var buttonClicked by remember(exercise.name, set) { mutableStateOf(false) }

    val isWarmup = remember(exercise.name) {
        val warmupKeywords = listOf(
            "Isınma", "Çevirme", "Döndürme", "Jumping", "Kedi-Deve",
            "Scapular", "Duvarda", "Leg Swing", "Pull Apart",
            "Bodyweight Squat", "Melek", "Stretch"
        )
        warmupKeywords.any { exercise.name.contains(it, ignoreCase = true) } || remainingTime != null
    }

    val initialWeight = remember(historyHint) {
        historyHint?.substringAfter("@ ", "")?.substringBefore(" kg") ?: ""
    }
    val initialNote = remember(historyHint) {
        historyHint?.substringAfter("(", "")?.substringBefore(")") ?: ""
    }

    var weightInput by remember(exercise.name) { mutableStateOf(initialWeight) }
    var noteInput by remember(exercise.name) { mutableStateOf(initialNote) }
    var repsInput by remember(exercise.name, set) { mutableStateOf("") }

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
        modifier = Modifier.padding(16.dp).fillMaxSize()
    ) {
        Spacer(Modifier.weight(1f))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(260.dp).scale(pulseScale)
        ) {
            val strokeWidth = 16.dp
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = Color.White.copy(alpha = 0.2f),
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

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (remainingTime != null) {
                    Text(
                        text = "$remainingTime",
                        style = MaterialTheme.typography.displayLarge.copy(
                            color = Color.White,
                            fontSize = 90.sp,
                            shadow = Shadow(Color.Black, blurRadius = 12f)
                        )
                    )
                    Text(
                        text = if (isTimerPaused) "Duraklatıldı" else "Saniye Kaldı",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        IconButton(onClick = onRestartTimer) {
                            Icon(Icons.Default.Refresh, contentDescription = "Başa Sar", tint = Color.White.copy(alpha = 0.7f))
                        }
                        IconButton(onClick = onToggleTimer) {
                            Icon(if (isTimerPaused) Icons.Default.PlayArrow else Icons.Default.Pause, contentDescription = "Durdur", tint = Color.White.copy(alpha = 0.7f))
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isWarmup) "Isınma" else "$set. Set",
                            style = MaterialTheme.typography.displaySmall.copy(
                                color = Color.White,
                                shadow = Shadow(Color.Black, blurRadius = 8f)
                            )
                        )
                        if (isSwapping) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp).padding(start = 8.dp), strokeWidth = 2.dp)
                        } else if (!isWarmup) {
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
        }

        Spacer(Modifier.weight(1f))

        if (!isWarmup && (historyHint != null || coachSuggestion != null)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(bottom = 12.dp)) {
                if (historyHint != null) {
                    Text(text = historyHint, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                }
                if (coachSuggestion != null) {
                    Text(text = "💡 $coachSuggestion", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        if (!isWarmup) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text("Ağırlık (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    if (exercise.reps.contains("MAX", ignoreCase = true)) {
                        OutlinedTextField(
                            value = repsInput,
                            onValueChange = { repsInput = it },
                            label = { Text("Yapılan") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = { Text("Not (Plaka/Ayar)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        if (isWarmup) {
            Button(
                onClick = {
                    if (!buttonClicked) {
                        buttonClicked = true
                        scope.launch { delay(500L); onSetFinished(null, "medium", "Isınma Tamamlandı", null) }
                    }
                },
                enabled = !buttonClicked,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Tamamla", style = MaterialTheme.typography.titleLarge)
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (!buttonClicked) {
                            buttonClicked = true
                            scope.launch { delay(800L); onSetFinished(weightInput.toDoubleOrNull(), "easy", noteInput, repsInput.toIntOrNull()) }
                        }
                    },
                    enabled = !buttonClicked,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    modifier = Modifier.weight(1f)
                ) { Text("Kolay") }
                Button(
                    onClick = {
                        if (!buttonClicked) {
                            buttonClicked = true
                            scope.launch { delay(800L); onSetFinished(weightInput.toDoubleOrNull(), "medium", noteInput, repsInput.toIntOrNull()) }
                        }
                    },
                    enabled = !buttonClicked,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                    modifier = Modifier.weight(1f)
                ) { Text("Orta") }
                Button(
                    onClick = {
                        if (!buttonClicked) {
                            buttonClicked = true
                            scope.launch { delay(800L); onSetFinished(weightInput.toDoubleOrNull(), "hard", noteInput, repsInput.toIntOrNull()) }
                        }
                    },
                    enabled = !buttonClicked,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                    modifier = Modifier.weight(1f)
                ) { Text("Zor") }
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
        Text(
            text = "Sıradaki: $nextExerciseName",
            style = MaterialTheme.typography.headlineSmall.copy(
                color = Color.White,
                shadow = Shadow(Color.Black, blurRadius = 8f)
            )
        )
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
            val volumeText = if (totalVolume > 1000) "${(totalVolume / 1000).toInt()} Ton" else "${totalVolume.toInt()} kg"
            InfoCard(title = "Toplam Yük", value = volumeText, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            InfoCard(title = "Hissiyat", value = dominantDifficulty, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(onClick = onNavigateToPrograms, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("Programa Dön", style = MaterialTheme.typography.titleLarge)
        }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onNavigateToHome, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("Ana Sayfa", style = MaterialTheme.typography.titleLarge, color = Color.White)
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
