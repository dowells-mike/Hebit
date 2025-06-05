package com.hebit.app.domain.model


import com.hebit.app.domain.model.Reminder
import com.hebit.app.ui.screens.tasks.SubTask
import java.time.LocalDate
import java.time.LocalTime

data class TaskCreationData(
    val title: String,
    val description: String?,
    val dueDate: LocalDate?,
    val dueTime: LocalTime?,
    val priority: TaskPriority,
    val category: String?,
    val labels: List<String>, 
    val subtasks: List<SubTask>,
    val reminders: List<Reminder>? = null,

    // New fields for iCalendar RRULE
    val rruleString: String? = null,
    val recurrenceStartDate: LocalDate? = null // This will be the DTSTART
)

enum class TaskPriority {
    LOW, MEDIUM, HIGH
} 