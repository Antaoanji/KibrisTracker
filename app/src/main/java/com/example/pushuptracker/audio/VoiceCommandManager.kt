package com.example.pushuptracker.audio

import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.log10
import kotlin.math.sqrt

@Singleton
class VoiceCommandManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioCoach: AudioCoach
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private val handler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(Dispatchers.IO)
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    
    private val recognizerIntent: Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra("android.speech.extra.BEEP_SUPPRESSED", true)
    }

    private val _commandFlow = MutableSharedFlow<String>(replay = 0)
    val commandFlow = _commandFlow.asSharedFlow()

    private var shouldBeListening = false
    private var isRecognizerActive = false
    private var lastEmittedCommand: String? = null
    private var lastEmittedTime = 0L
    
    private var vadJob: Job? = null
    // Eşik değerini 70 dB'e çıkardım (Daha yüksek gürültü direnci için)
    private val VAD_THRESHOLD_DB = 70.0 

    fun startListening() {
        if (shouldBeListening) return
        shouldBeListening = true
        Log.d("VoiceCommand", "Gürültü dirençli akıllı ses takip sistemi aktif.")
        startVoiceActivityDetection()
    }

    private fun startVoiceActivityDetection() {
        vadJob?.cancel()
        vadJob = scope.launch {
            val bufferSize = AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
            val audioRecord = AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, 16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize)
            
            val buffer = ShortArray(bufferSize)
            try {
                audioRecord.startRecording()
            } catch (e: Exception) {
                return@launch
            }
            
            while (shouldBeListening) {
                if (!isRecognizerActive) {
                    val readSize = audioRecord.read(buffer, 0, buffer.size)
                    if (readSize > 0) {
                        val db = calculateDecibels(buffer, readSize)
                        // Sadece yüksek eşik aşıldığında tanımayı başlat
                        if (db > VAD_THRESHOLD_DB) {
                            Log.d("VoiceCommand", "Komut algılama eşiği aşıldı: $db dB")
                            launch(Dispatchers.Main) { 
                                startSpeechRecognition() 
                            }
                            // Tanıma devam ederken 4 saniye VAD'ı sustur
                            delay(4000) 
                        }
                    }
                }
                delay(150) // Örnekleme sıklığını biraz düşürerek CPU tasarrufu sağla
            }
            audioRecord.stop()
            audioRecord.release()
        }
    }

    private fun calculateDecibels(buffer: ShortArray, size: Int): Double {
        var sum = 0.0
        for (i in 0 until size) sum += buffer[i].toDouble() * buffer[i]
        val rms = sqrt(sum / size)
        return if (rms > 0) 20 * log10(rms) else 0.0
    }

    private fun startSpeechRecognition() {
        if (isRecognizerActive) return
        isRecognizerActive = true
        
        handler.post {
            try {
                initRecognizer()
                speechRecognizer?.startListening(recognizerIntent)
            } catch (e: Exception) {
                isRecognizerActive = false
            }
        }
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
        vadJob?.cancel()
        handler.removeCallbacksAndMessages(null)
        handler.post {
            try {
                speechRecognizer?.cancel()
                speechRecognizer?.destroy()
                speechRecognizer = null
                isRecognizerActive = false
            } catch (e: Exception) {}
        }
    }

    private inner class VoiceRecognitionListener : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}

        override fun onError(error: Int) {
            isRecognizerActive = false
            Log.d("VoiceCommand", "Tanıma bitti, VAD moduna dönülüyor.")
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            matches?.firstOrNull()?.let { handleText(it) }
            isRecognizerActive = false
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            matches?.firstOrNull()?.let { handleText(it) }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun handleText(text: String) {
        val lowerText = text.lowercase(Locale.getDefault())
        
        val command = when {
            lowerText.contains("durdur") || lowerText.contains("beklet") || lowerText.contains("duraklat") -> "media_pause"
            lowerText.contains("başlat") || lowerText.contains("oynat") || lowerText.contains("devam et") -> "media_play"
            lowerText.contains("sonraki") || lowerText.contains("geç") -> "media_next"
            lowerText.contains("önceki") || lowerText.contains("geri") -> "media_prev"
            
            lowerText.contains("seti bitir") || lowerText.contains("tamam") || lowerText.contains("bitir") -> "finish"
            lowerText.contains("atla") || lowerText.contains("geç") -> "skip"
            
            lowerText.contains("kolay") -> "easy"
            lowerText.contains("orta") -> "medium"
            lowerText.contains("zor") -> "hard"
            
            else -> null
        }

        if (command != null) {
            val now = System.currentTimeMillis()
            if (command != lastEmittedCommand || (now - lastEmittedTime) > 3000) {
                lastEmittedCommand = command
                lastEmittedTime = now
                Log.d("VoiceCommand", "Komut Onaylandı: $command")
                audioCoach.announceExercise(getFeedbackText(command))
                CoroutineScope(Dispatchers.Main).launch { _commandFlow.emit(command) }
            }
        }
    }

    private fun getFeedbackText(command: String): String {
        return when(command) {
            "media_pause" -> "Durduruldu"
            "media_play" -> "Devam ediyor"
            "media_next" -> "Sıradaki"
            "media_prev" -> "Önceki"
            "finish" -> "Set bitti"
            "skip" -> "Atlandı"
            "easy" -> "Rahat"
            "medium" -> "Orta"
            "hard" -> "Zor"
            else -> "Anlaşıldı"
        }
    }
}
