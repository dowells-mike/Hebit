package com.hebit.app.domain.repository

import com.hebit.app.data.remote.dto.ProductivityInsightsResponse
import com.hebit.app.data.remote.dto.ProductivityMetricsDto
import com.hebit.app.domain.model.Resource

/**
 * Interface for productivity operations
 */
interface IProductivityRepository {
    /**
     * Get productivity metrics for a date range
     */
    suspend fun getProductivityMetrics(
        fromDate: String? = null,
        toDate: String? = null
    ): Resource<List<ProductivityMetricsDto>>
    
    /**
     * Track focus time
     */
    suspend fun trackFocusTime(
        minutes: Int,
        taskId: String? = null,
        habitId: String? = null,
        goalId: String? = null,
        notes: String? = null
    ): Resource<ProductivityMetricsDto>
    
    /**
     * Submit day rating
     */
    suspend fun submitDayRating(
        rating: Int,
        notes: String? = null
    ): Resource<ProductivityMetricsDto>
    
    /**
     * Get productivity insights
     */
    suspend fun getProductivityInsights(
        period: String? = null
    ): Resource<ProductivityInsightsResponse>
} 