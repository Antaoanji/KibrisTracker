package com.example.pushuptracker.ui.programs.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pushuptracker.data.local.CustomWorkoutDao
import com.example.pushuptracker.room.CustomExerciseEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutEditorViewModel @Inject constructor(
    private val customWorkoutDao: CustomWorkoutDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val workoutId: Long = checkNotNull(savedStateHandle["workoutId"])

    private val _exercises = MutableStateFlow<List<CustomExerciseEntity>>(emptyList())
    val exercises = _exercises.asStateFlow()

    init {
        loadExercises()
    }

    private fun loadExercises() {
        viewModelScope.launch {
            customWorkoutDao.getExercisesForWorkout(workoutId).collect {
                _exercises.value = it
            }
        }
    }

    fun updateExercise(exercise: CustomExerciseEntity) {
        viewModelScope.launch {
            customWorkoutDao.updateExercise(exercise)
        }
    }

    fun deleteExercise(exercise: CustomExerciseEntity) {
        viewModelScope.launch {
            customWorkoutDao.deleteExercise(exercise)
        }
    }

    fun addExercise() {
        viewModelScope.launch {
            val currentList = _exercises.value
            val newOrder = if (currentList.isEmpty()) 0 else currentList.last().orderIndex + 1
            val newExercise = CustomExerciseEntity(
                workoutId = workoutId,
                name = "Yeni Egzersiz",
                searchKey = "custom-exercise",
                sets = 3,
                reps = "12",
                restTimeSeconds = 60,
                description = "Açıklama girin",
                orderIndex = newOrder
            )
            customWorkoutDao.insertExercises(listOf(newExercise))
        }
    }

    fun moveExercise(from: Int, to: Int) {
        // Sıralama mantığı (Gelişmiş drag-drop için temel)
        viewModelScope.launch {
            val list = _exercises.value.toMutableList()
            val item = list.removeAt(from)
            list.add(to, item)
            
            // Veritabanında orderIndex'leri güncelle
            val updatedList = list.mapIndexed { index, entity ->
                entity.copy(orderIndex = index)
            }
            customWorkoutDao.insertExercises(updatedList)
        }
    }
}
