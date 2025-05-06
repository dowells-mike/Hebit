package com.hebit.app.domain.repository

import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.model.Task
import kotlinx.coroutines.flow.Flow
import com.hebit.app.domain.model.Category
import com.hebit.app.domain.model.TaskStatus

interface TaskRepository {
    suspend fun getTasks(): Flow<Resource<List<Task>>>
    suspend fun getTaskById(id: String): Flow<Resource<Task>>
    suspend fun createTask(task: Task): Flow<Resource<Task>>
    suspend fun updateTask(task: Task): Flow<Resource<Task>>
    suspend fun deleteTask(id: String): Flow<Resource<Boolean>>
    suspend fun toggleTaskCompletion(id: String): Flow<Resource<Task>>
    suspend fun updateTaskStatus(taskId: String, status: TaskStatus): Flow<Resource<Task>>
    suspend fun getPriorityTasks(limit: Int = 5): Flow<Resource<List<Task>>>
    suspend fun getTasksDueToday(): Flow<Resource<List<Task>>>

    suspend fun getCategories(): Flow<Resource<List<Category>>>
    suspend fun createCategory(name: String, color: String, icon: String?): Flow<Resource<Category>>
    suspend fun updateCategory(id: String, name: String?, color: String?, icon: String?): Flow<Resource<Category>>
    suspend fun deleteCategory(id: String): Flow<Resource<Boolean>>
} 