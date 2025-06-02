package com.hebit.app.domain.repository

import com.hebit.app.domain.model.Achievement
import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.model.UserAchievement


interface IAchievementRepository {

    /**
     * Fetches all defined achievements from the backend.
     */
    suspend fun getAllAchievements(): Resource<List<Achievement>>

    /**
     * Fetches all achievements (locked and unlocked) for a specific user.
     * @param userId The ID of the user. Can be "me" for the current authenticated user.
     */
    suspend fun getUserAchievements(userId: String): Resource<List<UserAchievement>>

    /**
     * Marks a specific user achievement as seen.
     * @param userAchievementId The ID of the UserAchievement record (not the Achievement ID itself).
     */
    suspend fun markUserAchievementAsSeen(userAchievementId: String): Resource<UserAchievement>
} 