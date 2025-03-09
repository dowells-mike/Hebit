package com.hebit.app.data.remote.dto

/**
 * Data Transfer Objects for task requests and responses
 */

data class TaskRequest(
    val title: String,
    val description: String? = null,
    val priority: String = "medium", // low, medium, high
    val dueDate: String? = null,
    val category: String? = null,
    val tags: List<String>? = null
)

data class TaskCompletionRequest(
    val completed: Boolean
)

data class TaskResponse(
    val id: String,
    val userId: String,
    val title: String,
    val description: String? = null,
    val completed: Boolean = false,
    val priority: String = "medium", // low, medium, high
    val dueDate: String? = null,
    val category: String? = null,
    val tags: List<String>? = null,
    val createdAt: String,
    val updatedAt: String
)
