package com.hebit.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HabitDto(
    @Json(name = "_id") val id: String,
    val title: String,
    val description: String,
    @Json(name = "icon_name") val iconName: String,
    val frequency: String,
    @Json(name = "completed_today") val completedToday: Boolean,
    val streak: Int,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class HabitListResponse(
    val habits: List<HabitDto>,
    val total: Int,
    val page: Int,
    @Json(name = "per_page") val perPage: Int
)

@JsonClass(generateAdapter = true)
data class CreateHabitRequest(
    val title: String,
    val description: String,
    @Json(name = "icon_name") val iconName: String,
    val frequency: String
)

@JsonClass(generateAdapter = true)
data class UpdateHabitRequest(
    val title: String? = null,
    val description: String? = null,
    @Json(name = "icon_name") val iconName: String? = null,
    val frequency: String? = null
)

@JsonClass(generateAdapter = true)
data class HabitCompletionRequest(
    val completed: Boolean
) 