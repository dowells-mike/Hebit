package com.hebit.app.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.hebit.app.ui.screens.auth.ForgotPasswordScreen
import com.hebit.app.ui.screens.auth.LoginScreen
import com.hebit.app.ui.screens.auth.RegistrationScreen
import com.hebit.app.ui.screens.auth.SplashScreen
import com.hebit.app.ui.screens.dashboard.DashboardScreen
import com.hebit.app.ui.screens.dashboard.ProgressStatsScreen
import com.hebit.app.ui.screens.dashboard.QuickActionsScreen
import com.hebit.app.ui.screens.habits.HabitListScreen
import com.hebit.app.ui.screens.habits.HabitDetailScreen
import com.hebit.app.ui.screens.habits.HabitStreakScreen
import com.hebit.app.ui.screens.tasks.TaskListScreen
import com.hebit.app.ui.screens.tasks.TaskDetailScreen
import com.hebit.app.ui.screens.tasks.TaskCategoriesScreen
import com.hebit.app.ui.screens.tasks.TaskBoardScreen
import com.hebit.app.ui.screens.goals.GoalListScreen
import com.hebit.app.ui.screens.goals.GoalDetailScreen
import com.hebit.app.ui.screens.settings.SettingsScreen
import com.hebit.app.ui.screens.profile.ProfileScreen
import com.hebit.app.ui.screens.profile.AchievementScreen
import com.hebit.app.ui.screens.profile.StatisticsScreen

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
    const val QUICK_ACTIONS = "quick_actions"
    const val PROGRESS_STATS = "progress_stats"
    const val TASKS = "tasks"
    const val TASK_DETAIL = "task_detail"
    const val TASK_CATEGORIES = "task_categories"
    const val TASK_BOARD = "task_board"
    const val HABITS = "habits"
    const val HABIT_DETAIL = "habit_detail"
    const val HABIT_STREAK = "habit_streak"
    const val GOALS = "goals"
    const val GOAL_DETAIL = "goal_detail"
    
    // Profile and Settings
    const val PROFILE = "profile"
    const val PROFILE_EDIT = "profile_edit"
    const val PROFILE_ACHIEVEMENTS = "profile_achievements"
    const val PROFILE_STATISTICS = "profile_statistics"
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
                onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                onQuickActionsClick = { navController.navigate(Routes.QUICK_ACTIONS) },
                onProgressStatsClick = { navController.navigate(Routes.PROGRESS_STATS) }
            )
        }
        
        // Task Screens
        composable(Routes.TASKS) {
            TaskListScreen(
                onNavigateBack = { navController.navigateUp() },
                onHomeClick = { navController.navigate(Routes.DASHBOARD) {
                    popUpTo(Routes.DASHBOARD) { inclusive = true }
                }},
                onHabitsClick = { navController.navigate(Routes.HABITS) },
                onGoalsClick = { navController.navigate(Routes.GOALS) },
                onProfileClick = { navController.navigate(Routes.PROFILE) },
                onTaskCategoriesClick = { navController.navigate(Routes.TASK_CATEGORIES) },
                onTaskBoardClick = { navController.navigate(Routes.TASK_BOARD) },
                onTaskClick = { taskId -> 
                    navController.navigate("${Routes.TASK_DETAIL}/$taskId")
                }
            )
        }
        
        composable(
            route = "${Routes.TASK_DETAIL}/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            TaskDetailScreen(
                taskId = taskId,
                onNavigateBack = { navController.navigateUp() },
                onHomeClick = { navController.navigate(Routes.DASHBOARD) {
                    popUpTo(Routes.DASHBOARD) { inclusive = true }
                }},
                onTasksClick = { navController.navigate(Routes.TASKS) },
                onHabitsClick = { navController.navigate(Routes.HABITS) },
                onGoalsClick = { navController.navigate(Routes.GOALS) },
                onProfileClick = { navController.navigate(Routes.PROFILE) }
            )
        }
        
        composable(Routes.TASK_CATEGORIES) {
            TaskCategoriesScreen(
                onNavigateBack = { navController.navigateUp() },
                onCategorySelect = { /* Navigate to filtered tasks */ },
                onHomeClick = { navController.navigate(Routes.DASHBOARD) {
                    popUpTo(Routes.DASHBOARD) { inclusive = true }
                }},
                onTasksClick = { navController.navigate(Routes.TASKS) },
                onHabitsClick = { navController.navigate(Routes.HABITS) },
                onGoalsClick = { navController.navigate(Routes.GOALS) },
                onProfileClick = { navController.navigate(Routes.PROFILE) }
            )
        }
        
        composable(Routes.TASK_BOARD) {
            TaskBoardScreen(
                onNavigateBack = { navController.navigateUp() },
                onTaskClick = { taskId -> 
                    navController.navigate("${Routes.TASK_DETAIL}/$taskId")
                },
                onHomeClick = { navController.navigate(Routes.DASHBOARD) {
                    popUpTo(Routes.DASHBOARD) { inclusive = true }
                }},
                onTasksClick = { navController.navigate(Routes.TASKS) },
                onHabitsClick = { navController.navigate(Routes.HABITS) },
                onGoalsClick = { navController.navigate(Routes.GOALS) },
                onProfileClick = { navController.navigate(Routes.PROFILE) }
            )
        }
        
        // Habit Screens
        composable(Routes.HABITS) {
            HabitListScreen(
                onNavigateBack = { navController.navigateUp() },
                onHomeClick = { navController.navigate(Routes.DASHBOARD) {
                    popUpTo(Routes.DASHBOARD) { inclusive = true }
                }},
                onTasksClick = { navController.navigate(Routes.TASKS) },
                onGoalsClick = { navController.navigate(Routes.GOALS) },
                onProfileClick = { navController.navigate(Routes.PROFILE) },
                onHabitClick = { habitId ->
                    navController.navigate("${Routes.HABIT_DETAIL}/$habitId")
                }
            )
        }
        
        composable(
            route = "${Routes.HABIT_DETAIL}/{habitId}",
            arguments = listOf(navArgument("habitId") { type = NavType.StringType })
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId") ?: ""
            HabitDetailScreen(
                habitId = habitId,
                onNavigateBack = { navController.navigateUp() },
                onHomeClick = { navController.navigate(Routes.DASHBOARD) {
                    popUpTo(Routes.DASHBOARD) { inclusive = true }
                }},
                onTasksClick = { navController.navigate(Routes.TASKS) },
                onHabitsClick = { navController.navigate(Routes.HABITS) },
                onGoalsClick = { navController.navigate(Routes.GOALS) },
                onProfileClick = { navController.navigate(Routes.PROFILE) },
                onStreakClick = { 
                    navController.navigate("${Routes.HABIT_STREAK}/$habitId")
                }
            )
        }
        
        composable(
            route = "${Routes.HABIT_STREAK}/{habitId}",
            arguments = listOf(navArgument("habitId") { type = NavType.StringType })
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId") ?: ""
            HabitStreakScreen(
                habitId = habitId,
                onNavigateBack = { navController.navigateUp() },
                onHomeClick = { navController.navigate(Routes.DASHBOARD) {
                    popUpTo(Routes.DASHBOARD) { inclusive = true }
                }},
                onTasksClick = { navController.navigate(Routes.TASKS) },
                onHabitsClick = { navController.navigate(Routes.HABITS) },
                onGoalsClick = { navController.navigate(Routes.GOALS) },
                onProfileClick = { navController.navigate(Routes.PROFILE) }
            )
        }

        // Goal Screens
        composable(Routes.GOALS) {
            GoalListScreen(
                onNavigateBack = { navController.navigateUp() },
                onGoalClick = { goalId ->
                    navController.navigate("${Routes.GOAL_DETAIL}/$goalId")
                },
                onHomeClick = { navController.navigate(Routes.DASHBOARD) {
                    popUpTo(Routes.DASHBOARD) { inclusive = true }
                }},
                onTasksClick = { navController.navigate(Routes.TASKS) },
                onHabitsClick = { navController.navigate(Routes.HABITS) },
                onProfileClick = { navController.navigate(Routes.PROFILE) }
            )
        }
        
        composable(
            route = "${Routes.GOAL_DETAIL}/{goalId}",
            arguments = listOf(navArgument("goalId") { type = NavType.StringType })
        ) { backStackEntry ->
            val goalId = backStackEntry.arguments?.getString("goalId") ?: ""
            GoalDetailScreen(
                goalId = goalId,
                onNavigateBack = { navController.navigateUp() },
                onHomeClick = { navController.navigate(Routes.DASHBOARD) {
                    popUpTo(Routes.DASHBOARD) { inclusive = true }
                }},
                onTasksClick = { navController.navigate(Routes.TASKS) },
                onHabitsClick = { navController.navigate(Routes.HABITS) },
                onGoalsClick = { navController.navigate(Routes.GOALS) },
                onProfileClick = { navController.navigate(Routes.PROFILE) },
                onShareClick = { /* Share functionality */ }
            )
        }

        composable(Routes.QUICK_ACTIONS) {
            QuickActionsScreen(
                onNavigateBack = { navController.navigateUp() },
                onHomeClick = { 
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                    }
                },
                onTasksClick = { navController.navigate(Routes.TASKS) },
                onHabitsClick = { navController.navigate(Routes.HABITS) },
                onGoalsClick = { navController.navigate(Routes.GOALS) },
                onProfileClick = { navController.navigate(Routes.PROFILE) }
            )
        }
        
        composable(Routes.PROGRESS_STATS) {
            ProgressStatsScreen(
                onNavigateBack = { navController.navigateUp() },
                onHomeClick = { 
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                    }
                },
                onTasksClick = { navController.navigate(Routes.TASKS) },
                onHabitsClick = { navController.navigate(Routes.HABITS) },
                onGoalsClick = { navController.navigate(Routes.GOALS) },
                onProfileClick = { navController.navigate(Routes.PROFILE) }
            )
        }
        
        // Profile and Settings Screens
        composable(Routes.PROFILE) {
            ProfileScreen(
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateToAchievements = { navController.navigate(Routes.PROFILE_ACHIEVEMENTS) },
                onNavigateToStatistics = { navController.navigate(Routes.PROFILE_STATISTICS) },
                onNavigateToEditProfile = { navController.navigate(Routes.PROFILE_EDIT) },
                onHomeClick = { navController.navigate(Routes.DASHBOARD) {
                    popUpTo(Routes.DASHBOARD) { inclusive = true }
                }},
                onTasksClick = { navController.navigate(Routes.TASKS) },
                onHabitsClick = { navController.navigate(Routes.HABITS) },
                onGoalsClick = { navController.navigate(Routes.GOALS) }
            )
        }
        
        composable(Routes.PROFILE_ACHIEVEMENTS) {
            AchievementScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }
        
        composable(Routes.PROFILE_STATISTICS) {
            StatisticsScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }
        
        composable(Routes.PROFILE_EDIT) {
            // Will be implemented later
            ProfileScreen(
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateToAchievements = { navController.navigate(Routes.PROFILE_ACHIEVEMENTS) },
                onNavigateToStatistics = { navController.navigate(Routes.PROFILE_STATISTICS) },
                onNavigateToEditProfile = { navController.navigateUp() },
                onHomeClick = { navController.navigate(Routes.DASHBOARD) {
                    popUpTo(Routes.DASHBOARD) { inclusive = true }
                }},
                onTasksClick = { navController.navigate(Routes.TASKS) },
                onHabitsClick = { navController.navigate(Routes.HABITS) },
                onGoalsClick = { navController.navigate(Routes.GOALS) }
            )
        }
        
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}
