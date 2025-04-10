package com.hebit.app.ui.screens.goals.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hebit.app.domain.model.Goal
import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.usecase.goal.CreateGoalUseCase
import com.hebit.app.domain.usecase.goal.DeleteGoalUseCase
import com.hebit.app.domain.usecase.goal.GetGoalsUseCase
import com.hebit.app.domain.usecase.goal.UpdateGoalProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class GoalListViewModel @Inject constructor(
    private val getGoalsUseCase: GetGoalsUseCase,
    private val createGoalUseCase: CreateGoalUseCase,
    private val updateGoalProgressUseCase: UpdateGoalProgressUseCase,
    private val deleteGoalUseCase: DeleteGoalUseCase
) : ViewModel() {

    private val _state = mutableStateOf(GoalListState())
    val state: State<GoalListState> = _state

    private val _eventMessage = mutableStateOf<String?>(null)
    val eventMessage: State<String?> = _eventMessage

    init {
        fetchGoals()
    }

    fun consumeEventMessage() {
        _eventMessage.value = null
    }

    private fun fetchGoals() {
        viewModelScope.launch {
            _state.value = GoalListState(isLoading = true)
            getGoalsUseCase().onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                    is Resource.Success -> {
                        _state.value = GoalListState(goals = result.data ?: emptyList())
                    }
                    is Resource.Error -> {
                        _state.value = GoalListState(error = result.message ?: "An unexpected error occurred")
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun createGoal(title: String, description: String, targetDate: LocalDate, category: String) {
        viewModelScope.launch {
            val newGoal = Goal(
                id = "",
                title = title,
                description = description,
                progress = 0,
                targetDate = targetDate,
                category = category.ifBlank { "General" },
                isCompleted = false,
                createdAt = LocalDate.now(),
                updatedAt = LocalDate.now()
            )

            createGoalUseCase(newGoal).onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                    is Resource.Success -> {
                        val updatedGoals = _state.value.goals + (result.data ?: return@onEach)
                        _state.value = _state.value.copy(isLoading = false, goals = updatedGoals, error = "")
                        _eventMessage.value = "Goal created successfully!"
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(isLoading = false, error = result.message ?: "Failed to create goal")
                        _eventMessage.value = result.message ?: "Failed to create goal"
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun updateGoalProgress(goalId: String, progress: Int) {
        viewModelScope.launch {
            updateGoalProgressUseCase(goalId, progress).onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        // Optionally indicate loading state for the specific item being updated
                    }
                    is Resource.Success -> {
                        result.data?.let { updatedGoal ->
                            val currentGoals = _state.value.goals.toMutableList()
                            val index = currentGoals.indexOfFirst { it.id == goalId }
                            if (index != -1) {
                                currentGoals[index] = updatedGoal
                                _state.value = _state.value.copy(goals = currentGoals, error = "")
                            }
                        }
                        _eventMessage.value = "Goal progress updated."
                    }
                    is Resource.Error -> {
                        _eventMessage.value = result.message ?: "Failed to update progress"
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch {
            deleteGoalUseCase(goalId).onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        // Optionally indicate loading state for the specific item being deleted
                    }
                    is Resource.Success -> {
                        if (result.data == true) {
                            val updatedGoals = _state.value.goals.filterNot { it.id == goalId }
                            _state.value = _state.value.copy(goals = updatedGoals, error = "")
                            _eventMessage.value = "Goal deleted successfully."
                        } else {
                            _eventMessage.value = "Failed to delete goal."
                        }
                    }
                    is Resource.Error -> {
                        _eventMessage.value = result.message ?: "Failed to delete goal"
                    }
                }
            }.launchIn(viewModelScope)
        }
    }
}

data class GoalListState(
    val isLoading: Boolean = false,
    val goals: List<Goal> = emptyList(),
    val error: String = ""
) 