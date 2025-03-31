package com.hebit.app.data.repository

import com.hebit.app.data.remote.api.HebitApiService
import com.hebit.app.data.remote.dto.*
import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.repository.IAchievementRepository
import com.hebit.app.util.safeApiCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AchievementRepository @Inject constructor(
    private val apiService: HebitApiService
) : IAchievementRepository {
    
    override suspend fun getAchievements(
        category: String?,
        earned: Boolean?,
        rarity: String?,
        page: Int,
        perPage: Int
    ): Resource<AchievementListResponse> = safeApiCall {
        apiService.getAchievements(category, earned, rarity, page, perPage)
    }
    
    override suspend fun getAchievementProgress(): Resource<AchievementProgressResponse> = safeApiCall {
        apiService.getAchievementProgress()
    }
    
    override suspend fun checkNewAchievements(): Resource<NewlyEarnedAchievementsResponse> = safeApiCall {
        apiService.checkNewAchievements()
    }
    
    override suspend fun getUserAchievements(): Resource<UserAchievementResponse> = safeApiCall {
        apiService.getUserAchievements()
    }
} 