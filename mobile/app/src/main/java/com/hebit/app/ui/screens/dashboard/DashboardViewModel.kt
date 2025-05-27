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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    // Holds the raw response from the repository
    private val _rawTaskSuggestionsState = MutableStateFlow<Resource<List<TaskSuggestionDto>>>(Resource.Loading())

    // Holds the IDs of suggestions that have been dismissed in the current session
    private val _dismissedSuggestionIds = MutableStateFlow<Set<String>>(emptySet())

    // Exposes the suggestions that should be displayed (raw list filtered by dismissed IDs)
    val displayedTaskSuggestionsState: StateFlow<Resource<List<TaskSuggestionDto>>> = 
        combine(_rawTaskSuggestionsState, _dismissedSuggestionIds) { resource, dismissedIds ->
            when (resource) {
                is Resource.Success -> {
                    val filteredList = resource.data?.filter { it.id !in dismissedIds } ?: emptyList()
                    Resource.Success(filteredList)
                }
                is Resource.Error -> resource // Pass through error state
                is Resource.Loading -> resource // Pass through loading state
            }
        }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), Resource.Loading())

    init {
        fetchTaskSuggestions()
    }

    fun fetchTaskSuggestions() {
        viewModelScope.launch {
            _dismissedSuggestionIds.value = emptySet() // Clear dismissed IDs on refresh
            _rawTaskSuggestionsState.value = Resource.Loading() // Reset to loading before fetching
            taskRepository.getTaskSuggestions().collect {
                _rawTaskSuggestionsState.value = it
            }
        }
    }

    fun dismissSuggestion(suggestionId: String) {
        _dismissedSuggestionIds.value = _dismissedSuggestionIds.value + suggestionId
    }
} 