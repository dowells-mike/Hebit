package com.hebit.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GoalDto(
    @Json(name = "_id") val id: String,
    val title: String,
    val description: String?,
    val progress: Int?,
    @Json(name = "target_date") val targetDate: String?,
    val category: String?,
    @Json(name = "is_completed") val isCompleted: Boolean?,
    @Json(name = "created_at") val createdAt: String?,
    @Json(name = "updated_at") val updatedAt: String?
)

@JsonClass(generateAdapter = true)
data class GoalListResponse(
    val goals: List<GoalDto>,
    val total: Int,
    val page: Int,
    @Json(name = "per_page") val perPage: Int
)

@JsonClass(generateAdapter = true)
data class CreateGoalRequest(
    val title: String,
    val description: String,
    @Json(name = "target_date") val targetDate: String,
    val category: String
)

@JsonClass(generateAdapter = true)
data class UpdateGoalRequest(
    val title: String? = null,
    val description: String? = null,
    @Json(name = "target_date") val targetDate: String? = null,
    val category: String? = null,
    val progress: Int? = null,
    @Json(name = "is_completed") val isCompleted: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class GoalProgressRequest(
    val progress: Int
) 