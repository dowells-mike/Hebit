package com.hebit.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO for updating an existing habit. Based on HabitDocument from backend.
 * All fields are optional for partial updates.
 */
@JsonClass(generateAdapter = true)
data class UpdateHabitRequest(
    @Json(name = "title") val title: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "icon") val icon: String? = null,
    @Json(name = "color") val color: String? = null,
    @Json(name = "frequency") val frequency: String? = null,
    @Json(name = "frequencyConfig") val frequencyConfig: HabitFrequencyConfigDto? = null,
    @Json(name = "category") val category: String? = null,
    @Json(name = "difficulty") val difficulty: String? = null,
    @Json(name = "impact") val impact: Int? = null,
    @Json(name = "startDate") val startDate: String? = null,
    @Json(name = "endDate") val endDate: String? = null,
    @Json(name = "status") val status: String? = null // e.g., "active", "archived"
    // reminderSettings, successCriteria etc. can be added if the backend supports updating them.
)