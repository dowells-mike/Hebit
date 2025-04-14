package com.hebit.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hebit.app.data.remote.dto.ProductivityInsightsResponse
import com.hebit.app.data.remote.dto.ProductivityMetricsDto
import com.hebit.app.domain.model.Habit
import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.repository.HabitRepository
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

/**
 * ViewModel for the Progress Stats screen, providing data for productivity metrics and habit streaks
 */
@HiltViewModel
class ProgressStatsViewModel @Inject constructor(
    private val productivityRepository: IProductivityRepository,
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressStatsUiState())
    val uiState: StateFlow<ProgressStatsUiState> = _uiState.asStateFlow()

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    init {
        loadData(TimePeriod.Week)
    }

    fun loadData(period: TimePeriod) {
        _uiState.update { it.copy(
            isLoading = true,
            selectedPeriod = period,
            error = null
        )}

        // Convert period to date range
        val (fromDate, toDate) = getDateRangeForPeriod(period)
        
        // Load productivity metrics
        loadProductivityMetrics(fromDate, toDate)
        
        // Load productivity insights
        loadProductivityInsights(period.name.lowercase())
        
        // Load active streaks
        loadActiveStreaks()
    }

    private fun loadProductivityMetrics(fromDate: LocalDate, toDate: LocalDate) {
        viewModelScope.launch {
            val fromDateStr = fromDate.format(dateFormatter)
            val toDateStr = toDate.format(dateFormatter)
            
            productivityRepository.getProductivityMetrics(fromDateStr, toDateStr)
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _uiState.update { it.copy(
                                metrics = result.data ?: emptyList(),
                                isLoading = false
                            )}
                            
                            // Calculate average productivity score
                            val avgScore = result.data?.let { metrics ->
                                if (metrics.isNotEmpty()) {
                                    metrics.map { it.productivityScore }.average().toInt()
                                } else null
                            }
                            
                            // Calculate task completion data
                            val taskData = result.data?.let { metrics ->
                                metrics.map { it.tasksCompleted }
                            } ?: emptyList()
                            
                            _uiState.update { it.copy(
                                productivityScore = avgScore,
                                taskCompletionData = taskData
                            )}
                        }
                        is Resource.Error -> {
                            _uiState.update { it.copy(
                                error = result.message,
                                isLoading = false
                            )}
                        }
                        is Resource.Loading -> {
                            // Already set loading state
                        }
                    }
                }
        }
    }

    private fun loadProductivityInsights(period: String) {
        viewModelScope.launch {
            productivityRepository.getProductivityInsights(period)
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _uiState.update { it.copy(
                                insights = result.data,
                                isLoading = false
                            )}
                            
                            // Update time distribution if insights have this data
                            result.data?.let { insights ->
                                // This is an example - your actual API might have different data
                                val timeDistribution = mapOf(
                                    "Tasks" to 0.4f, // 40%
                                    "Habits" to 0.3f, // 30%
                                    "Goals" to 0.2f,  // 20%
                                    "Other" to 0.1f   // 10%
                                )
                                
                                _uiState.update { it.copy(
                                    timeDistribution = timeDistribution
                                )}
                            }
                        }
                        is Resource.Error -> {
                            _uiState.update { it.copy(
                                error = result.message,
                                isLoading = false
                            )}
                        }
                        is Resource.Loading -> {
                            // Already set loading state
                        }
                    }
                }
        }
    }

    private fun loadActiveStreaks() {
        viewModelScope.launch {
            habitRepository.getHabits()
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            val activeStreaks = result.data
                                ?.filter { it.streak > 0 }
                                ?.sortedByDescending { it.streak }
                                ?.take(3)
                                ?: emptyList()
                                
                            _uiState.update { it.copy(
                                activeStreaks = activeStreaks,
                                isLoading = false
                            )}
                        }
                        is Resource.Error -> {
                            _uiState.update { it.copy(
                                error = result.message,
                                isLoading = false
                            )}
                        }
                        is Resource.Loading -> {
                            // Already set loading state
                        }
                    }
                }
        }
    }
    
    private fun getDateRangeForPeriod(period: TimePeriod): Pair<LocalDate, LocalDate> {
        val today = LocalDate.now()
        return when (period) {
            TimePeriod.Day -> today.minusDays(1) to today
            TimePeriod.Week -> today.minusDays(7) to today
            TimePeriod.Month -> today.minusDays(30) to today
        }
    }
    
    fun getTrendText(score: Int?): String {
        // Compare with previous period - this is simplified
        // In a real app, you'd calculate this from the actual data
        score ?: return ""
        val insights = _uiState.value.insights
        
        return if (insights != null) {
            val avgScore = insights.averageProductivityScore.toInt()
            val diff = score - avgScore
            
            if (diff > 0) {
                "+${diff}% above average"
            } else if (diff < 0) {
                "${diff}% below average"
            } else {
                "On average"
            }
        } else {
            ""
        }
    }
}

/**
 * UI state for the Progress Stats screen
 */
data class ProgressStatsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedPeriod: TimePeriod = TimePeriod.Week,
    val metrics: List<ProductivityMetricsDto> = emptyList(),
    val insights: ProductivityInsightsResponse? = null,
    val productivityScore: Int? = null,
    val taskCompletionData: List<Int> = emptyList(),
    val timeDistribution: Map<String, Float> = emptyMap(),
    val activeStreaks: List<Habit> = emptyList()
) 