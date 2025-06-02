package com.hebit.app.ui.screens.habits

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hebit.app.domain.model.Habit
import com.hebit.app.domain.model.HabitFrequency
import com.hebit.app.domain.model.HabitFrequencyConfig
import com.hebit.app.domain.model.HabitStatus // Assuming status might be set, default to ACTIVE
import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.repository.IHabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HabitFormState(
    val id: String? = null,
    val title: String = "",
    val titleError: String? = null,
    val description: String = "",
    val iconName: String = "taskalt", // Default icon
    val colorHex: String = "#FF6200EE", // Default color (e.g., a theme purple)

    val selectedFrequency: HabitFrequency = HabitFrequency.DAILY,
    val daysOfWeek: List<Int> = emptyList(), // For DAILY (specific days) or WEEKLY. Domain: 0(Sun)-6(Sat).
    val timesPerPeriod: String = "1", // For WEEKLY/MONTHLY type
    val timesPerPeriodError: String? = null,
    val datesOfMonth: List<Int> = emptyList(), // For MONTHLY type
    val specificDates: List<LocalDate> = emptyList(), // For SPECIFIC_DATES type

    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,

    val isLoading: Boolean = false,
    val generalError: String? = null,
    val isHabitLoaded: Boolean = false, // For edit mode, to ensure data is populated
    val isEditMode: Boolean = false
)

sealed class CreateEditHabitUiEvent {
    data class ShowSnackbar(val message: String) : CreateEditHabitUiEvent()
    object NavigateBack : CreateEditHabitUiEvent()
}

