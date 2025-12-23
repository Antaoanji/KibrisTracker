package com.example.pushuptracker.di

import com.example.pushuptracker.model.Workout
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutHolder @Inject constructor() {
    var workout: Workout? = null
}
