package com.hebit.app.ui.screens.habits

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.hebit.app.domain.model.Habit
import com.hebit.app.domain.model.HabitFrequency
import java.time.format.TextStyle
import java.util.Locale

enum class HabitCategoryUI(val title: String, val icon: ImageVector, val color: Color) {
    HEALTH("Health", Icons.Default.Favorite, Color(0xFFE91E63)),
    FITNESS("Fitness", Icons.Default.FitnessCenter, Color(0xFF2196F3)),
    MINDFULNESS("Mindfulness", Icons.Default.SelfImprovement, Color(0xFF9C27B0)),
    PRODUCTIVITY("Productivity", Icons.Default.Schedule, Color(0xFF4CAF50)),
    EDUCATION("Education", Icons.Default.School, Color(0xFFFF9800)),
    CREATIVITY("Creativity", Icons.Default.Palette, Color(0xFF795548)),
    SOCIAL("Social", Icons.Default.People, Color(0xFF3F51B5)),
    OTHER("Other", Icons.Default.Circle, Color.Gray)
}

fun getIconByName(iconName: String?): ImageVector {
    return when (iconName?.lowercase()) {
        null, "", "default_icon", "taskalt" -> Icons.Default.TaskAlt
        "water_drop", "waterdrop" -> Icons.Outlined.WaterDrop
        "book", "menu_book", "menubook" -> Icons.AutoMirrored.Filled.MenuBook
        "meditation", "self_improvement", "selfimprovement" -> Icons.Default.SelfImprovement
        "exercise", "fitness_center", "fitnesscenter" -> Icons.Default.FitnessCenter
        "journal", "edit" -> Icons.Default.Edit
        "favorite" -> Icons.Default.Favorite
        "schedule" -> Icons.Default.Schedule
        "school" -> Icons.Default.School
        "palette" -> Icons.Default.Palette
        "people" -> Icons.Default.People
        "directions_run", "directionsrun" -> Icons.Default.DirectionsRun
        "local_drink", "localdrink" -> Icons.Default.LocalDrink
        else -> Icons.Default.TaskAlt
    }
}

fun getCategoryUI(categoryName: String?): HabitCategoryUI {
    return HabitCategoryUI.values().find { it.title.equals(categoryName, ignoreCase = true) }
        ?: HabitCategoryUI.OTHER
}

fun formatHabitFrequency(habit: Habit): String {
    val freqEnum = habit.frequency
    val config = habit.frequencyConfig

    return when (freqEnum) {
        HabitFrequency.DAILY -> {
            if (config?.daysOfWeek.isNullOrEmpty() || config?.daysOfWeek?.size == 7) {
                "Daily"
            } else {
                val days = config?.daysOfWeek
                    ?.sorted()
                    // Assuming daysOfWeek in domain model is 0(Sun)-6(Sat)
                    // java.time.DayOfWeek uses 1(Mon)-7(Sun).
                    // Mapping 0-6 to 1-7 for DayOfWeek.of()
                    ?.mapNotNull { dayNumber ->
                        try {
                            // Adjust 0 (Sun) to 7 for DayOfWeek.of, and 1-6 remain 1-6
                            val javaDayOfWeek = if (dayNumber == 0) 7 else dayNumber
                            java.time.DayOfWeek.of(javaDayOfWeek).getDisplayName(TextStyle.SHORT, Locale.getDefault())
                        } catch (e: Exception) { null } // Catch invalid day numbers
                    }
                    ?.joinToString(", ")
                if (days.isNullOrBlank()) "Daily" else "Daily: $days"
            }
        }
        HabitFrequency.WEEKLY -> {
            val times = config?.timesPerPeriod ?: 1
            val plural = if (times > 1) "s" else ""
            // If daysOfWeek is relevant for WEEKLY, this needs enhancement. For now, simple.
            if (!config?.daysOfWeek.isNullOrEmpty()) {
                val days = config?.daysOfWeek
                    ?.sorted()
                    ?.mapNotNull { dayNumber ->
                        try {
                            val javaDayOfWeek = if (dayNumber == 0) 7 else dayNumber
                            java.time.DayOfWeek.of(javaDayOfWeek).getDisplayName(TextStyle.SHORT, Locale.getDefault())
                        } catch (e: Exception) { null }
                    }
                    ?.joinToString(", ")
                "$times time$plural a week on: $days"
            } else {
                "$times time$plural a week"
            }
        }
        HabitFrequency.MONTHLY -> {
            val times = config?.timesPerPeriod
            if (times != null && times > 0) {
                val plural = if (times > 1) "s" else ""
                "$times time$plural a month"
            } else if (!config?.datesOfMonth.isNullOrEmpty()) {
                val dates = config?.datesOfMonth?.joinToString(", ") {
                    if (it == -1) "Last Day" else it.toString() // 'it' is already an Int here
                }
                "Monthly on: $dates"
            } else {
                "Monthly"
            }
        }
        HabitFrequency.SPECIFIC_DATES -> {
            if (config?.specificDates?.isNotEmpty() == true) {
                // Potentially format the dates if needed, e.g., count or a summary
                "On ${config.specificDates.size} specific date(s)"
            } else {
                "Custom Schedule (Specific Dates)"
            }
        }
        HabitFrequency.UNKNOWN -> "Custom Schedule" // Handles UNKNOWN explicitly
        // No else needed if all enum cases are covered and the type is non-nullable.
        // If freqEnum could be null (though it shouldn't be based on Habit model), then an else would be required.
    }
} 