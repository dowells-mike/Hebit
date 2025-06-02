package com.hebit.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AchievementDto(
    @Json(name = "_id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String,
    @Json(name = "category") val category: String, // 'tasks' | 'habits' | 'goals' | 'special'
    @Json(name = "points") val points: Int,
    @Json(name = "icon") val icon: String,
    @Json(name = "criteria") val criteria: AchievementCriteriaDto,
    @Json(name = "rarity") val rarity: String, // 'common' | 'rare' | 'epic' | 'legendary'
    @Json(name = "secret") val secret: Boolean? = null,
    @Json(name = "createdAt") val createdAt: String, // ISO Date String
    @Json(name = "updatedAt") val updatedAt: String  // ISO Date String
)

@JsonClass(generateAdapter = true)
data class AchievementCriteriaDto(
    @Json(name = "type") val type: String, // 'count' | 'streak' | 'completion_time' | 'multi_condition' | 'event_based' | 'complex'
    @Json(name = "source") val source: String? = null, // 'tasks' | 'habits' | 'goals' | 'user_activity' | 'app_usage'
    @Json(name = "targetValue") val targetValue: Any, // Can be number or string
    @Json(name = "conditionDetails") val conditionDetails: Map<String, Any>? = null // Flexible map for various details
) 