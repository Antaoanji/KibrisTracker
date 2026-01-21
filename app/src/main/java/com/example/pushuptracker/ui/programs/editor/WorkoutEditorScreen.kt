package com.example.pushuptracker.ui.programs.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.pushuptracker.room.CustomExerciseEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutEditorScreen(
    navController: NavController,
    viewModel: WorkoutEditorViewModel = hiltViewModel()
) {
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Programı Düzenle") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.addExercise() },
                modifier = Modifier.navigationBarsPadding() // Android 15 için navigasyon barı payı
            ) {
                Icon(Icons.Default.Add, contentDescription = "Egzersiz Ekle")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
        ) {
            // OPTİMİZASYON: 'key' eklenerek listenin akıcılığı artırıldı
            itemsIndexed(
                items = exercises,
                key = { _, exercise -> exercise.id } 
            ) { index, exercise ->
                ExerciseEditItem(
                    exercise = exercise,
                    onUpdate = { viewModel.updateExercise(it) },
                    onDelete = { viewModel.deleteExercise(exercise) },
                    onMoveUp = { if (index > 0) viewModel.moveExercise(index, index - 1) },
                    onMoveDown = { if (index < exercises.size - 1) viewModel.moveExercise(index, index + 1) }
                )
            }
        }
    }
}

@Composable
fun ExerciseEditItem(
    exercise: CustomExerciseEntity,
    onUpdate: (CustomExerciseEntity) -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val showDialog = remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = exercise.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "${exercise.sets} Set x ${exercise.reps}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Row {
                IconButton(onClick = onMoveUp) { Icon(Icons.Default.ArrowUpward, contentDescription = null) }
                IconButton(onClick = onMoveDown) { Icon(Icons.Default.ArrowDownward, contentDescription = null) }
                IconButton(onClick = { showDialog.value = true }) { Icon(Icons.Default.Edit, contentDescription = null) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
            }
        }
    }

    if (showDialog.value) {
        EditExerciseDialog(
            exercise = exercise,
            onDismiss = { showDialog.value = false },
            onConfirm = { updatedExercise ->
                onUpdate(updatedExercise)
                showDialog.value = false
            }
        )
    }
}

@Composable
fun EditExerciseDialog(
    exercise: CustomExerciseEntity,
    onDismiss: () -> Unit,
    onConfirm: (CustomExerciseEntity) -> Unit
) {
    var name by remember { mutableStateOf(exercise.name) }
    var description by remember { mutableStateOf(exercise.description) }
    var videoUrl by remember { mutableStateOf(exercise.videoUrl) }
    var sets by remember { mutableStateOf(exercise.sets.toString()) }
    var reps by remember { mutableStateOf(exercise.reps) }
    var rest by remember { mutableStateOf(exercise.restTimeSeconds.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Egzersizi Düzenle") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = { Text("Egzersiz Adı") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description, 
                    onValueChange = { description = it }, 
                    label = { Text("Açıklama / Nasıl Yapılır?") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                    maxLines = 4
                )

                OutlinedTextField(
                    value = videoUrl, 
                    onValueChange = { videoUrl = it }, 
                    label = { Text("YouTube Video Linki") },
                    placeholder = { Text("https://youtube.com/...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.PlayCircleFilled, contentDescription = null, tint = Color.Red) }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sets, 
                        onValueChange = { sets = it }, 
                        label = { Text("Set") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = reps, 
                        onValueChange = { reps = it }, 
                        label = { Text("Tekrar") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                OutlinedTextField(
                    value = rest, 
                    onValueChange = { rest = it }, 
                    label = { Text("Dinlenme (sn)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(exercise.copy(
                    name = name,
                    description = description,
                    videoUrl = videoUrl,
                    sets = sets.toIntOrNull() ?: exercise.sets,
                    reps = reps,
                    restTimeSeconds = rest.toIntOrNull() ?: exercise.restTimeSeconds
                ))
            }) { Text("Kaydet") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("İptal") } }
    )
}
