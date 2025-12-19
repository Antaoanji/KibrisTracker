package com.example.pushuptracker.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.pushuptracker.R
import com.example.pushuptracker.model.Activities
import com.example.pushuptracker.model.TrackableActivity


@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val currentStreak by viewModel.currentStreak.collectAsStateWithLifecycle(initialValue = 0)
    var showAddDialog by remember { mutableStateOf<TrackableActivity?>(null) }
    var showCelebration by remember { mutableStateOf(false) }

    Scaffold {
            padding ->
        Box(modifier = Modifier.fillMaxSize()){
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentStreak > 0) {
                    Text(
                        text = stringResource(R.string.current_streak, currentStreak),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }

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
                val lottieComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.celebration))
                val lottieProgress by animateLottieCompositionAsState(composition = lottieComposition)
                LottieAnimation(
                    composition = lottieComposition,
                    progress = { lottieProgress },
                    modifier = Modifier.fillMaxSize()
                )
                if (lottieProgress == 1.0f) {
                    showCelebration = false
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
    val yesterdayValue = yesterdayRecord?.value ?: 0.0
    val progress = (todayValue / if (dailyGoal > 0) dailyGoal.toDouble() else 1.0).toFloat().coerceIn(0f, 1f)

    val progressColor by animateColorAsState(
        targetValue = if (progress >= 1.0f) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary,
        label = "Progress Bar Color"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(id = activity.imageRes),
                    contentDescription = activity.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp) // Constrained height
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop // Use Crop to fill the height
                )
                FloatingActionButton(
                    onClick = onAddClick,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add))
                }
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = activity.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                LinearProgressIndicator(
                    progress = { progress },
                    color = progressColor,
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoColumn(title = stringResource(R.string.today), value = todayValue.toInt().toString())
                    InfoColumn(title = stringResource(R.string.yesterday), value = yesterdayValue.toInt().toString())
                    InfoColumn(title = stringResource(R.string.goal_label), value = dailyGoal.toString())
                    InfoColumn(title = stringResource(R.string.total), value = total?.toInt()?.toString() ?: "0")
                }
            }
        }
    }
}

@Composable
fun InfoColumn(title: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, style = MaterialTheme.typography.titleSmall)
        Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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

