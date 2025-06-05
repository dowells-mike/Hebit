package com.hebit.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO for creating a new habit, based on HabitDocument from backend.
 * Title and frequency are mandatory.
 */
@JsonClass(generateAdapter = true)
data class CreateHabitRequest(
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String? = null,
    @Json(name = "icon") val icon: String? = null,
    @Json(name = "color") val color: String? = null,
    @Json(name = "frequency") val frequency: String, //"daily", "weekly"
    @Json(name = "frequencyConfig") val frequencyConfig: HabitFrequencyConfigDto? = null,
    @Json(name = "category") val category: String? = null,
    @Json(name = "difficulty") val difficulty: String? = null, //"easy", "medium", "hard"
    @Json(name = "impact") val impact: Int? = null,
    @Json(name = "startDate") val startDate: String? = null, // ISO_LOCAL_DATE string, "2024-10-27"
    @Json(name = "endDate") val endDate: String? = null, // ISO_LOCAL_DATE string
    @Json(name = "status") val status: String? = null //"active"
)