package com.hebit.app.domain.usecase.goal

import com.hebit.app.domain.model.Goal
import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateGoalUseCase @Inject constructor(
    private val repository: GoalRepository
) {
    /**
     * Updates a goal with all its attributes
     * Used for complete goal updates, including marking a goal as completed
     */
    suspend operator fun invoke(goal: Goal): Flow<Resource<Goal>> {
        return repository.updateGoal(goal)
    }
} 