package com.hebit.app.ui.screens.productivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hebit.app.data.remote.dto.ProductivityInsightsResponse
import com.hebit.app.data.remote.dto.ProductivityMetricsDto
import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.repository.IProductivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class ProductivityUiState(
    val isLoadingMetrics: Boolean = false,
    val isLoadingInsights: Boolean = false,
    val isSubmittingRating: Boolean = false,
    val isTrackingFocusTime: Boolean = false,
    val metrics: List<ProductivityMetricsDto> = emptyList(),
    val insights: ProductivityInsightsResponse? = null,
    val error: String? = null,
    val selectedPeriod: String = "week" // week, month, year
)

@HiltViewModel
class ProductivityViewModel @Inject constructor(
    private val productivityRepository: IProductivityRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProductivityUiState())
    val uiState: StateFlow<ProductivityUiState> = _uiState.asStateFlow()
    
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    
    init {
        getProductivityMetrics()
        getProductivityInsights()
    }
    
    fun getProductivityMetrics(
        fromDate: LocalDate = LocalDate.now().minusDays(30),
        toDate: LocalDate = LocalDate.now()
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMetrics = true) }
            
            val fromDateStr = fromDate.format(dateFormatter)
            val toDateStr = toDate.format(dateFormatter)
            
            when (val result = productivityRepository.getProductivityMetrics(fromDateStr, toDateStr)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoadingMetrics = false,
                            metrics = result.data ?: emptyList(),
                            error = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingMetrics = false,
                            error = result.message
                        )
                    }
                }
                is Resource.Loading -> {
                    _uiState.update {
                        it.copy(
                            isLoadingMetrics = true
                        )
                    }
                }
            }
        }
    }
    
    fun getProductivityInsights(period: String = _uiState.value.selectedPeriod) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingInsights = true, selectedPeriod = period) }
            
            when (val result = productivityRepository.getProductivityInsights(period)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoadingInsights = false,
                            insights = result.data,
                            error = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingInsights = false,
                            error = result.message
                        )
                    }
                }
                is Resource.Loading -> {
                    _uiState.update {
                        it.copy(
                            isLoadingInsights = true
                        )
                    }
                }
            }
        }
    }
    
    fun trackFocusTime(
        duration: Int,
        taskId: String? = null,
        habitId: String? = null,
        goalId: String? = null,
        notes: String? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isTrackingFocusTime = true) }
            
            when (val result = productivityRepository.trackFocusTime(duration, taskId, habitId, goalId, notes)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isTrackingFocusTime = false,
                            error = null
                        )
                    }
                    // Refresh metrics
                    getProductivityMetrics()
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isTrackingFocusTime = false,
                            error = result.message
                        )
                    }
                }
                is Resource.Loading -> {
                    _uiState.update {
                        it.copy(
                            isTrackingFocusTime = true
                        )
                    }
                }
            }
        }
    }
    
    fun submitDayRating(rating: Int, notes: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingRating = true) }
            
            when (val result = productivityRepository.submitDayRating(rating, notes)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmittingRating = false,
                            error = null
                        )
                    }
                    // Refresh metrics
                    getProductivityMetrics()
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmittingRating = false,
                            error = result.message
                        )
                    }
                }
                is Resource.Loading -> {
                    _uiState.update {
                        it.copy(
                            isSubmittingRating = true
                        )
                    }
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
} 