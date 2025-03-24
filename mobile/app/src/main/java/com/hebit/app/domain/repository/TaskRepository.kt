package com.hebit.app.domain.repository

import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    suspend fun getTasks(): Flow<Resource<List<Task>>>
    suspend fun getTaskById(id: String): Flow<Resource<Task>>
    suspend fun createTask(task: Task): Flow<Resource<Task>>
    suspend fun updateTask(task: Task): Flow<Resource<Task>>
    suspend fun deleteTask(id: String): Flow<Resource<Boolean>>
    suspend fun getPriorityTasks(limit: Int = 5): Flow<Resource<List<Task>>>
    suspend fun getTasksDueToday(): Flow<Resource<List<Task>>>
} 