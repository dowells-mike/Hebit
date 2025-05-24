package com.hebit.app.ui.screens.tasks

import com.hebit.app.domain.model.RecurrenceType

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
 * Handles both nullable and non-nullable strings
 * Format: "id:title:isCompleted,id:title:isCompleted,..."
 */
fun parseSubtasks(subtasksString: String?): List<SubTask> {
    if (subtasksString.isNullOrBlank()) {
        return emptyList()
    }
    return subtasksString.split(',').mapNotNull { subtaskPart ->
        val parts = subtaskPart.split(':', limit = 3)
        if (parts.size == 3) {
            SubTask(
                id = parts[0],
                title = parts[1],
                isCompleted = parts[2].toBooleanStrictOrNull() ?: false
            )
        } else {
            // Optionally log a warning for malformed subtask parts
            // Log.w("TaskUtils", "Malformed subtask part: $subtaskPart")
            null
        }
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

data class SubtaskProgress(val completed: Int, val total: Int)

/**
 * Calculates the number of completed and total subtasks from metadata.
 *
 * @param subtasksMetadata The subtasks data, expected to be a String (e.g., from task.metadata["subtasks"]).
 * @return SubtaskProgress containing the counts. Returns (0, 0) if no subtasks or invalid format.
 */
fun getSubtaskProgressCounts(subtasksMetadata: Any?): SubtaskProgress {
    if (subtasksMetadata !is String || subtasksMetadata.isBlank()) {
        return SubtaskProgress(0, 0)
    }
    val subtasksList = parseSubtasks(subtasksMetadata)
    val total = subtasksList.size
    val completed = subtasksList.count { it.isCompleted }
    return SubtaskProgress(completed, total)
}

// TODO: Add other utility functions like parseRecurrencePattern, parseReminderSettings if they are generic enough
// For now, parseSubtasks is included here for completeness of getSubtaskProgressCounts.
// If parseSubtasks is already defined elsewhere (e.g. TaskDetailScreen), it could be moved or referenced. 