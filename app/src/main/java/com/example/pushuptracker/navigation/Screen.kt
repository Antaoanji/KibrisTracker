package com.example.pushuptracker.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.pushuptracker.R

sealed class Screen(val route: String, @StringRes val titleRes: Int, val icon: ImageVector) {
    object Home : Screen("home", R.string.home, Icons.Default.Home)
    object Programs : Screen("programs", R.string.programs, Icons.Default.DateRange)
    object Stats : Screen("stats", R.string.statistics, Icons.AutoMirrored.Filled.TrendingUp)
    object Achievements : Screen("achievements", R.string.achievements, Icons.Outlined.EmojiEvents)
    object Profile : Screen("profile", R.string.profile, Icons.Default.Person)
}
