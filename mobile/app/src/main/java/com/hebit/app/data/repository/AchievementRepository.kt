package com.hebit.app.data.repository

import com.hebit.app.data.remote.api.HebitApiService
import com.hebit.app.data.remote.dto.*
import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.repository.IAchievementRepository
import com.hebit.app.util.safeApiCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementRepository @Inject constructor(
    private val apiService: HebitApiService
) : IAchievementRepository {
    
    override fun getAchievements(
        category: String?,
        earned: Boolean?,
        rarity: String?,
        page: Int,
        perPage: Int
    ): Flow<Resource<AchievementListResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getAchievements(category, earned, rarity, page, perPage)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error("Failed to get achievements"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }
    
    override fun getAchievementProgress(): Flow<Resource<AchievementProgressResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getAchievementProgress()
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error("Failed to get achievement progress"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }
    
    override fun checkNewAchievements(): Flow<Resource<NewlyEarnedAchievementsResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.checkNewAchievements()
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error("Failed to check new achievements"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }
    
    override fun getUserAchievements(): Flow<Resource<UserAchievementResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getUserAchievements()
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error("Failed to get user achievements"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }
} 