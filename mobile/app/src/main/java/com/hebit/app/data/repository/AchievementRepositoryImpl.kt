package com.hebit.app.data.repository

import com.hebit.app.data.mapper.toDomain
import com.hebit.app.data.remote.api.HebitApiService
import com.hebit.app.domain.model.Achievement
import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.model.UserAchievement
import com.hebit.app.domain.repository.IAchievementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementRepositoryImpl @Inject constructor(
    private val apiService: HebitApiService
) : IAchievementRepository {

    override suspend fun getAllAchievements(): Resource<List<Achievement>> {
        return try {
            val response = apiService.getAllAchievements()
            if (response.isSuccessful) {
                val achievementDtos = response.body()
                Resource.Success(achievementDtos?.map { it.toDomain() } ?: emptyList())
            } else {
                Resource.Error("Failed to fetch achievements: ${response.code()} - ${response.message()}")
            }
        } catch (e: HttpException) {
            Resource.Error("An unexpected error occurred: ${e.localizedMessage}")
        } catch (e: IOException) {
            Resource.Error("Couldn\'t reach server. Check your internet connection.")
        }
    }

    override suspend fun getUserAchievements(userId: String): Resource<List<UserAchievement>> {
        return try {
            val response = apiService.getUserAchievements(userId)
            if (response.isSuccessful) {
                val userAchievementDtos = response.body()
                Resource.Success(userAchievementDtos?.map { it.toDomain() } ?: emptyList())
            } else {
                Resource.Error("Failed to fetch user achievements: ${response.code()} - ${response.message()}")
            }
        } catch (e: HttpException) {
            Resource.Error("An unexpected error occurred: ${e.localizedMessage}")
        } catch (e: IOException) {
            Resource.Error("Couldn\'t reach server. Check your internet connection.")
        }
    }

    override suspend fun markUserAchievementAsSeen(userAchievementId: String): Resource<UserAchievement> {
        return try {
            val response = apiService.markUserAchievementSeen(userAchievementId)
            if (response.isSuccessful) {
                response.body()?.toDomain()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Failed to parse response after marking achievement as seen.")
            } else {
                Resource.Error("Failed to mark achievement as seen: ${response.code()} - ${response.message()}")
            }
        } catch (e: HttpException) {
            Resource.Error("An unexpected error occurred: ${e.localizedMessage}")
        } catch (e: IOException) {
            Resource.Error("Couldn\'t reach server. Check your internet connection.")
        }
    }
} 