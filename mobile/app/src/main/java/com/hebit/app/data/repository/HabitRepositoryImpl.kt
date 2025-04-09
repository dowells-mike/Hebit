package com.hebit.app.data.repository

import com.hebit.app.data.remote.api.HebitApiService
import com.hebit.app.data.remote.dto.CreateHabitRequest
import com.hebit.app.data.remote.dto.CreateNoteRequest
import com.hebit.app.data.remote.dto.HabitCompletionRequest
import com.hebit.app.data.remote.dto.HabitDto
import com.hebit.app.data.remote.dto.UpdateHabitRequest
import com.hebit.app.data.remote.dto.HabitStatsDto
import com.hebit.app.data.remote.dto.NoteDto
import com.hebit.app.domain.model.Habit
import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.model.CompletionHistoryEntry
import com.hebit.app.domain.model.HabitStats
import com.hebit.app.domain.model.Note
import com.hebit.app.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import java.util.UUID

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
                iconName = habit.iconName ?: "default_icon",
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
                val responseBody = response.body()!!
                android.util.Log.d("HabitRepo", "Today's habits API response: ${responseBody.habits.size} habits found")
                android.util.Log.d("HabitRepo", "Response details: total=${responseBody.total}, page=${responseBody.page}, perPage=${responseBody.perPage}")
                
                val habits = responseBody.habits.map { mapHabitDtoToDomain(it) }
                android.util.Log.d("HabitRepo", "Mapped to ${habits.size} domain habits")
                emit(Resource.Success(habits))
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                android.util.Log.e("HabitRepo", "Error fetching today's habits: $errorMessage")
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

    override suspend fun completeHabitForToday(id: String, completed: Boolean): Flow<Resource<Habit>> = flow {
        emit(Resource.Loading())
        
        try {
            val currentDate = java.time.LocalDate.now().toString()
            val request = HabitCompletionRequest(completed = completed, date = currentDate)
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

    override suspend fun getHabitStats(id: String): Flow<Resource<HabitStats>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getHabitStats(id)
            
            if (response.isSuccessful && response.body() != null) {
                val statsDomain = mapHabitStatsDtoToDomain(response.body()!!)
                emit(Resource.Success(statsDomain))
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Failed to get habit stats"
                emit(Resource.Error(errorMessage))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Server error fetching stats: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error fetching stats: ${e.localizedMessage ?: "Check connection"}"))
        } catch (e: Exception) {
            emit(Resource.Error("Unexpected error fetching stats: ${e.localizedMessage ?: "Unknown error"}"))
        }
    }
    
    private fun mapHabitDtoToDomain(dto: HabitDto): Habit {
        // Check if the habit is completed today using completion history
        val completedToday = if (dto.completedToday != null) {
            dto.completedToday
        } else {
            // Check completionHistory if completed_today field is missing
            val today = java.time.LocalDate.now().toString()
            dto.completionHistory.any { entry ->
                val entryDate = entry.date.split("T")[0] // Get YYYY-MM-DD part
                entryDate == today && entry.completed
            }
        }

        // Use current time as fallback for missing date fields
        val now = LocalDateTime.now()
        
        return Habit(
            id = dto.id,
            title = dto.title,
            description = dto.description,
            iconName = dto.iconName,
            frequency = dto.frequency,
            completedToday = completedToday,
            streak = dto.streak,
            completionHistory = dto.completionHistory.mapNotNull { entryDto ->
                try {
                    CompletionHistoryEntry(
                        date = LocalDateTime.parse(entryDto.date, dateFormatter),
                        completed = entryDto.completed,
                        value = entryDto.value,
                        notes = entryDto.notes,
                        mood = entryDto.mood,
                        skipReason = entryDto.skipReason
                    )
                } catch (e: Exception) {
                    null
                }
            },
            createdAt = if (dto.createdAt != null) LocalDateTime.parse(dto.createdAt, dateFormatter) else now,
            updatedAt = if (dto.updatedAt != null) LocalDateTime.parse(dto.updatedAt, dateFormatter) else now
        )
    }

    private fun mapHabitStatsDtoToDomain(dto: HabitStatsDto): HabitStats {
        return HabitStats(
            currentStreak = dto.currentStreak,
            longestStreak = dto.longestStreak,
            completionRate = dto.completionRate / 100f, // Convert from 0-100 to 0-1
            totalCompletions = dto.completedEntries,
            completionsByDay = dto.completionsByDay,
            completionsByTime = dto.completionsByTime,
            consistency = dto.consistency / 100f // Convert from 0-100 to 0-1
        )
    }

    override fun getNotesForHabit(habitId: String): Flow<Resource<List<Note>>> = flow {
        emit(Resource.Loading())

        try {
            val response = apiService.getNotesForHabit(habitId)
            
            if (response.isSuccessful && response.body() != null) {
                val notes = response.body()!!.notes.map { mapNoteDtoToDomain(it) }
                emit(Resource.Success(notes))
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Failed to fetch notes"
                emit(Resource.Error(message = errorMessage, data = emptyList()))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(message = "HTTP Error: ${e.message()}", data = emptyList()))
        } catch (e: IOException) {
            emit(Resource.Error(message = "Network Error: Could not reach server.", data = emptyList()))
        } catch (e: Exception) {
            emit(Resource.Error(message = "An unexpected error occurred: ${e.localizedMessage}", data = emptyList()))
        }
    }

    override fun addNoteForHabit(habitId: String, content: String): Flow<Resource<Note>> = flow {
        emit(Resource.Loading())

        try {
            if (content.isBlank()) {
                throw IllegalArgumentException("Note content cannot be empty.")
            }
            
            val request = CreateNoteRequest(habitId = habitId, content = content)
            val response = apiService.addNoteForHabit(request)
            
            if (response.isSuccessful && response.body() != null) {
                val createdNote = mapNoteDtoToDomain(response.body()!!)
                emit(Resource.Success(createdNote))
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Failed to add note"
                emit(Resource.Error(message = errorMessage))
            }
        } catch(e: IllegalArgumentException) {
            emit(Resource.Error(message = e.message ?: "Invalid input."))
        } catch (e: HttpException) {
            emit(Resource.Error(message = "HTTP Error: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error(message = "Network Error: Could not save note."))
        } catch (e: Exception) {
            emit(Resource.Error(message = "An unexpected error occurred: ${e.localizedMessage}"))
        }
    }
    
    private fun mapNoteDtoToDomain(dto: NoteDto): Note {
        return Note(
            id = dto.id,
            habitId = dto.habitId,
            content = dto.content,
            createdAt = LocalDateTime.parse(dto.createdAt, dateFormatter)
        )
    }
} 