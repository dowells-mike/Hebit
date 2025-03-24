package com.hebit.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TaskDto(
    @Json(name = "_id") val id: String,
    val title: String,
    val description: String,
    val category: String,
    @Json(name = "due_date") val dueDate: String?,
    val priority: Int,
    val progress: Int,
    @Json(name = "is_completed") val isCompleted: Boolean,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class TaskListResponse(
    val tasks: List<TaskDto>,
    val total: Int,
    val page: Int,
    @Json(name = "per_page") val perPage: Int
)

@JsonClass(generateAdapter = true)
data class CreateTaskRequest(
    val title: String,
    val description: String,
    val category: String,
    @Json(name = "due_date") val dueDate: String?,
    val priority: Int
)

@JsonClass(generateAdapter = true)
data class UpdateTaskRequest(
    val title: String? = null,
    val description: String? = null,
    val category: String? = null,
    @Json(name = "due_date") val dueDate: String? = null,
    val priority: Int? = null,
    val progress: Int? = null,
    @Json(name = "is_completed") val isCompleted: Boolean? = null
) 