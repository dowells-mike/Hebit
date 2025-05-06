package com.hebit.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data Transfer Object for Category received from the backend.
 */
@JsonClass(generateAdapter = true)
data class CategoryDto(
    @Json(name = "_id") val id: String,
    val name: String,
    val color: String, // Assuming backend sends hex color string
    val icon: String? = null, // Assuming backend sends icon name/identifier
    val userId: String // Assuming categories are user-specific
)

/**
 * Request body for creating a new Category.
 */
@JsonClass(generateAdapter = true)
data class CreateCategoryRequest(
    val name: String,
    val color: String,
    val icon: String? = null
)

/**
 * Request body for updating an existing Category.
 */
@JsonClass(generateAdapter = true)
data class UpdateCategoryRequest(
    val name: String? = null,
    val color: String? = null,
    val icon: String? = null
) 