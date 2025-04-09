package com.hebit.app.domain.model

import java.time.LocalDateTime

/**
 * Domain model representing a user note associated with a habit.
 */
data class Note(
    val id: String,
    val habitId: String,
    val content: String,
    val createdAt: LocalDateTime,
    // val updatedAt: LocalDateTime? = null // Optional: if notes can be edited
) 