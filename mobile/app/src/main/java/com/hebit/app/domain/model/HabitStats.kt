package com.hebit.app.domain.model

import com.hebit.app.data.remote.dto.DayCompletion
import com.hebit.app.data.remote.dto.TimeCompletion

/**
 * Statistics related to a specific habit
 */
data class HabitStats(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val completionRate: Float = 0f, // 0.0 to 1.0
    val totalCompletions: Int = 0,
    val completionsByDay: List<DayCompletion> = emptyList(),
    val completionsByTime: List<TimeCompletion> = emptyList(),
    val consistency: Float = 0f
    // Additional fields can be added as needed
) 