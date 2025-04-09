package com.hebit.app.domain.model

import java.time.LocalDateTime

// Domain model for individual completion history entry
data class CompletionHistoryEntry(
    val date: LocalDateTime,
    val completed: Boolean,
    val value: Float? = null,
    val notes: String? = null,
    val mood: Int? = null,
    val skipReason: String? = null
)

data class Habit(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String? = "default_icon",
    val frequency: String,
    val completedToday: Boolean = false,
    val streak: Int,
    val completionHistory: List<CompletionHistoryEntry> = emptyList(),
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) 