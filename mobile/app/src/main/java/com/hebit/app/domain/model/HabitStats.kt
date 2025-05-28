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

data class HabitStats(
    val currentStreak: Int,
    val longestStreak: Int,
    val completionRate: Float,
    val completedEntries: Int, // Domain expects this
    val totalEntries: Int,     // Domain expects this
    val completionsByDay: Map<String, Int>,
    val completionsByTime: Map<String, Int>,
    val consistency: Float,      // Domain expects this
    val lastCompletedDate: LocalDate?
)