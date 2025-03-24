package com.hebit.app.data.repository

import com.hebit.app.data.remote.api.HebitApiService
import com.hebit.app.data.remote.dto.CreateGoalRequest
import com.hebit.app.data.remote.dto.GoalDto
import com.hebit.app.data.remote.dto.GoalProgressRequest
import com.hebit.app.data.remote.dto.UpdateGoalRequest
import com.hebit.app.domain.model.Goal
import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepositoryImpl @Inject constructor(
    private val apiService: HebitApiService
) : GoalRepository {

    private val dateFormatter = DateTimeFormatter.ISO_DATE

    override suspend fun getGoals(): Flow<Resource<List<Goal>>> = flow {
        emit(Resource.Loading())
        
        try {
            val response = apiService.getGoals()
            
            if (response.isSuccessful && response.body() != null) {
                val goals = response.body()!!.goals.map { mapGoalDtoToDomain(it) }
                emit(Resource.Success(goals))
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

    override suspend fun getGoalById(id: String): Flow<Resource<Goal>> = flow {
        emit(Resource.Loading())
        
        try {
            val response = apiService.getGoalById(id)
            
            if (response.isSuccessful && response.body() != null) {
                val goal = mapGoalDtoToDomain(response.body()!!)
                emit(Resource.Success(goal))
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

    override suspend fun createGoal(goal: Goal): Flow<Resource<Goal>> = flow {
        emit(Resource.Loading())
        
        try {
            val createGoalRequest = CreateGoalRequest(
                title = goal.title,
                description = goal.description,
                targetDate = goal.targetDate.format(dateFormatter),
                category = goal.category
            )
            
            val response = apiService.createGoal(createGoalRequest)
            
            if (response.isSuccessful && response.body() != null) {
                val createdGoal = mapGoalDtoToDomain(response.body()!!)
                emit(Resource.Success(createdGoal))
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

    override suspend fun updateGoal(goal: Goal): Flow<Resource<Goal>> = flow {
        emit(Resource.Loading())
        
        try {
            val updateGoalRequest = UpdateGoalRequest(
                title = goal.title,
                description = goal.description,
                targetDate = goal.targetDate.format(dateFormatter),
                category = goal.category,
                progress = goal.progress,
                isCompleted = goal.isCompleted
            )
            
            val response = apiService.updateGoal(goal.id, updateGoalRequest)
            
            if (response.isSuccessful && response.body() != null) {
                val updatedGoal = mapGoalDtoToDomain(response.body()!!)
                emit(Resource.Success(updatedGoal))
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

    override suspend fun deleteGoal(id: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        
        try {
            val response = apiService.deleteGoal(id)
            
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

    override suspend fun getActiveGoals(): Flow<Resource<List<Goal>>> = flow {
        emit(Resource.Loading())
        
        try {
            val response = apiService.getActiveGoals()
            
            if (response.isSuccessful && response.body() != null) {
                val goals = response.body()!!.goals.map { mapGoalDtoToDomain(it) }
                emit(Resource.Success(goals))
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

    override suspend fun updateGoalProgress(id: String, progress: Int): Flow<Resource<Goal>> = flow {
        emit(Resource.Loading())
        
        try {
            val request = GoalProgressRequest(progress = progress)
            val response = apiService.updateGoalProgress(id, request)
            
            if (response.isSuccessful && response.body() != null) {
                val goal = mapGoalDtoToDomain(response.body()!!)
                emit(Resource.Success(goal))
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
    
    private fun mapGoalDtoToDomain(dto: GoalDto): Goal {
        return Goal(
            id = dto.id,
            title = dto.title,
            description = dto.description,
            progress = dto.progress,
            targetDate = LocalDate.parse(dto.targetDate, dateFormatter),
            category = dto.category,
            isCompleted = dto.isCompleted,
            createdAt = LocalDate.parse(dto.createdAt, dateFormatter),
            updatedAt = LocalDate.parse(dto.updatedAt, dateFormatter)
        )
    }
} 