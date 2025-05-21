package com.hebit.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecurrenceRuleDto(
    val frequency: String? = null, // e.g., "DAILY", "WEEKLY", "MONTHLY", "YEARLY"
    val interval: Int? = null,
    @Json(name = "end_date") val endDate: String? = null // ISO date string e.g., "2025-12-31"
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
    val recurrence: RecurrenceRuleDto? = null, // Replaced metadata
    val metadata: Map<String, Any>? = null // Keeping metadata for other potential uses, but recurrence is now separate
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
    val recurrence: RecurrenceRuleDto? = null,
    val metadata: Map<String, Any>? = null
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
    val recurrence: RecurrenceRuleDto? = null, // Replaced metadata for recurrence
    val metadata: Map<String, Any>? = null // For other metadata if any
) 