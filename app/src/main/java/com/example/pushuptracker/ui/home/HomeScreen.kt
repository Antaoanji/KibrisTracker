package com.example.pushuptracker.ui.home

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.IBinder
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.*
import com.example.pushuptracker.R
import com.example.pushuptracker.audio.WorkoutService
import com.example.pushuptracker.model.Activities
import com.example.pushuptracker.model.Streak
import com.example.pushuptracker.model.TrackableActivity
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.homeScreenState.collectAsStateWithLifecycle()
    val updateInfo by viewModel.updateInfo.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var workoutService by remember { mutableStateOf<WorkoutService?>(null) }
    val walkingState = workoutService?.walkingState?.collectAsStateWithLifecycle()
    val lymphaticState = workoutService?.lymphaticState?.collectAsStateWithLifecycle()

    val connection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as WorkoutService.WorkoutBinder
                workoutService = binder.getService()
            }
            override fun onServiceDisconnected(name: ComponentName?) {
                workoutService = null
            }
        }
    }

    LaunchedEffect(Unit) {
        val intent = Intent(context, WorkoutService::class.java)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    var showAddDialog by remember { mutableStateOf<TrackableActivity?>(null) }
    var showAutoTracker by remember { mutableStateOf(false) }
    var showCelebration by remember { mutableStateOf(false) }
    var showWalkingMenu by remember { mutableStateOf(false) }
    var showLymphaticMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Kibris Workout",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            brush = Brush.linearGradient(
                                colors = listOf( MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                            ),
                            fontWeight = FontWeight.ExtraBold
                        )
                    ) 
                },
                actions = {
                    Row(modifier = Modifier.padding(end = 8.dp)) {
                        uiState.streaks.sortedBy { 
                            when(it.type) {
                                Streak.Type.PUSHUP -> 0
                                Streak.Type.WATER -> 1
                                Streak.Type.WALKING -> 2
                                Streak.Type.LYMPHATIC -> 3
                                Streak.Type.WORKOUT -> 4
                            }
                        }.forEach { streak ->
                            StreakIcon(streak = streak)
                        }
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()){
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(items = Activities.allActivities, key = { it.id }) { activity ->
                    ActivityCard(
                        viewModel = viewModel, 
                        activity = activity, 
                        onAddClick = { 
                            when (activity.id) {
                                "walking" -> showWalkingMenu = true
                                "lymphatic" -> showLymphaticMenu = true
                                else -> showAddDialog = activity
                            }
                        },
                        onAutoTrackClick = { if(activity.id == "pushups") showAutoTracker = true }
                    )
                }
            }

            if (showWalkingMenu) {
                WalkingActiveMenu(
                    state = walkingState?.value,
                    onDismiss = { showWalkingMenu = false },
                    onStart = { viewModel.startWalkingWorkout() },
                    onPause = { context.startService(Intent(context, WorkoutService::class.java).apply { action = "PAUSE" }) },
                    onResume = { context.startService(Intent(context, WorkoutService::class.java).apply { action = "RESUME" }) },
                    onStop = { context.startService(Intent(context, WorkoutService::class.java).apply { action = "STOP" }) },
                    onCancel = { context.startService(Intent(context, WorkoutService::class.java).apply { action = "CANCEL" }) }
                )
            }

            if (showLymphaticMenu) {
                LymphaticActiveMenu(
                    state = lymphaticState?.value,
                    onDismiss = { showLymphaticMenu = false },
                    onStart = { viewModel.startLymphaticWorkout() },
                    onPause = { context.startService(Intent(context, WorkoutService::class.java).apply { action = "PAUSE" }) },
                    onResume = { context.startService(Intent(context, WorkoutService::class.java).apply { action = "RESUME" }) },
                    onStop = { context.startService(Intent(context, WorkoutService::class.java).apply { action = "STOP" }) },
                    onSkip = { context.startService(Intent(context, WorkoutService::class.java).apply { action = "SKIP_LYMPHATIC" }) },
                    onCancel = { context.startService(Intent(context, WorkoutService::class.java).apply { action = "CANCEL" }) }
                )
            }

            updateInfo?.let { _ ->
                AlertDialog(
                    onDismissRequest = { viewModel.onUpdateDismissed() },
                    title = { Text("Güncelleme Mevcut!") },
                    confirmButton = { Button(onClick = { viewModel.onUpdateConfirmed() }) { Text("Şimdi İndir") } }
                )
            }

            showAddDialog?.let {
                AddRecordDialog(activity = it, onDismiss = { showAddDialog = null }, onSave = { v ->
                    viewModel.addRecord(it.id, v) { reached -> if(reached) showCelebration = true }
                    showAddDialog = null
                })
            }

            if (showAutoTracker) {
                AutoPushupTrackerDialog(
                    onDismiss = { showAutoTracker = false },
                    onFinish = { count ->
                        viewModel.addRecord("pushups", count.toDouble()) { reached -> if(reached) showCelebration = true }
                        showAutoTracker = false
                    }
                )
            }

            if (showCelebration) {
                val trophyComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.trophy))
                val trophyProgress by animateLottieCompositionAsState(composition = trophyComposition)
                LottieAnimation(composition = trophyComposition, progress = { trophyProgress }, modifier = Modifier.fillMaxSize())
                if (trophyProgress == 1.0f) showCelebration = false
            }
        }
    }
}

