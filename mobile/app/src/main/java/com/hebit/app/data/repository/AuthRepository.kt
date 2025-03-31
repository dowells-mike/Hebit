package com.hebit.app.data.repository

import com.hebit.app.data.local.TokenManager
import com.hebit.app.data.remote.api.HebitApiService
import com.hebit.app.data.remote.dto.LoginRequest
import com.hebit.app.data.remote.dto.RegisterRequest
import com.hebit.app.data.remote.dto.ForgotPasswordRequest
import com.hebit.app.data.remote.dto.UserResponse
import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.model.User
import com.hebit.app.domain.repository.IAuthRepository
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
) : IAuthRepository {
    /**
     * Login user with email and password
     */
    override fun login(email: String, password: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        
        try {
            val loginRequest = LoginRequest(email, password)
            val response = apiService.login(loginRequest)
            
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                
                // Save auth token, refresh token and user ID
                tokenManager.saveToken(loginResponse.token)
                tokenManager.saveRefreshToken(loginResponse.refreshToken)
                tokenManager.saveUserId(loginResponse.user.id)
                
                // Map to domain model
                val user = mapUserResponseToDomain(loginResponse.user)
                emit(Resource.Success(user))
            } else {
                val errorCode = response.code()
                when (errorCode) {
                    401 -> emit(Resource.Error("Invalid email or password"))
                    429 -> emit(Resource.Error("Too many login attempts. Please try again later"))
                    else -> {
                        val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                        emit(Resource.Error(errorMessage))
                    }
                }
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
    override fun register(name: String, email: String, password: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        
        try {
            val registerRequest = RegisterRequest(name, email, password)
            val response = apiService.register(registerRequest)
            
            if (response.isSuccessful && response.body() != null) {
                val registerResponse = response.body()!!
                
                // Save auth token, refresh token and user ID
                tokenManager.saveToken(registerResponse.token)
                tokenManager.saveRefreshToken(registerResponse.refreshToken)
                tokenManager.saveUserId(registerResponse.user.id)
                
                // Map to domain model
                val user = mapUserResponseToDomain(registerResponse.user)
                emit(Resource.Success(user))
            } else {
                val errorCode = response.code()
                when (errorCode) {
                    409 -> emit(Resource.Error("Email already in use"))
                    400 -> emit(Resource.Error("Invalid registration data"))
                    else -> {
                        val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                        emit(Resource.Error(errorMessage))
                    }
                }
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
    override fun getUserProfile(): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        
        try {
            val response = apiService.getUserProfile()
            
            if (response.isSuccessful && response.body() != null) {
                val userResponse = response.body()!!
                
                // Map to domain model
                val user = mapUserResponseToDomain(userResponse)
                emit(Resource.Success(user))
            } else {
                val errorCode = response.code()
                when (errorCode) {
                    401 -> {
                        // Token expired or invalid, clear auth data
                        tokenManager.clearAuthData()
                        emit(Resource.Error("Authentication required"))
                    }
                    else -> {
                        val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                        emit(Resource.Error(errorMessage))
                    }
                }
            }
        } catch (e: HttpException) {
            if (e.code() == 401) {
                tokenManager.clearAuthData()
                emit(Resource.Error("Authentication required"))
            } else {
                emit(Resource.Error("Server error: ${e.message()}"))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network error: ${e.localizedMessage ?: "Check your internet connection"}"))
        } catch (e: Exception) {
            emit(Resource.Error("Unexpected error: ${e.localizedMessage ?: "An unexpected error occurred"}"))
        }
    }
    
    /**
     * Request password reset
     */
    override fun requestPasswordReset(email: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        
        try {
            val request = ForgotPasswordRequest(email)
            val response = apiService.requestPasswordReset(request)
            
            if (response.isSuccessful) {
                emit(Resource.Success(true))
            } else {
                val errorCode = response.code()
                when (errorCode) {
                    404 -> emit(Resource.Error("Email not found"))
                    429 -> emit(Resource.Error("Too many requests. Please try again later"))
                    else -> {
                        val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                        emit(Resource.Error(errorMessage))
                    }
                }
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
    override fun logout() {
        tokenManager.clearAuthData()
    }
    
    /**
     * Check if user is logged in
     */
    override fun isLoggedIn(): Boolean {
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
