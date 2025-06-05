package com.hebit.app.data.mapper

import com.hebit.app.data.remote.dto.AchievementCriteriaDto
import com.hebit.app.data.remote.dto.AchievementDto
import com.hebit.app.data.remote.dto.UserAchievementDto
import com.hebit.app.domain.model.*
import com.hebit.app.util.DateTimeUtil

fun AchievementCriteriaDto.toDomain(): AchievementCriteria {
    return AchievementCriteria(
        type = safeEnumValueOf(type, AchievementCriteriaType.UNKNOWN),
        source = source?.let { safeEnumValueOf(it, AchievementSourceType.UNKNOWN) },
        targetValue = targetValue,
        conditionDetails = conditionDetails
    )
}

fun AchievementDto.toDomain(): Achievement {
    return Achievement(
        id = id,
        name = name,
        description = description,
        category = safeEnumValueOf(category, AchievementCategory.UNKNOWN),
        points = points,
        icon = icon,
        criteria = criteria.toDomain(),
        rarity = safeEnumValueOf(rarity, AchievementRarity.UNKNOWN),
        secret = secret ?: false,
        createdAt = DateTimeUtil.parseIsoString(createdAt) ?: DateTimeUtil.defaultDateTime(),
        updatedAt = DateTimeUtil.parseIsoString(updatedAt) ?: DateTimeUtil.defaultDateTime()
    )
}

fun UserAchievementDto.toDomain(): UserAchievement {
    return UserAchievement(
        id = id,
        userId = userId,
        achievement = achievement.toDomain(),
        progress = progress,
        earned = earned,
        earnedAt = earnedAt?.let { DateTimeUtil.parseIsoString(it) },
        seenByUser = seenByUser ?: false,
        createdAt = DateTimeUtil.parseIsoString(createdAt) ?: DateTimeUtil.defaultDateTime(),
        updatedAt = DateTimeUtil.parseIsoString(updatedAt) ?: DateTimeUtil.defaultDateTime()
    )
}

// Helper function for safe enum parsing
inline fun <reified T : Enum<T>> safeEnumValueOf(value: String, default: T): T {
    return try {
        enumValueOf<T>(value.uppercase())
    } catch (e: IllegalArgumentException) {
        default
    }
} 