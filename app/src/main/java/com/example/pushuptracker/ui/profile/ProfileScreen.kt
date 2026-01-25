package com.example.pushuptracker.ui.profile

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.pushuptracker.BuildConfig
import com.example.pushuptracker.R
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val weight by viewModel.weight.collectAsStateWithLifecycle(70)
    val dailyGoal by viewModel.dailyGoal.collectAsStateWithLifecycle(50)
    val dailyWaterGoal by viewModel.dailyWaterGoal.collectAsStateWithLifecycle(2000)
    val workoutReminderEnabled by viewModel.workoutReminderEnabled.collectAsStateWithLifecycle(false)
    val workoutReminderTime by viewModel.workoutReminderTime.collectAsStateWithLifecycle("19:00")
    val pushupReminderEnabled by viewModel.pushupReminderEnabled.collectAsStateWithLifecycle(false)
    val pushupReminderTime by viewModel.pushupReminderTime.collectAsStateWithLifecycle("18:00")
    val waterReminderEnabled by viewModel.waterReminderEnabled.collectAsStateWithLifecycle(false)
    val waterReminderFrequency by viewModel.waterReminderFrequency.collectAsStateWithLifecycle(120)

    var weightInput by remember(weight) { mutableStateOf(weight.toString()) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showCheckAnimation by remember { mutableStateOf(false) }
    var showWaterReminderDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Tüm Verileri Sıfırla") },
            text = { Text("Bu işlem geri alınamaz. Tüm ilerlemeniz, hedefleriniz ve ayarlarınız silinecektir. Emin misiniz?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAllUserData()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Evet, Sıfırla")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    if (showWaterReminderDialog) {
        WaterReminderDialog(
            currentFrequency = waterReminderFrequency,
            onDismiss = { showWaterReminderDialog = false },
            onConfirm = { frequency ->
                viewModel.setWaterReminder(true, frequency)
                showWaterReminderDialog = false
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold {
            padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp)
            ) {
                // --- User Profile Section ---
                item {
                    SectionTitle(title = "Kullanıcı Profili")
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = weightInput,
                            onValueChange = { weightInput = it },
                            label = { Text("Vücut Ağırlığı (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = transparentTextFieldColors()
                        )
                        Button(
                            onClick = {
                                weightInput.toIntOrNull()?.let { viewModel.saveWeight(it) }
                                showCheckAnimation = true
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text("Güncelle")
                        }
                    }
                    Text(
                        text = "Kilonuz antrenman sırasında yakılan kaloriyi hesaplamak için kullanılır.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // --- Daily Goals Section ---
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))
                    SectionTitle(title = "Günlük Hedefler")
                    Spacer(modifier = Modifier.height(16.dp))
                    GoalInput(label = "Günlük Şınav Hedefi", currentGoal = dailyGoal) {
                        viewModel.saveDailyGoal(it)
                        showCheckAnimation = true
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    GoalInput(label = "Günlük Su Hedefi (ml)", currentGoal = dailyWaterGoal) {
                        viewModel.saveDailyWaterGoal(it)
                        showCheckAnimation = true
                    }
                }

                // --- Reminders Section ---
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))
                    SectionTitle(title = "Hatırlatıcılar")
                    Spacer(modifier = Modifier.height(16.dp))
                    ReminderSwitch(label = "Antrenman Hatırlatıcısı", enabled = workoutReminderEnabled, detail = workoutReminderTime) { enabled, time -> viewModel.setWorkoutReminder(enabled, time) }
                    Spacer(modifier = Modifier.height(16.dp))
                    ReminderSwitch(label = "Şınav Hatırlatıcısı", enabled = pushupReminderEnabled, detail = pushupReminderTime) { enabled, time -> viewModel.setPushupReminder(enabled, time) }
                    Spacer(modifier = Modifier.height(16.dp))
                    WaterReminderItem(
                        enabled = waterReminderEnabled,
                        frequency = waterReminderFrequency,
                        onEnable = { showWaterReminderDialog = true },
                        onDisable = { viewModel.setWaterReminder(false, null) }
                    )
                }

                // --- Reset Section ---
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))
                    Button(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text("Tüm Verileri Sıfırla", color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }

                // --- Version Info Section ---
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "Sürüm v${BuildConfig.VERSION_NAME}",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        if (showCheckAnimation) {
            val lottieComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.check))
            LottieAnimation(
                composition = lottieComposition,
                modifier = Modifier.size(150.dp).align(Alignment.Center)
            )
            LaunchedEffect(lottieComposition) {
                delay(1500)
                showCheckAnimation = false
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(text = title, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
}

@Composable
fun GoalInput(label: String, currentGoal: Int, onSave: (Int) -> Unit) {
    var value by remember { mutableStateOf("") }
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = value,
            onValueChange = { value = it },
            label = { Text(label) },
            placeholder = { Text("Mevcut: $currentGoal") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            colors = transparentTextFieldColors()
        )
        Button(onClick = { value.toIntOrNull()?.let { if(it > 0) onSave(it).also { value = "" } } }, modifier = Modifier.padding(start = 8.dp)) {
            Text("Kaydet")
        }
    }
}

@Composable
fun ReminderSwitch(label: String, enabled: Boolean, detail: String, onToggle: (Boolean, LocalTime?) -> Unit) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth().clickable { if(enabled) { showTimePicker(context, detail, onToggle) } else { onToggle(true, null)} },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            if (enabled) {
                Text(text = detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
        Switch(
            checked = enabled,
            onCheckedChange = { if (it) showTimePicker(context, detail, onToggle) else onToggle(false, null) },
            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary, uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        )
    }
}

fun showTimePicker(context: android.content.Context, currentTime: String, onTimeSelected: (Boolean, LocalTime?) -> Unit) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val parsedTime = try { LocalTime.parse(currentTime, timeFormatter) } catch (e: Exception) { LocalTime.now() }

    TimePickerDialog(
        context,
        { _, hour: Int, minute: Int ->
            onTimeSelected(true, LocalTime.of(hour, minute))
        },
        parsedTime.hour,
        parsedTime.minute,
        true
    ).show()
}

@Composable
fun WaterReminderDialog(
    currentFrequency: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var frequencyInput by remember { mutableStateOf(currentFrequency.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Su Hatırlatıcısı Ayarla") },
        text = {
            OutlinedTextField(
                value = frequencyInput,
                onValueChange = { frequencyInput = it },
                label = { Text("Sıklık (dakika)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                suffix = { Text("dk") }
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val frequencyMinutes = frequencyInput.toIntOrNull() ?: currentFrequency
                    onConfirm(frequencyMinutes)
                }
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}

@Composable
fun WaterReminderItem(
    enabled: Boolean,
    frequency: Int,
    onEnable: () -> Unit,
    onDisable: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { if (!enabled) onEnable() else onDisable() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Su Hatırlatıcısı", style = MaterialTheme.typography.bodyLarge)
            if (enabled) {
                val hours = frequency / 60
                val minutes = frequency % 60
                val frequencyText = when {
                    hours > 0 && minutes > 0 -> "$hours saat $minutes dk"
                    hours > 0 -> "$hours saat"
                    else -> "$minutes dk"
                }
                Text(
                    text = "Her $frequencyText bir",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Switch(
            checked = enabled,
            onCheckedChange = { if (it) onEnable() else onDisable() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun transparentTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
)
