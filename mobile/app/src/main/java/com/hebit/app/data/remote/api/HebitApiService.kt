package com.hebit.app.data.remote.api

import com.hebit.app.data.remote.dto.* // Ensure all used DTOs are imported
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit service interface for Hebit API
 * Updated based on actual backend implementation
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
    ): Response<List<TaskDto>> // Note: Backend might send a TaskListResponse DTO instead of raw List

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
    suspend fun deleteTask(@Path("id") id: String): Response<Void> // Or Response<Unit>

    @PATCH("tasks/{id}/complete") // Assuming PATCH is used for toggling completion
    suspend fun toggleTaskCompletion(@Path("id") id: String): Response<TaskDto>

    @PUT("tasks/{id}") // This seems to duplicate updateTask or is for specific status update
    suspend fun updateTaskStatus(@Path("id") id: String, @Body statusUpdate: Map<String, String>): Response<TaskDto>

    @GET("tasks/priority")
    suspend fun getPriorityTasks(@Query("limit") limit: Int = 5): Response<TaskListResponse>

    @GET("tasks/today")
    suspend fun getTasksDueToday(): Response<TaskListResponse>

    // === CORRECTED HABIT ENDPOINTS ===
    @GET("habits") // GET /api/habits
    suspend fun getHabits(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
        @Query("frequency") frequency: String? = null,
        @Query("category") category: String? = null,
        @Query("status") status: String? = null
    ): Response<HabitListResponse> // Response format: { habits: HabitDto[], total: number, page: number, per_page: number }

    @GET("habits/today") // GET /api/habits/today
    suspend fun getTodaysHabits(): Response<HabitListResponse> // Same response format as getHabits

    @GET("habits/{id}") // GET /api/habits/:id
    suspend fun getHabitById(@Path("id") id: String): Response<HabitDto> // Single habit with completed_today field

    @POST("habits") // POST /api/habits
    suspend fun createHabit(@Body createHabitRequest: CreateHabitRequest): Response<HabitDto>

    @PUT("habits/{id}") // PUT /api/habits/:id
    suspend fun updateHabit(
        @Path("id") id: String,
        @Body updateHabitRequest: UpdateHabitRequest
    ): Response<HabitDto>

    @DELETE("habits/{id}") // DELETE /api/habits/:id
    suspend fun deleteHabit(@Path("id") id: String): Response<Unit>

    // CORRECTED: This is PUT method, and it takes a body with { completed: boolean, date: string, notes?: string }
    @PUT("habits/{id}/track") // PUT /api/habits/:id/track
    suspend fun trackHabit(
        @Path("id") id: String,
        @Body request: HabitTrackRequest
    ): Response<HabitDto>

    @POST("habits/{id}/skip") // POST /api/habits/:id/skip
    suspend fun skipHabit(
        @Path("id") id: String,
        @Body request: HabitSkipRequest
    ): Response<HabitDto>

    @GET("habits/{id}/stats") // GET /api/habits/:id/stats
    suspend fun getHabitStats(@Path("id") id: String): Response<HabitStatsDto>

    // Note: The backend does not have these endpoints yet - these were assumptions
    // @GET("habits/{id}/notes")
    // @POST("habits/{id}/notes")
    // @GET("habits/{id}/performance-insights")
    // @GET("habits/{id}/related-achievements")
    // @GET("habits/{id}/suggestions")

    // Goal Endpoints
    @GET("goals")
    suspend fun getGoals(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<List<GoalDto>> // Note: Backend might send a GoalListResponse DTO

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
    suspend fun deleteGoal(@Path("id") id: String): Response<Void> // Or Response<Unit>

    @GET("goals/active")
    suspend fun getActiveGoals(): Response<GoalListResponse> // GoalListResponse DTO needs to exist

    @PATCH("goals/{id}/progress")
    suspend fun updateGoalProgress(
        @Path("id") id: String,
        @Body request: GoalProgressRequest // GoalProgressRequest DTO needs to exist
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

    // === ACHIEVEMENT ENDPOINTS ===
    @GET("achievements")
    suspend fun getAllAchievements(): Response<List<AchievementDto>>

    @GET("achievements/user/{userId}")
    suspend fun getUserAchievements(@Path("userId") userId: String): Response<List<UserAchievementDto>>

    @POST("achievements/user-achievements/{userAchievementId}/seen")
    suspend fun markUserAchievementSeen(@Path("userAchievementId") userAchievementId: String): Response<UserAchievementDto>

    // Category (List) Endpoints
    @GET("categories")
    suspend fun getCategories(): Response<List<CategoryDto>>

    @GET("categories/{id}")
    suspend fun getCategoryById(@Path("id") categoryId: String): Response<CategoryDto>

    @POST("categories")
    suspend fun createCategory(@Body request: CreateCategoryRequest): Response<CategoryDto>

    @PUT("categories/{id}")
    suspend fun updateCategory(@Path("id") id: String, @Body request: UpdateCategoryRequest): Response<CategoryDto>

    @DELETE("categories/{id}")
    suspend fun deleteCategory(@Path("id") id: String): Response<Void> // Or Response<Unit>

    // Stats Endpoints
    @GET("stats/tasks")
    suspend fun getTaskStatistics(
        @Query("period") period: String? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): Response<TaskStatisticsResponseDto> // DTO needs to exist

    @GET("stats/productivity-score")
    suspend fun getProductivityScore(
        @Query("period") period: String? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): Response<ProductivityScoreResponseDto> // DTO needs to exist

    @GET("stats/score-history")
    suspend fun getScoreHistory(
        @Query("periodType") periodType: String? = null,
        @Query("count") count: Int? = null
    ): Response<ScoreHistoryResponseDto> // DTO needs to exist

    // Task Suggestion Endpoints
    @GET("suggestions/tasks")
    suspend fun getTaskSuggestions(): Response<List<TaskSuggestionDto>>
}