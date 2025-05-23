package com.hebit.app.domain.model

import java.time.LocalDateTime

enum class ReminderType {
    RELATIVE,
    ABSOLUTE
}

data class Reminder(
    val type: ReminderType,
    val offsetMinutes: Int? = null,      // For relative reminders (e.g., -15 for 15 mins before)
    val absoluteDateTime: LocalDateTime? = null // For absolute reminders
) 