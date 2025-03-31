package com.hebit.app.domain.repository

import com.hebit.app.data.remote.dto.ProductivityInsightsResponse
import com.hebit.app.data.remote.dto.ProductivityMetricsDto
import com.hebit.app.domain.model.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Interface for productivity operations
 */
interface IProductivityRepository {
    /**
     * Get productivity metrics for a date range
     */
    fun getProductivityMetrics(
        fromDate: String? = null,
        toDate: String? = null
    ): Flow<Resource<List<ProductivityMetricsDto>>>
    
    /**
     * Track focus time
     */
    fun trackFocusTime(
        minutes: Int,
        taskId: String? = null,
        habitId: String? = null,
        goalId: String? = null,
        notes: String? = null
    ): Flow<Resource<ProductivityMetricsDto>>
    
    /**
     * Submit day rating
     */
    fun submitDayRating(
        rating: Int,
        notes: String? = null
    ): Flow<Resource<ProductivityMetricsDto>>
    
    /**
     * Get productivity insights
     */
    fun getProductivityInsights(
        period: String? = null
    ): Flow<Resource<ProductivityInsightsResponse>>
} 