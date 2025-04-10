package com.hebit.app.ui.screens.goals.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hebit.app.domain.model.Goal
import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.usecase.goal.GetGoalsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoalListViewModel @Inject constructor(
    private val getGoalsUseCase: GetGoalsUseCase
) : ViewModel() {

    private val _state = mutableStateOf(GoalListState())
    val state: State<GoalListState> = _state

    init {
        fetchGoals()
    }

    private fun fetchGoals() {
        viewModelScope.launch {
            getGoalsUseCase().onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.value = GoalListState(isLoading = true)
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
}

data class GoalListState(
    val isLoading: Boolean = false,
    val goals: List<Goal> = emptyList(),
    val error: String = ""
) 