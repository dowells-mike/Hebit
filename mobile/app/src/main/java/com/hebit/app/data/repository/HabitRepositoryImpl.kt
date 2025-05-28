package com.hebit.app.data.repository

import com.hebit.app.data.mapper.*
import com.hebit.app.data.remote.api.HebitApiService
import com.hebit.app.data.remote.dto.CreateHabitRequest // Ensure this DTO is defined correctly
import com.hebit.app.data.remote.dto.CreateNoteRequest
import com.hebit.app.data.remote.dto.HabitFrequencyConfigDto
import com.hebit.app.data.remote.dto.ReminderSettingsDto
import com.hebit.app.data.remote.dto.SuccessCriteriaDto
import com.hebit.app.data.remote.dto.TimePreferenceDto
import com.hebit.app.data.remote.dto.UpdateHabitRequest // Ensure this DTO is defined correctly
import com.hebit.app.domain.model.*
import com.hebit.app.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepositoryImpl @Inject constructor(
    private val apiService: HebitApiService
) : HabitRepository {

    override fun getHabits(): Flow<Resource<List<Habit>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getHabits()
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!.habits.toDomainModels()))
            } else {
                emit(Resource.Error(response.errorBody()?.string() ?: "Failed to fetch habits"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Server error while fetching habits: ${e.code()} ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error while fetching habits: ${e.localizedMessage ?: "Check connection"}"))
        } catch (e: Exception) {
            emit(Resource.Error("Unexpected error while fetching habits: ${e.localizedMessage}"))
        }
    }

    override fun getHabitById(id: String): Flow<Resource<Habit?>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getHabitById(id)
            if (response.isSuccessful) {
                emit(Resource.Success(response.body()?.toDomain())) // body() can be null for 204 or if DTO is nullable
            } else {
                if (response.code() == 404) {
                    emit(Resource.Success(null)) // Explicitly handle 404 as habit not found
                } else {
                    emit(Resource.Error(response.errorBody()?.string() ?: "Failed to fetch habit $id"))
                }
            }
        } catch (e: HttpException) { // Should be caught by above if (response.code == 404)
            if (e.code() == 404) {
                emit(Resource.Success(null))
            } else {
                emit(Resource.Error("Server error fetching habit $id: ${e.code()} ${e.message()}"))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network error fetching habit $id: ${e.localizedMessage ?: "Check connection"}"))
        } catch (e: Exception) {
            emit(Resource.Error("Unexpected error fetching habit $id: ${e.localizedMessage}"))
        }
    }

    override fun getHabitStats(id: String): Flow<Resource<HabitStats?>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getHabitStats(id)
            if (response.isSuccessful) {
                emit(Resource.Success(response.body()?.toDomain()))
            } else {
                if (response.code() == 404) {
                    emit(Resource.Success(null))
                } else {
                    emit(Resource.Error(response.errorBody()?.string() ?: "Failed to fetch habit stats for $id"))
                }
            }
        } catch (e: HttpException) {
            if (e.code() == 404) {
                emit(Resource.Success(null))
            } else {
                emit(Resource.Error("Server error fetching stats for $id: ${e.code()} ${e.message()}"))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network error fetching stats for $id: ${e.localizedMessage ?: "Check connection"}"))
        } catch (e: Exception) {
            emit(Resource.Error("Unexpected error fetching stats for $id: ${e.localizedMessage}"))
        }
    }

    override fun createHabit(habit: Habit): Flow<Resource<Habit>> = flow {
        emit(Resource.Loading())
        try {
            val createRequest = habit.toCreateHabitRequestDto()
            val response = apiService.createHabit(createRequest)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!.toDomain()))
            } else {
                emit(Resource.Error(response.errorBody()?.string() ?: "Failed to create habit"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Server error creating habit: ${e.code()} ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error creating habit: ${e.localizedMessage ?: "Check connection"}"))
        } catch (e: Exception) {
            emit(Resource.Error("Unexpected error creating habit: ${e.localizedMessage}"))
        }
    }

    override fun updateHabit(habit: Habit): Flow<Resource<Habit>> = flow {
        emit(Resource.Loading())
        try {
            val updateRequest = habit.toUpdateHabitRequestDto()
            val response = apiService.updateHabit(habit.id, updateRequest)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!.toDomain()))
            } else {
                emit(Resource.Error(response.errorBody()?.string() ?: "Failed to update habit ${habit.id}"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Server error updating habit ${habit.id}: ${e.code()} ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error updating habit ${habit.id}: ${e.localizedMessage ?: "Check connection"}"))
        } catch (e: Exception) {
            emit(Resource.Error("Unexpected error updating habit ${habit.id}: ${e.localizedMessage}"))
        }
    }

    override fun trackHabit(habitId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.trackHabit(habitId)
            if (response.isSuccessful) {
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error(response.errorBody()?.string() ?: "Failed to track habit $habitId", false))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Server error tracking habit $habitId: ${e.code()} ${e.message()}", false))
        } catch (e: IOException) {
            emit(Resource.Error("Network error tracking habit $habitId: ${e.localizedMessage ?: "Check connection"}", false))
        } catch (e: Exception) {
            emit(Resource.Error("Unexpected error tracking habit $habitId: ${e.localizedMessage}", false))
        }
    }

    override fun skipHabit(habitId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.skipHabit(habitId)
            if (response.isSuccessful) {
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error(response.errorBody()?.string() ?: "Failed to skip habit $habitId", false))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Server error skipping habit $habitId: ${e.code()} ${e.message()}", false))
        } catch (e: IOException) {
            emit(Resource.Error("Network error skipping habit $habitId: ${e.localizedMessage ?: "Check connection"}", false))
        } catch (e: Exception) {
            emit(Resource.Error("Unexpected error skipping habit $habitId: ${e.localizedMessage}", false))
        }
    }

    override fun deleteHabit(id: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.deleteHabit(id)
            if (response.isSuccessful) {
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error(response.errorBody()?.string() ?: "Failed to delete habit $id", false))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Server error deleting habit $id: ${e.code()} ${e.message()}", false))
        } catch (e: IOException) {
            emit(Resource.Error("Network error deleting habit $id: ${e.localizedMessage ?: "Check connection"}", false))
        } catch (e: Exception) {
            emit(Resource.Error("Unexpected error deleting habit $id: ${e.localizedMessage}", false))
        }
    }

    override fun getNotesForHabit(habitId: String): Flow<Resource<List<Note>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getNotesForHabit(habitId)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!.notes.toNoteDomainModels())) // Corrected
            } else {
                emit(Resource.Error(response.errorBody()?.string() ?: "Failed to get notes for habit $habitId"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Server error getting notes for $habitId: ${e.code()} ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error getting notes for $habitId: ${e.localizedMessage}"))
        } catch (e: Exception) {
            emit(Resource.Error("Unexpected error getting notes for $habitId: ${e.localizedMessage}"))
        }
    }

    override fun addNoteForHabit(habitId: String, content: String): Flow<Resource<Note>> = flow {
        emit(Resource.Loading())
        try {
            val request = CreateNoteRequest(content = content)
            val response = apiService.addNoteForHabit(habitId, request)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!.toDomain())) // Corrected
            } else {
                emit(Resource.Error(response.errorBody()?.string() ?: "Failed to add note for habit $habitId"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Server error adding note for $habitId: ${e.code()} ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error adding note for $habitId: ${e.localizedMessage}"))
        } catch (e: Exception) {
            emit(Resource.Error("Unexpected error adding note for $habitId: ${e.localizedMessage}"))
        }
    }

    // --- Placeholder Implementations for new methods ---
    override fun getPerformanceInsights(habitId: String): Flow<Resource<List<HabitPerformanceInsight>>> = flow {
        emit(Resource.Loading())
        // Simulate network delay and success with empty list
        kotlinx.coroutines.delay(500) // Simulate delay
        emit(Resource.Success(emptyList()))
        // TODO: Replace with actual API call when available
        // try {
        //     val response = apiService.getPerformanceInsights(habitId) // Assuming endpoint exists
        //     if (response.isSuccessful && response.body() != null) {
        //         // Assuming response.body() is a list of DTOs that can be mapped
        //         // emit(Resource.Success(response.body()!!.map { it.toDomain() }))
        //         emit(Resource.Success(emptyList())) // Placeholder
        //     } else {
        //         emit(Resource.Error(response.errorBody()?.string() ?: "Failed to get performance insights for $habitId"))
        //     }
        // } catch (e: HttpException) {
        //     emit(Resource.Error("Server error getting insights for $habitId: ${e.code()} ${e.message()}"))
        // } catch (e: IOException) {
        //     emit(Resource.Error("Network error getting insights for $habitId: ${e.localizedMessage}"))
        // } catch (e: Exception) {
        //     emit(Resource.Error("Unexpected error getting insights for $habitId: ${e.localizedMessage}"))
        // }
    }

    override fun getRelatedAchievements(habitId: String): Flow<Resource<List<HabitAchievement>>> = flow {
        emit(Resource.Loading())
        kotlinx.coroutines.delay(500) // Simulate delay
        emit(Resource.Success(emptyList()))
        // TODO: Replace with actual API call
    }

    override fun getSuggestionsForHabit(habitId: String): Flow<Resource<List<HabitSuggestion>>> = flow {
        emit(Resource.Loading())
        kotlinx.coroutines.delay(500) // Simulate delay
        emit(Resource.Success(emptyList()))
        // TODO: Replace with actual API call
    }

    // --- Helper: Domain Habit to CreateHabitRequest DTO ---
    private fun Habit.toCreateHabitRequestDto(): CreateHabitRequest {
        return CreateHabitRequest(
            title = this.title,
            description = this.description,
            icon = this.icon,
            color = this.color,
            goalLink = this.goalLink,
            frequency = this.frequency.type.name.lowercase(),
            frequencyConfig = this.frequency.toHabitFrequencyConfigDto(),
            timePreference = this.timePreference?.toTimePreferenceDto(),
            category = this.category,
            difficulty = this.difficulty.name.lowercase(),
            startDate = this.startDate?.format(DateTimeFormatter.ISO_LOCAL_DATE),
            endDate = this.endDate?.format(DateTimeFormatter.ISO_LOCAL_DATE),
            reminderSettings = this.reminderSettings?.toReminderSettingsDto(),
            successCriteria = this.successCriteria?.toSuccessCriteriaDto()
        )
    }

    private fun Habit.toUpdateHabitRequestDto(): UpdateHabitRequest {
        return UpdateHabitRequest(
            title = this.title,
            description = this.description,
            icon = this.icon,
            color = this.color,
            goalLink = this.goalLink,
            frequency = this.frequency.type.name.lowercase(),
            frequencyConfig = this.frequency.toHabitFrequencyConfigDto(),
            timePreference = this.timePreference?.toTimePreferenceDto(),
            category = this.category,
            difficulty = this.difficulty.name.lowercase(),
            startDate = this.startDate?.format(DateTimeFormatter.ISO_LOCAL_DATE),
            endDate = this.endDate?.format(DateTimeFormatter.ISO_LOCAL_DATE),
            reminderSettings = this.reminderSettings?.toReminderSettingsDto(),
            successCriteria = this.successCriteria?.toSuccessCriteriaDto()
        )
    }

    private fun HabitFrequency.toHabitFrequencyConfigDto(): HabitFrequencyConfigDto {
        return HabitFrequencyConfigDto(
            daysOfWeek = this.daysOfWeek?.map { it.ordinal },
            datesOfMonth = this.datesOfMonth,
            timesPerPeriod = this.timesPerPeriod,
            specificDates = this.specificDates?.map { it.format(DateTimeFormatter.ISO_LOCAL_DATE) }
        )
    }

    private fun HabitTimePreference.toTimePreferenceDto(): TimePreferenceDto {
        return TimePreferenceDto(
            preferredTime = this.preferredTime?.format(DateTimeFormatter.ISO_LOCAL_TIME),
            flexibility = this.flexibilityMinutes
        )
    }

    private fun HabitReminderSettings.toReminderSettingsDto(): ReminderSettingsDto {
        return ReminderSettingsDto(
            time = this.time?.format(DateTimeFormatter.ISO_LOCAL_TIME),
            customMessage = this.customMessage,
            notificationStyle = this.notificationStyle.name.lowercase()
        )
    }

    private fun HabitSuccessCriteria.toSuccessCriteriaDto(): SuccessCriteriaDto {
        return SuccessCriteriaDto(
            type = this.type.name.lowercase(),
            target = this.target,
            unit = this.unit,
            minimumThreshold = this.minimumThreshold
        )
    }
}