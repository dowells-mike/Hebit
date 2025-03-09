package com.hebit.app.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hebit.app.ui.screens.auth.ForgotPasswordScreen
import com.hebit.app.ui.screens.auth.LoginScreen
import com.hebit.app.ui.screens.auth.RegistrationScreen
import com.hebit.app.ui.screens.auth.SplashScreen
import com.hebit.app.ui.screens.dashboard.DashboardScreen
import com.hebit.app.ui.screens.habits.HabitListScreen
import com.hebit.app.ui.screens.tasks.TaskListScreen
import com.hebit.app.ui.screens.goals.GoalListScreen
import com.hebit.app.ui.screens.settings.SettingsScreen

/**
 * Main navigation routes for the app
 */
object Routes {
    // Auth flow
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    
    // Main app screens
    const val DASHBOARD = "dashboard"
    const val TASKS = "tasks"
    const val HABITS = "habits"
    const val GOALS = "goals"
    const val SETTINGS = "settings"
}

/**
 * Main navigation component for the app
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HebitNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.SPLASH
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth flow
        composable(Routes.SPLASH) {
            SplashScreen(
                onSplashComplete = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        // Clear back stack when logging in
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                    }
                },
                onRegisterClick = { navController.navigate(Routes.REGISTER) },
                onForgotPasswordClick = { navController.navigate(Routes.FORGOT_PASSWORD) }
            )
        }
        
        composable(Routes.REGISTER) {
            RegistrationScreen(
                onNavigateBack = { navController.navigateUp() },
                onRegistrationSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.navigateUp() },
                onReturnToLogin = { navController.navigate(Routes.LOGIN) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }}
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
