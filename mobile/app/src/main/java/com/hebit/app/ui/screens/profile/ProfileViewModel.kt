package com.hebit.app.ui.screens.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hebit.app.domain.model.Badge
import com.hebit.app.domain.model.Profile
import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.model.Statistics
import com.hebit.app.domain.repository.IAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: IAuthRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val profileState: StateFlow<ProfileUiState> = _profileState.asStateFlow()

    private val _statisticsState = MutableStateFlow<StatisticsUiState>(StatisticsUiState.Loading)
    val statisticsState: StateFlow<StatisticsUiState> = _statisticsState.asStateFlow()

    private val _achievementsState = MutableStateFlow<AchievementsUiState>(AchievementsUiState.Loading)
    val achievementsState: StateFlow<AchievementsUiState> = _achievementsState.asStateFlow()

    init {
        fetchProfile()
        fetchStatistics()
        fetchAchievements()
    }

    fun fetchProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileUiState.Loading
            authRepository.getUserProfile().onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (result.data != null) {
                            val user = result.data
                            val profile = Profile(
                                id = user.id,
                                name = user.name,
                                username = "@${user.name.lowercase().replace(" ", "")}",
                                email = user.email,
                                bio = "Hebit user",
                                location = "Not specified",
                                profileImageUrl = user.avatarUrl
                            )
                            _profileState.value = ProfileUiState.Success(profile)
                        } else {
                            _profileState.value = ProfileUiState.Error("User profile is empty")
                        }
                    }
                    is Resource.Error -> {
                        _profileState.value =
                            ProfileUiState.Error(result.message ?: "Unknown error")
                    }
                    is Resource.Loading -> {
                        _profileState.value = ProfileUiState.Loading
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun fetchStatistics() {
        _statisticsState.value = StatisticsUiState.Loading
        viewModelScope.launch {
            // Mock data for now
            _statisticsState.value = StatisticsUiState.Success(Statistics.empty())
        }
    }

    fun fetchAchievements() {
        _achievementsState.value = AchievementsUiState.Loading
        viewModelScope.launch {
            // Mock data for now
            _achievementsState.value = AchievementsUiState.Success(AchievementsData.empty())
        }
    }

    fun updateProfile(name: String, username: String, bio: String, location: String) {
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
            }
        }
    }

    fun updateProfilePicture(uri: Uri) {
        viewModelScope.launch {
            val currentState = _profileState.value
            if (currentState is ProfileUiState.Success) {
                val updatedProfile = currentState.profile.copy(profileImageUrl = uri.toString())
                _profileState.value = ProfileUiState.Success(updatedProfile)
            }
        }
    }
}

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val profile: Profile) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

sealed class StatisticsUiState {
    object Loading : StatisticsUiState()
    data class Success(val statistics: Statistics) : StatisticsUiState()
    data class Error(val message: String) : StatisticsUiState()
}

sealed class AchievementsUiState {
    object Loading : AchievementsUiState()
    data class Success(val data: AchievementsData) : AchievementsUiState()
    data class Error(val message: String) : AchievementsUiState()
}

data class AchievementsData(
    val level: Int,
    val points: Int,
    val pointsToNextLevel: Int,
    val levelProgress: Float, // 0.0f to 1.0f
    val unlockedBadges: List<Badge>,
    val lockedBadges: List<Badge>,
    val leaderboard: List<LeaderboardEntry> = emptyList()
) {
    companion object {
        fun empty() = AchievementsData(
            level = 1,
            points = 0,
            pointsToNextLevel = 100,
            levelProgress = 0f,
            unlockedBadges = emptyList(),
            lockedBadges = emptyList(),
            leaderboard = emptyList()
        )
    }
}

data class LeaderboardEntry(
    val id: String,
    val name: String,
    val points: Int,
    val rank: Int
)
