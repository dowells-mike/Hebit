package com.hebit.app.domain.model

import java.time.LocalDateTime

data class Task(
    val id: String,
    val title: String,
    val description: String,
    val category: String?,
    val dueDateTime: LocalDateTime?,
    val priority: Int, // 1 = low, 2 = medium, 3 = high
    val progress: Int,
    val isCompleted: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val metadata: Map<String, Any> = emptyMap(), // For storing subtasks, or other non-core data

    // New fields for recurrence and reminders
    val recurrenceRuleString: String? = null, // Stores the full RRULE string
    val recurrenceStartDate: LocalDateTime? = null, // DTSTART
    val recurrenceExceptions: List<LocalDateTime>? = emptyList(), // EXDATEs
    val reminders: List<Reminder> = emptyList()
) 