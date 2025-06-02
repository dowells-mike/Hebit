package com.hebit.app.ui.screens.habits

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun HabitDetailScreen(
    habitId: String, // Still needed for navigation arguments
    onNavigateBack: () -> Unit, // For back navigation
    onNavigateToEditHabit: (String) -> Unit, // To navigate to edit screen
    onNavigateToStreakScreen: (String) -> Unit // To navigate to streak screen
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Habit Detail Screen - Not implemented yet. Habit ID: $habitId")
    }
}

   