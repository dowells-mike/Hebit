package com.hebit.app.domain.model

import java.time.LocalDate
import java.time.LocalTime

// Using java.time for dates and times. Ensure your project's minSdk supports this
// or has core library desugaring enabled.

enum class HabitFrequencyType {
    DAILY, WEEKLY, MONTHLY, SPECIFIC_DATES, UNKNOWN
}

enum class DayOfWeekDomain { // Renamed to avoid conflict if java.time.DayOfWeek is used directly
    SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, UNKNOWN
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

data class HabitFrequency(
    val type: HabitFrequencyType,
    val daysOfWeek: List<DayOfWeekDomain>? = null,
    val datesOfMonth: List<Int>? = null,
    val timesPerPeriod: Int? = null,
    val specificDates: List<LocalDate>? = null
)

data class HabitTimePreference(
    val preferredTime: LocalTime? = null,
    val flexibilityMinutes: Int? = null
)

data class HabitStreakDetails(
    val current: Int,
    val longest: Int,
    val lastCompleted: LocalDate? = null
)

data class HabitReminderSettings(
    val time: LocalTime? = null,
    val customMessage: String? = null,
    val notificationStyle: ReminderNotificationStyle
)

data class HabitSuccessCriteria(
    val type: SuccessCriteriaType,
    val target: Float? = null,
    val unit: String? = null,
    val minimumThreshold: Float? = null
)

data class HabitCompletionRecord(
    val date: LocalDate,
    val completed: Boolean,
    val value: Float? = null,
    val notes: String? = null,
    val mood: Int? = null,
    val skipReason: String? = null
)

data class Habit(
    val id: String,
    val title: String,
    val description: String?,
    val icon: String?, // Changed from iconName
    val color: String?,
    val goalLink: String?,
    val frequency: HabitFrequency,
    val timePreference: HabitTimePreference?,
    val streakData: HabitStreakDetails?, // Replaced streak: Int and completedToday
    val category: String?,
    val completionHistory: List<HabitCompletionRecord>, // Uses updated HabitCompletionRecord
    val difficulty: HabitDifficulty,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val reminderSettings: HabitReminderSettings?,
    val successCriteria: HabitSuccessCriteria?,
    val createdAt: LocalDate?, // Or LocalDateTime if time precision is needed
    val updatedAt: LocalDate?  // Or LocalDateTime
)