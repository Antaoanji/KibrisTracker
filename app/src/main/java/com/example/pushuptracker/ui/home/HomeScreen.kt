package com.example.pushuptracker.ui.home

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieAnimatable
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.pushuptracker.R
import com.example.pushuptracker.model.Activities
import com.example.pushuptracker.model.Streak
import com.example.pushuptracker.model.TrackableActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.homeScreenState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf<TrackableActivity?>(null) }
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Activities.allActivities.forEach { activity ->
                    ActivityCard(viewModel = viewModel, activity = activity, onAddClick = { showAddDialog = activity })
                }
            }

            showAddDialog?.let {
                AddRecordDialog(
                    activity = it,
                    onDismiss = { showAddDialog = null },
                    onSave = { value ->
                        viewModel.addRecord(it.id, value) { goalReached ->
                            if (goalReached) {
                                showCelebration = true
                            }
                        }
                        showAddDialog = null
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

                val firewallComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.firewall))
                val firewallProgress by animateLottieCompositionAsState(composition = firewallComposition)
                LottieAnimation(
                    composition = firewallComposition,
                    progress = { firewallProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                )

                if (trophyProgress == 1.0f) {
                    showCelebration = false
                }
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
    onAddClick: () -> Unit
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
        // Main Content
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                Image(
                    painter = painterResource(id = activity.imageRes),
                    contentDescription = activity.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                 // Black overlay for better text contrast
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)))

                FloatingActionButton(
                    onClick = onAddClick,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add))
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
        // Background of the progress bar
        drawRoundRect(
            color = backgroundColor,
            cornerRadius = CornerRadius(10f, 10f),
            size = size
        )
        // Foreground (progress)
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
        onDismissRequest = onDismiss,
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
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
