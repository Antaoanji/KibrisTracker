package com.example.pushuptracker.ai

import com.example.pushuptracker.BuildConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST
import javax.inject.Inject
import javax.inject.Singleton

// --- Data Classes for Serialization ---
@Serializable
data class GenerateContentRequest(val contents: List<Content>)

@Serializable
data class Content(val parts: List<Part>)

@Serializable
data class Part(val text: String)

@Serializable
data class GenerateContentResponse(val candidates: List<Candidate>?)

@Serializable
data class Candidate(val content: Content?)

// --- Retrofit API Interface ---
interface GeminiApiService {
    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    suspend fun generateContent(
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

// --- Service using the Retrofit Interface ---
@Singleton
class GenerativeAiService @Inject constructor(private val geminiApi: GeminiApiService) {

    suspend fun generateWorkout(prompt: String): String {
        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt))))
        )
        return try {
            val response = geminiApi.generateContent(request)
            // Safely access the text, providing a default message if any part is null
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Yapay zekadan bir yanıt alınamadı. Lütfen tekrar deneyin."
        } catch (e: Exception) {
            // In a real app, you should log the error for debugging
            // Log.e("GenerativeAiService", "API Call failed", e)
            e.localizedMessage ?: "Bilinmeyen bir ağ hatası oluştu."
        }
    }
}