@Composable
fun WalkingActiveMenu(
    state: WorkoutService.WalkingState?,
    onDismiss: () -> Unit,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Japon Yürüyüşü", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                if (state == null) {
                    Text("33 Dakikalık interval programına hazır mısın?")
                    Spacer(Modifier.height(8.dp))
                    Text("• 3dk Hızlı / 3dk Yavaş", style = MaterialTheme.typography.bodySmall)
                } else {
                    val mins = state.remainingSeconds / 60
                    val secs = state.remainingSeconds % 60
                    Text(text = String.format(Locale.getDefault(), "%02d:%02d", mins, secs), style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(12.dp))
                    Surface(color = if (state.isFastMode) Color(0xFFFF5722) else Color(0xFF4CAF50), shape = RoundedCornerShape(12.dp)) {
                        Text(text = if (state.isFastMode) "🔥 HIZLI TEMPO" else "🍃 YAVAŞ TEMPO", modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    }
                    if (state.isPaused) Text("DURAKLATILDI", color = Color.Red, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                }
            }
        },
        confirmButton = {
            if (state == null) { Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) { Text("BAŞLAT") } }
            else {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (state.isPaused) Button(onClick = onResume, modifier = Modifier.weight(1f)) { Text("DEVAM") }
                        else OutlinedButton(onClick = onPause, modifier = Modifier.weight(1f)) { Text("DURAKLAT") }
                        Button(onClick = onStop, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) { Text("BİTİR") }
                    }
                    TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("YÜRÜYÜŞÜ İPTAL ET", color = Color.Gray) }
                }
            }
        }
    )
}

@Composable
fun LymphaticActiveMenu(
    state: WorkoutService.LymphaticState?,
    onDismiss: () -> Unit,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onSkip: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val videoUrl = "https://www.tiktok.com/@julius.rogonja/video/7538470302687169814"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Spa, contentDescription = null, tint = Color(0xFF9C27B0))
                    Spacer(Modifier.width(8.dp))
                    Text("Lenfatik Akış", fontWeight = FontWeight.Bold)
                }
                
                // Info butonu artık her zaman orada
                IconButton(onClick = {
                    if (state != null && !state.isPaused) onPause()
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
                    context.startActivity(intent)
                }) {
                    Icon(
                        imageVector = Icons.Default.Info, 
                        contentDescription = "Nasıl Yapılır?", 
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                if (state == null) {
                    Text("7 Dakikalık lenfatik drenaj döngüsüne hazır mısın?", textAlign = TextAlign.Center)
                    Spacer(Modifier.height(8.dp))
                    Text("• 7 Farklı Hareket\n• Her biri 1 dakika", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                } else {
                    Text(text = state.movementName, style = MaterialTheme.typography.headlineMedium, color = Color(0xFF9C27B0), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(8.dp))
                    val mins = state.remainingSeconds / 60
                    val secs = state.remainingSeconds % 60
                    Text(text = String.format(Locale.getDefault(), "%02d:%02d", mins, secs), style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(12.dp))
                    Text(text = state.movementDescription, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                    if (state.isPaused) Text("DURAKLATILDI", color = Color.Red, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                }
            }
        },
        confirmButton = {
            if (state == null) { Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) { Text("BAŞLAT") } }
            else {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (state.isPaused) Button(onClick = onResume, modifier = Modifier.weight(1f)) { Text("DEVAM") }
                        else OutlinedButton(onClick = onPause, modifier = Modifier.weight(1f)) { Text("DURAKLAT") }
                        Button(onClick = onSkip, modifier = Modifier.weight(1f)) { Text("ATLA") }
                    }
                    Button(onClick = onStop, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) { Text("BİTİR") }
                    TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("İPTAL ET", color = Color.Gray) }
                }
            }
        }
    )
}

