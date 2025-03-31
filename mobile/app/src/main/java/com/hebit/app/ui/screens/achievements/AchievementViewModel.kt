package com.hebit.app.ui.screens.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hebit.app.data.remote.dto.*
import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.repository.IAchievementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AchievementViewModel @Inject constructor(
    private val achievementRepository: IAchievementRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AchievementUiState())
    val uiState: StateFlow<AchievementUiState> = _uiState.asStateFlow()
    
    private var currentCategory: String? = null
    private var currentEarned: Boolean? = null
    private var currentRarity: String? = null

    init {
        loadAchievements()
        loadAchievementProgress()
        checkNewAchievements()
        loadUserAchievements()
    }
    
    fun loadAchievements(
        category: String? = currentCategory,
        earned: Boolean? = currentEarned,
        rarity: String? = currentRarity,
        page: Int = 1,
        perPage: Int = 20
    ) {
        currentCategory = category
        currentEarned = earned
        currentRarity = rarity

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingAchievements = true) }
            when (val response = achievementRepository.getAchievements(category, earned, rarity, page, perPage)) {
                is Resource.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoadingAchievements = false,
                            achievements = response.data?.achievements ?: emptyList(),
                            error = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoadingAchievements = false,
                            error = response.message
                        )
                    }
                }
                is Resource.Loading -> {
                    // Already handled
                }
            }
        }
    }
    
    fun loadAchievementProgress() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingProgress = true) }
            when (val response = achievementRepository.getAchievementProgress()) {
                is Resource.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoadingProgress = false,
                            achievementProgress = response.data?.achievements ?: emptyList(),
                            error = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoadingProgress = false,
                            error = response.message
                        )
                    }
                }
                is Resource.Loading -> {
                    // Already handled
                }
            }
        }
    }
    
    fun checkNewAchievements() {
        viewModelScope.launch {
            _uiState.update { it.copy(isChecking = true) }
            when (val response = achievementRepository.checkNewAchievements()) {
                is Resource.Success -> {
                    val newAchievements = response.data?.earnedAchievements ?: emptyList()
                    _uiState.update { 
                        it.copy(
                            isChecking = false,
                            newlyEarnedAchievements = newAchievements,
                            hasNewAchievements = newAchievements.isNotEmpty(),
                            error = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { 
                        it.copy(
                            isChecking = false,
                            error = response.message
                        )
                    }
                }
                is Resource.Loading -> {
                    // Already handled
                }
            }
        }
    }
    
    fun loadUserAchievements() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingUserAchievements = true) }
            when (val response = achievementRepository.getUserAchievements()) {
                is Resource.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoadingUserAchievements = false,
                            userAchievements = response.data?.userAchievements ?: emptyList(),
                            error = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoadingUserAchievements = false,
                            error = response.message
                        )
                    }
                }
                is Resource.Loading -> {
                    // Already handled
                }
            }
        }
    }
    
    fun filterAchievements(
        category: String? = null,
        earned: Boolean? = null,
        rarity: String? = null
    ) {
        loadAchievements(category, earned, rarity)
    }

    fun clearNewAchievementsState() {
        _uiState.update { it.copy(hasNewAchievements = false) }
    }
}

data class AchievementUiState(
    val isLoadingAchievements: Boolean = false,
    val isLoadingUserAchievements: Boolean = false,
    val isLoadingProgress: Boolean = false,
    val isChecking: Boolean = false,
    val achievements: List<AchievementDto> = emptyList(),
    val userAchievements: List<UserAchievementDto> = emptyList(),
    val achievementProgress: List<AchievementProgressDto> = emptyList(),
    val newlyEarnedAchievements: List<AchievementDto> = emptyList(),
    val hasNewAchievements: Boolean = false,
    val error: String? = null
) 