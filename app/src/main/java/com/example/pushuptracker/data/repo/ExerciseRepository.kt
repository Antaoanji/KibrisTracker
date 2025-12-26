package com.example.pushuptracker.data.repo

import android.util.Log
import com.example.pushuptracker.data.remote.WgerApiService
import com.example.pushuptracker.data.remote.WgerExerciseInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseRepository @Inject constructor(private val wgerApiService: WgerApiService) {

    // Cache for exercises to avoid repeated API calls
    private var exerciseList: List<WgerExerciseInfo> = emptyList()

    private suspend fun loadExercises() {
        if (exerciseList.isEmpty()) {
            try {
                // Handle nullable results from the API
                exerciseList = wgerApiService.getExercises().results ?: emptyList()
            } catch (e: Exception) {
                Log.e("ExerciseRepository", "Error loading exercises", e)
            }
        }
    }

    suspend fun findExerciseImage(exerciseName: String): String {
        loadExercises()
        if (exerciseList.isEmpty()) return ""

        // Find the best matching exercise from the cached list
        val bestMatch = findBestMatch(exerciseName)
        
        // Safely handle nullable bestMatch and its properties
        return bestMatch?.exerciseBase?.let { exerciseBaseId ->
            try {
                // Fetch the image for the matched exercise
                val imageResponse = wgerApiService.getExerciseImages(exerciseBaseId)
                // Safely handle nullable results and find the first valid image URL
                imageResponse.results?.firstOrNull()?.image ?: ""
            } catch (e: Exception) {
                Log.e("ExerciseRepository", "Error fetching exercise image", e)
                ""
            }
        } ?: ""
    }

    private fun findBestMatch(searchQuery: String): WgerExerciseInfo? {
        val lowerCaseQuery = searchQuery.lowercase()
        
        // 1. Exact match, safely handle nullable name
        val exactMatch = exerciseList.find { it.name?.equals(lowerCaseQuery, ignoreCase = true) == true }
        if (exactMatch != null) return exactMatch

        // 2. Match based on containing all words, safely handle nullable name
        val searchWords = lowerCaseQuery.split(" ")
        return exerciseList.maxByOrNull { exercise ->
            val exerciseWords = exercise.name?.lowercase()?.split(" ") ?: emptyList()
            searchWords.count { it in exerciseWords }
        }
    }
}
