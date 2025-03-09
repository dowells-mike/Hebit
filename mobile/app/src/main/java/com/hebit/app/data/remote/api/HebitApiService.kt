package com.hebit.app.data.remote.api

import com.hebit.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit service interface for Hebit API
 */
interface HebitApiService {
    
    // Auth Endpoints
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
    
    @POST("auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<LoginResponse>
    
    @GET("auth/profile")
    suspend fun getUserProfile(): Response<UserResponse>
    
    // Task Endpoints
    @GET("tasks")
    suspend fun getTasks(): Response<List<TaskResponse>>
    
    @POST("tasks")
    suspend fun createTask(@Body taskRequest: TaskRequest): Response<TaskResponse>
    
    @PUT("tasks/{id}")
    suspend fun updateTask(
        @Path("id") taskId: String, 
        @Body taskRequest: TaskRequest
    ): Response<TaskResponse>
    
    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") taskId: String): Response<Unit>
    
    @PATCH("tasks/{id}/complete")
    suspend fun toggleTaskCompletion(
        @Path("id") taskId: String,
        @Body completeRequest: TaskCompletionRequest
    ): Response<TaskResponse>
    
    // Habit Endpoints
    @GET("habits")
    suspend fun getHabits(): Response<List<HabitResponse>>
    
    @POST("habits")
    suspend fun createHabit(@Body habitRequest: HabitRequest): Response<HabitResponse>
    
    @PUT("habits/{id}")
    suspend fun updateHabit(
        @Path("id") habitId: String,
        @Body habitRequest: HabitRequest
    ): Response<HabitResponse>
    
    @DELETE("habits/{id}")
    suspend fun deleteHabit(@Path("id") habitId: String): Response<Unit>
    
    @POST("habits/{id}/track")
    suspend fun trackHabit(
        @Path("id") habitId: String,
        @Body trackingRequest: HabitTrackingRequest
    ): Response<HabitResponse>
    
    // Goal Endpoints
    @GET("goals")
    suspend fun getGoals(): Response<List<GoalResponse>>
    
    @POST("goals")
    suspend fun createGoal(@Body goalRequest: GoalRequest): Response<GoalResponse>
    
    @PUT("goals/{id}")
    suspend fun updateGoal(
        @Path("id") goalId: String,
        @Body goalRequest: GoalRequest
    ): Response<GoalResponse>
    
    @DELETE("goals/{id}")
    suspend fun deleteGoal(@Path("id") goalId: String): Response<Unit>
    
    @PATCH("goals/{id}/progress")
    suspend fun updateGoalProgress(
        @Path("id") goalId: String,
        @Body progressRequest: GoalProgressRequest
    ): Response<GoalResponse>
}
