package com.hebit.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hebit.app.ui.screens.auth.LoginScreen
import com.hebit.app.ui.screens.dashboard.DashboardScreen
import com.hebit.app.ui.screens.habits.HabitListScreen
import com.hebit.app.ui.screens.tasks.TaskListScreen
import com.hebit.app.ui.screens.goals.GoalListScreen
import com.hebit.app.ui.screens.settings.SettingsScreen

/**
 * Main navigation routes for the app
 */
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DASHBOARD = "dashboard"
    const val TASKS = "tasks"
    const val HABITS = "habits"
    const val GOALS = "goals"
    const val SETTINGS = "settings"
}

/**
 * Main navigation component for the app
 */
@Composable
fun HebitNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.LOGIN
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth flow
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { navController.navigate(Routes.DASHBOARD) },
                onRegisterClick = { navController.navigate(Routes.REGISTER) }
            )
        }
        
        // Main app screens
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onTasksClick = { navController.navigate(Routes.TASKS) },
                onHabitsClick = { navController.navigate(Routes.HABITS) },
                onGoalsClick = { navController.navigate(Routes.GOALS) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) }
            )
        }
        
        composable(Routes.TASKS) {
            TaskListScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }
        
        composable(Routes.HABITS) {
            HabitListScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }
        
        composable(Routes.GOALS) {
            GoalListScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }
        
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}
