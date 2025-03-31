package com.hebit.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AchievementDto(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val rarity: String,
    val points: Int,
    val iconUrl: String?,
    val earned: Boolean,
    @Json(name = "earned_at") val earnedAt: String?,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class AchievementListResponse(
    val achievements: List<AchievementDto>,
    val pagination: PaginationDto
)

@JsonClass(generateAdapter = true)
data class PaginationDto(
    val page: Int,
    @Json(name = "per_page") val perPage: Int,
    val total: Int,
    @Json(name = "total_pages") val totalPages: Int
)

@JsonClass(generateAdapter = true)
data class AchievementProgressResponse(
    val achievements: List<AchievementProgressDto>
)

@JsonClass(generateAdapter = true)
data class AchievementProgressDto(
    val id: String,
    val name: String,
    val category: String,
    val progress: Float, // 0.0 to 1.0
    val threshold: Int,
    val current: Int
)

@JsonClass(generateAdapter = true)
data class NewlyEarnedAchievementsResponse(
    @Json(name = "earned_achievements") val earnedAchievements: List<AchievementDto>
)

@JsonClass(generateAdapter = true)
data class UserAchievementResponse(
    @Json(name = "user_achievements") val userAchievements: List<UserAchievementDto>,
    val stats: UserAchievementStatsDto
)

@JsonClass(generateAdapter = true)
data class UserAchievementDto(
    val id: String,
    @Json(name = "achievement_id") val achievementId: String,
    @Json(name = "user_id") val userId: String,
    @Json(name = "earned_at") val earnedAt: String,
    val achievement: AchievementDto
)

@JsonClass(generateAdapter = true)
data class UserAchievementStatsDto(
    val total: Int,
    @Json(name = "total_earned") val totalEarned: Int,
    @Json(name = "percentage_complete") val percentageComplete: Float,
    @Json(name = "total_points") val totalPoints: Int,
    @Json(name = "points_earned") val pointsEarned: Int,
    @Json(name = "category_breakdown") val categoryBreakdown: Map<String, CategoryStatsDto>
)

@JsonClass(generateAdapter = true)
data class CategoryStatsDto(
    val total: Int,
    val earned: Int,
    val percentage: Float
) 