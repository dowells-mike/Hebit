package com.hebit.app.ui.screens.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hebit.app.domain.model.* // Updated to use the full Habit domain model
import com.hebit.app.domain.model.HabitAchievement
import com.hebit.app.domain.model.HabitPerformanceInsight
import com.hebit.app.domain.model.HabitSuggestion
import com.hebit.app.domain.repository.HabitRepository
// Import for HabitStats if it's a distinct domain model from your existing HabitStats.kt
// import com.hebit.app.domain.model.HabitStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

// Placeholder domain models for UI structure (These seem okay for now)
// data class HabitAchievement(val id: String, val title: String, val description: String, val earnedDate: LocalDate?) // Changed to LocalDate
// data class HabitSuggestion(val id: String, val title: String, val description: String)
// data class HabitPerformanceInsight(val insight: String)

@HiltViewModel
class HabitViewModel @Inject constructor(
    private val habitRepository: HabitRepository // This repository will need to be updated/implemented
) : ViewModel() {

    private val _habitsState = MutableStateFlow<Resource<List<Habit>>>(Resource.Loading())
    val habitsState: StateFlow<Resource<List<Habit>>> = _habitsState.asStateFlow()

    private val _todayHabitsState = MutableStateFlow<Resource<List<Habit>>>(Resource.Loading())
    val todayHabitsState: StateFlow<Resource<List<Habit>>> = _todayHabitsState.asStateFlow()

    private val _selectedHabitState = MutableStateFlow<Resource<Habit?>>(Resource.Success(null))
    val selectedHabitState: StateFlow<Resource<Habit?>> = _selectedHabitState.asStateFlow()

    // Assuming HabitStats is a defined domain model
    private val _habitStatsState = MutableStateFlow<Resource<HabitStats?>>(Resource.Success(null))
    val habitStatsState: StateFlow<Resource<HabitStats?>> = _habitStatsState.asStateFlow()

    // Notes State
    private val _notesState = MutableStateFlow<Resource<List<Note>>>(Resource.Loading())
    val notesState: StateFlow<Resource<List<Note>>> = _notesState.asStateFlow()

    // Placeholder states (seem fine for now)
    private val _performanceInsightsState = MutableStateFlow<Resource<List<HabitPerformanceInsight>>>(Resource.Loading())
    val performanceInsightsState: StateFlow<Resource<List<HabitPerformanceInsight>>> = _performanceInsightsState.asStateFlow()

    private val _relatedAchievementsState = MutableStateFlow<Resource<List<HabitAchievement>>>(Resource.Loading())
    val relatedAchievementsState: StateFlow<Resource<List<HabitAchievement>>> = _relatedAchievementsState.asStateFlow()

    private val _suggestionsState = MutableStateFlow<Resource<List<HabitSuggestion>>>(Resource.Loading())
    val suggestionsState: StateFlow<Resource<List<HabitSuggestion>>> = _suggestionsState.asStateFlow()

    init {
        android.util.Log.d("HabitViewModel", "Initializing HabitViewModel - loading initial habits")
        loadHabits() // This will also trigger filtering for today's habits
    }

    fun loadHabits() {
        viewModelScope.launch {
            _habitsState.value = Resource.Loading() // Set loading for main list
            _todayHabitsState.value = Resource.Loading() // Also for today's list
            habitRepository.getHabits() // Assumes this returns Flow<Resource<List<Habit>>>
                .catch { e -> 
                    _habitsState.value = Resource.Error("Failed to load habits: ${e.message}")
                    _todayHabitsState.value = Resource.Error("Failed to load today's habits: ${e.message}")
                }
                .collect { result ->
                    _habitsState.value = result
                    if (result is Resource.Success) {
                        filterAndUpdateTodayHabits(result.data ?: emptyList())
                    } else if (result is Resource.Error) {
                        // If main list fails, today's list also reflects error or empty
                        _todayHabitsState.value = Resource.Error(result.message ?: "Failed to filter today's habits", emptyList())
                    }
                }
        }
    }

    // Helper function to filter habits for today
    private fun filterAndUpdateTodayHabits(allHabits: List<Habit>) {
        // Basic filtering logic for daily habits or habits active today.
        // This needs to be robust and consider all frequency types from HabitFrequency.
        val today = LocalDate.now()
        val filtered = allHabits.filter { habit ->
            when (habit.frequency.type) {
                HabitFrequencyType.DAILY -> true
                HabitFrequencyType.WEEKLY -> habit.frequency.daysOfWeek?.map { it.toJavaTimeDayOfWeek() }?.contains(today.dayOfWeek) == true
                HabitFrequencyType.MONTHLY -> habit.frequency.datesOfMonth?.contains(today.dayOfMonth) == true ||
                        (habit.frequency.datesOfMonth?.contains(-1) == true && today.dayOfMonth == today.lengthOfMonth())
                HabitFrequencyType.SPECIFIC_DATES -> habit.frequency.specificDates?.contains(today) == true
                else -> false
            } && (habit.startDate == null || !today.isBefore(habit.startDate)) && (habit.endDate == null || !today.isAfter(habit.endDate))
        }
        _todayHabitsState.value = Resource.Success(filtered)
        android.util.Log.d("HabitViewModel", "Today's habits filtered: ${filtered.size} habits")
    }


    fun getHabitById(id: String) {
        viewModelScope.launch {
            _selectedHabitState.value = Resource.Loading()
            _habitStatsState.value = Resource.Loading() // Also make nullable if stats can be absent
            _notesState.value = Resource.Loading()
            // Reset other states too if they are habit-specific
            _performanceInsightsState.value = Resource.Loading()
            _relatedAchievementsState.value = Resource.Loading()
            _suggestionsState.value = Resource.Loading()

            habitRepository.getHabitById(id) // Assumes this returns Flow<Resource<Habit?>>
                .catch { e -> _selectedHabitState.value = Resource.Error("Failed to load habit ${id}: ${e.message}") }
                .collect { result ->
                    _selectedHabitState.value = result
                    if (result is Resource.Success && result.data != null) {
                        loadHabitStats(id)
                        loadNotesForHabit(id)
                        // TODO: Uncomment and implement these when repository methods are ready
                        // loadPerformanceInsights(id)
                        // loadRelatedAchievements(id)
                        // loadSuggestionsForHabit(id)
                        loadPerformanceInsights(result.data.id)
                        loadRelatedAchievements(result.data.id)
                        loadSuggestionsForHabit(result.data.id)
                    } else if (result is Resource.Success && result.data == null){
                        // Habit not found or null, reset dependent states to avoid showing old data
                        _habitStatsState.value = Resource.Success(null) 
                        _notesState.value = Resource.Success(emptyList()) 
                         _performanceInsightsState.value = Resource.Success(emptyList()) 
                        _relatedAchievementsState.value = Resource.Success(emptyList()) 
                        _suggestionsState.value = Resource.Success(emptyList()) 
                    }
                }
        }
    }

    fun loadHabitStats(habitId: String) {
        viewModelScope.launch {
            _habitStatsState.value = Resource.Loading()
            habitRepository.getHabitStats(habitId) // Assumes this returns Flow<Resource<HabitStats>>
                .catch { e -> _habitStatsState.value = Resource.Error("Failed to load stats for $habitId: ${e.message}") }
                .collect { result -> _habitStatsState.value = result }
        }
    }

    fun loadNotesForHabit(habitId: String) {
        viewModelScope.launch {
            // _notesState.value already set to Loading() in getHabitById
            habitRepository.getNotesForHabit(habitId)
                .catch { e -> _notesState.value = Resource.Error("Failed to load notes for $habitId: ${e.message}") }
                .collect { result -> _notesState.value = result }
        }
    }

    fun loadPerformanceInsights(habitId: String) {
        viewModelScope.launch {
            _performanceInsightsState.value = Resource.Loading()
            habitRepository.getPerformanceInsights(habitId)
                .catch { e -> _performanceInsightsState.value = Resource.Error("Failed to load insights for $habitId: ${e.message}") }
                .collect { result -> _performanceInsightsState.value = result }
        }
    }

    fun loadRelatedAchievements(habitId: String) {
        viewModelScope.launch {
            _relatedAchievementsState.value = Resource.Loading()
            habitRepository.getRelatedAchievements(habitId)
                .catch { e -> _relatedAchievementsState.value = Resource.Error("Failed to load related achievements for $habitId: ${e.message}") }
                .collect { result -> _relatedAchievementsState.value = result }
        }
    }

    fun loadSuggestionsForHabit(habitId: String) {
        viewModelScope.launch {
            _suggestionsState.value = Resource.Loading()
            habitRepository.getSuggestionsForHabit(habitId)
                .catch { e -> _suggestionsState.value = Resource.Error("Failed to load suggestions for $habitId: ${e.message}") }
                .collect { result -> _suggestionsState.value = result }
        }
    }

    // --- CRUD Operations ---

    // CreateHabit now takes the domain model directly or specific parameters to build it.
    // The repository should handle mapping this to a CreateHabitRequest DTO.
    fun createHabit(habit: Habit) { // Or pass individual parameters
        viewModelScope.launch {
            // Example: if habitRepository.createHabit now takes a Habit domain object
            habitRepository.createHabit(habit)
                .catch { e -> android.util.Log.e("HabitViewModel", "Error creating habit: ${e.message}") }
                .collect { result ->
                    if (result is Resource.Success) {
                        loadHabits() // Refresh lists
                        // Optionally, handle navigation or UI feedback
                    } else if (result is Resource.Error) {
                        // Handle error, e.g., show a toast
                        android.util.Log.e("HabitViewModel", "Error creating habit: ${result.message}")
                    }
                }
        }
    }

    // Example of a more detailed createHabit if you prefer passing individual fields
    // This is more verbose but gives more control from UI if needed.
    fun createNewHabitDetails(
        title: String,
        description: String?,
        icon: String?,
        color: String?,
        frequencyType: HabitFrequencyType,
        // ... other relevant parameters for HabitFrequency, TimePreference, etc.
        // For simplicity, this example is still basic. A real app might have a builder or more params.
        category: String? = null,
        difficulty: HabitDifficulty = HabitDifficulty.MEDIUM,
        startDate: LocalDate? = null
    ) {
        val newHabit = Habit(
            id = "", // Backend generates ID
            title = title,
            description = description,
            icon = icon,
            color = color,
            goalLink = null, // Add if part of creation form
            frequency = HabitFrequency(type = frequencyType), // Simplified: populate daysOfWeek, etc. based on UI
            timePreference = null, // Add if part of creation form
            streakData = HabitStreakDetails(current = 0, longest = 0, lastCompleted = null), // Initial state
            category = category,
            completionHistory = emptyList(),
            difficulty = difficulty,
            startDate = startDate,
            endDate = null, // Add if part of creation form
            reminderSettings = null, // Add if part of creation form
            successCriteria = null, // Add if part of creation form
            createdAt = LocalDate.now(), // Client can set, or backend can override
            updatedAt = LocalDate.now()
        )
        createHabit(newHabit) // Calls the other createHabit method
    }


    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            // Assumes habitRepository.updateHabit takes a Habit domain object
            // The repository would map this to an UpdateHabitRequest DTO
            habitRepository.updateHabit(habit)
                .catch { e -> android.util.Log.e("HabitViewModel", "Error updating habit: ${e.message}") }
                .collect { result ->
                    if (result is Resource.Success) {
                        loadHabits() // Refresh list
                        // If this habit was selected, update its state too
                        if (_selectedHabitState.value.data?.id == result.data?.id) {
                            getHabitById(habit.id) // Re-fetch the updated habit
                        }
                    } else if (result is Resource.Error) {
                        android.util.Log.e("HabitViewModel", "Error updating habit: ${result.message}")
                    }
                }
        }
    }

    fun trackHabitCompletion(habitId: String) {
        android.util.Log.d("HabitViewModel", "Tracking habit completion for ID: $habitId")
        viewModelScope.launch {
            // Assumes habitRepository.trackHabit calls POST /api/habits/:id/track
            habitRepository.trackHabit(habitId)
                .catch { e -> android.util.Log.e("HabitViewModel", "Error tracking habit $habitId: ${e.message}") }
                .collect { result ->
                    if (result is Resource.Success) {
                        android.util.Log.d("HabitViewModel", "Successfully tracked habit completion for $habitId")
                        // Refresh data or intelligently update the specific habit's streak/completion status
                        loadHabits() // Simplest way to refresh, could be optimized
                        // If the completed habit is currently selected, refresh its details
                        if (_selectedHabitState.value.data?.id == habitId) {
                            getHabitById(habitId) // Re-fetch selected habit details
                        }
                    } else if (result is Resource.Error) {
                        android.util.Log.e("HabitViewModel", "Error tracking habit $habitId: ${result.message}")
                    }
                }
        }
    }

    fun skipHabitCompletion(habitId: String) {
        android.util.Log.d("HabitViewModel", "Skipping habit completion for ID: $habitId")
        viewModelScope.launch {
            // Assumes habitRepository.skipHabit calls POST /api/habits/:id/skip
            habitRepository.skipHabit(habitId)
                .catch { e -> android.util.Log.e("HabitViewModel", "Error skipping habit $habitId: ${e.message}") }
                .collect { result ->
                    if (result is Resource.Success) {
                        android.util.Log.d("HabitViewModel", "Successfully skipped habit $habitId")
                        loadHabits() // Refresh
                        if (_selectedHabitState.value.data?.id == habitId) {
                            getHabitById(habitId)
                        }
                    } else if (result is Resource.Error) {
                        android.util.Log.e("HabitViewModel", "Error skipping habit $habitId: ${result.message}")
                    }
                }
        }
    }

    fun deleteHabit(id: String) {
        viewModelScope.launch {
            habitRepository.deleteHabit(id) // Assumes Flow<Resource<Boolean>> or similar
                .catch { e -> android.util.Log.e("HabitViewModel", "Error deleting habit $id: ${e.message}") }
                .collect { result ->
                    if (result is Resource.Success && result.data == true) {
                        loadHabits()
                        if (_selectedHabitState.value.data?.id == id) {
                            _selectedHabitState.value = Resource.Success(null)
                            _habitStatsState.value = Resource.Success(null)
                            _notesState.value = Resource.Success(emptyList())                            
                            _performanceInsightsState.value = Resource.Success(emptyList()) 
                            _relatedAchievementsState.value = Resource.Success(emptyList()) 
                            _suggestionsState.value = Resource.Success(emptyList()) 
                        }
                    } else if (result is Resource.Error) {
                        android.util.Log.e("HabitViewModel", "Error deleting habit $id: ${result.message}")
                    }
                }
        }
    }

    fun clearSelectedHabit() {
        _selectedHabitState.value = Resource.Success(null)
        _habitStatsState.value = Resource.Success(null) // Use Success(null) for nullable states
        _notesState.value = Resource.Success(emptyList()) // Or Resource.Loading() if preferred on clear
        _performanceInsightsState.value = Resource.Loading()
        _relatedAchievementsState.value = Resource.Loading()
        _suggestionsState.value = Resource.Loading()
    }

    // Helper to convert DayOfWeekDomain to java.time.DayOfWeek for filtering
    private fun DayOfWeekDomain.toJavaTimeDayOfWeek(): java.time.DayOfWeek? {
        return when (this) {
            DayOfWeekDomain.SUNDAY -> java.time.DayOfWeek.SUNDAY
            DayOfWeekDomain.MONDAY -> java.time.DayOfWeek.MONDAY
            DayOfWeekDomain.TUESDAY -> java.time.DayOfWeek.TUESDAY
            DayOfWeekDomain.WEDNESDAY -> java.time.DayOfWeek.WEDNESDAY
            DayOfWeekDomain.THURSDAY -> java.time.DayOfWeek.THURSDAY
            DayOfWeekDomain.FRIDAY -> java.time.DayOfWeek.FRIDAY
            DayOfWeekDomain.SATURDAY -> java.time.DayOfWeek.SATURDAY
            else -> null
        }
    }
}