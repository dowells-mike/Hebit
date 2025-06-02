package com.hebit.app.domain.model // Or your appropriate domain model package

import java.time.LocalDate
import java.time.LocalTime // If completionsByTime implies specific times

// Domain model for individual day completion stats (if needed beyond just a map)
data class DailyCompletionStat(
    val day: LocalDate, // Or String if you prefer to keep it as is from DTO
    val count: Int
)

// Domain model for completion counts at different times (if needed beyond just a map)
data class TimedCompletionStat(
    val timeSlotName: String, // e.g., "Morning", "Afternoon", or a LocalTime
    val count: Int
)

// Represents the completion count for a specific day of the week.
data class HabitCompletionByDay(
    val day: String, // e.g., "Sunday", "Monday"
    val count: Int
)

// Represents the completion count for a specific time range in a day.
data class HabitCompletionByTime(
    val name: String, // e.g., "Morning (5am-12pm)"
    val count: Int
)

// Domain model for habit statistics.
// This should map from HabitStatsDto and be used in the UI/ViewModel.
data class HabitStats(
    val completionRate: Float?,
    val currentStreak: Int?,
    val longestStreak: Int?,
    val consistency: Float?, // Percentage
    val totalEntries: Int?,
    val completedEntries: Int?,
    val completionsByDay: List<HabitCompletionByDay> = emptyList(),
    val completionsByTime: List<HabitCompletionByTime> = emptyList()
)