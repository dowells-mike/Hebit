package com.hebit.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
data class ProductivityMetricsDto(
    val id: String,
    val date: String,
    
    @Json(name = "tasksCompleted")
    val tasksCompleted: Int = 0,
    
    @Json(name = "tasksCreated")
    val tasksCreated: Int = 0,
    
    @Json(name = "habitCompletionRate")
    val habitCompletionRate: Float = 0f,
    
    @Json(name = "goalProgress")
    val goalProgress: List<GoalProgressDto> = emptyList(),
    
    @Json(name = "focusTime")
    val focusTime: Int = 0,
    
    @Json(name = "productivityScore")
    val productivityScore: Float = 0f,
    
    @Json(name = "dayRating")
    val dayRating: Int? = null,
    
    @Json(name = "createdAt")
    val createdAt: String,
    
    @Json(name = "updatedAt")
    val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class GoalProgressDto(
    @Json(name = "goalId")
    val goalId: String,
    
    @Json(name = "progress")
    val progress: Float
)

@JsonClass(generateAdapter = true)
data class ProductivityListResponse(
    val data: List<ProductivityMetricsDto>,
    val page: Int,
    val perPage: Int,
    val total: Int
)

@JsonClass(generateAdapter = true)
data class FocusTimeRequest(
    val minutes: Int,
    val date: String? = null,
    val category: String? = null,
    val taskId: String? = null,
    val habitId: String? = null,
    val goalId: String? = null,
    val notes: String? = null
)

@JsonClass(generateAdapter = true)
data class DayRatingRequest(
    val rating: Int,
    val date: String? = null,
    val notes: String? = null
)

@JsonClass(generateAdapter = true)
data class ProductivityInsightsResponse(
    val averageProductivityScore: Float,
    val totalTasksCompleted: Int,
    val totalFocusTime: Int,
    val averageFocusTime: Float,
    val averageDayRating: Float,
    val bestDay: ProductivityDayDto?,
    val worstDay: ProductivityDayDto?,
    val dailyTrend: List<Float>,
    val weeklyTrend: List<Float>,
    val monthlyTrend: List<Float>
)

@JsonClass(generateAdapter = true)
data class ProductivityDayDto(
    val date: String,
    val score: Float,
    val tasksCompleted: Int
) 