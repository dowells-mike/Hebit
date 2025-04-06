package com.hebit.app.domain.model

import java.time.LocalDate
import java.time.LocalTime
import com.hebit.app.ui.screens.tasks.SubTask
import com.hebit.app.ui.screens.tasks.RecurrencePattern
import com.hebit.app.ui.screens.tasks.ReminderSettings

data class TaskCreationData(
    val title: String,
    val description: String? = null,
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val category: String? = null,
    val labels: List<String> = emptyList(),
    val subtasks: List<SubTask> = emptyList(),
    val recurrencePattern: RecurrencePattern? = null,
    val reminderSettings: ReminderSettings? = null
)

enum class TaskPriority {
    LOW, MEDIUM, HIGH
} 