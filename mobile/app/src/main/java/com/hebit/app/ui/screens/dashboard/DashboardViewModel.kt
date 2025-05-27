package com.hebit.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hebit.app.data.remote.dto.TaskSuggestionDto
import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _taskSuggestionsState = MutableStateFlow<Resource<List<TaskSuggestionDto>>>(Resource.Loading())
    val taskSuggestionsState: StateFlow<Resource<List<TaskSuggestionDto>>> = _taskSuggestionsState.asStateFlow()

    init {
        fetchTaskSuggestions()
    }

    fun fetchTaskSuggestions() { // Made public for manual refresh if needed later
        viewModelScope.launch {
            _taskSuggestionsState.value = Resource.Loading() // Reset to loading before fetching
            taskRepository.getTaskSuggestions().collect {
                _taskSuggestionsState.value = it
            }
        }
    }
} 