package com.hebit.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


// DTO for individual completion history entry
@JsonClass(generateAdapter = true)
data class CompletionHistoryEntryDto(
    val date: String, // ISO date string
    val completed: Boolean,
    val value: Float? = null,
    val notes: String? = null,
    val mood: Int? = null,
    val skipReason: String? = null
)

@JsonClass(generateAdapter = true)
data class HabitFrequencyConfigDto(
    @Json(name = "daysOfWeek") val daysOfWeek: List<Int>? = null, // 0 (Sun) - 6 (Sat)
    @Json(name = "datesOfMonth") val datesOfMonth: List<Int>? = null, // 1-31, or -1 for last day
    @Json(name = "timesPerPeriod") val timesPerPeriod: Int? = null,
    @Json(name = "specificDates") val specificDates: List<String>? = null // ISO Date strings
)

@JsonClass(generateAdapter = true)
data class TimePreferenceDto(
    val preferredTime: String? = null, // "10:00" in HH:mm format
    val flexibility: Int? = null      // Flexibility in minutes before/after preferredTime
)

@JsonClass(generateAdapter = true)
data class HabitStreakDataDto(
    @Json(name = "current") val current: Int?,
    @Json(name = "longest") val longest: Int?,
    @Json(name = "lastCompleted") val lastCompleted: String? = null // ISO Date string
)

@JsonClass(generateAdapter = true)
data class HabitCompletionHistoryEntryDto(
    @Json(name = "date") val date: String, // ISO Date string
    @Json(name = "completed") val completed: Boolean,
    @Json(name = "notes") val notes: String? = null,
    @Json(name = "skipReason") val skipReason: String? = null,
    @Json(name = "value") val value: Int? = null, // For measurable habits
    @Json(name = "mood") val mood: Int? = null // 1-5 mood rating
)

@JsonClass(generateAdapter = true)
data class ReminderSettingsDto(
    @Json(name = "time") val time: String? = null,
    @Json(name = "customMessage") val customMessage: String? = null
)

@JsonClass(generateAdapter = true)
data class SuccessCriteriaDto(
    @Json(name = "type") val type: String? = null, // 'boolean', 'numeric', 'timer'
    @Json(name = "target") val target: Int? = null
)

@JsonClass(generateAdapter = true)
data class HabitMetadataDto(
    @Json(name = "successRate") val successRate: Float? = null
)

@JsonClass(generateAdapter = true)
data class HabitDto(
    @Json(name = "_id") val id: String,
    @Json(name = "user") val userId: String,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String? = null,
    @Json(name = "icon") val icon: String? = null,
    @Json(name = "color") val color: String? = null,
    @Json(name = "frequency") val frequency: String, // 'daily', 'weekly', 'monthly', 'specific_dates'
    @Json(name = "frequencyConfig") val frequencyConfig: HabitFrequencyConfigDto? = null,
    @Json(name = "streakData") val streakData: HabitStreakDataDto? = null,
    @Json(name = "category") val category: String? = null,
    @Json(name = "completionHistory") val completionHistory: List<HabitCompletionHistoryEntryDto>? = emptyList(),
    @Json(name = "status") val status: String? = "active", // 'active', 'archived'
    @Json(name = "difficulty") val difficulty: String? = null, // 'easy', 'medium', 'hard'
    @Json(name = "impact") val impact: Int? = null, // 1-5 scale
    @Json(name = "startDate") val startDate: String? = null, // ISO Date string
    @Json(name = "endDate") val endDate: String? = null, // ISO Date string
    @Json(name = "reminderSettings") val reminderSettings: ReminderSettingsDto? = null,
    @Json(name = "successCriteria") val successCriteria: SuccessCriteriaDto? = null,
    @Json(name = "metadata") val metadata: HabitMetadataDto? = null,
    @Json(name = "createdAt") val createdAt: String? = null, // ISO Date string
    @Json(name = "updatedAt") val updatedAt: String? = null, // ISO Date string
    @Json(name = "completed_today") val completedToday: Boolean? = null
)

// HabitListResponse, CreateHabitRequest, and UpdateHabitRequest have been moved to separate files
//to avoid redeclaration conflicts and improve code organization
