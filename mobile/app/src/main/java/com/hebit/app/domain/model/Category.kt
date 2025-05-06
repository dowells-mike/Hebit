package com.hebit.app.domain.model

/**
 * Represents a Task Category (used like a List in Reminders/Tasks apps).
 */
data class Category(
    val id: String,
    val name: String,
    val color: String, // Hex color code (e.g., "#FF5733") or predefined name
    val icon: String? = null // Optional: Name or resource ID of an icon
) 