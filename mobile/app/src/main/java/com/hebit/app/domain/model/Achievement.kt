package com.hebit.app.domain.model

import java.time.LocalDateTime

data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val category: AchievementCategory,
    val points: Int,
    val icon: String, // Consider mapping to a drawable resource or a Composable Icon
    val criteria: AchievementCriteria,
    val rarity: AchievementRarity,
    val secret: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class AchievementCriteria(
    val type: AchievementCriteriaType,
    val source: AchievementSourceType?,
    val targetValue: Any, // String or Number (Int/Double). Consider specific types or a sealed class.
    val conditionDetails: Map<String, Any>?
)

enum class AchievementCategory {
    TASKS, HABITS, GOALS, SPECIAL, UNKNOWN
}

enum class AchievementRarity {
    COMMON, RARE, EPIC, LEGENDARY, UNKNOWN
}

enum class AchievementCriteriaType {
    COUNT, STREAK, COMPLETION_TIME, MULTI_CONDITION, EVENT_BASED, COMPLEX, UNKNOWN
}

enum class AchievementSourceType {
    TASKS, HABITS, GOALS, USER_ACTIVITY, APP_USAGE, UNKNOWN
} 