package com.example.pushuptracker.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pushuptracker.ui.achievements.AchievementsScreen
import com.example.pushuptracker.ui.home.HomeScreen
import com.example.pushuptracker.ui.profile.ProfileScreen
import com.example.pushuptracker.ui.programs.ProgramsScreen
import com.example.pushuptracker.ui.programs.WorkoutPlayerScreen
import com.example.pushuptracker.ui.stats.StatsScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen()
        }
        composable(Screen.Programs.route) {
            ProgramsScreen(navController = navController)
        }
        composable(Screen.Stats.route) {
            StatsScreen()
        }
        composable(Screen.Achievements.route) {
            AchievementsScreen()
        }
        composable(Screen.Profile.route) {
            ProfileScreen()
        }
        composable(Screen.WorkoutPlayer.route) {
            // Pass the whole NavController for more flexible navigation
            WorkoutPlayerScreen(navController = navController)
        }
    }
}
