package com.example.pushuptracker.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioCoach @Inject constructor(
    @param:ApplicationContext private val context: Context
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        try {
            tts = TextToSpeech(context, this)
        } catch (e: Exception) {
            Log.e("AudioCoach", "TTS initialization failed", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.forLanguageTag("tr-TR"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("AudioCoach", "Turkish language is not supported.")
            } else {
                isInitialized = true
            }
        } else {
            Log.e("AudioCoach", "TTS initialization failed with status: $status")
        }
    }

    fun announceExercise(exerciseName: String) {
        if (isInitialized) {
            val textToSpeak = "Sıradaki: $exerciseName"
            tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            Log.w("AudioCoach", "TTS not ready for announcement.")
        }
    }

    suspend fun playCountdown() {
        if (isInitialized) {
            // Clear any ongoing speech
            tts?.stop()
            // Speak countdown with delays
            for (i in 3 downTo 1) {
                tts?.speak(i.toString(), TextToSpeech.QUEUE_ADD, null, i.toString())
                delay(1000) // Wait for 1 second between numbers
            }
        } else {
            Log.w("AudioCoach", "TTS not ready for countdown.")
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        if (tts != null) {
            tts?.stop()
            tts?.shutdown()
        }
        isInitialized = false
    }
}