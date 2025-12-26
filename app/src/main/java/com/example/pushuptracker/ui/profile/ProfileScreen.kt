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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.pushuptracker.R
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val age by viewModel.age.collectAsStateWithLifecycle(30)
    val weight by viewModel.weight.collectAsStateWithLifecycle(70)
    val gender by viewModel.gender.collectAsStateWithLifecycle("Erkek")
    val goal by viewModel.goal.collectAsStateWithLifecycle("Direnç Kazanma")
    val workoutFrequency by viewModel.workoutFrequency.collectAsStateWithLifecycle("Orta Seviye")
    val dailyGoal by viewModel.dailyGoal.collectAsStateWithLifecycle(50)
    val dailyWaterGoal by viewModel.dailyWaterGoal.collectAsStateWithLifecycle(2000)
    val pushupReminderEnabled by viewModel.pushupReminderEnabled.collectAsStateWithLifecycle(false)
    val pushupReminderTime by viewModel.pushupReminderTime.collectAsStateWithLifecycle("18:00")
    val waterReminderEnabled by viewModel.waterReminderEnabled.collectAsStateWithLifecycle(false)
    val waterReminderFrequency by viewModel.waterReminderFrequency.collectAsStateWithLifecycle(120)

    var ageInput by remember(age) { mutableStateOf(age.toString()) }
    var weightInput by remember(weight) { mutableStateOf(weight.toString()) }
    var selectedGender by remember(gender) { mutableStateOf(gender) }
    var selectedGoal by remember(goal) { mutableStateOf(goal) }
    var selectedFrequency by remember(workoutFrequency) { mutableStateOf(workoutFrequency) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showCheckAnimation by remember { mutableStateOf(false) }

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

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)){
                        OutlinedTextField(value = ageInput, onValueChange = {ageInput = it}, label = { Text("Yaş") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), colors = transparentTextFieldColors())
                        OutlinedTextField(value = weightInput, onValueChange = {weightInput = it}, label = { Text("Kilo (kg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), colors = transparentTextFieldColors())
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    ProfileDropdown(label = "Cinsiyet", selectedOption = selectedGender, options = listOf("Erkek", "Kadın", "Diğer")) { selectedGender = it }
                    Spacer(modifier = Modifier.height(8.dp))
                    ProfileDropdown(label = "Hedef", selectedOption = selectedGoal, options = listOf("Kilo Verme", "Direnç Kazanma", "Kas Kütlesi Artırma")) { selectedGoal = it }
                    Spacer(modifier = Modifier.height(8.dp))
                    ProfileDropdown(label = "Antrenman Sıklığı", selectedOption = selectedFrequency, options = listOf("Yeni Başlayan", "Orta Seviye", "Düzenli")) { selectedFrequency = it }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { 
                        ageInput.toIntOrNull()?.let { viewModel.saveAge(it) }
                        weightInput.toIntOrNull()?.let { viewModel.saveWeight(it) }
                        viewModel.saveGender(selectedGender)
                        viewModel.saveGoal(selectedGoal)
                        viewModel.saveWorkoutFrequency(selectedFrequency)
                        showCheckAnimation = true
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text("Profili Güncelle")
                    }
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
                    ReminderSwitch(label = "Şınav Hatırlatıcısı", enabled = pushupReminderEnabled, detail = pushupReminderTime) { enabled, time -> viewModel.setPushupReminder(enabled, time) }
                    Spacer(modifier = Modifier.height(16.dp))
                    ReminderSwitch(label = "Su Hatırlatıcısı", enabled = waterReminderEnabled, detail = "${waterReminderFrequency} dk") { enabled, _ -> viewModel.setWaterReminder(enabled, null) }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDropdown(label: String, selectedOption: String, options: List<String>, onOptionSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            value = selectedOption,
            onValueChange = {}, 
            label = { Text(label) },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = transparentTextFieldColors()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onOptionSelected(option); expanded = false })
            }
        }
    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun transparentTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
)
