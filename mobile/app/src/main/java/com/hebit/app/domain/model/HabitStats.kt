package com.hebit.app.domain.model

/**
 * Statistics related to a specific habit
 */
data class HabitStats(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val completionRate: Float = 0f, // 0.0 to 1.0
    val totalCompletions: Int = 0
    // Additional fields can be added as needed
) 