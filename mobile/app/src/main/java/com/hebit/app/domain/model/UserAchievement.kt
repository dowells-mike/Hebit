package com.hebit.app.domain.model

import java.time.LocalDateTime

data class UserAchievement(
    val id: String, // ID of the UserAchievement record itself
    val userId: String,
    val achievement: Achievement, // Embed the full Achievement object
    val progress: Double,
    val earned: Boolean,
    val earnedAt: LocalDateTime?,
    val seenByUser: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) 