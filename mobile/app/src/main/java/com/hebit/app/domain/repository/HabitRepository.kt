package com.hebit.app.domain.repository

import com.hebit.app.domain.model.Habit
import com.hebit.app.domain.model.Resource
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    suspend fun getHabits(): Flow<Resource<List<Habit>>>
    suspend fun getHabitById(id: String): Flow<Resource<Habit>>
    suspend fun createHabit(habit: Habit): Flow<Resource<Habit>>
    suspend fun updateHabit(habit: Habit): Flow<Resource<Habit>>
    suspend fun deleteHabit(id: String): Flow<Resource<Boolean>>
    suspend fun getTodaysHabits(): Flow<Resource<List<Habit>>>
    suspend fun completeHabitForToday(id: String): Flow<Resource<Habit>>
} 