package com.example.pushuptracker.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

// Retrofit Service Interface
interface WgerApiService {
    @GET("exerciseinfo")
    suspend fun getExercises(): WgerExerciseResponse

    @GET("exerciseimage")
    suspend fun getExerciseImages(@Query("exercise_base") exerciseBaseId: Int): WgerImageResponse
}

// --- Data Models for API Response ---

@Serializable
data class WgerExerciseResponse(
    val results: List<WgerExerciseInfo>? = null
)

@Serializable
data class WgerExerciseInfo(
    val id: Int,
    val name: String? = null,
    @SerialName("exercise_base")
    val exerciseBase: Int? = null,
    val description: String? = null,
    val category: Category? = null,
    val translations: List<Translation>? = null,
    val images: List<WgerExerciseImage>? = null // Added images to the main exercise info
)

@Serializable
data class Category(
    val name: String? = null
)

@Serializable
data class Translation(
    val language: Int? = null,
    val name: String? = null
)

@Serializable
data class WgerImageResponse(
    val results: List<WgerExerciseImage>? = null
)

@Serializable
data class WgerExerciseImage(
    val id: Int? = null,
    val image: String? = null
)
