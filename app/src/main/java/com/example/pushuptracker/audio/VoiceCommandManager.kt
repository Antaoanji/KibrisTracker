package com.example.pushuptracker.audio

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceCommandManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private val handler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(Dispatchers.Main)
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    
    private val recognizerIntent: Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 60000L)
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 60000L)
        putExtra("android.speech.extra.BEEP_SUPPRESSED", true)
    }

    private val _commandFlow = MutableSharedFlow<String>(replay = 0)
    val commandFlow = _commandFlow.asSharedFlow()

    private var shouldBeListening = false
    private var lastEmittedCommand: String? = null
    private var lastEmittedTime = 0L

    fun startListening() {
        if (shouldBeListening) return
        shouldBeListening = true
        Log.d("VoiceCommand", "Antrenman dinleme döngüsü aktif.")
        startListeningInternal()
    }

    private fun startListeningInternal() {
        if (!shouldBeListening) return
        handler.post {
            try {
                muteSystemSound()
                initRecognizer()
                speechRecognizer?.startListening(recognizerIntent)
                handler.postDelayed({ unmuteSystemSound() }, 400)
            } catch (e: Exception) {
                unmuteSystemSound()
                restartAfterDelay(1000)
            }
        }
    }

    private fun muteSystemSound() {
        audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_MUTE, 0)
    }

    private fun unmuteSystemSound() {
        audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_UNMUTE, 0)
    }

    private fun initRecognizer() {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(VoiceRecognitionListener())
            }
        }
    }

    fun stopListening() {
        shouldBeListening = false
        handler.removeCallbacksAndMessages(null)
        handler.post {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
            speechRecognizer = null
            unmuteSystemSound()
        }
    }

    private fun restartAfterDelay(delayMillis: Long = 50) {
        if (!shouldBeListening) return
        handler.postDelayed({ startListeningInternal() }, delayMillis)
    }

    private inner class VoiceRecognitionListener : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onError(error: Int) {
            if (shouldBeListening) restartAfterDelay(50)
        }
        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            matches?.firstOrNull()?.let { handleText(it, isFinal = true) }
            restartAfterDelay(50)
        }
        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            matches?.firstOrNull()?.let { handleText(it, isFinal = false) }
        }
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun handleText(text: String, isFinal: Boolean) {
        val lowerText = text.lowercase(Locale.getDefault())
        
        val command = when {
            // Antrenman Komutları
            lowerText.contains("kolay zorluk") -> "easy"
            lowerText.contains("orta zorluk") -> "medium"
            lowerText.contains("zor zorluk") -> "hard"
            lowerText.contains("dinlenmeyi atla") || lowerText.contains("dinlenmeyi geç") -> "skip"
            lowerText.contains("seti bitir") || lowerText.contains("antrenmanı bitir") -> "finish"

            // Medya Sarma Komutları
            lowerText.contains("dakika ileri sar") -> {
                val mins = extractNumberBefore(lowerText, "dakika ileri sar")
                "media_forward_$mins"
            }
            lowerText.contains("dakika geri sar") -> {
                val mins = extractNumberBefore(lowerText, "dakika geri sar")
                "media_backward_$mins"
            }

            // Medya Temel Komutlar
            lowerText.contains("medyayı durdur") || lowerText.contains("medyayı beklet") -> "media_pause"
            lowerText.contains("medyayı başlat") || lowerText.contains("medyayı oynat") || lowerText.contains("medyaya devam et") -> "media_play"
            lowerText.contains("bir sonraki medya") -> "media_next"
            lowerText.contains("bir önceki medya") -> "media_prev"
            
            else -> null
        }

        if (command != null) {
            val now = System.currentTimeMillis()
            if (command != lastEmittedCommand || (now - lastEmittedTime) > 2000) {
                lastEmittedCommand = command
                lastEmittedTime = now
                scope.launch { _commandFlow.emit(command) }
                if (!isFinal) {
                    handler.post {
                        speechRecognizer?.cancel()
                        startListeningInternal()
                    }
                }
            }
        }
    }

    private fun extractNumberBefore(text: String, anchor: String): Int {
        val index = text.indexOf(anchor)
        if (index <= 0) return 1
        val wordsBefore = text.substring(0, index).trim().split(" ")
        val numberWord = wordsBefore.lastOrNull() ?: return 1
        
        return when (numberWord) {
            "bir", "1" -> 1
            "iki", "2" -> 2
            "üç", "3" -> 3
            "dört", "4" -> 4
            "beş", "5" -> 5
            "on", "10" -> 10
            else -> numberWord.filter { it.isDigit() }.toIntOrNull() ?: 1
        }
    }
}
