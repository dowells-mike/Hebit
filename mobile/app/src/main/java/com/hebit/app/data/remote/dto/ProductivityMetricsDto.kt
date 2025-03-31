package com.hebit.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.util.Date

data class ProductivityMetricsDto(
    val id: String,
    val date: String,
    
    @SerializedName("tasksCompleted")
    val tasksCompleted: Int = 0,
    
    @SerializedName("tasksCreated")
    val tasksCreated: Int = 0,
    
    @SerializedName("habitCompletionRate")
    val habitCompletionRate: Float = 0f,
    
    @SerializedName("goalProgress")
    val goalProgress: List<GoalProgressDto> = emptyList(),
    
    @SerializedName("focusTime")
    val focusTime: Int = 0,
    
    @SerializedName("productivityScore")
    val productivityScore: Float = 0f,
    
    @SerializedName("dayRating")
    val dayRating: Int? = null,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("updatedAt")
    val updatedAt: String
)

data class GoalProgressDto(
    @SerializedName("goalId")
    val goalId: String,
    
    @SerializedName("progress")
    val progress: Float
)

data class ProductivityListResponse(
    val data: List<ProductivityMetricsDto>,
    val page: Int,
    val perPage: Int,
    val total: Int
)

data class FocusTimeRequest(
    val minutes: Int,
    val date: String? = null,
    val category: String? = null,
    val taskId: String? = null,
    val habitId: String? = null,
    val goalId: String? = null,
    val notes: String? = null
)

data class DayRatingRequest(
    val rating: Int,
    val date: String? = null,
    val notes: String? = null
)

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

data class ProductivityDayDto(
    val date: String,
    val score: Float,
    val tasksCompleted: Int
) 