package com.hebit.app.ui.screens.habits.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

// TODO: Define data classes for Frequency, Reminder, etc. based on backend API and feature requirements
// For example:
// data class FrequencySelection(
//    val type: String = "daily", // daily, weekly, monthly
//    val daysOfWeek: List<Int>? = null, // For weekly
//    val timesPerPeriod: Int? = null // For weekly/monthly
// )

// --- Data classes for Frequency ---
enum class HabitFrequencyType {
    DAILY, WEEKLY, MONTHLY, SPECIFIC_DATES
}

data class FrequencyConfig(
    // For DAILY (specific days) or WEEKLY
    val daysOfWeek: List<DayOfWeek> = emptyList(), // Monday-Sunday

    // For WEEKLY or MONTHLY
    val timesPerPeriod: Int? = null,

    // For MONTHLY (specific days of month)
    val datesOfMonth: List<Int> = emptyList(), // 1-31

    // For SPECIFIC_DATES
    val specificDates: List<LocalDate> = emptyList()
)

data class HabitFrequencySelection(
    val type: HabitFrequencyType = HabitFrequencyType.DAILY,
    val config: FrequencyConfig = FrequencyConfig()
)
// --- End of Frequency Data classes ---

object TimeOfDayOptions {
    const val ANY_TIME = "Any Time"
    const val MORNING = "Morning (6am-12pm)"
    const val AFTERNOON = "Afternoon (12pm-6pm)"
    const val EVENING = "Evening (6pm-10pm)"
    // Placeholder for custom time, actual implementation would involve TimePickerDialog
    // const val CUSTOM = "Custom"
    fun getAsList() = listOf(ANY_TIME, MORNING, AFTERNOON, EVENING)
}

// Placeholder data class for a reminder
data class HabitReminder(val id: String, val time: String, val label: String? = null)

