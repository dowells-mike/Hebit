package com.hebit.app.domain.repository

import com.hebit.app.data.remote.dto.*
import com.hebit.app.domain.model.Resource

/**
 * Interface for achievement operations
 */
interface IAchievementRepository {
    /**
     * Get achievements with optional filters
     */
    suspend fun getAchievements(
        category: String? = null,
        earned: Boolean? = null,
        rarity: String? = null,
        page: Int = 1,
        perPage: Int = 20
    ): Resource<AchievementListResponse>
    
    /**
     * Get user's achievement progress
     */
    suspend fun getAchievementProgress(): Resource<AchievementProgressResponse>
    
    /**
     * Check for newly earned achievements
     */
    suspend fun checkNewAchievements(): Resource<NewlyEarnedAchievementsResponse>
    
    /**
     * Get user's achievements summary
     */
    suspend fun getUserAchievements(): Resource<UserAchievementResponse>
} 