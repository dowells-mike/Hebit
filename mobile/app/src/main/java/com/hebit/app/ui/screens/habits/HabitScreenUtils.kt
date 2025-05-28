package com.hebit.app.ui.screens.habits

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.hebit.app.domain.model.DayOfWeekDomain
import com.hebit.app.domain.model.HabitFrequency
import com.hebit.app.domain.model.HabitFrequencyType

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

fun formatHabitFrequency(frequency: HabitFrequency?): String {
    if (frequency == null) return "Not set"
    return when (frequency.type) {
        HabitFrequencyType.DAILY -> "Daily"
        HabitFrequencyType.WEEKLY -> {
            val days = frequency.daysOfWeek?.joinToString(", ") { day ->
                when (day) {
                    DayOfWeekDomain.SUNDAY -> "Sun"
                    DayOfWeekDomain.MONDAY -> "Mon"
                    DayOfWeekDomain.TUESDAY -> "Tue"
                    DayOfWeekDomain.WEDNESDAY -> "Wed"
                    DayOfWeekDomain.THURSDAY -> "Thu"
                    DayOfWeekDomain.FRIDAY -> "Fri"
                    DayOfWeekDomain.SATURDAY -> "Sat"
                    DayOfWeekDomain.UNKNOWN -> ""
                }
            }?.trim()?.removeSuffix(",") ?: "days"

            if (days.isBlank() && frequency.timesPerPeriod == null) return "Weekly"
            if (days.isBlank() && frequency.timesPerPeriod != null) return "Weekly (${frequency.timesPerPeriod}x)"

            if (frequency.timesPerPeriod != null && frequency.timesPerPeriod > 0) {
                "${frequency.timesPerPeriod}x a week on $days"
            } else {
                "Weekly on $days"
            }
        }
        HabitFrequencyType.MONTHLY -> {
            val times = frequency.timesPerPeriod
            val dates = frequency.datesOfMonth?.joinToString(", ")

            if (times != null && times > 0) {
                if (dates?.isNotBlank() == true) "Monthly ($times x on $dates)" else "Monthly ($times x)"
            } else if (dates?.isNotBlank() == true) {
                "Monthly on day(s): $dates"
            } else {
                "Monthly"
            }
        }
        HabitFrequencyType.SPECIFIC_DATES -> {
            if (frequency.specificDates?.isNotEmpty() == true) {
                "On specific dates"
            } else {
                "Custom Schedule"
            }
        }
        HabitFrequencyType.UNKNOWN -> "Custom Schedule"
    }
} 