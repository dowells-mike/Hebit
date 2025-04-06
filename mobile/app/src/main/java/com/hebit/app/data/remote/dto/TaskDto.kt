package com.hebit.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

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
    val updatedAt: String
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
    val category: String,
    @Json(name = "due_date") val dueDate: String?,
    val priority: String // "low", "medium", "high" in backend
)

@JsonClass(generateAdapter = true)
data class UpdateTaskRequest(
    val title: String? = null,
    val description: String? = null,
    val category: String? = null,
    @Json(name = "due_date") val dueDate: String? = null,
    val priority: String? = null,
    val progress: Int? = null,
    @Json(name = "completed") val isCompleted: Boolean? = null
) 