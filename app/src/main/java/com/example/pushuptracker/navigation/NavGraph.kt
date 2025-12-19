package com.example.pushuptracker.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.pushuptracker.ui.achievements.AchievementsScreen
import com.example.pushuptracker.ui.home.HomeScreen
import com.example.pushuptracker.ui.profile.ProfileScreen
import com.example.pushuptracker.ui.programs.ProgramDetailScreen
import com.example.pushuptracker.ui.programs.ProgramsScreen
import com.example.pushuptracker.ui.stats.StatsScreen

object Routes {
    const val PROGRAM_DETAIL = "program_detail/{programId}"

    fun programDetail(programId: String) = "program_detail/$programId"
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen()
        }
        composable(route = Screen.Programs.route) {
            ProgramsScreen(navController = navController)
        }
        composable(route = Screen.Stats.route) {
            StatsScreen()
        }
        composable(route = Screen.Achievements.route) {
            AchievementsScreen()
        }
        composable(route = Screen.Profile.route) {
            ProfileScreen()
        }
        composable(
            route = Routes.PROGRAM_DETAIL,
            arguments = listOf(navArgument("programId") { type = NavType.StringType })
        ) {
            backStackEntry ->
            val programId = backStackEntry.arguments?.getString("programId")
            if (programId != null) {
                ProgramDetailScreen(programId = programId, navController = navController)
            }
        }
    }
}
