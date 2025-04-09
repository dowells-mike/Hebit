package com.hebit.app.ui.screens.profile

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hebit.app.data.repository.AuthRepository
import com.hebit.app.domain.model.*
import com.hebit.app.domain.repository.IAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for managing profile-related data and operations
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: IAuthRepository
) : ViewModel() {

    // Profile state
    private val _profileState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val profileState: StateFlow<ProfileUiState> = _profileState.asStateFlow()

    // Statistics state
    private val _statisticsState = MutableStateFlow<StatisticsUiState>(StatisticsUiState.Loading)
    val statisticsState: StateFlow<StatisticsUiState> = _statisticsState.asStateFlow()

    // Achievements state
    private val _achievementsState = MutableStateFlow<AchievementsUiState>(AchievementsUiState.Loading)
    val achievementsState: StateFlow<AchievementsUiState> = _achievementsState.asStateFlow()

    init {
        fetchProfile()
        fetchStatistics()
        fetchAchievements()
    }

    /**
     * Fetch user profile data
     */
    fun fetchProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileUiState.Loading
            
            authRepository.getUserProfile().onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (result.data != null) {
                            val profile = Profile(
                                id = result.data.id,
                                name = result.data.name,
                                username = "@${result.data.name.lowercase().replace(" ", "")}",
                                email = result.data.email,
                                bio = "Hebit user",
                                location = "Not specified",
                                profileImageUrl = null,
                                coverImageUrl = null,
                                level = 1,
                                points = 0,
                                pointsToNextLevel = 100,
                                tasksDone = 0,
                                streaks = 0,
                                badges = emptyList(),
                                recentActivities = emptyList()
                            )
                            _profileState.value = ProfileUiState.Success(profile)
                        } else {
                            _profileState.value = ProfileUiState.Error("User profile is empty")
                        }
                    }
                    is Resource.Error -> {
                        _profileState.value = ProfileUiState.Error(result.message ?: "Unknown error")
                    }
                    is Resource.Loading -> {
                        _profileState.value = ProfileUiState.Loading
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    /**
     * Fetch user statistics
     */
    fun fetchStatistics() {
        viewModelScope.launch {
            // For now, use mock data since we don't have a dedicated endpoint
            // In a real implementation, we would fetch this from a repository
            kotlinx.coroutines.delay(1000)
            
            // Mock statistics data
            val statistics = Statistics(
                userId = "1",
                taskStats = TaskStats(
                    total = 0,
                    completed = 0,
                    dailyAverage = 0f,
                    successRate = 0f
                ),
                habitStats = HabitStatsSummary(
                    total = 0,
                    active = 0,
                    longestStreak = 0,
                    currentStreaks = 0
                ),
                goalStats = GoalStats(
                    total = 0,
                    completed = 0,
                    inProgress = 0
                ),
                categoryDistribution = mapOf(),
                streakRecords = emptyList()
            )
            
            _statisticsState.value = StatisticsUiState.Success(statistics)
        }
    }

    /**
     * Fetch user achievements
     */
    fun fetchAchievements() {
        viewModelScope.launch {
            // For now, use mock data since we don't have a dedicated endpoint
            // In a real implementation, we would fetch this from a repository
            kotlinx.coroutines.delay(900)
            
            _achievementsState.value = AchievementsUiState.Success(
                AchievementsData(
                    level = 1,
                    points = 0,
                    pointsToNextLevel = 100,
                    levelProgress = 0f,
                    unlockedBadges = emptyList(),
                    lockedBadges = emptyList(),
                    leaderboard = emptyList()
                )
            )
        }
    }

    /**
     * Update user profile
     */
    fun updateProfile(
        name: String,
        username: String,
        bio: String,
        location: String
    ) {
        viewModelScope.launch {
            val currentState = _profileState.value
            if (currentState is ProfileUiState.Success) {
                val updatedProfile = currentState.profile.copy(
                    name = name,
                    username = username,
                    bio = bio,
                    location = location
                )
                _profileState.value = ProfileUiState.Success(updatedProfile)
                
                // In a real app, we would call a repository to persist the changes
            }
        }
    }
}

/**
 * UI state for profile section
 */
sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val profile: Profile) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

/**
 * UI state for statistics section
 */
sealed class StatisticsUiState {
    object Loading : StatisticsUiState()
    data class Success(val statistics: Statistics) : StatisticsUiState()
    data class Error(val message: String) : StatisticsUiState()
}

/**
 * UI state for achievements section
 */
sealed class AchievementsUiState {
    object Loading : AchievementsUiState()
    data class Success(val data: AchievementsData) : AchievementsUiState()
    data class Error(val message: String) : AchievementsUiState()
}

/**
 * Data for the achievements screen
 */
data class AchievementsData(
    val level: Int,
    val points: Int,
    val pointsToNextLevel: Int,
    val levelProgress: Float, // 0.0f to 1.0f
    val unlockedBadges: List<Badge>,
    val lockedBadges: List<Badge>,
    val leaderboard: List<LeaderboardEntry> = emptyList()
)

/**
 * Leaderboard entry for the achievement screen
 */
data class LeaderboardEntry(
    val id: String,
    val name: String,
    val points: Int,
    val rank: Int
)
