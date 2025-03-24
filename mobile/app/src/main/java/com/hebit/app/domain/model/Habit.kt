package com.hebit.app.domain.model

import java.time.LocalDateTime

data class Habit(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val frequency: String,
    val completedToday: Boolean,
    val streak: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) 