package com.example.pushuptracker.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.pushuptracker.R

sealed class Screen(val route: String, @field:StringRes val titleRes: Int, val icon: ImageVector?) {
    object Home : Screen("home", R.string.home, Icons.Rounded.Home)
    object Programs : Screen("programs", R.string.programs, Icons.Rounded.DateRange)
    object Stats : Screen("stats", R.string.statistics, Icons.AutoMirrored.Rounded.TrendingUp)
    object Achievements : Screen("achievements", R.string.achievements, Icons.Rounded.EmojiEvents)
    object Profile : Screen("profile", R.string.profile, Icons.Rounded.Person)
    object WorkoutPlayer : Screen("workout_player", R.string.programs, null)
    object WorkoutEditor : Screen("workout_editor/{workoutId}", R.string.programs, null) {
        fun createRoute(workoutId: Long) = "workout_editor/$workoutId"
    }
}
