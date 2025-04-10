package com.hebit.app.domain.usecase.goal

import com.hebit.app.domain.model.Goal
import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGoalsUseCase @Inject constructor(
    private val repository: GoalRepository
) {
    suspend operator fun invoke(): Flow<Resource<List<Goal>>> {
        return repository.getGoals()
    }
} 