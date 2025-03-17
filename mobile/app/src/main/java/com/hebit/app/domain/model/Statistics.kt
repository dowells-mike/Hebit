package com.hebit.app.domain.model

import java.time.LocalDate

/**
 * Data model for user activity statistics
 */
data class Statistics(
    val userId: String,
    val taskStats: TaskStats = TaskStats(),
    val habitStats: HabitStats = HabitStats(),
    val goalStats: GoalStats = GoalStats(),
    val categoryDistribution: Map<String, Float> = emptyMap(),
    val timeAnalysis: TimeAnalysis = TimeAnalysis(),
    val streakRecords: List<StreakRecord> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Statistics related to tasks
 */
data class TaskStats(
    val total: Int = 0,
    val completed: Int = 0,
    val overdue: Int = 0,
    val dailyAverage: Float = 0f,
    val successRate: Float = 0f,  // Percentage of tasks completed on time
    val weeklyTrend: List<DailyCount> = emptyList(),
    val monthlyTrend: List<DailyCount> = emptyList()
)

/**
 * Statistics related to habits
 */
data class HabitStats(
    val total: Int = 0,
    val active: Int = 0,
    val paused: Int = 0,
    val longestStreak: Int = 0,
    val currentStreaks: Int = 0,
    val completionRate: Float = 0f,  // Percentage of habit check-ins completed
    val weeklyTrend: List<DailyCount> = emptyList(),
    val monthlyTrend: List<DailyCount> = emptyList()
)

/**
 * Statistics related to goals
 */
data class GoalStats(
    val total: Int = 0,
    val completed: Int = 0,
    val inProgress: Int = 0,
    val avgCompletionTime: Int = 0,  // Average days to complete a goal
    val completionRate: Float = 0f   // Percentage of goals completed
)

/**
 * Record of a streak for a habit
 */
data class StreakRecord(
    val id: String,
    val habitId: String,
    val habitName: String,
    val days: Int,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val isActive: Boolean = true
)

/**
 * Analysis of time spent on different activities
 */
data class TimeAnalysis(
    val mostProductiveDay: String = "",  // e.g., "Monday"
    val mostProductiveHour: Int = 0,     // e.g., 14 (2 PM)
    val weekdayVsWeekend: Pair<Float, Float> = Pair(0f, 0f), // Percentage of tasks done on weekdays vs weekends
    val morningVsEvening: Pair<Float, Float> = Pair(0f, 0f)  // Percentage of tasks done in morning vs evening
)

/**
 * Count for a specific day, used in trends
 */
data class DailyCount(
    val date: LocalDate,
    val count: Int
)

/**
 * Time period for filtering statistics
 */
enum class StatPeriod {
    DAY,
    WEEK,
    MONTH,
    QUARTER,
    YEAR,
    ALL_TIME
}
