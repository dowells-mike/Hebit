package com.hebit.app.data.repository

import com.hebit.app.data.remote.api.HebitApiService
import com.hebit.app.data.remote.dto.CreateHabitRequest
import com.hebit.app.data.remote.dto.HabitCompletionRequest
import com.hebit.app.data.remote.dto.HabitDto
import com.hebit.app.data.remote.dto.UpdateHabitRequest
import com.hebit.app.domain.model.Habit
import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepositoryImpl @Inject constructor(
    private val apiService: HebitApiService
) : HabitRepository {

    private val dateFormatter = DateTimeFormatter.ISO_DATE_TIME

    override suspend fun getHabits(): Flow<Resource<List<Habit>>> = flow {
        emit(Resource.Loading())
        
        try {
            val response = apiService.getHabits()
            
            if (response.isSuccessful && response.body() != null) {
                val habits = response.body()!!.habits.map { mapHabitDtoToDomain(it) }
                emit(Resource.Success(habits))
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

    override suspend fun getHabitById(id: String): Flow<Resource<Habit>> = flow {
        emit(Resource.Loading())
        
        try {
            val response = apiService.getHabitById(id)
            
            if (response.isSuccessful && response.body() != null) {
                val habit = mapHabitDtoToDomain(response.body()!!)
                emit(Resource.Success(habit))
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

    override suspend fun createHabit(habit: Habit): Flow<Resource<Habit>> = flow {
        emit(Resource.Loading())
        
        try {
            val createHabitRequest = CreateHabitRequest(
                title = habit.title,
                description = habit.description,
                iconName = habit.iconName,
                frequency = habit.frequency
            )
            
            val response = apiService.createHabit(createHabitRequest)
            
            if (response.isSuccessful && response.body() != null) {
                val createdHabit = mapHabitDtoToDomain(response.body()!!)
                emit(Resource.Success(createdHabit))
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

    override suspend fun updateHabit(habit: Habit): Flow<Resource<Habit>> = flow {
        emit(Resource.Loading())
        
        try {
            val updateHabitRequest = UpdateHabitRequest(
                title = habit.title,
                description = habit.description,
                iconName = habit.iconName,
                frequency = habit.frequency
            )
            
            val response = apiService.updateHabit(habit.id, updateHabitRequest)
            
            if (response.isSuccessful && response.body() != null) {
                val updatedHabit = mapHabitDtoToDomain(response.body()!!)
                emit(Resource.Success(updatedHabit))
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

    override suspend fun deleteHabit(id: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        
        try {
            val response = apiService.deleteHabit(id)
            
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

    override suspend fun getTodaysHabits(): Flow<Resource<List<Habit>>> = flow {
        emit(Resource.Loading())
        
        try {
            val response = apiService.getTodaysHabits()
            
            if (response.isSuccessful && response.body() != null) {
                val habits = response.body()!!.habits.map { mapHabitDtoToDomain(it) }
                emit(Resource.Success(habits))
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

    override suspend fun completeHabitForToday(id: String): Flow<Resource<Habit>> = flow {
        emit(Resource.Loading())
        
        try {
            val request = HabitCompletionRequest(completed = true)
            val response = apiService.completeHabitForToday(id, request)
            
            if (response.isSuccessful && response.body() != null) {
                val habit = mapHabitDtoToDomain(response.body()!!)
                emit(Resource.Success(habit))
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
    
    private fun mapHabitDtoToDomain(dto: HabitDto): Habit {
        return Habit(
            id = dto.id,
            title = dto.title,
            description = dto.description,
            iconName = dto.iconName,
            frequency = dto.frequency,
            completedToday = dto.completedToday,
            streak = dto.streak,
            createdAt = LocalDateTime.parse(dto.createdAt, dateFormatter),
            updatedAt = LocalDateTime.parse(dto.updatedAt, dateFormatter)
        )
    }
} 