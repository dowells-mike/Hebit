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
    @Json(name = "refresh_token") val refreshToken: String,
    @Json(name = "user_id") val userId: String
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val token: String,
    @Json(name = "refresh_token") val refreshToken: String,
    val user: UserResponse
)

@JsonClass(generateAdapter = true)
data class RegisterResponse(
    val token: String,
    @Json(name = "refresh_token") val refreshToken: String,
    val user: UserResponse
)

@JsonClass(generateAdapter = true)
data class RefreshTokenResponse(
    val token: String,
    @Json(name = "refresh_token") val refreshToken: String
)

@JsonClass(generateAdapter = true)
data class UserResponse(
    val id: String,
    val name: String,
    val email: String,
    @Json(name = "is_admin") val isAdmin: Boolean
) 