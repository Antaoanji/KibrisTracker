package com.example.pushuptracker.ui.active

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.pushuptracker.audio.WorkoutService
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    navController: NavController,
    viewModel: ActiveWorkoutViewModel = hiltViewModel()
) {
    val service by viewModel.workoutService.collectAsStateWithLifecycle()
    
    // We use a dummy state flow when service is null to avoid nullability issues with collectAsStateWithLifecycle
    val walkingState by (service?.walkingState ?: remember { MutableStateFlow<WorkoutService.WalkingState?>(null) }).collectAsStateWithLifecycle()
    val lymphaticState by (service?.lymphaticState ?: remember { MutableStateFlow<WorkoutService.LymphaticState?>(null) }).collectAsStateWithLifecycle()

    val currentView = LocalView.current
    DisposableEffect(Unit) {
        currentView.keepScreenOn = true
        onDispose { currentView.keepScreenOn = false }
    }

    // Track if we have seen an active state to avoid immediate popBackStack
    var hasBeenActive by remember { mutableStateOf(false) }
    LaunchedEffect(walkingState, lymphaticState) {
        if (walkingState != null || lymphaticState != null) {
            hasBeenActive = true
        }
    }

    // Disable system back button to prevent accidental workout termination
    BackHandler(enabled = true) {
        // Do nothing or show a "Use Finish/Cancel to exit" message
    }

    // Automatically navigate back when workout is finished (both states null)
    LaunchedEffect(service, walkingState, lymphaticState, hasBeenActive) {
        if (hasBeenActive && service != null && walkingState == null && lymphaticState == null) {
            navController.popBackStack()
        }
    }

    Scaffold(
        containerColor = Color(0xFF0A0A0A),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (walkingState != null) "Japon Yürüyüşü" else if (lymphaticState != null) "Lenfatik Akış" else "Yükleniyor...",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF1A1A1A), Color(0xFF0A0A0A))
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (walkingState != null) {
                    WalkingContent(
                        state = walkingState!!,
                        onPause = { viewModel.pause() },
                        onResume = { viewModel.resume() },
                        onStop = { viewModel.stop() },
                        onCancel = { viewModel.cancel() }
                    )
                } else if (lymphaticState != null) {
                    LymphaticContent(
                        state = lymphaticState!!,
                        onPause = { viewModel.pause() },
                        onResume = { viewModel.resume() },
                        onStop = { viewModel.stop() },
                        onSkip = { viewModel.skipLymphatic() },
                        onCancel = { viewModel.cancel() }
                    )
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.WalkingContent(
    state: WorkoutService.WalkingState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onCancel: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val totalSeconds = 33 * 60
    val progress = state.remainingSeconds.toFloat() / totalSeconds.toFloat()
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(1000), label = "progress")

    LaunchedEffect(state.remainingSeconds) {
        if (state.remainingSeconds in 1..3 && !state.isPaused) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Column(
        modifier = Modifier.weight(1f).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(280.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = Color.White.copy(alpha = 0.1f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = if (state.isFastMode) Color(0xFFFF5722) else Color(0xFF00F5D4),
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val mins = state.remainingSeconds / 60
                val secs = state.remainingSeconds % 60
                Text(
                    text = String.format(Locale.getDefault(), "%02d:%02d", mins, secs),
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "KALAN SÜRE",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                    letterSpacing = 2.sp
                )
            }
        }

        Spacer(Modifier.height(48.dp))

        Surface(
            color = if (state.isFastMode) Color(0xFFFF5722).copy(alpha = 0.2f) else Color(0xFF4CAF50).copy(alpha = 0.2f),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (state.isFastMode) Color(0xFFFF5722) else Color(0xFF4CAF50)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (state.isFastMode) Icons.Default.Whatshot else Icons.Default.Eco,
                    contentDescription = null,
                    tint = if (state.isFastMode) Color(0xFFFF5722) else Color(0xFF4CAF50)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = if (state.isFastMode) "HIZLI TEMPO" else "YAVAŞ TEMPO",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        if (state.isPaused) {
            Spacer(Modifier.height(16.dp))
            Text("DURAKLATILDI", color = Color.Red, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        }
    }

    ActionButtons(
        isPaused = state.isPaused,
        onResume = onResume,
        onPause = onPause,
        onStop = onStop,
        onCancel = onCancel
    )
}

@Composable
private fun ColumnScope.LymphaticContent(
    state: WorkoutService.LymphaticState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onSkip: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val videoUrl = "https://www.tiktok.com/@julius.rogonja/video/7538470302687169814"

    LaunchedEffect(state.remainingSeconds) {
        if (state.remainingSeconds in 1..3 && !state.isPaused) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Column(
        modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = state.movementName,
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFF00F5D4),
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "${state.currentMovementIndex + 1} / 7 Hareket",
            style = MaterialTheme.typography.labelLarge,
            color = Color.Gray
        )

        Spacer(Modifier.height(32.dp))

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
            val progress = state.remainingSeconds.toFloat() / 60f
            val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")

            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = Color.White.copy(alpha = 0.1f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = Color(0xFF9C27B0),
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Text(
                text = "${state.remainingSeconds}",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFF00F5D4))
                    Spacer(Modifier.width(8.dp))
                    Text("Nasıl Yapılır?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(Modifier.height(8.dp))
                Text(text = state.movementDescription, style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.8f))

                Spacer(Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        if (!state.isPaused) onPause()
                        context.startActivity(Intent(Intent.ACTION_VIEW, videoUrl.toUri()))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00F5D4))
                ) {
                    Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color(0xFF00F5D4))
                    Spacer(Modifier.width(8.dp))
                    Text("Videoyu İzle", color = Color(0xFF00F5D4))
                }
            }
        }

        if (state.isPaused) {
            Spacer(Modifier.height(16.dp))
            Text("DURAKLATILDI", color = Color.Red, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        }
    }

    Spacer(Modifier.height(16.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = onSkip,
            modifier = Modifier.weight(1f).height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("ATLA", fontWeight = FontWeight.Bold)
        }

        ActionButtons(
            modifier = Modifier.weight(2f),
            isPaused = state.isPaused,
            onResume = onResume,
            onPause = onPause,
            onStop = onStop,
            onCancel = onCancel
        )
    }
}

@Composable
private fun ActionButtons(
    modifier: Modifier = Modifier,
    isPaused: Boolean,
    onResume: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onCancel: () -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (isPaused) {
                Button(
                    onClick = onResume,
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F5D4)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "DEVAM", tint = Color.Black)
                }
            } else {
                Button(
                    onClick = onPause,
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Pause, contentDescription = "DURAKLAT", tint = Color.White)
                }
            }

            Button(
                onClick = onStop,
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = "BİTİR", tint = Color.White)
            }
        }
        
        TextButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("İPTAL ET", color = Color.Gray)
        }
    }
}