class CreateEditHabitViewModel(
    // TODO: Inject UseCases/Repositories for saving/loading habits
    private val habitId: String? // Null for create mode, non-null for edit mode
) : ViewModel() {

    var habitName by mutableStateOf("")
        private set

    var habitDescription by mutableStateOf("")
        private set

    var frequencySelection by mutableStateOf(HabitFrequencySelection())
        private set

    var selectedIconName by mutableStateOf<String?>(null)
        private set

    var selectedColorHex by mutableStateOf<String?>(null)
        private set

    var selectedTimeOfDay by mutableStateOf<String?>(TimeOfDayOptions.ANY_TIME)
        private set

    var reminders = mutableStateListOf<HabitReminder>()
        private set

    var linkedGoalId by mutableStateOf<String?>(null)
        private set
    var linkedGoalName by mutableStateOf<String?>(null) // To display if a goal is linked
        private set

    private val _uiState = mutableStateOf<CreateEditHabitUiState>(CreateEditHabitUiState.Loading)
    val uiState: State<CreateEditHabitUiState> = _uiState

    // Helper for days of week selection in Daily/Weekly frequency
    val selectedDaysOfWeek = mutableStateListOf<DayOfWeek>()

    // Helper for dates of month selection in Monthly frequency
    val selectedDatesOfMonth = mutableStateListOf<Int>()
    
    // Helper for specific dates selection
    val selectedSpecificDates = mutableStateListOf<LocalDate>()

    init {
        if (habitId != null) {
            loadHabitDetails(habitId)
        } else {
            _uiState.value = CreateEditHabitUiState.Success(
                isEditMode = false,
                canSave = false // Initially cannot save until mandatory fields are filled
            )
            // Default to daily, all days selected
            onFrequencyTypeChanged(HabitFrequencyType.DAILY)
            selectedTimeOfDay = TimeOfDayOptions.ANY_TIME // Default for new habit
        }
    }

    fun onHabitNameChanged(newName: String) {
        habitName = newName
        validateInput()
    }

    fun onHabitDescriptionChanged(newDescription: String) {
        habitDescription = newDescription
    }

    fun onIconSelected(iconName: String) {
        selectedIconName = iconName
        // No validation impact for now, icon is optional
    }

    fun onColorSelected(colorHex: String) {
        selectedColorHex = colorHex
        // No validation impact for now, color is optional
    }

    fun onTimeOfDayChanged(timeOption: String?) {
        selectedTimeOfDay = timeOption
        // Potentially could trigger reminder suggestions or other logic in future
    }

    fun onFrequencyTypeChanged(newType: HabitFrequencyType) {
        // Reset selections when type changes
        selectedDaysOfWeek.clear()
        selectedDatesOfMonth.clear()
        selectedSpecificDates.clear()
        
        var newConfig = FrequencyConfig()
        if (newType == HabitFrequencyType.DAILY) {
             // Pre-select all days for daily, user can then customize
            selectedDaysOfWeek.addAll(DayOfWeek.values())
            newConfig = newConfig.copy(daysOfWeek = selectedDaysOfWeek.toList())
        }

        frequencySelection = frequencySelection.copy(type = newType, config = newConfig)
        validateInput()
    }

    fun onDayOfWeekToggled(day: DayOfWeek) {
        if (selectedDaysOfWeek.contains(day)) {
            selectedDaysOfWeek.remove(day)
        } else {
            selectedDaysOfWeek.add(day)
        }
        updateFrequencyConfig()
    }
    
    fun onTimesPerPeriodChanged(times: String) {
        val newTimes = times.toIntOrNull()?.coerceAtLeast(1) // Ensure positive number if not null
        frequencySelection = frequencySelection.copy(
            config = frequencySelection.config.copy(timesPerPeriod = newTimes)
        )
        validateInput()
    }

    fun onDateOfMonthToggled(date: Int) {
        if (selectedDatesOfMonth.contains(date)) {
            selectedDatesOfMonth.remove(date)
        } else {
            selectedDatesOfMonth.add(date)
        }
        updateFrequencyConfig()
    }
    
    fun onSpecificDateAdded(date: LocalDate) {
        if (!selectedSpecificDates.contains(date)) {
            selectedSpecificDates.add(date)
            updateFrequencyConfig()
        }
    }

    fun onSpecificDateRemoved(date: LocalDate) {
        if (selectedSpecificDates.contains(date)) {
            selectedSpecificDates.remove(date)
            updateFrequencyConfig()
        }
    }

    private fun updateFrequencyConfig() {
        val currentType = frequencySelection.type
        val newConfig = when (currentType) {
            HabitFrequencyType.DAILY -> frequencySelection.config.copy(
                daysOfWeek = selectedDaysOfWeek.toList().sorted(),
                timesPerPeriod = null, // Ensure others are null for daily
                datesOfMonth = emptyList(),
                specificDates = emptyList()
            )
            HabitFrequencyType.WEEKLY -> frequencySelection.config.copy(
                daysOfWeek = selectedDaysOfWeek.toList().sorted(), // Can also be used for weekly
                // timesPerPeriod is handled by onTimesPerPeriodChanged directly
                datesOfMonth = emptyList(),
                specificDates = emptyList()
            )
            HabitFrequencyType.MONTHLY -> frequencySelection.config.copy(
                daysOfWeek = emptyList(),
                // timesPerPeriod is handled by onTimesPerPeriodChanged directly
                datesOfMonth = selectedDatesOfMonth.toList().sorted(),
                specificDates = emptyList()
            )
            HabitFrequencyType.SPECIFIC_DATES -> frequencySelection.config.copy(
                daysOfWeek = emptyList(),
                timesPerPeriod = null,
                datesOfMonth = emptyList(),
                specificDates = selectedSpecificDates.toList().sorted()
            )
        }
        frequencySelection = frequencySelection.copy(config = newConfig)
        validateInput()
    }

    // Placeholder functions for reminders and goal linking
    fun addReminder(reminder: HabitReminder) {
        if (!reminders.any { it.id == reminder.id}) {
            reminders.add(reminder)
        }
    }
    fun removeReminder(reminderId: String) {
        reminders.removeAll { it.id == reminderId }
    }
    fun linkGoal(goalId: String, goalName: String) {
        linkedGoalId = goalId
        linkedGoalName = goalName
    }
    fun unlinkGoal() {
        linkedGoalId = null
        linkedGoalName = null
    }

    private fun validateInput() {
        val currentUiState = _uiState.value
        if (currentUiState is CreateEditHabitUiState.Success) {
            val isFrequencyValid = when (frequencySelection.type) {
                HabitFrequencyType.DAILY -> !frequencySelection.config.daysOfWeek.isNullOrEmpty() // At least one day selected for "specific days of week" daily type
                HabitFrequencyType.WEEKLY -> (frequencySelection.config.timesPerPeriod ?: 0) > 0 // And potentially daysOfWeek if that's also a criteria
                HabitFrequencyType.MONTHLY -> {
                    val timesValid = (frequencySelection.config.timesPerPeriod ?: 0) > 0
                    // If dates are selected, timesPerPeriod should not be the primary validation, or they should work together.
                    // For now, if timesPerPeriod is set, it must be > 0. If dates are selected, that implies validity too.
                    // Backend will ultimately decide complex rules. For now, either times or dates must be somewhat configured.
                    val datesValid = !frequencySelection.config.datesOfMonth.isNullOrEmpty()
                    (timesValid && !datesValid) || (datesValid && !timesValid) || (timesValid && datesValid) || (!timesValid && datesValid) // More flexible: allow times/period, specific dates, or both
                }
                HabitFrequencyType.SPECIFIC_DATES -> !frequencySelection.config.specificDates.isNullOrEmpty()
            }
            val canSave = habitName.isNotBlank() && isFrequencyValid
            _uiState.value = currentUiState.copy(canSave = canSave)
        }
    }

    private fun loadHabitDetails(id: String) {
        viewModelScope.launch {
            _uiState.value = CreateEditHabitUiState.Loading
            // TODO: Implement actual loading logic from repository/use case
            // For example:
            // val habit = habitRepository.getHabitById(id)
            // habitName = habit.name
            // habitDescription = habit.description
            // frequencySelection = mapBackendFrequencyToLocal(habit.frequency, habit.frequencyConfig)
            // selectedDaysOfWeek.clear()
            // selectedDaysOfWeek.addAll(frequencySelection.config.daysOfWeek)
            // ... etc. for other frequency parts
            kotlinx.coroutines.delay(500) // Simulate network delay
            habitName = "Loaded Habit Name"
            habitDescription = "Loaded Habit Description"
            selectedIconName = "fitness_center" // Simulate loaded icon
            selectedColorHex = "#FF0000"      // Simulate loaded color (Red)
            selectedTimeOfDay = TimeOfDayOptions.MORNING // Simulate loaded time of day
            reminders.addAll(listOf(HabitReminder("1", "09:00"), HabitReminder("2", "17:30"))) // Simulate loaded reminders
            linkedGoalId = "goal123"
            linkedGoalName = "Run a Marathon"
            // Simulate loaded frequency
            val loadedFrequency = HabitFrequencySelection(
                type = HabitFrequencyType.WEEKLY,
                config = FrequencyConfig(daysOfWeek = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY), timesPerPeriod = 3)
            )
            frequencySelection = loadedFrequency
            selectedDaysOfWeek.clear()
            selectedDaysOfWeek.addAll(loadedFrequency.config.daysOfWeek)
            selectedDatesOfMonth.clear()
            selectedSpecificDates.clear()
            // Update other helper lists based on loadedFrequency.config if necessary

            _uiState.value = CreateEditHabitUiState.Success(isEditMode = true, canSave = true) // Assuming loaded data is valid
            validateInput() // Re-validate after loading
        }
    }

    fun saveHabit() {
        val currentUiState = _uiState.value
        if (currentUiState !is CreateEditHabitUiState.Success || !currentUiState.canSave) {
            return
        }

        viewModelScope.launch {
            // TODO: Construct habit object from states including frequencySelection
            // val backendFrequencyType = mapLocalFrequencyTypeToBackend(frequencySelection.type)
            // val backendFrequencyConfig = mapLocalFrequencyConfigToBackend(frequencySelection.config)
            // val habitToSave = HabitDto(..., frequency = backendFrequencyType, frequencyConfig = backendFrequencyConfig)

            // TODO: Call repository/use case to save the habit
            println("Saving habit: Name=$habitName, Icon=$selectedIconName, Color=$selectedColorHex, TimeOfDay=$selectedTimeOfDay, Frequency=$frequencySelection, Reminders=$reminders, GoalID=$linkedGoalId")
            kotlinx.coroutines.delay(500) // Simulate network delay
            _uiState.value = currentUiState.copy(isSaved = true)
        }
    }

    // TODO: Add mapping functions if backend DTOs differ significantly from ViewModel's representation
    // private fun mapBackendFrequencyToLocal(type: String, config: BackendFrequencyConfig?): HabitFrequencySelection { ... }
    // private fun mapLocalFrequencyTypeToBackend(type: HabitFrequencyType): String { ... }
    // private fun mapLocalFrequencyConfigToBackend(config: FrequencyConfig): BackendFrequencyConfig { ... }

}

sealed interface CreateEditHabitUiState {
    object Loading : CreateEditHabitUiState
    data class Success(
        val isEditMode: Boolean,
        val canSave: Boolean,
        val isSaved: Boolean = false,
        val saveError: String? = null,
    ) : CreateEditHabitUiState
    data class Error(val message: String) : CreateEditHabitUiState
} 