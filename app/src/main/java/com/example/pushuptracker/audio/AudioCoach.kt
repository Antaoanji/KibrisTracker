package com.example.pushuptracker.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
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
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null

    // Ortak ses öznitelikleri (Kulaklık yönlendirmesi için kritik)
    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE) // Rehber ses kategorisi
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()

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
            
            // TTS için ses özniteliklerini ayarla (Bluetooth/Kulaklık yönlendirmesi sağlar)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tts?.setAudioAttributes(audioAttributes)
            }

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("AudioCoach", "Turkish language not supported, falling back to English.")
                tts?.setLanguage(Locale.US)
            }
            isInitialized = true
        } else {
            Log.e("AudioCoach", "TTS initialization failed status: $status")
        }
    }

    /**
     * Ses odağını talep eder. Diğer medya seslerini (müzik vb.) %50 seviyesine kısar.
     */
    private fun requestFocus(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // GAIN_TRANSIENT_MAY_DUCK: Mevcut medyayı durdurmaz, sesini kısar.
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(audioAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener { /* No-op */ }
                .build()

            audioManager.requestAudioFocus(audioFocusRequest!!) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    /**
     * Ses odağını bırakır.
     */
    private fun abandonFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
    }

    fun announceExercise(text: String) {
        if (isInitialized) {
            requestFocus()
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "announcement")
        }
    }

    suspend fun playCountdown() {
        if (isInitialized) {
            if (requestFocus()) {
                tts?.speak("3", TextToSpeech.QUEUE_FLUSH, null, "3")
                delay(1000)
                tts?.speak("2", TextToSpeech.QUEUE_FLUSH, null, "2")
                delay(1000)
                tts?.speak("1", TextToSpeech.QUEUE_FLUSH, null, "1")
                delay(1000)
                abandonFocus()
            }
        }
    }

    fun stop() {
        tts?.stop()
        abandonFocus()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        abandonFocus()
        isInitialized = false
    }
}
