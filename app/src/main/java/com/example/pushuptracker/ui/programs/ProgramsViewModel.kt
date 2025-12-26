package com.example.pushuptracker.ui.programs

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pushuptracker.SettingsManager
import com.example.pushuptracker.ai.AiResult
import com.example.pushuptracker.data.repo.ExerciseRepository
import com.example.pushuptracker.data.repo.WorkoutPlanRepository
import com.example.pushuptracker.di.WorkoutHolder
import com.example.pushuptracker.model.Exercise
import com.example.pushuptracker.model.Workout
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgramsViewModel @Inject constructor(
    private val workoutPlanRepository: WorkoutPlanRepository,
    private val exerciseRepository: ExerciseRepository,
    private val settingsManager: SettingsManager,
    val workoutHolder: WorkoutHolder
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgramScreenUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsManager.lastWorkoutSummaryFlow.collect {
                _uiState.update { state -> state.copy(lastWorkoutSummary = it) }
            }
        }
        viewModelScope.launch {
            workoutHolder.currentDay.collect { day ->
                _uiState.update { it.copy(currentDay = day) }
            }
        }
    }

    fun onStartWorkoutClicked() {
        if (!workoutHolder.isWorkoutActive()) {
            _uiState.value.structuredWorkout?.let {
                // We only want to start the *first day's* workout
                val firstDayExercises = it.exercises.drop(1).takeWhile { ex -> ex.sets > 0 }
                if (firstDayExercises.isNotEmpty()) {
                    val firstDayWorkout = Workout(
                        title = it.exercises.first().name, // "Day 1: ..."
                        exercises = firstDayExercises
                    )
                    workoutHolder.workout = firstDayWorkout
                }
            }
        }
    }

    fun showEquipmentDialog() {
        _uiState.update { it.copy(showEquipmentDialog = true) }
    }

    fun dismissEquipmentDialog() {
        _uiState.update { it.copy(showEquipmentDialog = false) }
    }

    fun clearGeneratedWorkout() {
        _uiState.update {
            it.copy(
                workoutPlan = null,
                structuredWorkout = null
            )
        }
    }

    fun generateWeeklyWorkoutPlan(equipments: List<String>) {
        if (_uiState.value.isLoading) return

        _uiState.update {
            it.copy(
                showEquipmentDialog = false,
                isLoading = true,
                workoutPlan = null,
                structuredWorkout = null,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            val age = settingsManager.ageFlow.first()
            val weight = settingsManager.weightFlow.first()
            val gender = settingsManager.genderFlow.first()
            val goal = settingsManager.goalFlow.first()
            val frequency = settingsManager.workoutFrequencyFlow.first()

            val userLevel = when (frequency) {
                "Yeni Başlayan" -> "Başlangıç"
                "Orta Seviye" -> "Orta"
                "Düzenli" -> "İleri"
                else -> "Bilinmiyor"
            }

            val prompt = createWeeklyPrompt(
                age = age,
                weight = weight,
                gender = gender,
                userGoal = goal,
                userLevel = userLevel, // Pass the calculated user level
                equipments = equipments
            )

            when (val result = workoutPlanRepository.getWorkoutPlan(prompt)) {
                is AiResult.Success -> {
                    val parsedWorkout = parseWeeklyWorkout(result.text)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            workoutPlan = result.text,
                            structuredWorkout = parsedWorkout
                        )
                    }
                }

                is AiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    private fun createWeeklyPrompt(
        age: Int,
        weight: Int,
        gender: String,
        userGoal: String,
        userLevel: String,
        equipments: List<String>
    ): String {
        val equipmentString = if (equipments.isEmpty()) "Vücut Ağırlığı" else equipments.joinToString(", ")
        return """
Kullanıcı Bilgileri:
- Yaş: $age
- Kilo: $weight kg
- Cinsiyet: $gender
- Fitness Seviyesi: $userLevel
- Ana Hedef: $userGoal
- Mevcut Ekipmanlar: $equipmentString

İstek:
Yukarıdaki kullanıcı bilgileri ve ekipmanlara dayanarak 7 günlük bir antrenman programı oluştur.
Program aşağıdaki yapıya harfiyen uymalıdır:
- Gün 1: Push A (Göğüs, Triceps, Karın)
- Gün 2: Pull A (Sırt, Biceps, Ön Kol)
- Gün 3: Legs & Shoulders A (Bacak, Omuz)
- Gün 4: Aktif Dinlenme (30 dakika "Japon Yürüyüşü" veya tam dinlenme)
- Gün 5: Push B (Gün 1'in farklı bir varyasyonu)
- Gün 6: Pull B (Gün 2'nin farklı bir varyasyonu)
- Gün 7: Legs & Shoulders B (Gün 3'ün farklı bir varyasyonu)

Çıktı Formatı Kuralları (ZORUNLU):
- Genel bir başlık için "### Haftalık Antrenman Programı" kullan.
- Her gün "#### Gün X: [Açıklama]" formatında bir başlıkla başlamalıdır.
- Her egzersiz "## [Egzersiz Adı] ([ingilizce-anahtar-kelime])" formatında bir başlıkla başlamalıdır.
- Egzersiz detayları (Set, Tekrar, Dinlenme, MET) "*" ile başlayan maddeler halinde olmalıdır. Sadece tam sayı veya ondalıklı sayı kullan (virgül yerine nokta).
- MET (Metabolik Eşdeğer) değeri, egzersizin yoğunluğunu belirten bilimsel bir değerdir ve zorunludur.
- Dinlenme birimi her zaman saniye cinsinden olmalıdır.
- Her egzersizden sonra bir satırlık "Açıklama:" metni ekle.
- Başka hiçbir metin, giriş, sonuç veya emoji kullanma. Sadece belirtilen formatta çıktı üret.

Örnek Çıktı:
### Haftalık Antrenman Programı
#### Gün 1: Göğüs + Triceps + Karın (Push A)
## Bench Press (barbell-press)
* Set: 4
* Tekrar: 8
* Dinlenme: 90
* MET: 5.0
Açıklama: Göğüs kaslarını hedef alan temel bir itiş egzersizidir.
## Incline Dumbbell Press (incline-dumbbell-press)
* Set: 3
* Tekrar: 10
* Dinlenme: 60
* MET: 4.5
Açıklama: Üst göğüs kaslarını çalıştırmak için dumbell ile yapılan bir varyasyondur.
... (diğer günler ve egzersizler)
""".trimIndent()
    }

    private suspend fun parseWeeklyWorkout(markdown: String): Workout? {
        try {
            val lines = markdown.lines().filter { it.isNotBlank() }
            if (lines.isEmpty() || !lines.first().trim().startsWith("###")) return null

            val mainTitle = lines.first().removePrefix("###").trim()
            val allExercises = mutableListOf<Exercise>()

            val dayChunks = markdown.split("\n#### ").drop(1)
            if (dayChunks.isEmpty()) return null

            for (dayChunk in dayChunks) {
                val dayLines = dayChunk.lines()
                val dayTitle = dayLines.first().trim()

                allExercises.add(Exercise(name = dayTitle, sets = 0, reps = "", restTimeSeconds = 0, description = "", imageUrl = ""))

                val exerciseChunks = dayChunk.split("\n## ").drop(1)

                for (chunk in exerciseChunks) {
                    val chunkLines = chunk.lines()
                    val fullName = chunkLines.first().trim()
                    if (fullName.isEmpty()) continue

                    val displayName = fullName.substringBeforeLast(" (").trim()
                    val searchKey = fullName.substringAfterLast("(").substringBefore(")").trim()
                    if (searchKey.isEmpty()) continue

                    val details = mutableMapOf<String, String>()
                    chunkLines.drop(1)
                        .takeWhile { it.startsWith("*") }
                        .forEach {
                            val parts = it.removePrefix("*").split(":", limit = 2)
                            if (parts.size == 2) {
                                details[parts[0].trim().lowercase()] = parts[1].trim()
                            }
                        }

                    val sets = details["set"]?.toIntOrNull() ?: continue
                    val reps = details["tekrar"]?.toIntOrNull()?.toString() ?: continue
                    val rest = details["dinlenme"]?.toIntOrNull() ?: continue
                    val met = details["met"]?.toDoubleOrNull() ?: 3.0 // Default MET

                    val description = chunkLines.firstOrNull { it.startsWith("Açıklama:") }?.removePrefix("Açıklama:")?.trim() ?: ""

                    val imageUrl = exerciseRepository.findExerciseImage(searchKey)

                    allExercises.add(
                        Exercise(
                            name = displayName,
                            sets = sets,
                            reps = reps,
                            restTimeSeconds = rest,
                            description = description,
                            imageUrl = imageUrl,
                            metValue = met
                        )
                    )
                }
            }
            if (allExercises.none { it.sets > 0 }) return null
            return Workout(mainTitle, allExercises)
        } catch (e: Exception) {
            Log.e("ProgramsViewModel", "Weekly workout parse error", e)
            return null
        }
    }
}

data class ProgramScreenUiState(
    val isLoading: Boolean = false,
    val workoutPlan: String? = null,
    val structuredWorkout: Workout? = null,
    val showEquipmentDialog: Boolean = false,
    val errorMessage: String? = null,
    val lastWorkoutSummary: com.example.pushuptracker.model.WorkoutSummary? = null,
    val currentDay: Int = 0
)
