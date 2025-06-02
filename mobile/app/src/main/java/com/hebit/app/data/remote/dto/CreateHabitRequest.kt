package com.hebit.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO for creating a new habit. Based on HabitDocument from backend.
 * Title and frequency are mandatory.
 */
@JsonClass(generateAdapter = true)
data class CreateHabitRequest(
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String? = null,
    @Json(name = "icon") val icon: String? = null,
    @Json(name = "color") val color: String? = null,
    @Json(name = "frequency") val frequency: String, // e.g., "daily", "weekly"
    @Json(name = "frequencyConfig") val frequencyConfig: HabitFrequencyConfigDto? = null,
    @Json(name = "category") val category: String? = null,
    @Json(name = "difficulty") val difficulty: String? = null, // e.g., "easy", "medium", "hard"
    @Json(name = "impact") val impact: Int? = null,
    @Json(name = "startDate") val startDate: String? = null, // ISO_LOCAL_DATE string, e.g., "2023-10-27"
    @Json(name = "endDate") val endDate: String? = null, // ISO_LOCAL_DATE string
    // reminderSettings, successCriteria, goalLink, timePreference etc. can be added if supported by backend create endpoint
    // For now, keeping it to core fields observed in HabitDocument that are likely create-time settable.
    @Json(name = "status") val status: String? = null // e.g., "active"
)