package com.hebit.app.domain.model

import java.time.LocalDate


// Domain model for individual day completion stats
data class DailyCompletionStat(
    val day: LocalDate,
    val count: Int
)

// Domain model for completion counts at different times
data class TimedCompletionStat(
    val timeSlotName: String,
    val count: Int
)

// Represents the completion count for a specific day of the week.
data class HabitCompletionByDay(
    val day: String,
    val count: Int
)

// Represents the completion count for a specific time range in a day.
data class HabitCompletionByTime(
    val name: String,
    val count: Int
)

// Domain model for habit statistics.
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