package com.hebit.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserAchievementDto(
    @Json(name = "_id") val id: String, // ID of this UserAchievement record itself
    @Json(name = "user") val userId: String,
    // The 'achievement' field can be either a String (ID) or a populated AchievementDto object.
    // For simplicity in initial DTO, we'll expect it to be populated by the backend, or handle ID-only cases in the mapper/repository if needed.
    @Json(name = "achievement") val achievement: AchievementDto,
    @Json(name = "progress") val progress: Double, // Changed from Int to Double to handle potential fractional progress
    @Json(name = "earned") val earned: Boolean,
    @Json(name = "earnedAt") val earnedAt: String? = null, // ISO Date String
    @Json(name = "seenByUser") val seenByUser: Boolean? = null,
    @Json(name = "createdAt") val createdAt: String, // ISO Date String
    @Json(name = "updatedAt") val updatedAt: String  // ISO Date String
) 