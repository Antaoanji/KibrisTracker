package com.example.pushuptracker.ui.programs

import androidx.lifecycle.ViewModel
import com.example.pushuptracker.model.ExerciseProgram
import com.example.pushuptracker.model.ProgramsRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProgramsViewModel : ViewModel() {

    private val _programs = MutableStateFlow<List<ExerciseProgram>>(emptyList())
    val programs: StateFlow<List<ExerciseProgram>> = _programs.asStateFlow()

    init {
        loadPrograms()
    }

    private fun loadPrograms() {
        // For now, we just load the static data from the repository.
        // In the future, this could come from a database or a network call.
        _programs.value = ProgramsRepo.programs
    }
}
