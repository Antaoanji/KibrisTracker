package com.example.pushuptracker.ai

import kotlinx.serialization.Serializable
import retrofit2.HttpException
import retrofit2.http.Body
import retrofit2.http.POST
import javax.inject.Inject
import javax.inject.Singleton

// ---------- API MODELS ----------

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>
)

@Serializable
data class Content(
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String
)

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate>?
)

@Serializable
data class Candidate(
    val content: Content?
)

// ---------- RESULT WRAPPER ----------

sealed class AiResult {
    data class Success(val text: String) : AiResult()
    data class Error(
        val code: Int,
        val message: String
    ) : AiResult()
}

// ---------- RETROFIT API ----------

interface GeminiApiService {
    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    suspend fun generateContent(
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

// ---------- SERVICE ----------

@Singleton
class GenerativeAiService @Inject constructor(
    private val geminiApi: GeminiApiService
) {

    suspend fun generateWorkout(prompt: String): AiResult {
        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(Part(text = prompt))
                )
            )
        )

        return try {
            val response = geminiApi.generateContent(request)

            val text = response
                .candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text

            if (text.isNullOrBlank()) {
                AiResult.Error(
                    code = -1,
                    message = "Boş yanıt alındı"
                )
            } else {
                AiResult.Success(text)
            }

        } catch (e: HttpException) {

            if (e.code() == 429) {
                // ❌ RETRY YOK — FREE TIER
                AiResult.Error(
                    code = 429,
                    message = "Limit doldu"
                )
            } else {
                AiResult.Error(
                    code = e.code(),
                    message = "Sunucu hatası"
                )
            }

        } catch (e: Exception) {

            AiResult.Error(
                code = -1,
                message = "Ağ hatası"
            )
        }
    }
}
