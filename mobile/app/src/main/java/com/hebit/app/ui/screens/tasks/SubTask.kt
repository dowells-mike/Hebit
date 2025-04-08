package com.hebit.app.ui.screens.tasks

import java.util.UUID

/**
 * Represents a subtask of a main task
 */
data class SubTask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val isCompleted: Boolean = false
) 