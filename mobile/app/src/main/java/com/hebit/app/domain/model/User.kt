package com.hebit.app.domain.model

/**
 * Domain model for User entity
 */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val isAdmin: Boolean = false
)
