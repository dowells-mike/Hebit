package com.hebit.app.domain.model

import java.time.LocalDate

enum class RecurrenceType {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

data class RecurrencePattern(
    val type: RecurrenceType = RecurrenceType.NONE,
    val interval: Int = 1,
    val endDate: LocalDate? = null,
    val daysOfWeek: List<Int> = emptyList() // For weekly recurrence
) 