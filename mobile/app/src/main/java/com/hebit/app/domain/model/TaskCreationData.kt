package com.hebit.app.domain.model

// Ensure RecurrencePattern is not imported if it's removed from fields
// import com.hebit.app.ui.screens.tasks.RecurrencePattern 
// import com.hebit.app.ui.screens.tasks.ReminderSettings // REMOVE THIS IMPORT
import com.hebit.app.domain.model.Reminder // ADD THIS IMPORT
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
    // recurrencePattern: RecurrencePattern, // This line is now removed
    // val reminderSettings: ReminderSettings, // REMOVE THIS LINE
    val reminders: List<Reminder>? = null, // ADD THIS LINE

    // New fields for iCalendar RRULE
    val rruleString: String? = null,
    val recurrenceStartDate: LocalDate? = null // This will be the DTSTART
)

enum class TaskPriority {
    LOW, MEDIUM, HIGH
} 