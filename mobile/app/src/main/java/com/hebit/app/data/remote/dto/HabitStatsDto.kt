package com.hebit.app.data.remote.dto

/**
 * Data transfer object for habit statistics from the API
 */
data class HabitStatsDto(
    val currentStreak: Int,
    val longestStreak: Int,
    val completionRate: Float, // API returns as 0-100 value
    val totalCompletions: Int,
    val completionsByDay: Map<String, Int> = emptyMap(),
    val completionsByTime: Map<String, Int> = emptyMap(),
    val consistency: Float = 0f
) 