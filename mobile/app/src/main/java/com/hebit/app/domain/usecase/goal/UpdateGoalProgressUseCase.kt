package com.hebit.app.domain.usecase.goal

import com.hebit.app.domain.model.Goal
import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateGoalProgressUseCase @Inject constructor(
    private val repository: GoalRepository
) {
    suspend operator fun invoke(goalId: String, progress: Int): Flow<Resource<Goal>> {
        // Basic validation could be added here (e.g., progress between 0 and 100)
        return repository.updateGoalProgress(goalId, progress)
    }
} 