package com.example.pushuptracker.ui.profile

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pushuptracker.R
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = viewModel()) {
    val dailyGoal by viewModel.dailyGoal.collectAsStateWithLifecycle()
    val dailyWaterGoal by viewModel.dailyWaterGoal.collectAsStateWithLifecycle()
    val pushupReminderEnabled by viewModel.pushupReminderEnabled.collectAsStateWithLifecycle()
    val pushupReminderTime by viewModel.pushupReminderTime.collectAsStateWithLifecycle()
    val waterReminderEnabled by viewModel.waterReminderEnabled.collectAsStateWithLifecycle()
    val waterReminderFrequency by viewModel.waterReminderFrequency.collectAsStateWithLifecycle()

    Scaffold {
        padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                GoalSettingCard(
                    title = stringResource(R.string.daily_pushup_goal),
                    currentGoal = dailyGoal,
                    label = stringResource(R.string.push_up_count),
                    onSave = { newGoal ->
                        viewModel.saveDailyGoal(newGoal)
                    }
                )
            }
            item {
                GoalSettingCard(
                    title = stringResource(R.string.daily_water_goal),
                    currentGoal = dailyWaterGoal,
                    label = stringResource(R.string.water_drunk_ml),
                    onSave = { newGoal ->
                        viewModel.saveDailyWaterGoal(newGoal)
                    }
                )
            }
            item {
                RemindersCard(
                    pushupReminderEnabled = pushupReminderEnabled,
                    pushupReminderTime = pushupReminderTime,
                    onPushupReminderChanged = { enabled, time -> viewModel.setPushupReminder(enabled, time) },
                    waterReminderEnabled = waterReminderEnabled,
                    waterReminderFrequency = waterReminderFrequency,
                    onWaterReminderChanged = { enabled, freq -> viewModel.setWaterReminder(enabled, freq) }
                )
            }
        }
    }
}

@Composable
fun GoalSettingCard(title: String, currentGoal: Int, label: String, onSave: (Int) -> Unit) {
    var newGoalInput by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.current_goal, currentGoal),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = newGoalInput,
                onValueChange = { newGoalInput = it },
                label = { Text(label) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val newGoal = newGoalInput.toIntOrNull()
                    if (newGoal != null && newGoal > 0) {
                        onSave(newGoal)
                        newGoalInput = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}

@Composable
fun RemindersCard(
    pushupReminderEnabled: Boolean,
    pushupReminderTime: String,
    onPushupReminderChanged: (Boolean, LocalTime?) -> Unit,
    waterReminderEnabled: Boolean,
    waterReminderFrequency: Int,
    onWaterReminderChanged: (Boolean, Int?) -> Unit
) {
    val context = LocalContext.current
    var showWaterFrequencyDialog by remember { mutableStateOf(false) }

    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val parsedTime = LocalTime.parse(pushupReminderTime, timeFormatter)

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour: Int, minute: Int ->
            onPushupReminderChanged(true, LocalTime.of(hour, minute))
        },
        parsedTime.hour,
        parsedTime.minute,
        true
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.reminders_title),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Push-up Reminder
            ReminderItem(
                label = stringResource(R.string.pushup_reminder),
                enabled = pushupReminderEnabled,
                detail = pushupReminderTime,
                onEnabledChange = {
                    if (it) timePickerDialog.show() else onPushupReminderChanged(false, null)
                },
                onClick = { if(pushupReminderEnabled) timePickerDialog.show() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Water Reminder
            ReminderItem(
                label = stringResource(R.string.water_reminder),
                enabled = waterReminderEnabled,
                detail = "${waterReminderFrequency} dk",
                onEnabledChange = { 
                    if (it) showWaterFrequencyDialog = true else onWaterReminderChanged(false, null)
                },
                onClick = { if(waterReminderEnabled) showWaterFrequencyDialog = true }
            )
        }
    }

    if (showWaterFrequencyDialog) {
        WaterFrequencyDialog(
            onDismiss = { showWaterFrequencyDialog = false },
            onSelect = {
                onWaterReminderChanged(true, it)
                showWaterFrequencyDialog = false
            }
        )
    }
}

@Composable
fun ReminderItem(label: String, enabled: Boolean, detail: String, onEnabledChange: (Boolean) -> Unit, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            if (enabled) {
                Text(text = detail, style = MaterialTheme.typography.bodySmall)
            }
        }
        Switch(checked = enabled, onCheckedChange = onEnabledChange)
    }
}

@Composable
fun WaterFrequencyDialog(onDismiss: () -> Unit, onSelect: (Int) -> Unit) {
    val frequencies = listOf(30, 60, 90, 120, 180, 240)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hatırlatıcı Sıklığı") },
        text = {
            Column {
                frequencies.forEach { freq ->
                    Text("$freq Dakikada Bir", modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(freq) }
                        .padding(vertical = 12.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}





