package com.hebit.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecurrenceRuleDto(
    val frequency: String? = null, // e.g., "DAILY", "WEEKLY", "MONTHLY", "YEARLY"
    val interval: Int? = null,
    @Json(name = "end_date") val endDate: String? = null, // ISO date string e.g., "2025-12-31"
    // Potentially add other RRULE components if parsed on mobile, or expect a full RRULE string
    @Json(name = "rrule_string") val rruleString: String? = null, // To hold the full RRULE string
    @Json(name = "dt_start") val dtStart: String? = null, // ISO DateTime string for recurrence start
    @Json(name = "ex_dates") val exDates: List<String>? = null // List of ISO DateTime strings for exception dates
)

@JsonClass(generateAdapter = true)
data class TaskDto(
    @Json(name = "_id") val _id: String,
    val title: String,
    val description: String,
    val category: String,
    @Json(name = "dueDate") val dueDate: String?,
    val priority: String, // "low", "medium", "high" from backend
    val progress: Int? = 0,
    val completed: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val metadata: Map<String, Any>? = null, // Keeping metadata for other potential uses

    // New fields for recurrence and reminders
    @Json(name = "recurrenceRule") val recurrenceRule: String? = null, // RRULE string
    @Json(name = "recurrenceStartDate") val recurrenceStartDate: String? = null, // ISO DateTime string
    @Json(name = "recurrenceExceptions") val recurrenceExceptions: List<String>? = null, // List of ISO DateTime strings
    val reminders: List<ReminderDto>? = null
)

@JsonClass(generateAdapter = true)
data class TaskListResponse(
    val tasks: List<TaskDto>,
    val total: Int = 0,
    val page: Int = 1,
    @Json(name = "per_page") val perPage: Int = 20
)

@JsonClass(generateAdapter = true)
data class CreateTaskRequest(
    val title: String,
    val description: String,
    val category: String?,
    @Json(name = "due_date") val dueDate: String?,
    val priority: String, // "low", "medium", "high" in backend
    val metadata: Map<String, Any>? = null,

    // New fields for recurrence and reminders in CreateTaskRequest
    @Json(name = "recurrenceRule") val recurrenceRuleRequest: String? = null,
    @Json(name = "recurrenceStartDate") val recurrenceStartDateRequest: String? = null,
    @Json(name = "recurrenceExceptions") val recurrenceExceptionsRequest: List<String>? = null,
    val remindersRequest: List<ReminderDto>? = null
)

@JsonClass(generateAdapter = true)
data class UpdateTaskRequest(
    val title: String? = null,
    val description: String? = null,
    val category: String?,
    @Json(name = "due_date") val dueDate: String? = null,
    val priority: String? = null,
    val progress: Int? = null,
    @Json(name = "completed") val isCompleted: Boolean? = null,
    val metadata: Map<String, Any>? = null, // For other metadata if any

    // New fields for recurrence and reminders in UpdateTaskRequest
    @Json(name = "recurrenceRule") val recurrenceRuleRequest: String? = null,
    @Json(name = "recurrenceStartDate") val recurrenceStartDateRequest: String? = null,
    @Json(name = "recurrenceExceptions") val recurrenceExceptionsRequest: List<String>? = null,
    val remindersRequest: List<ReminderDto>? = null
) 