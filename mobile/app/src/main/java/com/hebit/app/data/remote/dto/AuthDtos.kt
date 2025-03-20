package com.hebit.app.data.remote.dto

/**
 * Data Transfer Objects for authentication requests and responses
 */

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val refreshToken: String,
    val user: UserResponse
)

data class RefreshTokenRequest(
    val refreshToken: String,
    val userId: String
)

data class RefreshTokenResponse(
    val token: String
)

data class UserResponse(
    val id: String,
    val name: String,
    val email: String,
    val isAdmin: Boolean = false,
    val createdAt: String
)