@Composable
fun ActivityCard(viewModel: HomeViewModel, activity: TrackableActivity, onAddClick: () -> Unit, onAutoTrackClick: () -> Unit) {
    val todayRecord by viewModel.getTodayRecord(activity.id).collectAsStateWithLifecycle(null)
    val yesterdayRecord by viewModel.getYesterdayRecord(activity.id).collectAsStateWithLifecycle(null)
    val total by viewModel.getTotal(activity.id).collectAsStateWithLifecycle(0.0)
    val dailyGoal by viewModel.getDailyGoal(activity.id).collectAsStateWithLifecycle(0)

    val todayValue = todayRecord?.value ?: 0.0
    val progress = if (dailyGoal > 0) (todayValue / dailyGoal.toDouble()).toFloat() else 0f

    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                Image(painter = painterResource(id = activity.imageRes), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alignment = activity.alignment)
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)))
                Row(modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp)) {
                    if (activity.id == "pushups") FloatingActionButton(onClick = onAutoTrackClick, modifier = Modifier.padding(end = 8.dp), shape = CircleShape, containerColor = MaterialTheme.colorScheme.secondary) { Icon(Icons.Default.TouchApp, null) }
                    FloatingActionButton(onClick = onAddClick, shape = CircleShape, containerColor = when(activity.id) {
                        "walking" -> MaterialTheme.colorScheme.tertiary
                        "lymphatic" -> Color(0xFF9C27B0)
                        else -> MaterialTheme.colorScheme.primary
                    }) { Icon(if(activity.id == "walking" || activity.id == "lymphatic") Icons.Default.PlayArrow else Icons.Default.Add, null) }
                }
            }

            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = activity.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                GradientProgressBar(progress = progress.coerceIn(0f, 1f))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    InfoColumn(title = stringResource(R.string.today), value = todayValue.toInt().toString())
                    InfoColumn(title = stringResource(R.string.yesterday), value = (yesterdayRecord?.value ?: 0.0).toInt().toString())
                    InfoColumn(title = stringResource(R.string.goal_label), value = dailyGoal.toString())
                    InfoColumn(title = stringResource(R.string.total), value = total.toInt().toString())
                }
            }
        }
    }
}

@Composable
fun InfoColumn(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun GradientProgressBar(progress: Float) {
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(durationMillis = 1000), label = "")
    val gradient = Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.primary))
    Canvas(modifier = Modifier.fillMaxWidth().height(12.dp)) {
        drawRoundRect(color = Color.LightGray.copy(alpha = 0.3f), cornerRadius = CornerRadius(10f, 10f), size = size)
        drawRoundRect(brush = gradient, cornerRadius = CornerRadius(10f, 10f), size = size.copy(width = size.width * animatedProgress))
    }
}

@Composable
fun AddRecordDialog(activity: TrackableActivity, onDismiss: () -> Unit, onSave: (Double) -> Unit) {
    var input by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${activity.name} Ekle") },
        text = { OutlinedTextField(value = input, onValueChange = { input = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)) },
        confirmButton = { Button(onClick = { input.toDoubleOrNull()?.let { onSave(it) } }) { Text("Kaydet") } }
    )
}

@Composable
fun AutoPushupTrackerDialog(onDismiss: () -> Unit, onFinish: (Int) -> Unit, viewModel: HomeViewModel = hiltViewModel()) {
    var count by remember { mutableIntStateOf(0) }
    DisposableEffect(Unit) {
        viewModel.startAutoPushupCounting { count = it }
        onDispose { viewModel.stopAutoPushupCounting() }
    }
    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black.copy(alpha = 0.95f)) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("Şınav Takibi Aktif", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Spacer(modifier = Modifier.height(64.dp))
            Text(text = count.toString(), style = MaterialTheme.typography.displayLarge.copy(fontSize = 120.sp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(64.dp))
            Button(onClick = { onFinish(count) }, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Bitir ve Kaydet") }
            TextButton(onClick = onDismiss) { Text("İptal Et", color = Color.Red) }
        }
    }
}

@Composable
fun StreakIcon(streak: Streak) {
    val composition by rememberLottieComposition(
        when (streak.type) {
            Streak.Type.PUSHUP -> LottieCompositionSpec.RawRes(R.raw.redfire)
            Streak.Type.WATER -> LottieCompositionSpec.RawRes(R.raw.bluefire)
            Streak.Type.WALKING -> LottieCompositionSpec.RawRes(R.raw.yellowfire)
            Streak.Type.LYMPHATIC -> LottieCompositionSpec.RawRes(R.raw.purplefire)
            Streak.Type.WORKOUT -> LottieCompositionSpec.RawRes(R.raw.greenfire)
        }
    )
    val animatable = rememberLottieAnimatable()
    LaunchedEffect(composition, streak.isCompletedToday) {
        if (composition != null && streak.isCompletedToday) animatable.animate(composition = composition, iterations = LottieConstants.IterateForever)
    }
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(42.dp)) {
        LottieAnimation(composition = composition, progress = { animatable.progress }, modifier = Modifier.size(38.dp))
        if (streak.count > 0) {
            Box(modifier = Modifier.size(18.dp).background(MaterialTheme.colorScheme.primary, CircleShape).align(Alignment.BottomEnd), contentAlignment = Alignment.Center) {
                Text(text = streak.count.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
