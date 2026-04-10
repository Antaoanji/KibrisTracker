package com.example.pushuptracker.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.pushuptracker.ui.achievements.AchievementsScreen
import com.example.pushuptracker.ui.active.ActiveWorkoutScreen
import com.example.pushuptracker.ui.home.HomeScreen
import com.example.pushuptracker.ui.profile.ProfileScreen
import com.example.pushuptracker.ui.programs.ProgramsScreen
import com.example.pushuptracker.ui.programs.WorkoutPlayerScreen
import com.example.pushuptracker.ui.programs.editor.WorkoutEditorScreen
import com.example.pushuptracker.ui.stats.StatsScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
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
            WorkoutPlayerScreen(navController = navController)
        }
        composable(
            route = Screen.WorkoutEditor.route,
            arguments = listOf(navArgument("workoutId") { type = NavType.LongType })
        ) {
            WorkoutEditorScreen(navController = navController)
        }
        composable(Screen.ActiveWorkout.route) {
            ActiveWorkoutScreen(navController = navController)
        }
    }
}
