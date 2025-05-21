package com.hebit.app.domain.model

import com.hebit.app.data.remote.dto.RecurrenceRuleDto
import java.time.LocalDateTime

data class Task(
    val id: String,
    val title: String,
    val description: String,
    val category: String?,
    val dueDateTime: LocalDateTime?,
    val priority: Int, // 1 = low, 2 = medium, 3 = high (corresponding to backend's 'low', 'medium', 'high')
    val progress: Int,
    val isCompleted: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val recurrenceRule: RecurrenceRuleDto? = null,
    val metadata: Map<String, Any> = emptyMap() // For storing subtasks, recurrence, reminders, etc.
) 