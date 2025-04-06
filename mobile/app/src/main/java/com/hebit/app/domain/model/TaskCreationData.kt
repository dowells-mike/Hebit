package com.hebit.app.domain.model

import java.time.LocalDate
import java.time.LocalTime

data class TaskCreationData(
    val title: String,
    val description: String? = null,
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val category: String? = null,
    val labels: List<String> = emptyList()
)

enum class TaskPriority {
    LOW, MEDIUM, HIGH
} 