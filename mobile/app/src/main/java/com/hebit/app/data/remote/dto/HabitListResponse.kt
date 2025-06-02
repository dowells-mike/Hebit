package com.hebit.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO for the response when fetching a list of habits.
 * Matches the backend structure: { habits: [], total: Int, page: Int, per_page: Int }
 */
@JsonClass(generateAdapter = true)
data class HabitListResponse(
    @Json(name = "habits") val habits: List<HabitDto>,
    @Json(name = "total") val total: Int,
    @Json(name = "page") val page: Int,
    @Json(name = "per_page") val perPage: Int
)