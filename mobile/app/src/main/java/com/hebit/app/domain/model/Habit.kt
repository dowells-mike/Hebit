package com.hebit.app.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

// Using java.time for dates and times. Ensure your project's minSdk supports this
// or has core library desugaring enabled.

enum class HabitFrequency {
    DAILY, WEEKLY, MONTHLY, SPECIFIC_DATES, UNKNOWN
}

enum class HabitStatus {
    ACTIVE, ARCHIVED, UNKNOWN
}

enum class HabitDifficulty {
    EASY, MEDIUM, HARD, UNKNOWN
}

enum class ReminderNotificationStyle {
    BASIC, MOTIVATIONAL, UNKNOWN
}

enum class SuccessCriteriaType {
    BOOLEAN, NUMERIC, TIMER, UNKNOWN
}

data class HabitFrequencyConfig(
    val daysOfWeek: List<Int>? = null, // 0 (Sun) - 6 (Sat)
    val datesOfMonth: List<Int>? = null, // 1-31, or -1 for last day
    val timesPerPeriod: Int? = null,
    val specificDates: List<LocalDate>? = null
)

data class HabitStreakData(
    val current: Int?,
    val longest: Int?,
    val lastCompleted: LocalDateTime? = null
)

data class HabitCompletionHistoryEntry(
    val date: LocalDateTime,
    val completed: Boolean,
    val notes: String? = null,
    val skipReason: String? = null,
    val value: Int? = null, // For measurable habits
    val mood: Int? = null // 1-5 mood rating
)

data class ReminderSettings(
    val time: String? = null, // Consider LocalTime if appropriate
    val customMessage: String? = null
)

data class SuccessCriteria(
    val type: SuccessCriteriaType?,
    val target: Int? = null
)

data class HabitMetadata(
    val successRate: Float? = null
)

data class Habit(
    val id: String,
    val userId: String,
    val title: String,
    val description: String? = null,
    val icon: String? = null,
    val color: String? = null,
    val frequency: HabitFrequency,
    val frequencyConfig: HabitFrequencyConfig? = null,
    val streakData: HabitStreakData? = null,
    val category: String? = null, // Could be a Category domain model if it exists
    val completionHistory: List<HabitCompletionHistoryEntry> = emptyList(),
    val status: HabitStatus = HabitStatus.ACTIVE,
    val difficulty: HabitDifficulty? = null,
    val impact: Int? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val reminderSettings: ReminderSettings? = null,
    val successCriteria: SuccessCriteria? = null,
    val metadata: HabitMetadata? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
    val completedToday: Boolean? = null // Derived information
)