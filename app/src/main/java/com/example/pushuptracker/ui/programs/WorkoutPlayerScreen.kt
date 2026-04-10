package com.example.pushuptracker.ui.programs

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.createBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.pushuptracker.R
import com.example.pushuptracker.model.Badge
import com.example.pushuptracker.model.Exercise
import com.example.pushuptracker.navigation.Screen
import com.example.pushuptracker.util.ShareUtils
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
    val selectedBar by viewModel.selectedBarWeight.collectAsStateWithLifecycle()
    val selectedPlates by viewModel.selectedPlates.collectAsStateWithLifecycle()
    val totalWeight by viewModel.totalCalculatedWeight.collectAsStateWithLifecycle()
    val machineMode by viewModel.machineMode.collectAsStateWithLifecycle()
    val machineLevel by viewModel.machineLevel.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedExerciseForInfo by remember { mutableStateOf<Exercise?>(null) }

    val currentView = LocalView.current
    DisposableEffect(Unit) {
        currentView.keepScreenOn = true
        onDispose { currentView.keepScreenOn = false }
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
                    Text(text = titleText, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleMedium)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    if (state is WorkoutState.InProgress) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.moveToPreviousExercise() }) { Icon(Icons.Default.SkipPrevious, null) }
                            IconButton(onClick = { viewModel.moveToNextExercise() }) { Icon(Icons.Default.SkipNext, null) }
                            IconButton(onClick = {
                                selectedExerciseForInfo = (state as WorkoutState.InProgress).exercise
                                viewModel.pauseWorkout()
                                showBottomSheet = true
                            }) { Icon(Icons.Default.Info, null) }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black.copy(alpha = 0.4f), titleContentColor = Color.White, navigationIconContentColor = Color.White, actionIconContentColor = Color.White)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (currentExerciseImage != null) {
                AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(currentExerciseImage).build(), contentDescription = null, modifier = Modifier.fillMaxSize().blur(32.dp), contentScale = ContentScale.Crop)
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)))
            }

            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                when (val currentState = state) {
                    is WorkoutState.Loading -> CircularProgressIndicator()
                    is WorkoutState.InProgress -> ExerciseScreen(
                        exercise = currentState.exercise,
                        category = currentState.category,
                        set = currentState.currentSet,
                        coachSuggestion = currentState.coachSuggestion,
                        remainingTime = currentState.remainingExerciseTime,
                        isTimerPaused = currentState.isTimerPaused,
                        selectedBar = selectedBar,
                        selectedPlates = selectedPlates,
                        totalWeight = totalWeight,
                        machineMode = machineMode,
                        machineLevel = machineLevel,
                        onBarSelect = { viewModel.selectBar(it) },
                        onPlateAdd = { w, c -> viewModel.addPlate(w, c) },
                        onPlateRemove = { viewModel.removePlateAt(it) },
                        onMachineModeSelect = { viewModel.setMachineMode(it) },
                        onMachineLevelSelect = { viewModel.setMachineLevel(it) },
                        onRestartTimer = { viewModel.restartExerciseTimer() },
                        onToggleTimer = { viewModel.toggleExerciseTimer() },
                        onSetFinished = { difficulty, note, reps ->
                            viewModel.onSetFinished(difficulty, note, reps)
                        }
                    )
                    is WorkoutState.Resting -> RestScreen(
                        restTime = currentState.remainingTime,
                        initialDuration = currentState.initialDuration,
                        nextExerciseName = currentState.nextExercise.name,
                        onSkip = { viewModel.skipRest() },
                        onAddTime = { viewModel.addRestTime() }
                    )
                    is WorkoutState.Finished -> {
                        val context = LocalContext.current
                        var showShareCard by remember { mutableStateOf(false) }
                        
                        FinishedScreen(
                            totalTimeMinutes = currentState.totalTimeMinutes,
                            totalVolume = currentState.totalVolume,
                            dominantDifficulty = currentState.dominantDifficulty,
                            workoutTitle = currentState.workoutTitle,
                            onNavigateToPrograms = { navController.navigate(Screen.Programs.route) { popUpTo(Screen.Programs.route) { inclusive = true } } },
                            onNavigateToHome = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } } },
                            onShare = { showShareCard = true }
                        )

                        if (showShareCard) {
                            ShareCardDialog(
                                workoutTitle = currentState.workoutTitle,
                                totalTimeMinutes = currentState.totalTimeMinutes,
                                totalVolume = currentState.totalVolume,
                                onDismiss = { showShareCard = false },
                                onCapture = { bitmap ->
                                    ShareUtils.shareBitmap(context, bitmap, "Antrenman Başarı Kartım")
                                    showShareCard = false
                                }
                            )
                        }
                    }
                }
            }
        }

        if (showBottomSheet && selectedExerciseForInfo != null) {
            ModalBottomSheet(onDismissRequest = { showBottomSheet = false; viewModel.resumeWorkout() }, sheetState = sheetState, containerColor = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState())) {
                    Text(text = selectedExerciseForInfo?.name ?: "", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    val videoId = YoutubeUtils.getYoutubeId(selectedExerciseForInfo?.videoUrl)
                    if (videoId.isNotEmpty()) { YoutubePlayerComposable(videoId = videoId); Spacer(Modifier.height(16.dp)) }
                    Text(text = "Nasıl Yapılır?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Text(text = selectedExerciseForInfo?.description ?: "Açıklama bulunamadı.", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(32.dp))
                    Button(onClick = { scope.launch { sheetState.hide() }.invokeOnCompletion { showBottomSheet = false; viewModel.resumeWorkout() } }, modifier = Modifier.fillMaxWidth()) { Text("Anladım") }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun ShareCardDialog(
    workoutTitle: String,
    totalTimeMinutes: Int,
    totalVolume: Double,
    onDismiss: () -> Unit,
    onCapture: (Bitmap) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF121212))
                .padding(2.dp)
                .border(1.dp, Color(0xFF00F5D4).copy(alpha = 0.3f), RoundedCornerShape(24.dp))
        ) {
            val view = LocalView.current
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF1E1E1E), Color(0xFF0A0A0A))
                        )
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFF00F5D4),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "ANTRENMAN TAMAMLANDI",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF00F5D4),
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = formatWorkoutTitle(workoutTitle),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SÜRE", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text("$totalTimeMinutes dk", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TOPLAM YÜK", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        val volumeText = if (totalVolume > 1000) "${(totalVolume / 1000).toInt()} Ton" else "${totalVolume.toInt()} kg"
                        Text(volumeText, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(Modifier.height(32.dp))
                Text(
                    text = "KIBRIS WORKOUT",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Light
                )
                
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        val bitmap = createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)
                        view.draw(canvas)
                        onCapture(bitmap)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F5D4), contentColor = Color.Black)
                ) {
                    Text("GÖRSELİ PAYLAŞ")
                }
            }
        }
    }
}

