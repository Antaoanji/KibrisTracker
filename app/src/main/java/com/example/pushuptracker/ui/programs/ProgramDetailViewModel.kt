package com.example.pushuptracker.ui.programs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pushuptracker.model.ExerciseProgram
import com.example.pushuptracker.model.ProgramsRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProgramDetailViewModel(programId: String) : ViewModel() {

    private val _program = MutableStateFlow<ExerciseProgram?>(null)
    val program: StateFlow<ExerciseProgram?> = _program.asStateFlow()

    init {
        // In a real app, you might fetch this from a database or network based on the ID.
        _program.value = ProgramsRepo.programs.find { it.id == programId }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val programId: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProgramDetailViewModel(programId) as T
        }
    }
}