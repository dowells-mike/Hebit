package com.hebit.app.data.repository

import com.hebit.app.data.local.TokenManager
import com.hebit.app.data.remote.api.HebitApiService
import com.hebit.app.data.remote.dto.LoginRequest
import com.hebit.app.data.remote.dto.RegisterRequest
import com.hebit.app.data.remote.dto.UserResponse
import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that handles authentication operations
 */
@Singleton
class AuthRepository @Inject constructor(
    private val apiService: HebitApiService,
    private val tokenManager: TokenManager
) {
    /**
     * Login user with email and password
     */
    suspend fun login(email: String, password: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        
        try {
            val loginRequest = LoginRequest(email, password)
            val response = apiService.login(loginRequest)
            
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                
                // Save auth token and user ID
                tokenManager.saveToken(loginResponse.token)
                tokenManager.saveUserId(loginResponse.user.id)
                
                // Map to domain model
                val user = mapUserResponseToDomain(loginResponse.user)
                emit(Resource.Success(user))
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                emit(Resource.Error(errorMessage))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Server error: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error: ${e.localizedMessage ?: "Check your internet connection"}"))
        } catch (e: Exception) {
            emit(Resource.Error("Unexpected error: ${e.localizedMessage ?: "An unexpected error occurred"}"))
        }
    }
    
    /**
     * Register new user
     */
    suspend fun register(name: String, email: String, password: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        
        try {
            val registerRequest = RegisterRequest(name, email, password)
            val response = apiService.register(registerRequest)
            
            if (response.isSuccessful && response.body() != null) {
                val registerResponse = response.body()!!
                
                // Save auth token and user ID
                tokenManager.saveToken(registerResponse.token)
                tokenManager.saveUserId(registerResponse.user.id)
                
                // Map to domain model
                val user = mapUserResponseToDomain(registerResponse.user)
                emit(Resource.Success(user))
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                emit(Resource.Error(errorMessage))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Server error: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error: ${e.localizedMessage ?: "Check your internet connection"}"))
        } catch (e: Exception) {
            emit(Resource.Error("Unexpected error: ${e.localizedMessage ?: "An unexpected error occurred"}"))
        }
    }
    
    /**
     * Get currently logged in user profile
     */
    suspend fun getUserProfile(): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        
        try {
            val response = apiService.getUserProfile()
            
            if (response.isSuccessful && response.body() != null) {
                val userResponse = response.body()!!
                
                // Map to domain model
                val user = mapUserResponseToDomain(userResponse)
                emit(Resource.Success(user))
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                emit(Resource.Error(errorMessage))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Server error: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error: ${e.localizedMessage ?: "Check your internet connection"}"))
        } catch (e: Exception) {
            emit(Resource.Error("Unexpected error: ${e.localizedMessage ?: "An unexpected error occurred"}"))
        }
    }
    
    /**
     * Logout user
     */
    fun logout() {
        tokenManager.clearAuthData()
    }
    
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }
    
    /**
     * Map UserResponse DTO to User domain model
     */
    private fun mapUserResponseToDomain(userResponse: UserResponse): User {
        return User(
            id = userResponse.id,
            name = userResponse.name,
            email = userResponse.email,
            isAdmin = userResponse.isAdmin
        )
    }
}
