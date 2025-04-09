package com.hebit.app.data.remote.dto

import com.squareup.moshi.JsonClass

/**
 * Data transfer object for habit statistics from the API
 */
@JsonClass(generateAdapter = true)
data class HabitStatsDto(
    val currentStreak: Int,
    val longestStreak: Int,
    val completionRate: Float, // API returns as 0-100 value
    val completedEntries: Int,
    val totalEntries: Int,
    val completionsByDay: List<DayCompletion> = emptyList(),
    val completionsByTime: List<TimeCompletion> = emptyList(),
    val consistency: Float = 0f
)

@JsonClass(generateAdapter = true)
data class DayCompletion(
    val day: String,
    val count: Int
)

@JsonClass(generateAdapter = true)
data class TimeCompletion(
    val name: String,
    val count: Int
) 