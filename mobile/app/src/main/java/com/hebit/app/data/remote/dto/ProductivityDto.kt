package com.hebit.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProductivityDto(
    val id: String,
    @Json(name = "user_id") val userId: String,
    val date: String,
    @Json(name = "focus_time") val focusTime: Int,
    @Json(name = "day_rating") val dayRating: Int?,
    @Json(name = "task_completions") val taskCompletions: Int,
    @Json(name = "habit_completions") val habitCompletions: Int,
    @Json(name = "productivity_score") val productivityScore: Float?,
    val notes: String?,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class FocusTimeRequestDto(
    val duration: Int,
    @Json(name = "task_id") val taskId: String? = null,
    @Json(name = "habit_id") val habitId: String? = null,
    @Json(name = "goal_id") val goalId: String? = null,
    val notes: String? = null
)

@JsonClass(generateAdapter = true)
data class DayRatingRequestDto(
    val rating: Int, // 1-5
    val notes: String? = null
)

@JsonClass(generateAdapter = true)
data class ProductivityInsightsDto(
    val period: String, // week, month, year
    @Json(name = "total_focus_time") val totalFocusTime: Int,
    @Json(name = "average_focus_time") val averageFocusTime: Float,
    @Json(name = "average_day_rating") val averageDayRating: Float,
    @Json(name = "total_task_completions") val totalTaskCompletions: Int,
    @Json(name = "total_habit_completions") val totalHabitCompletions: Int,
    @Json(name = "average_productivity_score") val averageProductivityScore: Float,
    @Json(name = "daily_breakdown") val dailyBreakdown: List<DailyBreakdownDto>,
    @Json(name = "focus_by_category") val focusByCategory: Map<String, Int>,
    val trends: TrendsDto
)

@JsonClass(generateAdapter = true)
data class DailyBreakdownDto(
    val date: String,
    @Json(name = "focus_time") val focusTime: Int,
    @Json(name = "day_rating") val dayRating: Int?,
    @Json(name = "productivity_score") val productivityScore: Float?
)

@JsonClass(generateAdapter = true)
data class TrendsDto(
    @Json(name = "focus_time_trend") val focusTimeTrend: String, // up, down, stable
    @Json(name = "day_rating_trend") val dayRatingTrend: String,
    @Json(name = "productivity_score_trend") val productivityScoreTrend: String
) 