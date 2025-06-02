package com.hebit.app.data.repository

import com.hebit.app.data.mapper.toDomain
import com.hebit.app.data.mapper.toDto
import com.hebit.app.data.remote.api.HebitApiService
import com.hebit.app.data.remote.dto.CreateHabitRequest
import com.hebit.app.data.remote.dto.UpdateHabitRequest
import com.hebit.app.data.remote.dto.HabitTrackRequest
import com.hebit.app.data.remote.dto.HabitSkipRequest
import com.hebit.app.domain.model.Habit
import com.hebit.app.domain.model.HabitStats
import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.repository.IHabitRepository
import com.hebit.app.domain.model.HabitFrequencyConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepositoryImpl @Inject constructor(
    private val apiService: HebitApiService
) : IHabitRepository {

    override fun getHabits(
        page: Int?,
        perPage: Int?,
        frequency: String?,
        category: String?,
        status: String?
    ): Flow<Resource<List<Habit>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getHabits(
                page = page ?: 1,
                perPage = perPage ?: 20,
                frequency = frequency,
                category = category,
                status = status
            )
            if (response.isSuccessful && response.body() != null) {
                val domainHabits = response.body()!!.habits.map { it.toDomain() }
                emit(Resource.Success(domainHabits))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to get habits"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred while fetching habits"))
        }
    }

    override fun getTodaysHabits(): Flow<Resource<List<Habit>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getTodaysHabits()
            if (response.isSuccessful && response.body() != null) {
                val domainHabits = response.body()!!.habits.map { it.toDomain() }
                emit(Resource.Success(domainHabits))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to get today's habits"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred while fetching today's habits"))
        }
    }

    override fun getHabitById(id: String): Flow<Resource<Habit>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getHabitById(id)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!.toDomain()))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to get habit by ID"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred while fetching habit by ID"))
        }
    }

    override fun createHabit(habit: Habit): Flow<Resource<Habit>> = flow {
        emit(Resource.Loading())
        try {
            val createRequest = CreateHabitRequest(
                title = habit.title,
                description = habit.description,
                icon = habit.icon,
                color = habit.color,
                frequency = habit.frequency.name.lowercase(),
                frequencyConfig = habit.frequencyConfig?.toDto(),
                category = habit.category,
                difficulty = habit.difficulty?.name?.lowercase(),
                impact = habit.impact,
                startDate = habit.startDate?.format(DateTimeFormatter.ISO_LOCAL_DATE),
                endDate = habit.endDate?.format(DateTimeFormatter.ISO_LOCAL_DATE),
                status = habit.status.name.lowercase()
            )
            val response = apiService.createHabit(createRequest)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!.toDomain()))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to create habit"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred while creating habit"))
        }
    }

    override fun updateHabit(habit: Habit): Flow<Resource<Habit>> = flow {
        emit(Resource.Loading())
        try {
            val updateRequest = UpdateHabitRequest(
                title = habit.title,
                description = habit.description,
                icon = habit.icon,
                color = habit.color,
                frequency = habit.frequency.name.lowercase(),
                frequencyConfig = habit.frequencyConfig?.toDto(),
                category = habit.category,
                difficulty = habit.difficulty?.name?.lowercase(),
                impact = habit.impact,
                startDate = habit.startDate?.format(DateTimeFormatter.ISO_LOCAL_DATE),
                endDate = habit.endDate?.format(DateTimeFormatter.ISO_LOCAL_DATE),
                status = habit.status.name.lowercase()
            )
            val response = apiService.updateHabit(habit.id, updateRequest)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!.toDomain()))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to update habit"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred while updating habit"))
        }
    }

    override fun deleteHabit(id: String, permanent: Boolean): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            // Note: The 'permanent' parameter for hard delete is not currently implemented in HebitApiService
            // If needed, the API service should be updated to include @Query("permanent") permanent: Boolean? = null
            val response = apiService.deleteHabit(id)
            if (response.isSuccessful) {
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to delete habit"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred while deleting habit"))
        }
    }

    override fun trackHabit(
        habitId: String,
        completed: Boolean,
        date: String,
        notes: String?
    ): Flow<Resource<Habit>> = flow {
        emit(Resource.Loading())
        try {
            val request = HabitTrackRequest(completed = completed, date = date, notes = notes)
            val response = apiService.trackHabit(habitId, request)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!.toDomain()))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to track habit"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred while tracking habit"))
        }
    }

    override fun skipHabit(
        habitId: String,
        date: String,
        skipReason: String?
    ): Flow<Resource<Habit>> = flow {
        emit(Resource.Loading())
        try {
            val request = HabitSkipRequest(date = date, skipReason = skipReason)
            val response = apiService.skipHabit(habitId, request)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!.toDomain()))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to skip habit"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred while skipping habit"))
        }
    }

    override fun getHabitStats(id: String): Flow<Resource<HabitStats>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getHabitStats(id)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!.toDomain()))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to get habit stats"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred while fetching habit stats"))
        }
    }
}