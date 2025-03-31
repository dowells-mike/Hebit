package com.hebit.app.data.repository

import com.hebit.app.data.remote.api.HebitApiService
import com.hebit.app.data.remote.dto.DayRatingRequest
import com.hebit.app.data.remote.dto.FocusTimeRequest
import com.hebit.app.data.remote.dto.ProductivityInsightsResponse
import com.hebit.app.data.remote.dto.ProductivityMetricsDto
import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.repository.IProductivityRepository
import com.hebit.app.util.safeApiCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductivityRepository @Inject constructor(
    private val apiService: HebitApiService
) : IProductivityRepository {
    
    override suspend fun getProductivityMetrics(
        fromDate: String?,
        toDate: String?
    ): Resource<List<ProductivityMetricsDto>> = safeApiCall {
        apiService.getProductivityMetrics(fromDate, toDate)
    }
    
    override suspend fun trackFocusTime(
        minutes: Int,
        taskId: String?,
        habitId: String?,
        goalId: String?,
        notes: String?
    ): Resource<ProductivityMetricsDto> = safeApiCall {
        val request = FocusTimeRequest(
            minutes = minutes,
            taskId = taskId,
            habitId = habitId,
            goalId = goalId,
            notes = notes
        )
        apiService.trackFocusTime(request)
    }
    
    override suspend fun submitDayRating(
        rating: Int,
        notes: String?
    ): Resource<ProductivityMetricsDto> = safeApiCall {
        val request = DayRatingRequest(
            rating = rating,
            notes = notes
        )
        apiService.submitDayRating(request)
    }
    
    override suspend fun getProductivityInsights(
        period: String?
    ): Resource<ProductivityInsightsResponse> = safeApiCall {
        apiService.getProductivityInsights(period)
    }
} 