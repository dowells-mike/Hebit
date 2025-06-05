package com.hebit.app.ui.screens.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hebit.app.domain.model.Achievement
import com.hebit.app.domain.model.UserAchievement
import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.repository.IAchievementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AchievementsUiEvent {
    data class ShowSnackbar(val message: String) : AchievementsUiEvent()
}

// Data class to hold combined data for UI display
data class AchievementDisplayData(
    val achievement: Achievement,
    val userAchievement: UserAchievement? // Null if not earned or no progress yet
) {
    val isEarned: Boolean = userAchievement?.earned == true
    val progress: Int = userAchievement?.progress?.toInt() ?: 0
    val earnedAt: String? = userAchievement?.earnedAt?.toString() // Format as needed
    val seenByUser: Boolean = userAchievement?.seenByUser == true
}

enum class AchievementFilterType {
    ALL,
    UNLOCKED,
    LOCKED
}

data class AchievementsScreenState(
    val allAchievementsInternal: List<Achievement> = emptyList(), // For internal storage
    val userAchievementsInternal: List<UserAchievement> = emptyList(), // For internal storage
    val displayedAchievements: List<AchievementDisplayData> = emptyList(), // For UI
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentFilter: AchievementFilterType = AchievementFilterType.ALL
)

@HiltViewModel
class AchievementViewModel @Inject constructor(
    private val achievementRepository: IAchievementRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AchievementsScreenState())
    val uiState: StateFlow<AchievementsScreenState> = _uiState.asStateFlow()
    
    private val _eventChannel = Channel<AchievementsUiEvent>()
    val uiEvent = _eventChannel.receiveAsFlow()
    
    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Fetch all achievement definitions
            val allAchievementsResult = achievementRepository.getAllAchievements()
            
            // Fetch user-specific achievement progress
            val userAchievementsResult = achievementRepository.getUserAchievements("me") // Assuming "me"

            var anErrorOccurred = false
            var errorMessage: String? = null

            if (allAchievementsResult is Resource.Success) {
                _uiState.value = _uiState.value.copy(allAchievementsInternal = allAchievementsResult.data ?: emptyList())
            } else if (allAchievementsResult is Resource.Error) {
                anErrorOccurred = true
                errorMessage = allAchievementsResult.message ?: "Error fetching all achievements"
            }

            if (userAchievementsResult is Resource.Success) {
                _uiState.value = _uiState.value.copy(userAchievementsInternal = userAchievementsResult.data ?: emptyList())
            } else if (userAchievementsResult is Resource.Error) {
                anErrorOccurred = true
                errorMessage = errorMessage ?: userAchievementsResult.message ?: "Error fetching user achievements"
                if (allAchievementsResult is Resource.Success) { // if first one was success, this is the primary error
                     errorMessage = userAchievementsResult.message ?: "Error fetching user achievements"
                }
            }

            if (anErrorOccurred) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = errorMessage)
                _eventChannel.send(AchievementsUiEvent.ShowSnackbar(errorMessage ?: "An unknown error occurred"))
            } else {
                combineAndFilterAchievements() // Process and update displayedAchievements
                 _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    fun retryLoadInitialData() {
        loadInitialData()
    }

    private fun combineAndFilterAchievements() {
        val allDefs = _uiState.value.allAchievementsInternal
        val userProgress = _uiState.value.userAchievementsInternal.associateBy { it.achievement.id }

        val combined = allDefs.map { definition ->
            AchievementDisplayData(
                achievement = definition,
                userAchievement = userProgress[definition.id]
            )
        }

        val filtered = when (_uiState.value.currentFilter) {
            AchievementFilterType.ALL -> combined
            AchievementFilterType.UNLOCKED -> combined.filter { it.isEarned }
            AchievementFilterType.LOCKED -> combined.filter { !it.isEarned }
        }
        _uiState.value = _uiState.value.copy(displayedAchievements = filtered, isLoading = false, error = null)
    }

    fun setFilter(filterType: AchievementFilterType) {
        _uiState.value = _uiState.value.copy(currentFilter = filterType)
        combineAndFilterAchievements()
    }
    
    fun markAchievementAsSeen(userAchievementId: String) {
        viewModelScope.launch {
            // Find the specific UserAchievement to get the main achievement ID
            val userAchievementToMark = _uiState.value.userAchievementsInternal.find { it.id == userAchievementId }
            if (userAchievementToMark == null) {
                 _eventChannel.send(AchievementsUiEvent.ShowSnackbar("Could not find achievement to mark as seen."))
                return@launch
            }

            // Optionally, show a loading state for this specific action if needed
            // _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = achievementRepository.markUserAchievementAsSeen(userAchievementId)) {
                is Resource.Success -> {
                    // Update local state
                    val updatedUserAchievements = _uiState.value.userAchievementsInternal.map {
                        if (it.id == userAchievementId) result.data ?: it.copy(seenByUser = true) else it
                    }
                    _uiState.value = _uiState.value.copy(userAchievementsInternal = updatedUserAchievements)
                    combineAndFilterAchievements() // Re-apply filter to update displayed list
                    // _uiState.value = _uiState.value.copy(isLoading = false)
                    // _eventChannel.send(AchievementsUiEvent.ShowSnackbar("Achievement marked as seen."))
                }
                is Resource.Error -> {
                    // _uiState.value = _uiState.value.copy(isLoading = false)
                    _eventChannel.send(AchievementsUiEvent.ShowSnackbar(result.message ?: "Error marking achievement as seen"))
                }
                is Resource.Loading -> {
                }
            }
        }
    }
}

