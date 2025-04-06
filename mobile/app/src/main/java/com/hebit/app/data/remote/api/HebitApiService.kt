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
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>
    
    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<RefreshTokenResponse>
    
    @GET("auth/profile")
    suspend fun getUserProfile(): Response<UserResponse>
    
    @POST("auth/forgot-password")
    suspend fun requestPasswordReset(@Body request: ForgotPasswordRequest): Response<ForgotPasswordResponse>
    
    // Task Endpoints
    @GET("tasks")
    suspend fun getTasks(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<List<TaskDto>>
    
    @GET("tasks/{id}")
    suspend fun getTaskById(@Path("id") id: String): Response<TaskDto>
    
    @POST("tasks")
    suspend fun createTask(@Body createTaskRequest: CreateTaskRequest): Response<TaskDto>
    
    @PUT("tasks/{id}")
    suspend fun updateTask(
        @Path("id") id: String,
        @Body updateTaskRequest: UpdateTaskRequest
    ): Response<TaskDto>
    
    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") id: String): Response<Void>
    
    @PATCH("tasks/{id}/complete")
    suspend fun toggleTaskCompletion(@Path("id") id: String): Response<TaskDto>
    
    @GET("tasks/priority")
    suspend fun getPriorityTasks(@Query("limit") limit: Int = 5): Response<TaskListResponse>
    
    @GET("tasks/today")
    suspend fun getTasksDueToday(): Response<TaskListResponse>
    
    // Habit Endpoints
    @GET("habits")
    suspend fun getHabits(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<HabitListResponse>
    
    @GET("habits/{id}")
    suspend fun getHabitById(@Path("id") id: String): Response<HabitDto>
    
    @POST("habits")
    suspend fun createHabit(@Body createHabitRequest: CreateHabitRequest): Response<HabitDto>
    
    @PUT("habits/{id}")
    suspend fun updateHabit(
        @Path("id") id: String,
        @Body updateHabitRequest: UpdateHabitRequest
    ): Response<HabitDto>
    
    @DELETE("habits/{id}")
    suspend fun deleteHabit(@Path("id") id: String): Response<Void>
    
    @GET("habits/today")
    suspend fun getTodaysHabits(): Response<HabitListResponse>
    
    @PUT("habits/{id}/complete")
    suspend fun completeHabitForToday(
        @Path("id") id: String,
        @Body request: HabitCompletionRequest
    ): Response<HabitDto>
    
    // Goal Endpoints
    @GET("goals")
    suspend fun getGoals(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<GoalListResponse>
    
    @GET("goals/{id}")
    suspend fun getGoalById(@Path("id") id: String): Response<GoalDto>
    
    @POST("goals")
    suspend fun createGoal(@Body createGoalRequest: CreateGoalRequest): Response<GoalDto>
    
    @PUT("goals/{id}")
    suspend fun updateGoal(
        @Path("id") id: String,
        @Body updateGoalRequest: UpdateGoalRequest
    ): Response<GoalDto>
    
    @DELETE("goals/{id}")
    suspend fun deleteGoal(@Path("id") id: String): Response<Void>
    
    @GET("goals/active")
    suspend fun getActiveGoals(): Response<GoalListResponse>
    
    @PUT("goals/{id}/progress")
    suspend fun updateGoalProgress(
        @Path("id") id: String,
        @Body request: GoalProgressRequest
    ): Response<GoalDto>
    
    // Productivity Metrics Endpoints
    @GET("productivity/metrics")
    suspend fun getProductivityMetrics(
        @Query("from_date") fromDate: String?,
        @Query("to_date") toDate: String?
    ): Response<List<ProductivityMetricsDto>>
    
    @POST("productivity/focus-time")
    suspend fun trackFocusTime(@Body request: FocusTimeRequest): Response<ProductivityMetricsDto>
    
    @POST("productivity/day-rating")
    suspend fun submitDayRating(@Body request: DayRatingRequest): Response<ProductivityMetricsDto>
    
    @GET("productivity/insights")
    suspend fun getProductivityInsights(@Query("period") period: String?): Response<ProductivityInsightsResponse>
    
    // Achievement Endpoints
    @GET("achievements")
    suspend fun getAchievements(
        @Query("category") category: String?,
        @Query("earned") earned: Boolean?,
        @Query("rarity") rarity: String?,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): Response<AchievementListResponse>
    
    @GET("achievements/progress")
    suspend fun getAchievementProgress(): Response<AchievementProgressResponse>
    
    @GET("achievements/check")
    suspend fun checkNewAchievements(): Response<NewlyEarnedAchievementsResponse>
    
    @GET("achievements/user")
    suspend fun getUserAchievements(): Response<UserAchievementResponse>
}
