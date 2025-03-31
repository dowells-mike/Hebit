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
    
    override fun getProductivityMetrics(
        fromDate: String?,
        toDate: String?
    ): Flow<Resource<List<ProductivityMetricsDto>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getProductivityMetrics(fromDate, toDate)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error("Failed to get productivity metrics"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }
    
    override fun trackFocusTime(
        minutes: Int,
        taskId: String?,
        habitId: String?,
        goalId: String?,
        notes: String?
    ): Flow<Resource<ProductivityMetricsDto>> = flow {
        emit(Resource.Loading())
        try {
            val request = FocusTimeRequest(
                minutes = minutes,
                taskId = taskId,
                habitId = habitId,
                goalId = goalId,
                notes = notes
            )
            val response = apiService.trackFocusTime(request)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error("Failed to track focus time"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }
    
    override fun submitDayRating(
        rating: Int,
        notes: String?
    ): Flow<Resource<ProductivityMetricsDto>> = flow {
        emit(Resource.Loading())
        try {
            val request = DayRatingRequest(
                rating = rating,
                notes = notes
            )
            val response = apiService.submitDayRating(request)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error("Failed to submit day rating"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }
    
    override fun getProductivityInsights(
        period: String?
    ): Flow<Resource<ProductivityInsightsResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getProductivityInsights(period)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error("Failed to get productivity insights"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }
} 