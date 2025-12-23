package com.example.pushuptracker.ui.programs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pushuptracker.SettingsManager
import com.example.pushuptracker.ai.GenerativeAiService
import com.example.pushuptracker.di.WorkoutHolder
import com.example.pushuptracker.model.Exercise
import com.example.pushuptracker.model.Workout
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgramsViewModel @Inject constructor(
    private val generativeAiService: GenerativeAiService,
    private val settingsManager: SettingsManager,
    private val workoutHolder: WorkoutHolder // Inject the holder
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgramScreenUiState())
    val uiState = _uiState.asStateFlow()

    fun onStartWorkout(workout: Workout) {
        workoutHolder.workout = workout
    }

    fun generateWorkoutPlan(bodyPart: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, structuredWorkout = null, workoutPlan = null)

            val age = settingsManager.ageFlow.first()
            val weight = settingsManager.weightFlow.first()
            val gender = settingsManager.genderFlow.first()
            val userGoal = settingsManager.goalFlow.first()
            val frequency = settingsManager.workoutFrequencyFlow.first()

            val prompt = createPrompt(age, weight, gender, userGoal, bodyPart, frequency)
            val generatedPlan = generativeAiService.generateWorkout(prompt)
            val parsedWorkout = parseWorkout(generatedPlan)

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                workoutPlan = generatedPlan, // Keep the raw markdown
                structuredWorkout = parsedWorkout // Add the parsed workout object
            )
        }
    }

    private fun createPrompt(age: Int, weight: Int, gender: String, userGoal: String, bodyPart: String, frequency: String): String {
        // Added instruction for a GIF URL
        return """
            Kullanıcı Bilgileri:
            - Yaş: $age
            - Kilo: $weight kg
            - Cinsiyet: $gender
            - Ana Hedef: $userGoal
            - Antrenman Sıklığı / Deneyim: $frequency

            İstenen Antrenman:
            - Odaklanılacak Vücut Bölgesi: $bodyPart
            
            Lütfen yukarıdaki bilgilere dayanarak, bu kullanıcı için evde yapabileceği, belirtilen deneyim seviyesine uygun bir antrenman programı oluştur. 
            Program, her hareket için set ve tekrar sayılarını, set aralarındaki dinlenme sürelerini, her hareketin nasıl yapılacağına dair kısa, net ve motive edici açıklamalar ve hareketin kendisini gösteren public bir GIF URL'si içersin.
            Programın başlığı "###" ile, her egzersiz adı "##" ile başlasın. Egzersiz detayları (set, tekrar, dinlenme, GIF) "*" ile başlayan maddeler halinde olsun. Açıklama metni ise normal paragraf olarak yazılsın.
            Örnek Format:
            ### Haftalık Tüm Vücut Antrenmanı
            ## Şınav
            * Set: 3
            * Tekrar: Maksimum
            * Dinlenme: 60 saniye
            * GIF: https://some.url/pushup.gif
            Bu bir şınav açıklamasıdır...
            
            Programın tamamı bu formatta ve Türkçe olsun.
            """
    }

    private fun parseWorkout(markdown: String): Workout? {
        try {
            val lines = markdown.lines().filter { it.isNotBlank() }
            val title = lines.first { it.startsWith("###") }.removePrefix("### ").trim()
            val exercises = mutableListOf<Exercise>()

            var i = 1
            while (i < lines.size) {
                if (lines[i].startsWith("##")) {
                    val name = lines[i].removePrefix("## ").trim()
                    i++
                    val details = mutableMapOf<String, String>()
                    while (i < lines.size && lines[i].startsWith("*")) {
                        val parts = lines[i].removePrefix("*").split(":", limit = 2)
                        if (parts.size == 2) {
                            details[parts[0].trim().lowercase()] = parts[1].trim()
                        }
                        i++
                    }
                    val description = lines.getOrNull(i)?.takeIf { !it.startsWith("##") && !it.startsWith("*") } ?: ""
                    if(description.isNotBlank()) i++

                    exercises.add(Exercise(
                        name = name,
                        sets = details["set"]?.toIntOrNull() ?: 3,
                        reps = details["tekrar"] ?: "10",
                        restTimeSeconds = details["dinlenme"]?.filter { it.isDigit() }?.toIntOrNull() ?: 60,
                        description = description,
                        gifUrl = details["gif"] ?: "" // Extract GIF URL
                    ))
                }
                 else {
                    i++
                }
            }
            return Workout(title, exercises)
        } catch (e: Exception) {
            return null // Return null if parsing fails
        }
    }
}

data class ProgramScreenUiState(
    val isLoading: Boolean = false,
    val workoutPlan: String? = null,
    val structuredWorkout: Workout? = null // Add a field for the parsed workout
)
