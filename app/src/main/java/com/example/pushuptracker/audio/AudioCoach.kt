package com.example.pushuptracker.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
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

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
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
            tts?.setLanguage(Locale.forLanguageTag("tr-TR"))
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tts?.setAudioAttributes(audioAttributes)
            }

            // DÜZELTME: Konuşma bittiğinde müziğin devam etmesini sağlayan tetikleyici
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    Log.d("AudioCoach", "Speech started: $utteranceId")
                }

                override fun onDone(utteranceId: String?) {
                    Log.d("AudioCoach", "Speech done: $utteranceId")
                    // Konuşma bittiğinde odağı bırakıyoruz ki müzik çalar devam etsin
                    abandonFocus()
                }

                override fun onError(utteranceId: String?) {
                    abandonFocus()
                }
            })
            isInitialized = true
        }
    }

    private fun requestFocus(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // GAIN_TRANSIENT: Müziği durdurur, odak bırakıldığında geri başlatır.
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(audioAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener { /* Diğer uygulama değişimlerini dinle */ }
                .build()

            audioManager.requestAudioFocus(audioFocusRequest!!) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun abandonFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
        Log.d("AudioCoach", "Audio focus abandoned")
    }

    fun announceExercise(text: String) {
        if (isInitialized) {
            requestFocus()
            // UtteranceId vererek listener'ın tetiklenmesini sağlıyoruz
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "announcement_${System.currentTimeMillis()}")
        }
    }

    suspend fun playCountdown() {
        if (isInitialized) {
            if (requestFocus()) {
                // Geri sayımda sadece son sayıda odağı bırakıyoruz
                tts?.speak("3", TextToSpeech.QUEUE_ADD, null, "3")
                delay(1000)
                tts?.speak("2", TextToSpeech.QUEUE_ADD, null, "2")
                delay(1000)
                tts?.speak("1", TextToSpeech.QUEUE_ADD, null, "countdown_end")
                // countdown_end bittiğinde UtteranceProgressListener otomatik abandonFocus() yapacak
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
