package com.hebit.app.ui.screens.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hebit.app.domain.model.Habit
import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.model.HabitStats
import com.hebit.app.domain.repository.HabitRepository
// Import stats DTO if needed, or define domain model
// import com.hebit.app.data.remote.dto.HabitStatsDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

// Define placeholder domain models for UI structure
data class HabitAchievement(val id: String, val title: String, val description: String, val earnedDate: LocalDateTime?)
data class HabitSuggestion(val id: String, val title: String, val description: String)
data class HabitPerformanceInsight(val insight: String) // Simple insight model for now

@HiltViewModel
class HabitViewModel @Inject constructor(
    private val habitRepository: HabitRepository
) : ViewModel() {
    
    private val _habitsState = MutableStateFlow<Resource<List<Habit>>>(Resource.Loading())
    val habitsState: StateFlow<Resource<List<Habit>>> = _habitsState.asStateFlow()
    
    private val _todayHabitsState = MutableStateFlow<Resource<List<Habit>>>(Resource.Loading())
    val todayHabitsState: StateFlow<Resource<List<Habit>>> = _todayHabitsState.asStateFlow()
    
    private val _selectedHabitState = MutableStateFlow<Resource<Habit?>>(Resource.Success(null))
    val selectedHabitState: StateFlow<Resource<Habit?>> = _selectedHabitState.asStateFlow()
    
    // StateFlow for habit statistics
    private val _habitStatsState = MutableStateFlow<Resource<HabitStats>>(Resource.Loading())
    val habitStatsState: StateFlow<Resource<HabitStats>> = _habitStatsState.asStateFlow()
    
    // Placeholder StateFlows for additional analytics
    private val _performanceInsightsState = MutableStateFlow<Resource<List<HabitPerformanceInsight>>>(Resource.Success(emptyList()))
    val performanceInsightsState: StateFlow<Resource<List<HabitPerformanceInsight>>> = _performanceInsightsState.asStateFlow()

    private val _relatedAchievementsState = MutableStateFlow<Resource<List<HabitAchievement>>>(Resource.Success(emptyList()))
    val relatedAchievementsState: StateFlow<Resource<List<HabitAchievement>>> = _relatedAchievementsState.asStateFlow()

    private val _suggestionsState = MutableStateFlow<Resource<List<HabitSuggestion>>>(Resource.Success(emptyList()))
    val suggestionsState: StateFlow<Resource<List<HabitSuggestion>>> = _suggestionsState.asStateFlow()
    
    init {
        loadHabits()
        loadTodayHabits()
    }
    
    fun loadHabits() {
        viewModelScope.launch {
            habitRepository.getHabits()
                .onEach { result ->
                    _habitsState.value = result
                }
                .launchIn(this)
        }
    }
    
    fun loadTodayHabits() {
        viewModelScope.launch {
            habitRepository.getTodaysHabits()
                .onEach { result ->
                    _todayHabitsState.value = result
                }
                .launchIn(this)
        }
    }
    
    fun getHabitById(id: String) {
        viewModelScope.launch {
             _selectedHabitState.value = Resource.Loading() // Set loading state
             _habitStatsState.value = Resource.Loading() // Also reset stats
             _performanceInsightsState.value = Resource.Loading() // Reset others
             _relatedAchievementsState.value = Resource.Loading()
             _suggestionsState.value = Resource.Loading()

            habitRepository.getHabitById(id)
                .onEach { result ->
                     _selectedHabitState.value = result as? Resource<Habit?> ?: Resource.Error("Failed to process habit data")
                     if (result is Resource.Success && result.data != null) {
                         // Trigger loading for other sections if main habit loads
                         loadHabitStats(id)
                         loadPerformanceInsights(id)
                         loadRelatedAchievements(id)
                         loadSuggestions(id)
                     }
                }
                .launchIn(this)
        }
    }
    
    // Function to load habit statistics
    fun loadHabitStats(habitId: String) {
        viewModelScope.launch {
            _habitStatsState.value = Resource.Loading()
            
            // Use repository to fetch the stats
            habitRepository.getHabitStats(habitId).onEach { result ->
                _habitStatsState.value = result
            }.launchIn(this)
        }
    }
    
    // Placeholder loading functions
    fun loadPerformanceInsights(habitId: String) {
        // This functionality will be implemented later when API support is available
        viewModelScope.launch {
            _performanceInsightsState.value = Resource.Success(emptyList())
        }
    }

    fun loadRelatedAchievements(habitId: String) {
        // This functionality will be implemented later when API support is available
        viewModelScope.launch {
            _relatedAchievementsState.value = Resource.Success(emptyList())
        }
    }

    fun loadSuggestions(habitId: String) {
        // This functionality will be implemented later when API support is available
        viewModelScope.launch {
            _suggestionsState.value = Resource.Success(emptyList())
        }
    }
    
    fun createHabit(
        title: String,
        description: String,
        iconName: String,
        frequency: String
    ) {
        val newHabit = Habit(
            id = "", // Will be generated by the backend
            title = title,
            description = description,
            iconName = iconName,
            frequency = frequency,
            completedToday = false,
            streak = 0,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        viewModelScope.launch {
            habitRepository.createHabit(newHabit)
                .onEach { result ->
                    if (result is Resource.Success) {
                        loadHabits()
                        loadTodayHabits()
                    }
                }
                .launchIn(this)
        }
    }
    
    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            habitRepository.updateHabit(habit)
                .onEach { result ->
                    if (result is Resource.Success) {
                        loadHabits()
                        loadTodayHabits()
                        _selectedHabitState.value = Resource.Success(result.data)
                    }
                }
                .launchIn(this)
        }
    }
    
    fun toggleHabitCompletion(id: String, completed: Boolean) {
        viewModelScope.launch {
            habitRepository.completeHabitForToday(id)
                .onEach { result ->
                    if (result is Resource.Success) {
                        loadHabits()
                        loadTodayHabits()
                    }
                }
                .launchIn(this)
        }
    }
    
    fun deleteHabit(id: String) {
        viewModelScope.launch {
            habitRepository.deleteHabit(id)
                .onEach { result ->
                    if (result is Resource.Success && result.data == true) {
                        loadHabits()
                        loadTodayHabits()
                        _selectedHabitState.value = Resource.Success(null)
                    }
                }
                .launchIn(this)
        }
    }
    
    fun clearSelectedHabit() {
        _selectedHabitState.value = Resource.Success(null)
        _habitStatsState.value = Resource.Loading()
        _performanceInsightsState.value = Resource.Loading() // Reset placeholders
        _relatedAchievementsState.value = Resource.Loading()
        _suggestionsState.value = Resource.Loading()
    }
} 