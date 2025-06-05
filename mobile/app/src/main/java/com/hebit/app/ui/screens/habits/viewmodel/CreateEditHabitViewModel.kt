package com.hebit.app.ui.screens.habits.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hebit.app.domain.model.Habit
import com.hebit.app.domain.model.HabitFrequency
import com.hebit.app.domain.model.HabitFrequencyConfig
import com.hebit.app.domain.model.HabitStatus
import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.repository.IHabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

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

@HiltViewModel
class CreateEditHabitViewModel @Inject constructor(
    private val habitRepository: IHabitRepository
    // habitId will be passed via SavedStateHandle if needed, or through a separate method
) : ViewModel() {

    private var habitId: String? = null // Will be set via setHabitId method

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

    fun initializeWithHabitId(habitId: String?) {
        this.habitId = habitId
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
                    // Simplified: if timesPerPeriod is set, it must be valid. Or, if dates are selected, it's valid.
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
            habitRepository.getHabitById(id)
                .catch { e ->
                    _uiState.value = CreateEditHabitUiState.Error("Failed to load habit: ${e.localizedMessage}")
                }
                .collect { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _uiState.value = CreateEditHabitUiState.Loading
                        }
                        is Resource.Success -> {
                            val habit = result.data
                            if (habit != null) {
                                // Populate UI state from loaded habit
                                habitName = habit.title
                                habitDescription = habit.description ?: ""
                                selectedIconName = habit.icon
                                selectedColorHex = habit.color
                                selectedTimeOfDay = TimeOfDayOptions.ANY_TIME // TODO: Map from habit if available
                                linkedGoalId = null // TODO: Map from habit.goalLink if available
                                linkedGoalName = null // TODO: Map from habit if available
                                
                                // Map frequency
                                frequencySelection = mapDomainFrequencyToUI(habit.frequency, habit.frequencyConfig)
                                updateUISelectionsFromFrequency()
                                
                                _uiState.value = CreateEditHabitUiState.Success(isEditMode = true, canSave = true)
                                validateInput()
                            } else {
                                _uiState.value = CreateEditHabitUiState.Error("Habit not found")
                            }
                        }
                        is Resource.Error -> {
                            _uiState.value = CreateEditHabitUiState.Error(result.message ?: "Failed to load habit")
                        }
                    }
                }
        }
    }

    fun saveHabit() {
        val currentUiState = _uiState.value
        if (currentUiState !is CreateEditHabitUiState.Success || !currentUiState.canSave) {
            println("SaveHabit: Cannot save - currentUiState is not Success or canSave is false")
            return
        }

        _uiState.value = currentUiState.copy(saveError = null) // Clear any previous error
        
        viewModelScope.launch {
            try {
                val habitToSave = createHabitFromUIState()
                println("SaveHabit: Created habit from UI state: ${habitToSave.title}, frequency: ${habitToSave.frequency}")
                
                val result = if (habitId != null) {
                    // Update existing habit
                    println("SaveHabit: Updating existing habit with ID: $habitId")
                    habitRepository.updateHabit(habitToSave.copy(id = habitId!!))
                } else {
                    // Create new habit
                    println("SaveHabit: Creating new habit")
                    habitRepository.createHabit(habitToSave)
                }
                
                result.catch { e ->
                    println("SaveHabit: Error in repository call: ${e.localizedMessage}")
                    _uiState.value = currentUiState.copy(
                        saveError = "Error saving habit: ${e.localizedMessage}"
                    )
                }.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            println("SaveHabit: Repository returned Loading")
                            // Keep current state, maybe add loading indicator later
                        }
                        is Resource.Success -> {
                            println("SaveHabit: Repository returned Success")
                            _uiState.value = currentUiState.copy(isSaved = true)
                        }
                        is Resource.Error -> {
                            println("SaveHabit: Repository returned Error: ${resource.message}")
                            _uiState.value = currentUiState.copy(
                                saveError = resource.message ?: "Failed to save habit"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                println("SaveHabit: Exception in saveHabit: ${e.localizedMessage}")
                _uiState.value = currentUiState.copy(
                    saveError = "Error creating habit: ${e.localizedMessage}"
                )
            }
        }
    }

    private fun createHabitFromUIState(): Habit {
        val domainFrequency = mapUIFrequencyToDomain(frequencySelection.type)
        val domainFrequencyConfig = mapUIFrequencyConfigToDomain(frequencySelection.config)
        
        return Habit(
            id = habitId ?: UUID.randomUUID().toString(), // Use existing ID or generate new
            userId = "", // Will be set by repository/backend
            title = habitName,
            description = if (habitDescription.isBlank()) null else habitDescription,
            icon = selectedIconName,
            color = selectedColorHex,
            frequency = domainFrequency,
            frequencyConfig = domainFrequencyConfig,
            status = HabitStatus.ACTIVE,
            // Set other fields to defaults for now
            streakData = null,
            category = null,
            completionHistory = emptyList(),
            difficulty = null,
            impact = null,
            startDate = LocalDate.now(),
            endDate = null,
            reminderSettings = null,
            successCriteria = null,
            metadata = null,
            createdAt = null,
            updatedAt = null,
            completedToday = null
        )
    }

    private fun mapUIFrequencyToDomain(uiType: HabitFrequencyType): HabitFrequency {
        return when (uiType) {
            HabitFrequencyType.DAILY -> HabitFrequency.DAILY
            HabitFrequencyType.WEEKLY -> HabitFrequency.WEEKLY
            HabitFrequencyType.MONTHLY -> HabitFrequency.MONTHLY
            HabitFrequencyType.SPECIFIC_DATES -> HabitFrequency.SPECIFIC_DATES
        }
    }

    private fun mapUIFrequencyConfigToDomain(uiConfig: FrequencyConfig): HabitFrequencyConfig? {
        if (uiConfig.daysOfWeek.isEmpty() && 
            uiConfig.timesPerPeriod == null && 
            uiConfig.datesOfMonth.isEmpty() && 
            uiConfig.specificDates.isEmpty()) {
            return null
        }
        
        return HabitFrequencyConfig(
            daysOfWeek = uiConfig.daysOfWeek.map { dayOfWeek ->
                // Convert java.time.DayOfWeek to 0-6 (Sun-Sat) for backend
                when (dayOfWeek) {
                    DayOfWeek.SUNDAY -> 0
                    DayOfWeek.MONDAY -> 1
                    DayOfWeek.TUESDAY -> 2
                    DayOfWeek.WEDNESDAY -> 3
                    DayOfWeek.THURSDAY -> 4
                    DayOfWeek.FRIDAY -> 5
                    DayOfWeek.SATURDAY -> 6
                }
            }.takeIf { it.isNotEmpty() },
            datesOfMonth = uiConfig.datesOfMonth.takeIf { it.isNotEmpty() },
            timesPerPeriod = uiConfig.timesPerPeriod,
            specificDates = uiConfig.specificDates.takeIf { it.isNotEmpty() }
        )
    }

    private fun mapDomainFrequencyToUI(domainFreq: HabitFrequency, domainConfig: HabitFrequencyConfig?): HabitFrequencySelection {
        val uiType = when (domainFreq) {
            HabitFrequency.DAILY -> HabitFrequencyType.DAILY
            HabitFrequency.WEEKLY -> HabitFrequencyType.WEEKLY
            HabitFrequency.MONTHLY -> HabitFrequencyType.MONTHLY
            HabitFrequency.SPECIFIC_DATES -> HabitFrequencyType.SPECIFIC_DATES
            HabitFrequency.UNKNOWN -> HabitFrequencyType.DAILY // Fallback
        }
        
        val uiConfig = if (domainConfig != null) {
            FrequencyConfig(
                daysOfWeek = domainConfig.daysOfWeek?.map { dayInt ->
                    // Convert 0-6 (Sun-Sat) to DayOfWeek
                    when (dayInt) {
                        0 -> DayOfWeek.SUNDAY
                        1 -> DayOfWeek.MONDAY
                        2 -> DayOfWeek.TUESDAY
                        3 -> DayOfWeek.WEDNESDAY
                        4 -> DayOfWeek.THURSDAY
                        5 -> DayOfWeek.FRIDAY
                        6 -> DayOfWeek.SATURDAY
                        else -> DayOfWeek.MONDAY // Fallback
                    }
                } ?: emptyList(),
                timesPerPeriod = domainConfig.timesPerPeriod,
                datesOfMonth = domainConfig.datesOfMonth ?: emptyList(),
                specificDates = domainConfig.specificDates ?: emptyList()
            )
        } else {
            FrequencyConfig()
        }
        
        return HabitFrequencySelection(type = uiType, config = uiConfig)
    }

    private fun updateUISelectionsFromFrequency() {
        selectedDaysOfWeek.clear()
        selectedDaysOfWeek.addAll(frequencySelection.config.daysOfWeek)
        
        selectedDatesOfMonth.clear()
        selectedDatesOfMonth.addAll(frequencySelection.config.datesOfMonth)
        
        selectedSpecificDates.clear()
        selectedSpecificDates.addAll(frequencySelection.config.specificDates)
    }
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