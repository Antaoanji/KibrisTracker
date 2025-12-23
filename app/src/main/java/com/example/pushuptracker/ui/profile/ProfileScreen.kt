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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pushuptracker.R
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val dailyGoal by viewModel.dailyGoal.collectAsStateWithLifecycle()
    val dailyWaterGoal by viewModel.dailyWaterGoal.collectAsStateWithLifecycle()
    val pushupReminderEnabled by viewModel.pushupReminderEnabled.collectAsStateWithLifecycle()
    val pushupReminderTime by viewModel.pushupReminderTime.collectAsStateWithLifecycle()
    val waterReminderEnabled by viewModel.waterReminderEnabled.collectAsStateWithLifecycle()
    val waterReminderFrequency by viewModel.waterReminderFrequency.collectAsStateWithLifecycle()
    val age by viewModel.age.collectAsStateWithLifecycle()
    val weight by viewModel.weight.collectAsStateWithLifecycle()
    val gender by viewModel.gender.collectAsStateWithLifecycle()
    val goal by viewModel.goal.collectAsStateWithLifecycle()
    val workoutFrequency by viewModel.workoutFrequency.collectAsStateWithLifecycle()


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
                UserProfileCard(age = age, weight = weight, gender = gender, goal = goal, workoutFrequency = workoutFrequency, viewModel = viewModel)
            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileCard(
    age: Int, 
    weight: Int, 
    gender: String, 
    goal: String,
    workoutFrequency: String,
    viewModel: ProfileViewModel
) {
    var ageInput by remember { mutableStateOf(age.toString()) }
    var weightInput by remember { mutableStateOf(weight.toString()) }
    
    val genderOptions = listOf("Erkek", "Kadın", "Diğer")
    var genderExpanded by remember { mutableStateOf(false) }
    var selectedGender by remember { mutableStateOf(gender) }

    val goalOptions = listOf("Kilo Verme", "Direnç Kazanma", "Kas Kütlesi Artırma")
    var goalExpanded by remember { mutableStateOf(false) }
    var selectedGoal by remember { mutableStateOf(goal) }

    val frequencyOptions = listOf("Yeni Başlayan", "Orta Seviye", "Düzenli")
    var frequencyExpanded by remember { mutableStateOf(false) }
    var selectedFrequency by remember { mutableStateOf(workoutFrequency) }

    // Update inputs when the initial values change
    LaunchedEffect(age) { ageInput = age.toString() }
    LaunchedEffect(weight) { weightInput = weight.toString() }
    LaunchedEffect(gender) { selectedGender = gender }
    LaunchedEffect(goal) { selectedGoal = goal }
    LaunchedEffect(workoutFrequency) { selectedFrequency = workoutFrequency }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Kullanıcı Profili", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)){
                OutlinedTextField(value = ageInput, onValueChange = {ageInput = it}, label = { Text("Yaş") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                OutlinedTextField(value = weightInput, onValueChange = {weightInput = it}, label = { Text("Kilo (kg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Gender Dropdown
            ExposedDropdownMenuBox(expanded = genderExpanded, onExpandedChange = { genderExpanded = !genderExpanded }) {
                OutlinedTextField(
                    value = selectedGender,
                    onValueChange = {}, 
                    label = { Text("Cinsiyet") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = genderExpanded, onDismissRequest = { genderExpanded = false }) {
                    genderOptions.forEach { option ->
                        DropdownMenuItem(text = { Text(option) }, onClick = {
                            selectedGender = option
                            genderExpanded = false
                        })
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Goal Dropdown
            ExposedDropdownMenuBox(expanded = goalExpanded, onExpandedChange = { goalExpanded = !goalExpanded }) {
                OutlinedTextField(
                    value = selectedGoal,
                    onValueChange = {}, 
                    label = { Text("Hedef") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = goalExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = goalExpanded, onDismissRequest = { goalExpanded = false }) {
                    goalOptions.forEach { option ->
                        DropdownMenuItem(text = { Text(option) }, onClick = {
                            selectedGoal = option
                            goalExpanded = false
                        })
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            // Frequency Dropdown
            ExposedDropdownMenuBox(expanded = frequencyExpanded, onExpandedChange = { frequencyExpanded = !frequencyExpanded }) {
                OutlinedTextField(
                    value = selectedFrequency,
                    onValueChange = {}, 
                    label = { Text("Antrenman Sıklığı") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = frequencyExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = frequencyExpanded, onDismissRequest = { frequencyExpanded = false }) {
                    frequencyOptions.forEach { option ->
                        DropdownMenuItem(text = { Text(option) }, onClick = {
                            selectedFrequency = option
                            frequencyExpanded = false
                        })
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { 
                ageInput.toIntOrNull()?.let { viewModel.saveAge(it) }
                weightInput.toIntOrNull()?.let { viewModel.saveWeight(it) }
                viewModel.saveGender(selectedGender)
                viewModel.saveGoal(selectedGoal)
                viewModel.saveWorkoutFrequency(selectedFrequency)
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Profili Güncelle")
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

    fun showTimePicker() {
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val parsedTime = try { LocalTime.parse(pushupReminderTime, timeFormatter) } catch (e: Exception) { LocalTime.now() }
        
        TimePickerDialog(
            context,
            { _, hour: Int, minute: Int ->
                onPushupReminderChanged(true, LocalTime.of(hour, minute))
            },
            parsedTime.hour,
            parsedTime.minute,
            true
        ).show()
    }

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
                    if (it) showTimePicker() else onPushupReminderChanged(false, null)
                },
                onClick = { if(pushupReminderEnabled) showTimePicker() }
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
