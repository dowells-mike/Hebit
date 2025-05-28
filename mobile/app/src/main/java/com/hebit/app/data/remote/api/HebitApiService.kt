package com.hebit.app.data.remote.api

import com.hebit.app.data.remote.dto.* // Ensure all used DTOs are imported
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

    // Habit Endpoints (Refactored Section)
    @GET("habits") // Corresponds to GET /api/habits
    suspend fun getHabits(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<HabitListResponse> // Contains List<HabitDto>

    @GET("habits/{id}") // Corresponds to GET /api/habits/:id
    suspend fun getHabitById(@Path("id") id: String): Response<HabitDto>

    @POST("habits") // Corresponds to POST /api/habits
    suspend fun createHabit(@Body createHabitRequest: CreateHabitRequest): Response<HabitDto>

    @PUT("habits/{id}") // Corresponds to PUT /api/habits/:id
    suspend fun updateHabit(
        @Path("id") id: String,
        @Body updateHabitRequest: UpdateHabitRequest
    ): Response<HabitDto>

    @DELETE("habits/{id}") // Corresponds to DELETE /api/habits/:id
    suspend fun deleteHabit(@Path("id") id: String): Response<Unit> // Changed from Void for consistency

    @POST("habits/{id}/track") // Corresponds to POST /api/habits/:id/track (no body)
    suspend fun trackHabit(@Path("id") id: String): Response<Unit> // Changed signature

    @POST("habits/{id}/skip") // Corresponds to POST /api/habits/:id/skip (no body) - NEW
    suspend fun skipHabit(@Path("id") id: String): Response<Unit>

    @GET("habits/{id}/stats") // Corresponds to GET /api/habits/:id/stats
    suspend fun getHabitStats(@Path("id") id: String): Response<HabitStatsDto>

    // Notes for Habits
    @GET("habits/{id}/notes") // Path per habit
    suspend fun getNotesForHabit(@Path("id") habitId: String): Response<NotesResponse> // Assuming NotesResponse contains List<NoteDto>

    @POST("habits/{id}/notes") // Path per habit, corrected
    suspend fun addNoteForHabit(
        @Path("id") habitId: String,
        @Body request: CreateNoteRequest // CreateNoteRequest DTO needs to exist
    ): Response<NoteDto>

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

    // Achievement Endpoints
    @GET("achievements")
    suspend fun getAchievements(
        @Query("category") category: String?,
        @Query("earned") earned: Boolean?,
        @Query("rarity") rarity: String?,
        @Query("page") page: Int, // Assuming page is not optional
        @Query("per_page") perPage: Int // Assuming perPage is not optional
    ): Response<AchievementListResponse> // AchievementListResponse DTO needs to exist

    @GET("achievements/progress")
    suspend fun getAchievementProgress(): Response<AchievementProgressResponse> // DTO needs to exist

    @GET("achievements/check")
    suspend fun checkNewAchievements(): Response<NewlyEarnedAchievementsResponse> // DTO needs to exist

    @GET("achievements/user") // This path might conflict with GET /api/users/:id/achievements from backend. Verify.
    suspend fun getUserAchievements(): Response<UserAchievementResponse> // DTO needs to exist. Usually needs user context.

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

    // Define internal data classes for specific responses if not already defined
    // These should ideally be in their respective DTO files for clarity.
    // Example: data class TaskListResponse(val tasks: List<TaskDto>)
    // Example: data class GoalListResponse(val goals: List<GoalDto>)
    // Ensure all ...Response DTOs (like AchievementListResponse, NotesResponse etc.) are properly defined in your DTO package.
}