@HiltViewModel
class CreateEditHabitViewModel @Inject constructor(
    private val habitRepository: IHabitRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _formState = MutableStateFlow(HabitFormState())
    val formState: StateFlow<HabitFormState> = _formState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<CreateEditHabitUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val habitId: String? = savedStateHandle.get<String>("habitId")

    init {
        if (habitId != null) {
            _formState.update { it.copy(isEditMode = true) }
            loadHabitForEditing(habitId)
        } else {
            // Ready for creation with default values
             _formState.update { it.copy(isEditMode = false, isHabitLoaded = true) } // For create mode, consider "loaded"
        }
    }

    private fun loadHabitForEditing(id: String) {
        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true) }
            habitRepository.getHabitById(id).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let { habit ->
                            _formState.update { currentState ->
                                currentState.copy(
                                    id = habit.id,
                                    title = habit.title,
                                    description = habit.description ?: "",
                                    iconName = habit.icon ?: "taskalt",
                                    colorHex = habit.color ?: "#FF6200EE",
                                    selectedFrequency = habit.frequency,
                                    daysOfWeek = habit.frequencyConfig?.daysOfWeek ?: emptyList(),
                                    timesPerPeriod = habit.frequencyConfig?.timesPerPeriod?.toString() ?: "1",
                                    datesOfMonth = habit.frequencyConfig?.datesOfMonth ?: emptyList(),
                                    specificDates = habit.frequencyConfig?.specificDates ?: emptyList(),
                                    startDate = habit.startDate,
                                    endDate = habit.endDate,
                                    isHabitLoaded = true,
                                    isLoading = false
                                )
                            }
                        } ?: _formState.update {
                            it.copy(isLoading = false, generalError = "Failed to load habit data.", isHabitLoaded = true)
                        }
                    }
                    is Resource.Error -> {
                        _formState.update {
                            it.copy(isLoading = false, generalError = resource.message ?: "Error loading habit.", isHabitLoaded = true)
                        }
                        _uiEvent.emit(CreateEditHabitUiEvent.ShowSnackbar(resource.message ?: "Error loading habit"))
                    }
                    is Resource.Loading -> {
                        // Already handled by initial isLoading update
                    }
                }
            }
        }
    }

    fun onTitleChange(title: String) {
        _formState.update { it.copy(title = title, titleError = if (title.isBlank()) "Title cannot be empty" else null) }
    }

    fun onDescriptionChange(description: String) {
        _formState.update { it.copy(description = description) }
    }

    fun onIconChange(iconName: String) {
        _formState.update { it.copy(iconName = iconName) }
    }

    fun onColorChange(colorHex: String) {
        _formState.update { it.copy(colorHex = colorHex) }
    }

    fun onFrequencyTypeChange(frequency: HabitFrequency) {
        _formState.update { it.copy(selectedFrequency = frequency) }
    }

    fun onDaysOfWeekChange(days: List<Int>) { // Expects 0-6 for Sun-Sat
        _formState.update { it.copy(daysOfWeek = days) }
    }
    
    fun onTimesPerPeriodChange(times: String) {
        val error = try {
            val num = times.toInt()
            if (num <= 0) "Must be positive" else null
        } catch (e: NumberFormatException) {
            "Invalid number"
        }
        _formState.update { it.copy(timesPerPeriod = times, timesPerPeriodError = error) }
    }

    fun onDatesOfMonthChange(dates: List<Int>) {
        _formState.update { it.copy(datesOfMonth = dates) }
    }

    fun onSpecificDatesChange(dates: List<LocalDate>) {
        _formState.update { it.copy(specificDates = dates) }
    }
    
    fun onStartDateChange(date: LocalDate?) {
        _formState.update { it.copy(startDate = date) }
    }

    fun onEndDateChange(date: LocalDate?) {
        _formState.update { it.copy(endDate = date) }
    }

    fun onSaveClick() {
        val currentFormState = _formState.value
        // Validate title
        if (currentFormState.title.isBlank()) {
            _formState.update { it.copy(titleError = "Title cannot be empty") }
            return
        } else {
             _formState.update { it.copy(titleError = null) }
        }

        // Validate timesPerPeriod if applicable
        val timesPerPeriodInt = try {
            currentFormState.timesPerPeriod.toInt()
        } catch (e: NumberFormatException) { null }

        if ((currentFormState.selectedFrequency == HabitFrequency.WEEKLY || currentFormState.selectedFrequency == HabitFrequency.MONTHLY) &&
            (timesPerPeriodInt == null || timesPerPeriodInt <= 0)) {
            _formState.update { it.copy(timesPerPeriodError = "Must be a positive number for weekly/monthly frequency if 'times per period' is used.") }
             // Optionally emit snackbar
            viewModelScope.launch { _uiEvent.emit(CreateEditHabitUiEvent.ShowSnackbar("Times per period is invalid.")) }
            return
        } else {
            _formState.update { it.copy(timesPerPeriodError = null) }
        }


        val habitToSave = Habit(
            id = currentFormState.id ?: "", // For create, repo should ignore. For update, it's used.
            userId = "", // Assuming repository or use case will fill this from authenticated user
            title = currentFormState.title,
            description = currentFormState.description.ifBlank { null },
            icon = currentFormState.iconName,
            color = currentFormState.colorHex,
            frequency = currentFormState.selectedFrequency,
            frequencyConfig = HabitFrequencyConfig(
                daysOfWeek = if (currentFormState.selectedFrequency == HabitFrequency.DAILY && currentFormState.daysOfWeek.isNotEmpty()) currentFormState.daysOfWeek else null, // Only for daily specific days
                timesPerPeriod = if (currentFormState.selectedFrequency == HabitFrequency.WEEKLY || currentFormState.selectedFrequency == HabitFrequency.MONTHLY) timesPerPeriodInt else null,
                datesOfMonth = if (currentFormState.selectedFrequency == HabitFrequency.MONTHLY && currentFormState.datesOfMonth.isNotEmpty()) currentFormState.datesOfMonth else null,
                specificDates = if (currentFormState.selectedFrequency == HabitFrequency.SPECIFIC_DATES) currentFormState.specificDates else null
            ),
            status = HabitStatus.ACTIVE, // Default
            startDate = currentFormState.startDate,
            endDate = currentFormState.endDate
            // Other fields like streakData, completionHistory, etc., will be handled by backend or default.
        )

        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true) }
            val resultFlow = if (currentFormState.isEditMode && currentFormState.id != null) {
                habitRepository.updateHabit(habitToSave.copy(id = currentFormState.id)) // Ensure ID is correct for update
            } else {
                habitRepository.createHabit(habitToSave)
            }

            resultFlow.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _formState.update { it.copy(isLoading = false) }
                        _uiEvent.emit(CreateEditHabitUiEvent.ShowSnackbar("Habit saved successfully!"))
                        _uiEvent.emit(CreateEditHabitUiEvent.NavigateBack)
                    }
                    is Resource.Error -> {
                        _formState.update { it.copy(isLoading = false, generalError = resource.message) }
                        _uiEvent.emit(CreateEditHabitUiEvent.ShowSnackbar(resource.message ?: "Failed to save habit."))
                    }
                    is Resource.Loading -> {
                        // isLoading already true
                    }
                }
            }
        }
    }
} 