package com.example.pushuptracker.ui.active

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pushuptracker.audio.WorkoutService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActiveWorkoutViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _workoutService = MutableStateFlow<WorkoutService?>(null)
    val workoutService: StateFlow<WorkoutService?> = _workoutService.asStateFlow()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as WorkoutService.WorkoutBinder
            _workoutService.value = binder.getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            _workoutService.value = null
        }
    }

    init {
        val intent = Intent(context, WorkoutService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onCleared() {
        super.onCleared()
        try {
            context.unbindService(serviceConnection)
        } catch (e: Exception) {
            // Service might already be unbound
        }
    }

    fun pause() = sendAction("PAUSE")
    fun resume() = sendAction("RESUME")
    fun stop() = sendAction("STOP")
    fun cancel() = sendAction("CANCEL")
    fun skipLymphatic() = sendAction("SKIP_LYMPHATIC")

    private fun sendAction(action: String) {
        val intent = Intent(context, WorkoutService::class.java).apply {
            this.action = action
        }
        context.startService(intent)
    }
}
