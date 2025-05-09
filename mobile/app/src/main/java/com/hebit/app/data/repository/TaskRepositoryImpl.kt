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
import com.squareup.moshi.JsonDataException
import android.util.Log

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val apiService: HebitApiService
) : TaskRepository {

    private val dateFormatter = DateTimeFormatter.ISO_DATE_TIME

    override suspend fun getTasks(): Flow<Resource<List<Task>>> = flow {
        emit(Resource.Loading())
        
        try {
            val response = apiService.getTasks()
            
            if (response.isSuccessful) {
                // Handle direct list response
                val taskDtos = response.body() ?: emptyList()
                val tasks = taskDtos.map { mapTaskDtoToDomain(it) }
                emit(Resource.Success(tasks))
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                emit(Resource.Error(errorMessage))
            }
        } catch (e: JsonDataException) {
            // This might still happen if the array is malformed, keep handling it
            emit(Resource.Error("Error parsing task list: ${e.localizedMessage ?: "Invalid format received"}"))
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
            // Map integer priority to backend string format
            val priorityString = when(task.priority) {
                1 -> "low"
                2 -> "medium"
                3 -> "high"
                else -> "medium" // default
            }
            
            val createTaskRequest = CreateTaskRequest(
                title = task.title,
                description = task.description,
                category = task.category,
                dueDate = task.dueDateTime?.format(dateFormatter),
                priority = priorityString
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
            // Map integer priority to backend string format
            val priorityString = when(task.priority) {
                1 -> "low"
                2 -> "medium"
                3 -> "high"
                else -> "medium" // default
            }
            
            val updateTaskRequest = UpdateTaskRequest(
                title = task.title,
                description = task.description,
                category = task.category,
                dueDate = task.dueDateTime?.format(dateFormatter),
                priority = priorityString,
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

    override suspend fun toggleTaskCompletion(id: String): Flow<Resource<Task>> = flow {
        emit(Resource.Loading())
        try {
            Log.d("TaskRepository", "Sending toggle completion request for task ID: $id")
            val response = apiService.toggleTaskCompletion(id)
            
            if (response.isSuccessful && response.body() != null) {
                val taskDto = response.body()!!
                Log.d("TaskRepository", "Toggle completion response - ID: ${taskDto._id}, completed: ${taskDto.completed}, priority: ${taskDto.priority}")
                
                val updatedTask = mapTaskDtoToDomain(taskDto)
                Log.d("TaskRepository", "Mapped to domain - ID: ${updatedTask.id}, completed: ${updatedTask.isCompleted}")
                emit(Resource.Success(updatedTask))
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = errorBody ?: "Unknown error completing task"
                Log.e("TaskRepository", "Error toggling task: $errorMessage (code: ${response.code()})")
                emit(Resource.Error(errorMessage))
            }
        } catch (e: HttpException) {
            Log.e("TaskRepository", "HTTP error toggling task: ${e.message()}", e)
            emit(Resource.Error("Server error toggling task: ${e.message()}"))
        } catch (e: IOException) {
            Log.e("TaskRepository", "IO error toggling task: ${e.localizedMessage}", e)
            emit(Resource.Error("Network error toggling task: ${e.localizedMessage ?: "Check connection"}"))
        } catch (e: Exception) {
            Log.e("TaskRepository", "Unexpected error toggling task: ${e.localizedMessage}", e)
            emit(Resource.Error("Unexpected error toggling task: ${e.localizedMessage ?: "Unknown error"}"))
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
        // Convert backend string priority to integer priority
        val priorityInt = when(dto.priority.lowercase()) {
            "low" -> 1
            "medium" -> 2
            "high" -> 3
            else -> 2 // Default to medium
        }
        
        return Task(
            id = dto._id,
            title = dto.title,
            description = dto.description,
            category = dto.category,
            dueDateTime = dto.dueDate?.let { LocalDateTime.parse(it, dateFormatter) },
            priority = priorityInt,
            progress = dto.progress ?: 0,
            isCompleted = dto.completed,
            createdAt = LocalDateTime.parse(dto.createdAt, dateFormatter),
            updatedAt = LocalDateTime.parse(dto.updatedAt, dateFormatter)
        )
    }
} 