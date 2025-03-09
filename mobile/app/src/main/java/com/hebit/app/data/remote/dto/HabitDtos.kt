package com.hebit.app.data.remote.dto

/**
 * Data Transfer Objects for habit requests and responses
 */

data class HabitRequest(
    val title: String,
    val description: String? = null,
    val frequency: String, // daily, weekly, monthly
    val timeOfDay: String? = null,
    val daysOfWeek: List<Int>? = null // 0-6, Sunday to Saturday
)

data class HabitTrackingRequest(
    val completed: Boolean,
    val date: String // ISO format date
)

data class HabitResponse(
    val id: String,
    val userId: String,
    val title: String,
    val description: String? = null,
    val frequency: String,
    val timeOfDay: String? = null,
    val daysOfWeek: List<Int>? = null,
    val streak: Int = 0,
    val completionHistory: List<HabitCompletionEntry> = emptyList(),
    val createdAt: String,
    val updatedAt: String
)

data class HabitCompletionEntry(
    val date: String, // ISO format date
    val completed: Boolean
)
