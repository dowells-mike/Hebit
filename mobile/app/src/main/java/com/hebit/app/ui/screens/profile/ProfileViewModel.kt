package com.hebit.app.ui.screens.profile

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hebit.app.domain.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

/**
 * ViewModel for managing profile-related data and operations
 */
class ProfileViewModel : ViewModel() {

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
        // Initialize with mock data for now
        // In a real implementation, we would fetch this from a repository
        fetchProfile()
        fetchStatistics()
        fetchAchievements()
    }

    /**
     * Fetch user profile data
     */
    fun fetchProfile() {
        viewModelScope.launch {
            // Simulate network delay
            kotlinx.coroutines.delay(800)
            
            // Mock profile data
            val profile = Profile(
                id = "1",
                name = "Sarah Wilson",
                username = "@sarahw",
                email = "sarah.wilson@example.com",
                bio = "UX Designer passionate about creating meaningful digital experiences. Always learning, always growing.",
                location = "San Francisco, CA",
                profileImageUrl = null, // This would be a URL in production
                coverImageUrl = null,
                level = 5,
                points = 1250,
                pointsToNextLevel = 250,
                tasksDone = 248,
                streaks = 15,
                badges = listOf(
                    Badge(
                        id = "1",
                        name = "Early Bird",
                        description = "Complete 5 tasks before 9 AM",
                        iconUrl = "",
                        dateEarned = LocalDate.now().minusDays(5),
                        isLocked = false
                    ),
                    Badge(
                        id = "2",
                        name = "Streak Pro",
                        description = "Maintain a streak for 10 days",
                        iconUrl = "",
                        dateEarned = LocalDate.now().minusDays(10),
                        isLocked = false
                    ),
                    Badge(
                        id = "3",
                        name = "Super User",
                        description = "Complete 50 tasks",
                        iconUrl = "",
                        dateEarned = LocalDate.now().minusDays(5),
                        isLocked = false
                    )
                ),
                recentActivities = listOf(
                    Activity(
                        id = UUID.randomUUID().toString(),
                        type = ActivityType.TASK_COMPLETED,
                        description = "Completed daily meditation",
                        timestamp = System.currentTimeMillis() - 7_200_000 // 2 hours ago
                    ),
                    Activity(
                        id = UUID.randomUUID().toString(),
                        type = ActivityType.BADGE_EARNED,
                        description = "Earned \"Early Bird\" badge",
                        timestamp = System.currentTimeMillis() - 18_000_000 // 5 hours ago
                    )
                )
            )
            
            _profileState.value = ProfileUiState.Success(profile)
        }
    }

    /**
     * Fetch user statistics
     */
    fun fetchStatistics() {
        viewModelScope.launch {
            // Simulate network delay
            kotlinx.coroutines.delay(1000)
            
            // Mock statistics data
            val statistics = Statistics(
                userId = "1",
                taskStats = TaskStats(
                    total = 300,
                    completed = 248,
                    dailyAverage = 12.4f,
                    successRate = 92f
                ),
                habitStats = HabitStats(
                    total = 5,
                    active = 4,
                    longestStreak = 21,
                    currentStreaks = 15
                ),
                goalStats = GoalStats(
                    total = 12,
                    completed = 7,
                    inProgress = 5
                ),
                categoryDistribution = mapOf(
                    "Work" to 45f,
                    "Health" to 30f,
                    "Learning" to 25f
                ),
                streakRecords = listOf(
                    StreakRecord(
                        id = "1",
                        habitId = "101",
                        habitName = "Morning Meditation",
                        days = 15,
                        startDate = LocalDate.now().minusDays(15),
                        isActive = true
                    ),
                    StreakRecord(
                        id = "2",
                        habitId = "102",
                        habitName = "Reading",
                        days = 12,
                        startDate = LocalDate.now().minusDays(27),
                        endDate = LocalDate.now().minusDays(15),
                        isActive = false
                    )
                )
            )
            
            _statisticsState.value = StatisticsUiState.Success(statistics)
        }
    }

    /**
     * Fetch user achievements
     */
    fun fetchAchievements() {
        viewModelScope.launch {
            // Simulate network delay
            kotlinx.coroutines.delay(900)
            
            // Mock achievements data
            val unlockedBadges = listOf(
                Badge(
                    id = "1",
                    name = "Early Bird",
                    description = "Complete 5 tasks before 9 AM",
                    iconUrl = "",
                    dateEarned = LocalDate.now().minusDays(5),
                    isLocked = false
                ),
                Badge(
                    id = "2",
                    name = "Streak Pro",
                    description = "Maintain a streak for 10 days",
                    iconUrl = "",
                    dateEarned = LocalDate.now().minusDays(10),
                    isLocked = false
                ),
                Badge(
                    id = "3",
                    name = "Super User",
                    description = "Complete 50 tasks",
                    iconUrl = "",
                    dateEarned = LocalDate.now().minusDays(5),
                    isLocked = false
                )
            )
            
            val lockedBadges = listOf(
                Badge(
                    id = "4",
                    name = "Night Owl",
                    description = "Complete 10 tasks after 8 PM",
                    iconUrl = "",
                    pointsRequired = 500,
                    isLocked = true
                ),
                Badge(
                    id = "5",
                    name = "Pro User",
                    description = "Complete 100 tasks",
                    iconUrl = "",
                    pointsRequired = 1000,
                    isLocked = true
                )
            )
            
            val leaderboard = listOf(
                LeaderboardEntry(
                    id = "user1",
                    name = "Alex Chen",
                    points = 2450,
                    rank = 1
                ),
                LeaderboardEntry(
                    id = "1", // Current user
                    name = "You",
                    points = 1250,
                    rank = 2
                ),
                LeaderboardEntry(
                    id = "user3",
                    name = "Emma Johnson",
                    points = 1100,
                    rank = 3
                )
            )
            
            _achievementsState.value = AchievementsUiState.Success(
                AchievementsData(
                    level = 5,
                    points = 1250,
                    pointsToNextLevel = 250,
                    levelProgress = 0.75f,
                    unlockedBadges = unlockedBadges,
                    lockedBadges = lockedBadges,
                    leaderboard = leaderboard
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
    val leaderboard: List<LeaderboardEntry>
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
