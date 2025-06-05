package com.hebit.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data transfer object for habit statistics from the API
 */
@JsonClass(generateAdapter = true)
data class HabitCompletionByDayDto(
    @Json(name = "day") val day: String, // "Sunday"etc
)

@JsonClass(generateAdapter = true)
data class HabitCompletionByTimeDto(
    @Json(name = "name") val name: String, // Morning (5am-12pm)"
    @Json(name = "count") val count: Int
)

@JsonClass(generateAdapter = true)
data class HabitStatsDto(
    @Json(name = "completionRate") val completionRate: Float?,
    @Json(name = "currentStreak") val currentStreak: Int?,
    @Json(name = "longestStreak") val longestStreak: Int?,
    @Json(name = "consistency") val consistency: Float?, // Percentage
    @Json(name = "totalEntries") val totalEntries: Int?,
    @Json(name = "completedEntries") val completedEntries: Int?,
    @Json(name = "completionsByDay") val completionsByDay: List<HabitCompletionByDayDto>? = emptyList(),
    @Json(name = "completionsByTime") val completionsByTime: List<HabitCompletionByTimeDto>? = emptyList()
) 