fun formatWorkoutTitle(title: String): String {
    return title.replace("MACHINE_WEIGHT_", "Makine + ")
        .replace("CALISTHENICS_WEIGHT_", "Cali + ")
        .replace("_", " ")
}

@Composable
fun ExerciseScreen(
    exercise: Exercise,
    category: ExerciseCategory,
    set: Int,
    coachSuggestion: String?,
    remainingTime: Int?,
    isTimerPaused: Boolean,
    selectedBar: Double,
    selectedPlates: List<Double>,
    totalWeight: Double,
    machineMode: String,
    machineLevel: Int,
    onBarSelect: (Double) -> Unit,
    onPlateAdd: (Double, Int) -> Unit,
    onPlateRemove: (Int) -> Unit,
    onMachineModeSelect: (String) -> Unit,
    onMachineLevelSelect: (Int) -> Unit,
    onRestartTimer: () -> Unit,
    onToggleTimer: () -> Unit,
    onSetFinished: (difficulty: String, note: String?, actualReps: Int?) -> Unit
) {
    val scope = rememberCoroutineScope()
    var buttonClicked by remember(exercise.name, set) { mutableStateOf(false) }
    var noteInput by remember(exercise.name) { mutableStateOf("") }
    var repsInput by remember(exercise.name, set) { mutableStateOf("") }
    var showNoteField by remember { mutableStateOf(false) }

    val initialProgress = if (exercise.sets > 0) (set - 1).toFloat() / exercise.sets.toFloat() else 0f
    val finalProgress = if (exercise.sets > 0) set.toFloat() / exercise.sets.toFloat() else 0f
    val animatedProgress by animateFloatAsState(targetValue = if (buttonClicked) finalProgress else initialProgress, animationSpec = tween(durationMillis = 750), label = "")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        // Üst Alan (Kaydırılabilir içerik)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // --- SAYAÇ / SET BİLGİSİ ---
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
                ComposeCanvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(color = Color.White.copy(alpha = 0.15f), startAngle = -90f, sweepAngle = 360f, useCenter = false, style = Stroke(width = 8.dp.toPx()))
                    drawArc(color = Color(0xFF00F5D4), startAngle = -90f, sweepAngle = animatedProgress * 360f, useCenter = false, style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (remainingTime != null) {
                        Text(text = "$remainingTime", style = MaterialTheme.typography.displayLarge.copy(color = Color.White, fontSize = 54.sp))
                        Row {
                            IconButton(onClick = onRestartTimer) { Icon(Icons.Default.Refresh, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(20.dp)) }
                            IconButton(onClick = onToggleTimer) { Icon(if (isTimerPaused) Icons.Default.PlayArrow else Icons.Default.Pause, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(20.dp)) }
                        }
                    } else {
                        Text(text = "$set. Set", style = MaterialTheme.typography.headlineMedium.copy(color = Color.White, fontWeight = FontWeight.ExtraBold))
                        Text(text = "Hedef: ${exercise.reps}", style = MaterialTheme.typography.titleMedium.copy(color = Color.White.copy(alpha = 0.7f)))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Spacer(Modifier.height(12.dp))

            // --- AKILLI EKİPMAN UI ---
            Column(modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(16.dp)).padding(12.dp)) {
                when (category) {
                    ExerciseCategory.BARBELL, ExerciseCategory.DUMBBELL, ExerciseCategory.BODYWEIGHT -> {
                        if (category != ExerciseCategory.BODYWEIGHT) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Bar:", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    BarChip(label = "2 KG", isSelected = selectedBar == 2.0, onClick = { onBarSelect(2.0) })
                                    BarChip(label = "10 KG", isSelected = selectedBar == 10.0, onClick = { onBarSelect(10.0) })
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }

                        Text(if(category == ExerciseCategory.BODYWEIGHT) "Ek Ağırlık Ekle:" else "Plaka Ekle (Sağ+Sol):", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Spacer(Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            PlateButton(weight = 10.0, color = Color(0xFFE53935)) { count -> onPlateAdd(10.0, count) }
                            PlateButton(weight = 7.5, color = Color(0xFF1E88E5)) { count -> onPlateAdd(7.5, count) }
                            PlateButton(weight = 2.5, color = Color(0xFFFFB300)) { count -> onPlateAdd(2.5, count) }
                            PlateButton(weight = 2.0, color = Color(0xFF43A047)) { count -> onPlateAdd(2.0, count) }
                            PlateButton(weight = 1.5, color = Color(0xFFBDBDBD)) { count -> onPlateAdd(1.5, count) }
                        }

                        Spacer(Modifier.height(8.dp))

                        Box(modifier = Modifier.fillMaxWidth().height(28.dp).background(Color.DarkGray.copy(alpha = 0.2f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                            if (selectedPlates.isEmpty()) {
                                Text("Ağırlık Yok", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            } else {
                                LazyRow(modifier = Modifier.padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    itemsIndexed(selectedPlates) { index, p ->
                                        Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(getPlateColor(p)).clickable { onPlateRemove(index) }.padding(horizontal = 6.dp, vertical = 2.dp)) {
                                            Text("$p", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    ExerciseCategory.MACHINE -> {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            listOf("Light", "Medium", "Heavy").forEach { mode ->
                                Button(
                                    onClick = { onMachineModeSelect(mode) },
                                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (machineMode == mode) MaterialTheme.colorScheme.primary else Color.DarkGray.copy(alpha = 0.5f),
                                        contentColor = if (machineMode == mode) Color.Black else Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) { Text(mode, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Ağırlık Seviyesi", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                                Text("Lvl $machineLevel", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = machineLevel.toFloat(),
                                onValueChange = { onMachineLevelSelect(it.toInt()) },
                                valueRange = 0f..10f,
                                steps = 9,
                                colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Column {
                        Text("Şu Anki Yük", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        val weightText = if (category == ExerciseCategory.MACHINE) "$machineMode - Lvl $machineLevel" else "${totalWeight} KG"
                        Text(weightText, style = MaterialTheme.typography.headlineSmall, color = Color(0xFF00F5D4), fontWeight = FontWeight.ExtraBold)
                    }
                    IconButton(onClick = { showNoteField = !showNoteField }) { Icon(Icons.Default.EditNote, null, tint = if(noteInput.isNotEmpty()) Color(0xFF00F5D4) else Color.Gray) }
                }
            }

            Spacer(Modifier.height(12.dp))

            // --- GİRİŞ ALANLARI (Yapılan / Not) ---
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (exercise.reps.contains("MAX", ignoreCase = true)) {
                    OutlinedTextField(
                        value = repsInput,
                        onValueChange = { repsInput = it },
                        label = { Text("Yapılan Tekrar") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = if(showNoteField) ImeAction.Next else ImeAction.Done),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                if (showNoteField) {
                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        label = { Text("Not Ekle") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // Alt Alan (Sabit Butonlar)
        Surface(
            color = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    if (!buttonClicked) {
                        buttonClicked = true
                        scope.launch {
                            delay(500L)
                            onSetFinished("medium", noteInput, repsInput.toIntOrNull())
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F5D4))
            ) {
                Text("TAMAMLA", fontWeight = FontWeight.ExtraBold, color = Color.Black, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun RestScreen(restTime: Int, initialDuration: Int, nextExerciseName: String, onSkip: () -> Unit, onAddTime: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Spacer(Modifier.weight(1f))
        
        Text("Dinlen", style = MaterialTheme.typography.displayMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(24.dp))
        
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(260.dp)) {
            CircularProgressIndicator(
                progress = { if (initialDuration > 0) restTime.toFloat() / initialDuration.toFloat() else 0f },
                modifier = Modifier.fillMaxSize(), 
                strokeWidth = 12.dp, 
                color = Color(0xFF00F5D4), 
                trackColor = Color.White.copy(alpha = 0.1f)
            )
            Text("$restTime", style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp, color = Color.White, fontWeight = FontWeight.Black))
        }
        
        Spacer(Modifier.height(32.dp))
        
        Text(text = "Sıradaki:", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
        Text(
            text = nextExerciseName, 
            style = MaterialTheme.typography.headlineSmall.copy(color = Color.White, fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        
        Spacer(Modifier.weight(1f))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Atla Butonu (Özel Tasarım)
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clickable { onSkip() },
                shape = RoundedCornerShape(12.dp),
                color = Color.DarkGray
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Atla", 
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // +15 sn Ekle Butonu (Özel Tasarım)
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clickable { onAddTime() },
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF00F5D4)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "+15 sn Ekle", 
                        color = Color.Black,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun FinishedScreen(totalTimeMinutes: Int, totalVolume: Double, dominantDifficulty: String, workoutTitle: String, onNavigateToPrograms: () -> Unit, onNavigateToHome: () -> Unit, onShare: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, 
        verticalArrangement = Arrangement.Top, 
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = null,
            tint = Color(0xFF00F5D4),
            modifier = Modifier.size(52.dp)
        )
        
        Spacer(Modifier.height(8.dp))
        Text("Tebrikler!", style = MaterialTheme.typography.headlineMedium.copy(color = Color.White, shadow = Shadow(Color.Black, blurRadius = 8f)))
        
        Spacer(Modifier.height(4.dp))
        Text(text = formatWorkoutTitle(workoutTitle), style = MaterialTheme.typography.titleMedium, color = Color(0xFF00F5D4), fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(20.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            InfoCard(title = "Süre", value = "$totalTimeMinutes dk", modifier = Modifier.weight(1f))
            val volumeText = if (totalVolume > 1000) "${(totalVolume / 1000).toInt()} Ton" else "${totalVolume.toInt()} kg"
            InfoCard(title = "Toplam Yük", value = volumeText, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        InfoCard(title = "Hissiyat", value = dominantDifficulty, modifier = Modifier.fillMaxWidth())
        
        Spacer(Modifier.weight(1f)) 
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onShare, modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F5D4), contentColor = Color.Black)) {
                Icon(Icons.Default.Share, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Başarı Kartını Paylaş", fontWeight = FontWeight.Bold)
            }
            
            Button(onClick = onNavigateToPrograms, modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F5D4), contentColor = Color.Black)) {
                Text("Programa Dön", style = MaterialTheme.typography.titleMedium)
            }
            
            OutlinedButton(onClick = onNavigateToHome, modifier = Modifier.fillMaxWidth().height(48.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)) {
                Text("Ana Sayfa", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
        }
        
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun InfoCard(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = title, style = MaterialTheme.typography.labelLarge.copy(color = Color.White.copy(alpha = 0.6f)))
        Text(text = value, style = MaterialTheme.typography.headlineMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
    }
}

@Composable
fun BarChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) Color(0xFF00F5D4).copy(alpha = 0.2f) else Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF00F5D4) else Color.DarkGray)
    ) {
        Text(text = label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = if (isSelected) Color(0xFF00F5D4) else Color.Gray, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PlateButton(weight: Double, color: Color, onClick: (Int) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(
                onTap = { onClick(1) },
                onLongPress = { onClick(2) }
            )
        }
    ) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(color.copy(alpha = 0.2f)).border(2.dp, color, CircleShape), contentAlignment = Alignment.Center) {
            Text("${if(weight == 7.5 || weight == 2.5 || weight == 1.5) weight else weight.toInt()}", color = color, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
        }
    }
}

@Composable
fun DifficultyButton(label: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(label, fontWeight = FontWeight.ExtraBold, color = Color.Black, fontSize = 16.sp)
    }
}

fun getPlateColor(w: Double) = when(w) {
    10.0 -> Color(0xFFE53935)
    7.5 -> Color(0xFF1E88E5)
    2.5 -> Color(0xFFFFB300)
    2.0 -> Color(0xFF43A047)
    else -> Color(0xFFBDBDBD)
}
