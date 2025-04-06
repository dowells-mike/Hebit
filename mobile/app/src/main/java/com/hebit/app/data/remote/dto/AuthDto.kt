package com.hebit.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val email: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class RefreshTokenRequest(
    @Json(name = "refreshToken") val refreshToken: String,
    @Json(name = "userId") val userId: String
)

@JsonClass(generateAdapter = true)
data class ForgotPasswordRequest(
    val email: String
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val token: String,
    @Json(name = "refreshToken") val refreshToken: String,
    val user: UserResponse
)

@JsonClass(generateAdapter = true)
data class RegisterResponse(
    val token: String,
    @Json(name = "refreshToken") val refreshToken: String,
    val user: UserResponse
)

@JsonClass(generateAdapter = true)
data class RefreshTokenResponse(
    val token: String,
    @Json(name = "refreshToken") val refreshToken: String
)

@JsonClass(generateAdapter = true)
data class UserResponse(
    val id: String,
    val name: String,
    val email: String,
    @Json(name = "is_admin") val isAdmin: Boolean = false,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class ForgotPasswordResponse(
    val success: Boolean,
    val message: String
) 