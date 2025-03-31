package com.hebit.app.domain.repository

import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Interface for authentication operations
 */
interface IAuthRepository {
    /**
     * Login user with email and password
     */
    fun login(email: String, password: String): Flow<Resource<User>>
    
    /**
     * Register new user
     */
    fun register(name: String, email: String, password: String): Flow<Resource<User>>
    
    /**
     * Get user profile using stored token
     */
    fun getUserProfile(): Flow<Resource<User>>
    
    /**
     * Request password reset
     */
    fun requestPasswordReset(email: String): Flow<Resource<Boolean>>
    
    /**
     * Logout current user (clear auth data)
     */
    fun logout()
    
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean
} 