package com.hebit.app.domain.usecase.goal

import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeleteGoalUseCase @Inject constructor(
    private val repository: GoalRepository
) {
    suspend operator fun invoke(goalId: String): Flow<Resource<Boolean>> {
        return repository.deleteGoal(goalId)
    }
} 