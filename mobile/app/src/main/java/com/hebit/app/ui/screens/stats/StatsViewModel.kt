package com.hebit.app.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hebit.app.data.remote.dto.ProductivityScoreResponseDto
import com.hebit.app.data.remote.dto.ScoreHistoryResponseDto
import com.hebit.app.data.remote.dto.TaskStatisticsResponseDto
import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _taskStatisticsState = MutableStateFlow<Resource<TaskStatisticsResponseDto>>(Resource.Loading())
    val taskStatisticsState: StateFlow<Resource<TaskStatisticsResponseDto>> = _taskStatisticsState.asStateFlow()

    private val _productivityScoreState = MutableStateFlow<Resource<ProductivityScoreResponseDto>>(Resource.Loading())
    val productivityScoreState: StateFlow<Resource<ProductivityScoreResponseDto>> = _productivityScoreState.asStateFlow()

    private val _scoreHistoryState = MutableStateFlow<Resource<ScoreHistoryResponseDto>>(Resource.Loading())
    val scoreHistoryState: StateFlow<Resource<ScoreHistoryResponseDto>> = _scoreHistoryState.asStateFlow()

    init {
        refreshStats("week") // Initial load with weekly stats
    }

    fun refreshStats(period: String? = null, startDate: String? = null, endDate: String? = null) {
        fetchTaskStatistics(period, startDate, endDate)
        fetchProductivityScore(period, startDate, endDate)
        // Fetch default history (e.g., last 7 days) or based on current period selection
        // For simplicity, let's fetch last 7 days daily history initially or when period is 'week' or 'today'
        // More complex logic can be added later if a separate history period selector is introduced.
        fetchScoreHistory("daily", 7)
    }

    private fun fetchTaskStatistics(period: String?, startDate: String?, endDate: String?) {
        viewModelScope.launch {
            _taskStatisticsState.value = Resource.Loading()
            taskRepository.getTaskStatistics(period, startDate, endDate).collect {
                _taskStatisticsState.value = it
            }
        }
    }

    private fun fetchProductivityScore(period: String?, startDate: String?, endDate: String?) {
        viewModelScope.launch {
            _productivityScoreState.value = Resource.Loading()
            taskRepository.getProductivityScore(period, startDate, endDate).collect {
                _productivityScoreState.value = it
            }
        }
    }

    fun fetchScoreHistory(periodType: String, count: Int) {
        viewModelScope.launch {
            _scoreHistoryState.value = Resource.Loading()
            taskRepository.getScoreHistory(periodType, count).collect {
                _scoreHistoryState.value = it
            }
        }
    }
} 