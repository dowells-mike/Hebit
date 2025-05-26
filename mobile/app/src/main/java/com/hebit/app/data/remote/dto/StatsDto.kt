package com.hebit.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Data classes for /api/stats/tasks endpoint
@JsonClass(generateAdapter = true)
data class PeriodDto(
    @Json(name = "startDate") val startDate: String,
    @Json(name = "endDate") val endDate: String,
    @Json(name = "query") val query: Map<String, String>? // For debugging
)

@JsonClass(generateAdapter = true)
data class PriorityBreakdownDto(
    @Json(name = "priority") val priority: String?,
    @Json(name = "totalCreated") val totalCreated: Int,
    @Json(name = "totalCompleted") val totalCompleted: Int,
    @Json(name = "completedOnTime") val completedOnTime: Int,
    @Json(name = "completionRate") val completionRate: Int
)

@JsonClass(generateAdapter = true)
data class CategoryBreakdownDto(
    @Json(name = "category") val category: String?,
    @Json(name = "categoryId") val categoryId: String?,
    @Json(name = "totalCreated") val totalCreated: Int,
    @Json(name = "totalCompleted") val totalCompleted: Int,
    @Json(name = "completionRate") val completionRate: Int
)

@JsonClass(generateAdapter = true)
data class TaskStatisticsResponseDto(
    @Json(name = "period") val period: PeriodDto,
    @Json(name = "totalTasksCreated") val totalTasksCreated: Int,
    @Json(name = "totalTasksCompleted") val totalTasksCompleted: Int,
    @Json(name = "completionRate") val completionRate: Int, // Overall completion rate
    @Json(name = "tasksCompletedOnTime") val tasksCompletedOnTime: Int,
    @Json(name = "tasksCompletedLate") val tasksCompletedLate: Int,
    @Json(name = "tasksOverdue") val tasksOverdue: Int,
    @Json(name = "priorityBreakdown") val priorityBreakdown: List<PriorityBreakdownDto>?,
    @Json(name = "categoryBreakdown") val categoryBreakdown: List<CategoryBreakdownDto>?
)

// Data class for /api/stats/productivity-score endpoint
@JsonClass(generateAdapter = true)
data class ProductivityScoreResponseDto(
    @Json(name = "period") val period: PeriodDto,
    @Json(name = "productivityScore") val productivityScore: Int
)

// Data class for /api/stats/score-history endpoint (even if placeholder)
@JsonClass(generateAdapter = true)
data class ScoreHistoryItemDto(
    @Json(name = "date") val date: String, // Assuming YYYY-MM-DD
    @Json(name = "score") val score: Int
)

@JsonClass(generateAdapter = true)
data class ScoreHistoryResponseDto(
    @Json(name = "message") val message: String?,
    @Json(name = "query") val query: Map<String, String>?,
    @Json(name = "history") val history: List<ScoreHistoryItemDto>
) 