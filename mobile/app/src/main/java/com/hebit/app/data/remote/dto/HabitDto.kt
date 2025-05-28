package com.hebit.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
// Removed java.time.LocalDateTime as it's not directly used in DTOs; parsing will be handled elsewhere.

// DTO for individual completion history entry
@JsonClass(generateAdapter = true)
data class CompletionHistoryEntryDto(
    val date: String, // Assuming ISO date string
    val completed: Boolean,
    val value: Float? = null,
    val notes: String? = null,
    val mood: Int? = null,
    val skipReason: String? = null
)

@JsonClass(generateAdapter = true)
data class HabitFrequencyConfigDto(
    val daysOfWeek: List<Int>? = null,         // 0-6 for Sunday-Saturday, aligning with common libraries
    val datesOfMonth: List<Int>? = null,       // 1-31 for monthly, -1 for last day
    val timesPerPeriod: Int? = null,       // How many times in period (e.g., 3 times a week)
    val specificDates: List<String>? = null // ISO date strings for specific irregular dates
)

@JsonClass(generateAdapter = true)
data class TimePreferenceDto(
    val preferredTime: String? = null, // e.g., "10:00" in HH:mm format
    val flexibility: Int? = null      // Flexibility in minutes before/after preferredTime
)

@JsonClass(generateAdapter = true)
data class StreakDataDto(
    val current: Int,
    val longest: Int,
    val lastCompleted: String? = null // ISO date string of last completion
)

@JsonClass(generateAdapter = true)
data class ReminderSettingsDto(
    val time: String? = null, // e.g., "09:00" in HH:mm format
    val customMessage: String? = null,
    val notificationStyle: String? = null // e.g., 'basic', 'motivational' (align with backend enum if any)
)

@JsonClass(generateAdapter = true)
data class SuccessCriteriaDto(
    val type: String? = null, // e.g., 'boolean', 'numeric', 'timer' (align with backend enum)
    val target: Float? = null, // Target value for numeric/timer types
    val unit: String? = null, // e.g., 'times', 'minutes', 'pages'
    val minimumThreshold: Float? = null // Minimum value to count as completion for numeric types
)

@JsonClass(generateAdapter = true)
data class HabitDto(
    @Json(name = "_id") val id: String,
    val title: String,
    val description: String? = null, // Made optional
    @Json(name = "icon") val icon: String? = null, // Aligned with backend, was icon_name
    val color: String? = null, // Added
    val goalLink: String? = null, // Added (e.g., an ID of a linked Goal)
    val frequency: String, // e.g., "daily", "weekly", "monthly", "specific_dates"
    val frequencyConfig: HabitFrequencyConfigDto? = null, // Added
    val timePreference: TimePreferenceDto? = null, // Added
    val streakData: StreakDataDto? = null, // Added (replaces old streak: Int)
    val category: String? = null, // Added (e.g., an ID or name of a category)
    val completionHistory: List<CompletionHistoryEntryDto> = emptyList(),
    val difficulty: String? = null, // Added (e.g., 'easy', 'medium', 'hard')
    val startDate: String? = null, // Added (ISO date string)
    val endDate: String? = null, // Added (ISO date string)
    val reminderSettings: ReminderSettingsDto? = null, // Added
    val successCriteria: SuccessCriteriaDto? = null, // Added
    // `completedToday` field removed. This should be derived on the client-side (ViewModel/Domain layer)
    // based on `completionHistory` or `streakData.lastCompleted` and current date.
    @Json(name = "createdAt") val createdAt: String? = null, // Keep as String (ISO date)
    @Json(name = "updatedAt") val updatedAt: String? = null  // Keep as String (ISO date)
)

@JsonClass(generateAdapter = true)
data class HabitListResponse(
    val habits: List<HabitDto>,
    // These fields are often part of paginated responses. Adjust if backend sends different metadata.
    val total: Int? = null,
    val page: Int? = null,
    @Json(name = "per_page") val perPage: Int? = null,
    val message: String? = null // Optional: For any general messages from the API (e.g., errors not tied to HTTP status)
)

@JsonClass(generateAdapter = true)
data class CreateHabitRequest( // DTO for creating a new habit. Align fields with HabitDto where applicable.
    val title: String,
    val description: String? = null,
    val icon: String? = null,
    val color: String? = null,
    val goalLink: String? = null,
    val frequency: String,
    val frequencyConfig: HabitFrequencyConfigDto? = null,
    val timePreference: TimePreferenceDto? = null,
    val category: String? = null,
    val difficulty: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val reminderSettings: ReminderSettingsDto? = null,
    val successCriteria: SuccessCriteriaDto? = null
)

@JsonClass(generateAdapter = true)
data class UpdateHabitRequest( // DTO for updating an existing habit. All fields are optional.
    val title: String? = null,
    val description: String? = null,
    val icon: String? = null,
    val color: String? = null,
    val goalLink: String? = null,
    val frequency: String? = null,
    val frequencyConfig: HabitFrequencyConfigDto? = null,
    val timePreference: TimePreferenceDto? = null,
    val category: String? = null,
    val difficulty: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val reminderSettings: ReminderSettingsDto? = null,
    val successCriteria: SuccessCriteriaDto? = null
)

/* Commenting out HabitCompletionRequest as the current backend /track endpoint for habits does not take a body.
   If the backend API changes for habit tracking (e.g. to allow back-dating or adding notes upon completion),
   this DTO might need to be reinstated and adjusted.
@JsonClass(generateAdapter = true)
data class HabitCompletionRequest(
    val completed: Boolean,
    val date: String, // ISO Date string for which completion is recorded
    val notes: String? = null
)
*/