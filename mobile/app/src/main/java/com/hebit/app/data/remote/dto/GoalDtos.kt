package com.hebit.app.data.remote.dto

/**
 * Data Transfer Objects for goal requests and responses
 */

data class GoalRequest(
    val title: String,
    val description: String? = null,
    val targetDate: String? = null, // ISO format date
    val status: String = "not_started" // not_started, in_progress, completed
)

data class GoalProgressRequest(
    val progress: Int // 0-100
)

data class GoalResponse(
    val id: String,
    val userId: String,
    val title: String,
    val description: String? = null,
    val targetDate: String? = null,
    val progress: Int = 0,
    val status: String = "not_started", // not_started, in_progress, completed
    val relatedTasks: List<String>? = null,
    val relatedHabits: List<String>? = null,
    val createdAt: String,
    val updatedAt: String
)
