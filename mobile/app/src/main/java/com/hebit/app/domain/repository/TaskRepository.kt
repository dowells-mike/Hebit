package com.hebit.app.domain.repository

import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.model.Task
import kotlinx.coroutines.flow.Flow
import com.hebit.app.domain.model.Category
import com.hebit.app.domain.model.TaskStatus
import com.hebit.app.data.remote.dto.TaskStatisticsResponseDto
import com.hebit.app.data.remote.dto.ProductivityScoreResponseDto
import com.hebit.app.data.remote.dto.ScoreHistoryResponseDto
import com.hebit.app.data.remote.dto.TaskSuggestionDto

interface TaskRepository {
    suspend fun getTasks(): Flow<Resource<List<Task>>>
    suspend fun getTaskById(id: String): Flow<Resource<Task>>
    suspend fun getTaskByIdOnce(taskId: String): Resource<Task?>
    suspend fun createTask(task: Task): Flow<Resource<Task>>
    suspend fun updateTask(task: Task): Flow<Resource<Task>>
    suspend fun deleteTask(id: String): Flow<Resource<Boolean>>
    suspend fun toggleTaskCompletion(id: String): Flow<Resource<Task>>
    suspend fun updateTaskStatus(taskId: String, status: TaskStatus): Flow<Resource<Task>>
    suspend fun getPriorityTasks(limit: Int = 5): Flow<Resource<List<Task>>>
    suspend fun getTasksDueToday(): Flow<Resource<List<Task>>>

    suspend fun getCategories(): Flow<Resource<List<Category>>>
    suspend fun getCategoryById(categoryId: String): Flow<Resource<Category>>
    suspend fun createCategory(name: String, color: String, icon: String?): Flow<Resource<Category>>
    suspend fun updateCategory(id: String, name: String?, color: String?, icon: String?): Flow<Resource<Category>>
    suspend fun deleteCategory(id: String): Flow<Resource<Boolean>>

    // Stats
    suspend fun getTaskStatistics(
        period: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): Flow<Resource<TaskStatisticsResponseDto>>

    suspend fun getProductivityScore(
        period: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): Flow<Resource<ProductivityScoreResponseDto>>

    suspend fun getScoreHistory(
        periodType: String? = null,
        count: Int? = null
    ): Flow<Resource<ScoreHistoryResponseDto>>

    // Task Suggestions
    suspend fun getTaskSuggestions(): Flow<Resource<List<TaskSuggestionDto>>>
} 