package com.hebit.app.data.repository

import com.hebit.app.data.remote.api.HebitApiService
import com.hebit.app.data.remote.dto.CreateTaskRequest
import com.hebit.app.data.remote.dto.TaskDto
import com.hebit.app.data.remote.dto.UpdateTaskRequest
import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.model.Task
import com.hebit.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val apiService: HebitApiService
) : TaskRepository {

    private val dateFormatter = DateTimeFormatter.ISO_DATE_TIME

    override suspend fun getTasks(): Flow<Resource<List<Task>>> = flow {
        emit(Resource.Loading())
        
        try {
            val response = apiService.getTasks()
            
            if (response.isSuccessful && response.body() != null) {
                val tasks = response.body()!!.tasks.map { mapTaskDtoToDomain(it) }
                emit(Resource.Success(tasks))
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

    override suspend fun getTaskById(id: String): Flow<Resource<Task>> = flow {
        emit(Resource.Loading())
        
        try {
            val response = apiService.getTaskById(id)
            
            if (response.isSuccessful && response.body() != null) {
                val task = mapTaskDtoToDomain(response.body()!!)
                emit(Resource.Success(task))
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

    override suspend fun createTask(task: Task): Flow<Resource<Task>> = flow {
        emit(Resource.Loading())
        
        try {
            val createTaskRequest = CreateTaskRequest(
                title = task.title,
                description = task.description,
                category = task.category,
                dueDate = task.dueDateTime?.format(dateFormatter),
                priority = task.priority
            )
            
            val response = apiService.createTask(createTaskRequest)
            
            if (response.isSuccessful && response.body() != null) {
                val createdTask = mapTaskDtoToDomain(response.body()!!)
                emit(Resource.Success(createdTask))
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

    override suspend fun updateTask(task: Task): Flow<Resource<Task>> = flow {
        emit(Resource.Loading())
        
        try {
            val updateTaskRequest = UpdateTaskRequest(
                title = task.title,
                description = task.description,
                category = task.category,
                dueDate = task.dueDateTime?.format(dateFormatter),
                priority = task.priority,
                progress = task.progress,
                isCompleted = task.isCompleted
            )
            
            val response = apiService.updateTask(task.id, updateTaskRequest)
            
            if (response.isSuccessful && response.body() != null) {
                val updatedTask = mapTaskDtoToDomain(response.body()!!)
                emit(Resource.Success(updatedTask))
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

    override suspend fun deleteTask(id: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        
        try {
            val response = apiService.deleteTask(id)
            
            if (response.isSuccessful) {
                emit(Resource.Success(true))
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

    override suspend fun getPriorityTasks(limit: Int): Flow<Resource<List<Task>>> = flow {
        emit(Resource.Loading())
        
        try {
            val response = apiService.getPriorityTasks(limit)
            
            if (response.isSuccessful && response.body() != null) {
                val tasks = response.body()!!.tasks.map { mapTaskDtoToDomain(it) }
                emit(Resource.Success(tasks))
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

    override suspend fun getTasksDueToday(): Flow<Resource<List<Task>>> = flow {
        emit(Resource.Loading())
        
        try {
            val response = apiService.getTasksDueToday()
            
            if (response.isSuccessful && response.body() != null) {
                val tasks = response.body()!!.tasks.map { mapTaskDtoToDomain(it) }
                emit(Resource.Success(tasks))
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
    
    private fun mapTaskDtoToDomain(dto: TaskDto): Task {
        return Task(
            id = dto.id,
            title = dto.title,
            description = dto.description,
            category = dto.category,
            dueDateTime = dto.dueDate?.let { LocalDateTime.parse(it, dateFormatter) },
            priority = dto.priority,
            progress = dto.progress,
            isCompleted = dto.isCompleted,
            createdAt = LocalDateTime.parse(dto.createdAt, dateFormatter),
            updatedAt = LocalDateTime.parse(dto.updatedAt, dateFormatter)
        )
    }
} 