package com.hebit.app.domain.model

import java.time.LocalDate

data class Goal(
    val id: String,
    val title: String,
    val description: String,
    val progress: Int,
    val targetDate: LocalDate,
    val category: String,
    val isCompleted: Boolean,
    val createdAt: LocalDate,
    val updatedAt: LocalDate
) 