package com.hebit.app.domain.repository

import com.hebit.app.domain.model.Goal
import com.hebit.app.domain.model.Resource
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    suspend fun getGoals(): Flow<Resource<List<Goal>>>
    suspend fun getGoalById(id: String): Flow<Resource<Goal>>
    suspend fun createGoal(goal: Goal): Flow<Resource<Goal>>
    suspend fun updateGoal(goal: Goal): Flow<Resource<Goal>>
    suspend fun deleteGoal(id: String): Flow<Resource<Boolean>>
    suspend fun getActiveGoals(): Flow<Resource<List<Goal>>>
    suspend fun updateGoalProgress(id: String, progress: Int): Flow<Resource<Goal>>
} 