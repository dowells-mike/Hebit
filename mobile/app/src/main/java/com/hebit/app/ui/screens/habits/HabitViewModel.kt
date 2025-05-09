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
    
    // States for habits list
    private val _habitsState = MutableStateFlow<Resource<List<Habit>>>(Resource.Loading())
    val habitsState = _habitsState.asStateFlow()
    
    // State for today's habits
    private val _todayHabitsState = MutableStateFlow<Resource<List<Habit>>>(Resource.Loading())
    val todayHabitsState = _todayHabitsState.asStateFlow()
    
    // State for selected habit
    private val _selectedHabitState = MutableStateFlow<Resource<Habit?>>(Resource.Loading())
    val selectedHabitState = _selectedHabitState.asStateFlow()
    
    // State for habit stats
    private val _habitStatsState = MutableStateFlow<Resource<HabitStats>>(Resource.Loading())
    val habitStatsState = _habitStatsState.asStateFlow()
    
    // States for additional habit detail sections
    private val _performanceInsightsState = MutableStateFlow<Resource<List<HabitPerformanceInsight>>>(Resource.Loading())
    val performanceInsightsState = _performanceInsightsState.asStateFlow()
    
    private val _relatedAchievementsState = MutableStateFlow<Resource<List<HabitAchievement>>>(Resource.Loading())
    val relatedAchievementsState = _relatedAchievementsState.asStateFlow()
    
    private val _suggestionsState = MutableStateFlow<Resource<List<HabitSuggestion>>>(Resource.Loading())
    val suggestionsState = _suggestionsState.asStateFlow()
    
    init {
        android.util.Log.d("HabitViewModel", "Initializing HabitViewModel - loading data")
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
        android.util.Log.d("HabitViewModel", "Loading today's habits...")
        viewModelScope.launch {
            habitRepository.getTodaysHabits()
                .onEach { result ->
                    when(result) {
                        is Resource.Success -> {
                            val habits = result.data ?: emptyList()
                            android.util.Log.d("HabitViewModel", "Today's habits loaded successfully: ${habits.size} habits")
                            if (habits.isEmpty()) {
                                android.util.Log.d("HabitViewModel", "No habits returned from API")
                            } else {
                                android.util.Log.d("HabitViewModel", "Habits returned: ${habits.map { it.title }}")
                            }
                        }
                        is Resource.Error -> {
                            android.util.Log.e("HabitViewModel", "Error loading today's habits: ${result.message}")
                        }
                        is Resource.Loading -> {
                            android.util.Log.d("HabitViewModel", "Loading today's habits...")
                        }
                    }
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
        android.util.Log.d("HabitViewModel", "Toggling habit $id completion to: ${!completed}")
        viewModelScope.launch {
            // Pass the inverse of the current state to toggle it
            habitRepository.completeHabitForToday(id, !completed)
                .onEach { result ->
                    when(result) {
                        is Resource.Success -> {
                            android.util.Log.d("HabitViewModel", "Successfully toggled habit completion")
                            loadHabits()
                            loadTodayHabits()
                        }
                        is Resource.Error -> {
                            android.util.Log.e("HabitViewModel", "Error toggling habit: ${result.message}")
                        }
                        is Resource.Loading -> {
                            android.util.Log.d("HabitViewModel", "Toggling habit completion...")
                        }
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