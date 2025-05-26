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

    // Default to fetching weekly stats for now
    init {
        fetchTaskStatistics(period = "week")
        fetchProductivityScore(period = "week")
        // fetchScoreHistory(periodType = "daily", count = 7) // History is placeholder for now
    }

    fun fetchTaskStatistics(period: String? = null, startDate: String? = null, endDate: String? = null) {
        viewModelScope.launch {
            taskRepository.getTaskStatistics(period, startDate, endDate).collect {
                _taskStatisticsState.value = it
            }
        }
    }

    fun fetchProductivityScore(period: String? = null, startDate: String? = null, endDate: String? = null) {
        viewModelScope.launch {
            taskRepository.getProductivityScore(period, startDate, endDate).collect {
                _productivityScoreState.value = it
            }
        }
    }

    fun fetchScoreHistory(periodType: String? = null, count: Int? = null) {
        viewModelScope.launch {
            taskRepository.getScoreHistory(periodType, count).collect {
                _scoreHistoryState.value = it
            }
        }
    }

    fun refreshStats(period: String? = "week", startDate: String? = null, endDate: String? = null) {
        fetchTaskStatistics(period, startDate, endDate)
        fetchProductivityScore(period, startDate, endDate)
        // Optionally fetch score history if it becomes non-placeholder
        // fetchScoreHistory(periodType = if (period == "week") "daily" else period, count = 7)
    }
} 