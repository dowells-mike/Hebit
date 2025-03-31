package com.hebit.app.domain.repository

import com.hebit.app.data.remote.dto.*
import com.hebit.app.domain.model.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Interface for achievement operations
 */
interface IAchievementRepository {
    /**
     * Get achievements with optional filters
     */
    fun getAchievements(
        category: String? = null,
        earned: Boolean? = null,
        rarity: String? = null,
        page: Int = 1,
        perPage: Int = 20
    ): Flow<Resource<AchievementListResponse>>
    
    /**
     * Get user's achievement progress
     */
    fun getAchievementProgress(): Flow<Resource<AchievementProgressResponse>>
    
    /**
     * Check for newly earned achievements
     */
    fun checkNewAchievements(): Flow<Resource<NewlyEarnedAchievementsResponse>>
    
    /**
     * Get user's achievements summary
     */
    fun getUserAchievements(): Flow<Resource<UserAchievementResponse>>
} 