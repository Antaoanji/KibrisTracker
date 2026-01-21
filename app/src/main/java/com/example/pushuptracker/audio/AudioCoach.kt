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
                Log.e("AudioCoach", "Turkish language not supported, falling back to English.")
                tts?.setLanguage(Locale.US)
            }
            isInitialized = true
        } else {
            Log.e("AudioCoach", "TTS initialization failed status: $status")
        }
    }

    fun announceExercise(text: String) {
        if (isInitialized) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "announcement")
        }
    }

    suspend fun playCountdown() {
        if (isInitialized) {
            // Flush any current speech to ensure countdown is immediate
            tts?.speak("3", TextToSpeech.QUEUE_FLUSH, null, "3")
            delay(1000)
            tts?.speak("2", TextToSpeech.QUEUE_FLUSH, null, "2")
            delay(1000)
            tts?.speak("1", TextToSpeech.QUEUE_FLUSH, null, "1")
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        isInitialized = false
    }
}
