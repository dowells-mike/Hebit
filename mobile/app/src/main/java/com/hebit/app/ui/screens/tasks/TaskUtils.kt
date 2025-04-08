package com.hebit.app.ui.screens.tasks

/**
 * Utility functions for task-related operations
 * These were previously duplicated across multiple files
 */

/**
 * Parse recurrence pattern from metadata string
 */
fun parseRecurrencePattern(recurrenceStr: String): RecurrenceType? {
    val parts = recurrenceStr.split(",")
    if (parts.isNotEmpty()) {
        return try {
            RecurrenceType.valueOf(parts[0])
        } catch (e: Exception) {
            null
        }
    }
    return null
}

/**
 * Parse subtasks from metadata string
 */
fun parseSubtasks(subtasksStr: String): List<SubTask> {
    if (subtasksStr.isBlank()) return emptyList()
    
    return subtasksStr.split(",").mapNotNull { subTaskStr ->
        val parts = subTaskStr.split(":")
        if (parts.size >= 3) {
            SubTask(
                id = parts[0],
                title = parts[1],
                isCompleted = parts[2].toBoolean()
            )
        } else null
    }
}

/**
 * Parse reminder settings from metadata string
 */
fun parseReminderSettings(reminderStr: String): String {
    val parts = reminderStr.split(",")
    if (parts.isNotEmpty()) {
        val minutes = parts[0].toIntOrNull() ?: 0
        return if (parts.size > 1 && parts[1].isNotBlank()) {
            "At ${parts[1]}"
        } else {
            "$minutes minutes before"
        }
    }
    return "Reminder set"
} 