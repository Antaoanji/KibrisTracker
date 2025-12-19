package com.example.pushuptracker.audio

import android.content.Context
import android.media.SoundPool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SoundPlayer(private val context: Context) {

    private val soundPool: SoundPool = SoundPool.Builder().setMaxStreams(2).build()
    private val loadedSounds = mutableMapOf<Int, Int>()

    suspend fun loadSound(resId: Int) {
        withContext(Dispatchers.IO) {
            val soundId = soundPool.load(context, resId, 1)
            loadedSounds[resId] = soundId
        }
    }

    fun playSound(resId: Int) {
        loadedSounds[resId]?.let {
            soundPool.play(it, 1f, 1f, 0, 0, 1f)
        }
    }

    fun release() {
        soundPool.release()
    }
}
