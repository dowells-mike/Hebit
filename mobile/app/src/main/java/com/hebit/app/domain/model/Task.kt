package com.hebit.app.domain.model

import java.time.LocalDateTime

data class Task(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val dueDateTime: LocalDateTime?,
    val priority: Int,
    val progress: Int,
    val isCompleted: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) 