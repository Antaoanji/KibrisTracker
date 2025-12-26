package com.example.pushuptracker.data.repo

import com.example.pushuptracker.ai.AiResult
import com.example.pushuptracker.ai.GenerativeAiService
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutPlanRepository @Inject constructor(
    private val generativeAiService: GenerativeAiService
) {

    // ✅ Cache successful results
    private val workoutCache = ConcurrentHashMap<String, String>()

    suspend fun getWorkoutPlan(prompt: String): AiResult {
        // The prompt itself is a good cache key, but we hash it for a fixed length
        val cacheKey = prompt.hashCode().toString()

        // 🔹 Cache hit
        workoutCache[cacheKey]?.let { cachedText ->
            return AiResult.Success(cachedText)
        }

        // 🔹 Cache miss → API call
        val result = generativeAiService.generateWorkout(prompt)

        // 🔹 Cache only successful results
        if (result is AiResult.Success) {
            workoutCache[cacheKey] = result.text
        }

        return result
    }
}
