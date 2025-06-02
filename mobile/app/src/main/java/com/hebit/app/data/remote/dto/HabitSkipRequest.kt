package com.hebit.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO for skipping a habit.
 * Corresponds to POST /api/habits/:id/skip request body.
 */
@JsonClass(generateAdapter = true)
data class HabitSkipRequest(
    @Json(name = "date") val date: String, // ISO8601 String, e.g., "YYYY-MM-DDTHH:mm:ss.sssZ"
    @Json(name = "skipReason") val skipReason: String? = null
) 