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
import com.example.pushuptracker.model.WorkoutSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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

    // This is the single source of truth for the UI state
    val uiState: StateFlow<ProgramScreenUiState> = combine(
        settingsManager.activeWorkoutPlanFlow,
        settingsManager.activeWorkoutCurrentDayFlow,
        settingsManager.lastWorkoutSummaryFlow
    ) { planMarkdown, currentDay, summary ->
        val parsedWorkout = if (planMarkdown != null) parseWeeklyWorkout(planMarkdown) else null
        // Update the in-memory holder as well, for the workout player to use
        workoutHolder.structuredWorkout.value = parsedWorkout

        ProgramScreenUiState(
            structuredWorkout = parsedWorkout,
            currentDay = currentDay,
            lastWorkoutSummary = summary
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProgramScreenUiState(isLoading = true)
    )

    // Separate state for ephemeral UI events that shouldn't be in the main state flow
    private val _eventState = MutableStateFlow(ProgramEventState())
    val eventState = _eventState.asStateFlow()

    fun onStartWorkoutClicked() {
        if (!workoutHolder.isWorkoutActive()) {
            uiState.value.structuredWorkout?.let { fullPlan ->
                val currentDay = uiState.value.currentDay
                val exercisesForDay = getExercisesForDay(fullPlan, currentDay)

                if (exercisesForDay.isNotEmpty()) {
                    val dayTitle = fullPlan.exercises.firstOrNull { it.name.startsWith("Gün $currentDay") }?.name ?: "Antrenman"
                    val dayWorkout = Workout(
                        title = dayTitle,
                        exercises = exercisesForDay
                    )
                    workoutHolder.workout = dayWorkout
                }
            }
        }
        // Dismiss the details screen when workout starts
        onPlanDetailsDismissed()
    }

    private fun getExercisesForDay(workout: Workout, day: Int): List<Exercise> {
        if (day <= 0) return emptyList()
        val dayHeader = "Gün $day"
        val startIndex = workout.exercises.indexOfFirst { it.name.startsWith(dayHeader) }
        if (startIndex == -1) return emptyList()

        return workout.exercises
            .drop(startIndex + 1)
            .takeWhile { it.sets > 0 } // Take exercises until the next day header
    }

    fun showEquipmentDialog() {
        _eventState.update { it.copy(showEquipmentDialog = true) }
    }

    fun dismissEquipmentDialog() {
        _eventState.update { it.copy(showEquipmentDialog = false) }
    }

    fun onPlanDetailsDismissed() {
        _eventState.update { it.copy(displayingPlanDetails = false) }
    }

    fun clearGeneratedWorkout() {
        viewModelScope.launch {
            settingsManager.clearActiveWorkout()
            workoutHolder.clearWorkout()
            _eventState.update { it.copy(displayingPlanDetails = false) } // Also hide details
        }
    }

    fun generateWeeklyWorkoutPlan(equipments: List<String>) {
        if (uiState.value.isLoading) return

        clearGeneratedWorkout()
         _eventState.update {
            it.copy(
                showEquipmentDialog = false,
                isLoading = true,
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
                "Yeni Başlayan" -> "Beginner"
                "Orta Seviye" -> "Intermediate"
                "Düzenli" -> "Advanced"
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
                    // Save the new plan to settings, the UI will update automatically via the main state flow
                    settingsManager.saveActiveWorkout(result.text, 1)
                    _eventState.update { it.copy(isLoading = false, displayingPlanDetails = true) } // Show details for the new plan
                }

                is AiResult.Error -> {
                    _eventState.update {
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
- Fitness Seviyesi: $userLevel (Beginner | Intermediate | Advanced)
- Ana Hedef: $userGoal
- Mevcut Ekipmanlar: $equipmentString

İstek:
Yukarıdaki kullanıcı bilgileri ve mevcut ekipmanlara kesinlikle bağlı kalarak 7 günlük bir antrenman programı oluştur.

Program aşağıdaki yapıya harfi harfine uymalıdır:
- Gün 1: Push A (Göğüs, Triceps, Karın)
- Gün 2: Pull A (Sırt, Biceps, Ön Kol)
- Gün 3: Legs & Shoulders A (Bacak, Omuz)
- Gün 4: Aktif Dinlenme (30 dakika "Japon Yürüyüşü" veya tam dinlenme)
- Gün 5: Push B (Gün 1’in farklı egzersiz varyasyonları)
- Gün 6: Pull B (Gün 2’nin farklı egzersiz varyasyonları)
- Gün 7: Legs & Shoulders B (Gün 3’ün farklı egzersiz varyasyonları)

ZORUNLU EGZERSİZ KURALLARI:
- Her antrenman günü 5 veya 6 egzersiz içermelidir.
- Push, Pull ve Legs günlerinde yalnızca ilgili kas gruplarına ait egzersizler yazılmalıdır.
- B günleri, A günlerindeki egzersizlerin aynısını içeremez; hareket varyasyonu zorunludur.
- Kullanıcının sahip olmadığı ekipmana ait hiçbir egzersiz yazılmamalıdır.
- Kardiyo veya stretching egzersizleri yalnızca Gün 4’te yer alabilir.

FITNESS SEVİYESİNE GÖRE SET VE TEKRAR KURALLARI:
- Beginner:
  - Set: 2–3
  - Tekrar: 10–15
- Intermediate:
  - Set: 3–4
  - Tekrar: 8–12
- Advanced:
  - Set: 4–5
  - Tekrar: 5–10
Bu aralıkların dışına çıkma.

MET (METABOLİK EŞDEĞER) KURALLARI:
- Barbell compound egzersizleri: 5.0 – 6.0
- Dumbbell compound egzersizleri: 4.0 – 5.0
- İzolasyon egzersizleri: 3.0 – 4.0
- Karın egzersizleri: 3.5 – 4.5
- Aktif dinlenme yürüyüşü: 3.0 – 3.5
Bu aralıkların dışında MET değeri kullanma.

DİNLENME KURALLARI:
- Dinlenme süresi her zaman saniye cinsinden yazılmalıdır.
- Compound egzersizler: 60–120 saniye
- İzolasyon egzersizleri: 45–75 saniye

TANIM:
- “Japon Yürüyüşü”: Düz zeminde, konuşma temposunda, kesintisiz ve tempolu yürüyüş.

ÇIKTI FORMAT KURALLARI (KESİNLİKLE ZORUNLU):
- Genel başlık:
  ### Haftalık Antrenman Programı
- Her gün şu formatla başlamalıdır:
  #### Gün X: [Açıklama]
- Her egzersiz şu formatla başlamalıdır:
  ## Egzersiz Adı (english-keyword)
- İngilizce anahtar kelime kuralları:
  - lowercase
  - kebab-case
  - boşluk veya özel karakter yok
- Egzersiz detayları "*" ile başlayan maddeler halinde yazılmalıdır:
  * Set: sayı
  * Tekrar: sayı
  * Dinlenme: saniye
  * MET: ondalıklı_sayı
- Her egzersizin sonunda tek satırlık açıklama olmalıdır:
  Açıklama: ...
- Başka hiçbir metin, giriş, sonuç, uyarı veya emoji yazma.
- Sadece bu formatta çıktı üret.
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
                    val rest = details["dinlenme"]?.replace(" saniye", "")?.toIntOrNull() ?: continue
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

// Represents the persistent state of the screen
data class ProgramScreenUiState(
    val isLoading: Boolean = false,
    val structuredWorkout: Workout? = null,
    val lastWorkoutSummary: WorkoutSummary? = null,
    val currentDay: Int = 1
)

// Represents ephemeral (one-off) UI events
data class ProgramEventState(
    val showEquipmentDialog: Boolean = false,
    val displayingPlanDetails: Boolean = false,
    val errorMessage: String? = null,
    val isLoading: Boolean = false // For generation loading
)
