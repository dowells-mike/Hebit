package com.hebit.app.ui.screens.goals.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hebit.app.domain.model.Goal
import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.usecase.goal.GetGoalByIdUseCase
import com.hebit.app.domain.usecase.goal.UpdateGoalProgressUseCase
import com.hebit.app.domain.usecase.goal.UpdateGoalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoalDetailViewModel @Inject constructor(
    private val getGoalByIdUseCase: GetGoalByIdUseCase,
    private val updateGoalProgressUseCase: UpdateGoalProgressUseCase,
    private val updateGoalUseCase: UpdateGoalUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = mutableStateOf(GoalDetailState())
    val state: State<GoalDetailState> = _state

    private val _eventMessage = mutableStateOf<String?>(null)
    val eventMessage: State<String?> = _eventMessage

    // Grab the goalId from navigation args
    init {
        savedStateHandle.get<String>("goalId")?.let { goalId ->
            fetchGoalDetails(goalId)
        }
    }

    fun consumeEventMessage() {
        _eventMessage.value = null
    }

    private fun fetchGoalDetails(goalId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            getGoalByIdUseCase(goalId).onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                    is Resource.Success -> {
                        _state.value = GoalDetailState(
                            goal = result.data
                        )
                    }
                    is Resource.Error -> {
                        _state.value = GoalDetailState(
                            error = result.message ?: "An unexpected error occurred"
                        )
                        _eventMessage.value = result.message
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    /**
     * Updates the goal's progress and handles completion logic
     * @param progress The new progress value (0-100)
     */
    fun updateGoalProgress(progress: Int) {
        viewModelScope.launch {
            _state.value.goal?.let { goal ->
                // Validate input
                if (progress < 0 || progress > 100) {
                    _eventMessage.value = "Progress must be between 0 and 100"
                    return@launch
                }

                // Don't allow updating progress if goal is already completed
                if (goal.isCompleted) {
                    _eventMessage.value = "This goal is already completed"
                    return@launch
                }
                
                // Set loading state
                _state.value = _state.value.copy(isLoading = true)
                
                // Update progress first
                updateGoalProgressUseCase(goal.id, progress).onEach { result ->
                    when (result) {
                        is Resource.Loading -> {
                            // Already in loading state
                        }
                        is Resource.Success -> {
                            val updatedGoal = result.data
                            if (updatedGoal == null) {
                                _state.value = _state.value.copy(isLoading = false)
                                _eventMessage.value = "Failed to update progress: No data returned"
                                return@onEach
                            }
                            
                            // Check if goal should be marked as completed (progress = 100%)
                            if (progress == 100 && !updatedGoal.isCompleted) {
                                // Mark goal as completed
                                completeGoal(updatedGoal)
                            } else {
                                // Just update the progress without marking as completed
                                _state.value = _state.value.copy(
                                    goal = updatedGoal,
                                    isLoading = false
                                )
                                _eventMessage.value = "Progress updated successfully"
                            }
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(isLoading = false)
                            _eventMessage.value = result.message ?: "Failed to update progress"
                        }
                    }
                }.launchIn(viewModelScope)
            } ?: run {
                _eventMessage.value = "No goal found to update"
            }
        }
    }
    
    /**
     * Marks a goal as completed
     * @param goal The goal to mark as completed
     */
    private fun completeGoal(goal: Goal) {
        viewModelScope.launch {
            // Create a copy of the goal with isCompleted = true
            val completedGoal = goal.copy(isCompleted = true)
            
            // Call the updateGoal use case
            updateGoalUseCase(completedGoal).onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        // Already in loading state
                    }
                    is Resource.Success -> {
                        result.data?.let { updatedGoal ->
                            _state.value = _state.value.copy(
                                goal = updatedGoal,
                                isLoading = false
                            )
                            _eventMessage.value = "Goal completed! 🎉"
                        } ?: run {
                            _state.value = _state.value.copy(isLoading = false)
                            _eventMessage.value = "Failed to mark goal as completed: No data returned"
                        }
                    }
                    is Resource.Error -> {
                        // We still have the goal with updated progress but we couldn't mark it as completed
                        _state.value = _state.value.copy(
                            goal = goal,
                            isLoading = false
                        )
                        _eventMessage.value = result.message ?: "Progress updated but couldn't mark goal as completed"
                    }
                }
            }.launchIn(viewModelScope)
        }
    }
    
    /**
     * Updates the goal with new information
     * @param updatedGoal The updated goal object
     */
    fun updateGoal(updatedGoal: Goal) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            updateGoalUseCase(updatedGoal).onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        // Already in loading state
                    }
                    is Resource.Success -> {
                        result.data?.let { goal ->
                            _state.value = _state.value.copy(
                                goal = goal,
                                isLoading = false
                            )
                            _eventMessage.value = "Goal updated successfully"
                        } ?: run {
                            _state.value = _state.value.copy(isLoading = false)
                            _eventMessage.value = "Failed to update goal: No data returned"
                        }
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(isLoading = false)
                        _eventMessage.value = result.message ?: "Failed to update goal"
                    }
                }
            }.launchIn(viewModelScope)
        }
    }
    
    /**
     * Refreshes the goal details from the server
     */
    fun refreshGoal() {
        _state.value.goal?.let { goal ->
            fetchGoalDetails(goal.id)
        }
    }
}

data class GoalDetailState(
    val isLoading: Boolean = false,
    val goal: Goal? = null,
    val error: String = ""
) 