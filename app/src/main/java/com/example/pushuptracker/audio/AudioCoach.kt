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
            val result = tts?.setLanguage(Locale("tr", "TR"))
            
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("AudioCoach", "Türkçe dil desteği bulunamadı veya desteklenmiyor!")
                isInitialized = false
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    tts?.setAudioAttributes(audioAttributes)
                }

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        Log.d("AudioCoach", "Konuşma başladı: $utteranceId")
                    }

                    override fun onDone(utteranceId: String?) {
                        Log.d("AudioCoach", "Konuşma bitti: $utteranceId")
                        // Sadece son sayı veya normal duyuru bittiğinde odağı bırak
                        if (utteranceId?.startsWith("announcement") == true || utteranceId == "countdown_1") {
                            abandonFocus()
                        }
                    }

                    override fun onError(utteranceId: String?) {
                        Log.e("AudioCoach", "Konuşma hatası: $utteranceId")
                        abandonFocus()
                    }
                })
                isInitialized = true
                Log.d("AudioCoach", "TTS başarıyla başlatıldı ve Türkçe ayarlandı.")
            }
        } else {
            Log.e("AudioCoach", "TTS başlatılamadı! Durum kodu: $status")
        }
    }

    private fun requestFocus(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // GAIN_TRANSIENT_MAY_DUCK: Müziği durdurmaz, sadece sesini kısar (Ducking). 
            // Bu sayede Spotify/YouTube kesilmeden arka planda çalmaya devam eder.
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(audioAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener { focusChange ->
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                        tts?.stop()
                    }
                }
                .build()

            audioManager.requestAudioFocus(audioFocusRequest!!) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
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
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "announcement_${System.currentTimeMillis()}")
        }
    }

    suspend fun playCountdown() {
        if (isInitialized) {
            if (requestFocus()) {
                // Her sayı için QUEUE_FLUSH kullanarak önceki sayının (eğer gecikme varsa) kesilmesini sağlıyoruz
                // Ama odağı sadece son sayıda (1) bırakacağız
                tts?.speak("3", TextToSpeech.QUEUE_FLUSH, null, "countdown_3")
                delay(1000)
                tts?.speak("2", TextToSpeech.QUEUE_FLUSH, null, "countdown_2")
                delay(1000)
                tts?.speak("1", TextToSpeech.QUEUE_FLUSH, null, "countdown_1")
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
