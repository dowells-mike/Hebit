package com.hebit.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReminderDto(
    @Json(name = "type") val type: String, // "relative" or "absolute"
    @Json(name = "offset_minutes") val offsetMinutes: Int? = null, // For relative
    @Json(name = "absolute_time") val absoluteTime: String? = null // ISO 8601 string for absolute UTC date-time
) 