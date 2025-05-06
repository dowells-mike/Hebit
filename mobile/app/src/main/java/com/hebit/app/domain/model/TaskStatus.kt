package com.hebit.app.domain.model

enum class TaskStatus(val value: String) {
    TODO("todo"),
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    ARCHIVED("archived")
} 