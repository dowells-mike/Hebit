package com.hebit.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecurrenceRuleDto(
    val frequency: String? = null,               // Possible values: "DAILY", "WEEKLY", "MONTHLY", "YEARLY"
    val interval: Int? = null,                    // Number of units between occurrences
    @Json(name = "end_date") val endDate: String? = null,            // ISO date, for example "2025-12-31"
    @Json(name = "rrule_string") val rruleString: String? = null,     // Full RRULE string when provided
    @Json(name = "dt_start") val dtStart: String? = null,             // ISO date-time for the start of recurrence
    @Json(name = "ex_dates") val exDates: List<String>? = null        // ISO date-time strings for excluded dates
)

@JsonClass(generateAdapter = true)
data class TaskDto(
    @Json(name = "_id") val _id: String,
    val title: String,
    val description: String,
    val category: String,
    @Json(name = "dueDate") val dueDate: String?,
    val priority: String,                         // "low", "medium", or "high"
    val progress: Int? = 0,
    val completed: Boolean,
    val createdAt: String,                        // ISO date-time when task was created
    val updatedAt: String,                        // ISO date-time when task was last updated
    val metadata: Map<String, Any>? = null,       // Additional data if needed

    @Json(name = "recurrenceRule") val recurrenceRule: String? = null,        // RRULE string for recurrence
    @Json(name = "recurrenceStartDate") val recurrenceStartDate: String? = null,   // ISO date-time for when recurrence begins
    @Json(name = "recurrenceExceptions") val recurrenceExceptions: List<String>? = null, // ISO date-time strings for skipped occurrences
    val reminders: List<ReminderDto>? = null,     // List of reminders associated with the task
    @Json(name = "upcomingOccurrences") val upcomingOccurrences: List<String>? = null // ISO date-time strings for next occurrences
)

@JsonClass(generateAdapter = true)
data class TaskListResponse(
    val tasks: List<TaskDto>,
    val total: Int = 0,                           // Total number of tasks in the current query
    val page: Int = 1,                            // Current page index
    @Json(name = "per_page") val perPage: Int = 20 // Number of tasks per page
)

@JsonClass(generateAdapter = true)
data class CreateTaskRequest(
    val title: String,
    val description: String,
    val category: String?,
    @Json(name = "due_date") val dueDate: String?, // ISO date for task deadline
    val priority: String,                         // "low", "medium", or "high"
    val metadata: Map<String, Any>? = null,       // Optional extra fields

    @Json(name = "recurrenceRule") val recurrenceRuleRequest: String? = null,       // RRULE string for recurrence
    @Json(name = "recurrenceStartDate") val recurrenceStartDateRequest: String? = null, // ISO date-time for start of recurrence
    @Json(name = "recurrenceExceptions") val recurrenceExceptionsRequest: List<String>? = null, // ISO date-time strings to skip
    val remindersRequest: List<ReminderDto>? = null // List of reminder data
)

@JsonClass(generateAdapter = true)
data class UpdateTaskRequest(
    val title: String? = null,
    val description: String? = null,
    val category: String?,
    @Json(name = "due_date") val dueDate: String? = null, // ISO date for updated deadline
    val priority: String? = null,                    // "low", "medium", or "high"
    val progress: Int? = null,                       // Progress percentage
    @Json(name = "completed") val isCompleted: Boolean? = null,
    val metadata: Map<String, Any>? = null,          // Optional extra fields

    @Json(name = "recurrenceRule") val recurrenceRuleRequest: String? = null,       // New RRULE string if changing recurrence
    @Json(name = "recurrenceStartDate") val recurrenceStartDateRequest: String? = null, // New ISO date-time for recurrence start
    @Json(name = "recurrenceExceptions") val recurrenceExceptionsRequest: List<String>? = null, // New ISO date-time strings to skip
    val remindersRequest: List<ReminderDto>? = null   // Updated list of reminders
)
