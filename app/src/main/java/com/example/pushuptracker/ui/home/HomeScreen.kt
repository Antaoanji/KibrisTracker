package com.example.pushuptracker.ui.home

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.example.pushuptracker.model.Activities
import com.example.pushuptracker.model.Streak
import com.example.pushuptracker.model.TrackableActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.homeScreenState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf<TrackableActivity?>(null) }
    var showAutoTracker by remember { mutableStateOf(false) }
    var showCelebration by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Kibris Workout",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            ),
                            fontWeight = FontWeight.ExtraBold
                        )
                    ) 
                },
                actions = {
                    Row {
                        uiState.streaks.forEach { streak ->
                            StreakIcon(streak = streak)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) {
            padding ->
        Box(modifier = Modifier.fillMaxSize()){
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(Activities.allActivities) { activity ->
                    ActivityCard(
                        viewModel = viewModel, 
                        activity = activity, 
                        onAddClick = { showAddDialog = activity },
                        onAutoTrackClick = { if(activity.id == "pushups") showAutoTracker = true }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            showAddDialog?.let {
                AddRecordDialog(
                    activity = it,
                    onDismiss = { showAddDialog = null },
                    onSave = { value ->
                        viewModel.addRecord(it.id, value) { goalReached ->
                            if (goalReached) showCelebration = true
                        }
                        showAddDialog = null
                    }
                )
            }

            if (showAutoTracker) {
                AutoPushupTrackerDialog(
                    onDismiss = { showAutoTracker = false },
                    onFinish = { count ->
                        viewModel.addRecord("pushups", count.toDouble()) { goalReached ->
                            if (goalReached) showCelebration = true
                        }
                        showAutoTracker = false
                    }
                )
            }

            if (showCelebration) {
                val trophyComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.trophy))
                val trophyProgress by animateLottieCompositionAsState(composition = trophyComposition)
                LottieAnimation(
                    composition = trophyComposition,
                    progress = { trophyProgress },
                    modifier = Modifier.fillMaxSize()
                )

                if (trophyProgress == 1.0f) {
                    showCelebration = false
                }
            }
        }
    }
}

@Composable
fun AutoPushupTrackerDialog(
    onDismiss: () -> Unit,
    onFinish: (Int) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    var count by remember { mutableStateOf(0) }
    
    // Sensör dinlemeyi başlat
    DisposableEffect(Unit) {
        viewModel.startAutoPushupCounting { newCount ->
            count = newCount
        }
        onDispose {
            viewModel.stopAutoPushupCounting()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black.copy(alpha = 0.95f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Şınav Takibi Aktif",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            Text(
                "Burnunuzu ekrana yaklaştırın",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 120.sp),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            Button(
                onClick = { onFinish(count) },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Bitir ve Kaydet", style = MaterialTheme.typography.titleLarge)
            }
            
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("İptal Et", color = Color.Red)
            }
        }
    }
}

@Composable
fun StreakIcon(streak: Streak) {
    val composition by rememberLottieComposition(
        when (streak.type) {
            Streak.Type.PUSHUP -> LottieCompositionSpec.RawRes(R.raw.redfire)
            Streak.Type.WATER -> LottieCompositionSpec.RawRes(R.raw.bluefire)
            Streak.Type.WORKOUT -> LottieCompositionSpec.RawRes(R.raw.greenfire)
        }
    )
    
    val animatable = rememberLottieAnimatable()

    LaunchedEffect(composition, streak.isCompletedToday) {
        if (composition == null) return@LaunchedEffect

        if (streak.isCompletedToday) {
             animatable.animate(
                composition = composition,
                iterations = LottieConstants.IterateForever
            )
        } else {
            animatable.snapTo(composition, 0f)
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (streak.isCompletedToday) 1f else 0.5f, 
        animationSpec = tween(500),
        label = "streak_alpha"
    )
    
    val scale by rememberInfiniteTransition(label = "streak_pulse").animateFloat(
        initialValue = 1f,
        targetValue = if (streak.isCompletedToday) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "streak_scale"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(42.dp)) {
            LottieAnimation(
                composition = composition,
                progress = { animatable.progress },
                modifier = Modifier.size(38.dp).alpha(alpha).scale(scale)
            )
            if (streak.count > 0) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .align(Alignment.BottomEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = streak.count.toString(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }
}

@Composable
fun ActivityCard(
    viewModel: HomeViewModel,
    activity: TrackableActivity, 
    onAddClick: () -> Unit,
    onAutoTrackClick: () -> Unit
) {
    val todayRecord by viewModel.getTodayRecord(activity.id).collectAsStateWithLifecycle(null)
    val yesterdayRecord by viewModel.getYesterdayRecord(activity.id).collectAsStateWithLifecycle(null)
    val total by viewModel.getTotal(activity.id).collectAsStateWithLifecycle(0.0)
    val dailyGoal by viewModel.getDailyGoal(activity.id).collectAsStateWithLifecycle(0)

    val todayValue = todayRecord?.value ?: 0.0
    val progress = if (dailyGoal > 0) (todayValue / dailyGoal.toDouble()).toFloat() else 0f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)) 
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                Image(
                    painter = painterResource(id = activity.imageRes),
                    contentDescription = activity.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)))

                Row(modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp)) {
                    if (activity.id == "pushups") {
                        FloatingActionButton(
                            onClick = onAutoTrackClick,
                            modifier = Modifier.padding(end = 8.dp),
                            shape = CircleShape,
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ) {
                            Icon(Icons.Default.TouchApp, contentDescription = "Otomatik Say")
                        }
                    }
                    FloatingActionButton(
                        onClick = onAddClick,
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add))
                    }
                }
            }

            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = activity.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = stringResource(R.string.today_progress), style = MaterialTheme.typography.bodyMedium)
                        Text(text = "${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    GradientProgressBar(progress = progress.coerceIn(0f, 1f))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
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
fun GradientProgressBar(progress: Float) {
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(durationMillis = 1000), label = "progressAnimation")

    val gradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.tertiary, 
            MaterialTheme.colorScheme.primary
        )
    )
    val backgroundColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)

    Canvas(modifier = Modifier.fillMaxWidth().height(12.dp)) {
        drawRoundRect(
            color = backgroundColor,
            cornerRadius = CornerRadius(10f, 10f),
            size = size
        )
        drawRoundRect(
            brush = gradient,
            cornerRadius = CornerRadius(10f, 10f),
            size = size.copy(width = size.width * animatedProgress)
        )
    }
}


@Composable
fun InfoColumn(title: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AddRecordDialog(
    activity: TrackableActivity, 
    onDismiss: () -> Unit, 
    onSave: (value: Double) -> Unit
) {
    var input by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "${activity.name} Ekle") },
        text = {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text(text = "${activity.name} (${activity.unit})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = {
                input.toDoubleOrNull()?.let { value ->
                    if (value > 0) {
                        onSave(value)
                    }
                }
            }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
