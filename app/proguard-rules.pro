# TextToSpeech için gerekli kurallar
-keep class android.speech.tts.** { *; }
-keep interface android.speech.tts.** { *; }

# Hilt için standart kurallar
-keep class androidx.hilt.** { *; }
-keep class dagger.hilt.** { *; }

# Gemini AI / Generative AI için kurallar
-keep class com.google.ai.client.generativeai.** { *; }
