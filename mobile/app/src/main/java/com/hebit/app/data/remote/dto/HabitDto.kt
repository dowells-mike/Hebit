package com.hebit.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalDateTime

// DTO for individual completion history entry
@JsonClass(generateAdapter = true)
data class CompletionHistoryEntryDto(
    val date: String, // Assuming ISO date string
    val completed: Boolean,
    val value: Float? = null,
    val notes: String? = null,
    val mood: Int? = null,
    val skipReason: String? = null
)

@JsonClass(generateAdapter = true)
data class HabitDto(
    @Json(name = "_id") val id: String,
    val title: String,
    val description: String,
    @Json(name = "icon_name") val iconName: String? = "default_icon",
    val frequency: String,
    @Json(name = "completed_today") val completedToday: Boolean? = false,
    val streak: Int,
    val completionHistory: List<CompletionHistoryEntryDto> = emptyList(),
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null
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
    val completed: Boolean,
    val date: String,
    val notes: String? = null
) 