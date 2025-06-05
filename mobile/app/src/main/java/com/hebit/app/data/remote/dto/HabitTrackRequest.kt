package com.hebit.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO for tracking habit completion.
 * Corresponds to PUT /api/habits/:id/track request body.
 */
@JsonClass(generateAdapter = true)
data class HabitTrackRequest(
    @Json(name = "completed") val completed: Boolean,
    @Json(name = "date") val date: String, // ISO8601 String e.g. "YYYY-MM-DDTHH:mm:ss.sssZ"
    @Json(name = "notes") val notes: String? = null
) 