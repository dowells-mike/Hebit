package com.hebit.app.ui.screens.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hebit.app.domain.model.Habit
import com.hebit.app.domain.model.HabitStats
import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.repository.IHabitRepository // Changed to IHabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class HabitViewModel @Inject constructor(
    private val habitRepository: IHabitRepository // Use IHabitRepository
) : ViewModel() {

    private val _habitsState = MutableStateFlow<Resource<List<Habit>>>(Resource.Loading())
    val habitsState: StateFlow<Resource<List<Habit>>> = _habitsState.asStateFlow()

    private val _todayHabitsState = MutableStateFlow<Resource<List<Habit>>>(Resource.Loading())
    val todayHabitsState: StateFlow<Resource<List<Habit>>> = _todayHabitsState.asStateFlow()

    // Changed to Resource<Habit> and initialized with Resource.Loading()
    // The absence of a selected habit is represented by Loading or Error state.
    private val _selectedHabitState = MutableStateFlow<Resource<Habit>>(Resource.Loading())
    val selectedHabitState: StateFlow<Resource<Habit>> = _selectedHabitState.asStateFlow()

    // Changed to Resource<HabitStats> and initialized with Resource.Loading()
    private val _habitStatsState = MutableStateFlow<Resource<HabitStats>>(Resource.Loading())
    val habitStatsState: StateFlow<Resource<HabitStats>> = _habitStatsState.asStateFlow()

    // Single event shared flow for UI messages (e.g., Toasts)
    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
    }

    init {
        loadAllHabits()
        loadTodaysHabits()
    }

    fun loadAllHabits(
        page: Int? = null,
        perPage: Int? = null,
        frequency: String? = null,
        category: String? = null,
        status: String? = null
    ) {
        viewModelScope.launch {
            habitRepository.getHabits(page, perPage, frequency, category, status)
                .onStart { _habitsState.value = Resource.Loading() }
                .catch { e ->
                    _habitsState.value = Resource.Error("Failed to load habits: ${'$'}{e.localizedMessage}")
                    _eventFlow.emit(UiEvent.ShowSnackbar("Error loading habits."))
                }
                .collect { result ->
                    _habitsState.value = result
                }
        }
    }

    fun loadTodaysHabits() {
        viewModelScope.launch {
            habitRepository.getTodaysHabits()
                .onStart { _todayHabitsState.value = Resource.Loading() }
                .catch { e ->
                    _todayHabitsState.value = Resource.Error("Failed to load today's habits: ${'$'}{e.localizedMessage}")
                    _eventFlow.emit(UiEvent.ShowSnackbar("Error loading today's habits."))
                }
                .collect { result ->
                    _todayHabitsState.value = result
                }
        }
    }

    fun getHabitDetails(habitId: String) {
        viewModelScope.launch {
            _selectedHabitState.value = Resource.Loading() // Indicate loading for the selected habit
            _habitStatsState.value = Resource.Loading()    // Indicate loading for stats

            habitRepository.getHabitById(habitId)
                .catch { e ->
                    val errorMsg = "Failed to load habit $habitId: ${'$'}{e.localizedMessage}"
                    _selectedHabitState.value = Resource.Error(errorMsg)
                    // Stats will remain Loading or show a specific error if desired
                    _habitStatsState.value = Resource.Error("Habit details failed to load, stats not fetched.") 
                    _eventFlow.emit(UiEvent.ShowSnackbar("Error loading habit details."))
                }
                .collect { result -> // result is Resource<Habit>
                    _selectedHabitState.value = result // This should now be fine
                    if (result is Resource.Success) { // Data is non-null if Success
                        loadHabitStats(habitId) 
                    } else if (result is Resource.Error) {
                         _habitStatsState.value = Resource.Error("Habit details failed to load, stats not fetched.")
                    }
                    // If result is Loading, states are already Loading
                }
        }
    }

    private fun loadHabitStats(habitId: String) {
        viewModelScope.launch {
            habitRepository.getHabitStats(habitId)
                .onStart { _habitStatsState.value = Resource.Loading() }
                .catch { e ->
                    _habitStatsState.value = Resource.Error("Failed to load stats for $habitId: ${'$'}{e.localizedMessage}")
                }
                .collect { result -> // result is Resource<HabitStats>
                    _habitStatsState.value = result // This should now be fine
                }
        }
    }

    fun createHabit(habit: Habit) {
        viewModelScope.launch {
            habitRepository.createHabit(habit)
                .catch { e ->
                    _eventFlow.emit(UiEvent.ShowSnackbar("Error creating habit: ${'$'}{e.localizedMessage}"))
                }
                .collect { result ->
                    if (result is Resource.Success) {
                        _eventFlow.emit(UiEvent.ShowSnackbar("Habit created successfully!"))
                        refreshHabitLists()
                    } else if (result is Resource.Error) {
                        _eventFlow.emit(UiEvent.ShowSnackbar(result.message ?: "Failed to create habit."))
                    }
                }
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            habitRepository.updateHabit(habit)
                .catch { e ->
                    _eventFlow.emit(UiEvent.ShowSnackbar("Error updating habit: ${'$'}{e.localizedMessage}"))
                }
                .collect { result ->
                    if (result is Resource.Success) {
                        _eventFlow.emit(UiEvent.ShowSnackbar("Habit updated successfully!"))
                        refreshHabitLists()
                        if (_selectedHabitState.value.data?.id == result.data?.id) {
                            _selectedHabitState.value = Resource.Success(result.data!!)
                            loadHabitStats(result.data!!.id)
                        }
                    } else if (result is Resource.Error) {
                        _eventFlow.emit(UiEvent.ShowSnackbar(result.message ?: "Failed to update habit."))
                    }
                }
        }
    }

    fun deleteHabit(habitId: String, permanent: Boolean = false) {
        viewModelScope.launch {
            habitRepository.deleteHabit(habitId, permanent)
                .catch { e ->
                    _eventFlow.emit(UiEvent.ShowSnackbar("Error deleting habit: ${'$'}{e.localizedMessage}"))
                }
                .collect { result ->
                    if (result is Resource.Success && result.data == true) {
                        _eventFlow.emit(UiEvent.ShowSnackbar("Habit deleted successfully!"))
                        refreshHabitLists()
                        if (_selectedHabitState.value.data?.id == habitId) {
                            _selectedHabitState.value = Resource.Loading() // Back to loading/empty state
                            _habitStatsState.value = Resource.Loading()
                        }
                    } else if (result is Resource.Error) {
                        _eventFlow.emit(UiEvent.ShowSnackbar(result.message ?: "Failed to delete habit."))
                    }
                }
        }
    }

    fun trackHabitCompletion(habitId: String, completed: Boolean, notes: String? = null) {
        val dateString = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
        viewModelScope.launch {
            habitRepository.trackHabit(habitId, completed, dateString, notes)
                .catch { e ->
                    _eventFlow.emit(UiEvent.ShowSnackbar("Error tracking habit: ${'$'}{e.localizedMessage}"))
                }
                .collect { result ->
                    if (result is Resource.Success) {
                        _eventFlow.emit(UiEvent.ShowSnackbar("Habit tracking updated!"))
                        refreshHabitLists()
                        if (_selectedHabitState.value.data?.id == habitId) {
                            _selectedHabitState.value = Resource.Success(result.data!!)
                            loadHabitStats(result.data!!.id)
                        }
                        val todayHabits = _todayHabitsState.value.data
                        if (todayHabits?.any { it.id == habitId } == true) {
                            loadTodaysHabits()
                        }
                    } else if (result is Resource.Error) {
                        _eventFlow.emit(UiEvent.ShowSnackbar(result.message ?: "Failed to track habit."))
                    }
                }
        }
    }

    fun skipHabit(habitId: String, skipReason: String? = null) {
        val dateString = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
        viewModelScope.launch {
            habitRepository.skipHabit(habitId, dateString, skipReason)
                .catch { e ->
                    _eventFlow.emit(UiEvent.ShowSnackbar("Error skipping habit: ${'$'}{e.localizedMessage}"))
                }
                .collect { result ->
                    if (result is Resource.Success) {
                        _eventFlow.emit(UiEvent.ShowSnackbar("Habit skipped!"))
                        refreshHabitLists()
                        if (_selectedHabitState.value.data?.id == habitId) {
                            _selectedHabitState.value = Resource.Success(result.data!!)
                             loadHabitStats(result.data!!.id)
                        }
                         val todayHabits = _todayHabitsState.value.data
                        if (todayHabits?.any { it.id == habitId } == true) {
                            loadTodaysHabits()
                        }
                    } else if (result is Resource.Error) {
                        _eventFlow.emit(UiEvent.ShowSnackbar(result.message ?: "Failed to skip habit."))
                    }
                }
        }
    }
    
    private fun refreshHabitLists() {
        loadAllHabits() // Reload all habits (which might include filters if they were applied)
        loadTodaysHabits() // Reload today's habits
    }

    fun clearSelectedHabit() {
        _selectedHabitState.value = Resource.Loading() // Represent empty/cleared selection as Loading
        _habitStatsState.value = Resource.Loading()
    }
}

