package com.hebit.app.ui.screens.habits

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hebit.app.domain.model.HabitFrequency
import com.hebit.app.ui.components.ColorPicker
import com.hebit.app.ui.components.IconPicker
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditHabitScreen(
    navController: androidx.navigation.NavController, // For navigation back
    viewModel: CreateEditHabitViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showStartDatePickerDialog by remember { mutableStateOf(false) }
    var showEndDatePickerDialog by remember { mutableStateOf(false) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy") }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect {
            when (it) {
                is CreateEditHabitUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = it.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is CreateEditHabitUiEvent.NavigateBack -> {
                    navController.popBackStack()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (formState.isEditMode) "Edit Habit" else "Create Habit") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.onSaveClick() }) {
                Icon(Icons.Default.Done, contentDescription = "Save Habit")
            }
        }
    ) { paddingValues ->
        if (formState.isLoading && !formState.isHabitLoaded) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = formState.title,
                    onValueChange = { viewModel.onTitleChange(it) },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = formState.titleError != null,
                    singleLine = true
                )
                formState.titleError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = formState.description,
                    onValueChange = { viewModel.onDescriptionChange(it) },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Spacer(modifier = Modifier.height(16.dp))

                IconPicker(
                    selectedIconName = formState.iconName,
                    onIconSelected = { viewModel.onIconChange(it) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                ColorPicker(
                    selectedColorHex = formState.colorHex,
                    onColorSelected = { viewModel.onColorChange(it) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text("Frequency", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                FrequencyTypeSelector(formState.selectedFrequency) {
                    viewModel.onFrequencyTypeChange(it)
                }
                Spacer(modifier = Modifier.height(8.dp))

                when (formState.selectedFrequency) {
                    HabitFrequency.DAILY -> {
                        DaysOfWeekSelector(formState.daysOfWeek) {
                            viewModel.onDaysOfWeekChange(it)
                        }
                    }
                    HabitFrequency.WEEKLY, HabitFrequency.MONTHLY -> {
                        OutlinedTextField(
                            value = formState.timesPerPeriod,
                            onValueChange = { viewModel.onTimesPerPeriodChange(it) },
                            label = { Text("Times per ${formState.selectedFrequency.name.lowercase(Locale.getDefault())}") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = formState.timesPerPeriodError != null,
                            singleLine = true
                        )
                        formState.timesPerPeriodError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                        if(formState.selectedFrequency == HabitFrequency.MONTHLY){
                             Spacer(modifier = Modifier.height(8.dp))
                             DatesOfMonthSelector(formState.datesOfMonth) {
                                 viewModel.onDatesOfMonthChange(it)
                             }
                        }
                    }
                    HabitFrequency.SPECIFIC_DATES -> {
                        SpecificDatesSelector(
                            selectedDates = formState.specificDates,
                            onDatesChange = { viewModel.onSpecificDatesChange(it) },
                            dateFormatter = dateFormatter
                        )
                    }
                    HabitFrequency.UNKNOWN -> {
                        Text("Please select a valid frequency type.")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text("Start Date: ${formState.startDate?.format(dateFormatter) ?: "Not set"}")
                Button(onClick = { showStartDatePickerDialog = true }) { Text("Select Start Date") }
                Spacer(modifier = Modifier.height(8.dp))
                Text("End Date: ${formState.endDate?.format(dateFormatter) ?: "Not set"}")
                Button(onClick = { showEndDatePickerDialog = true }) { Text("Select End Date (Optional)") }

                formState.generalError?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Start Date Picker Dialog
    if (showStartDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = formState.startDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli() ?: Instant.now().toEpochMilli(),
            yearRange = (LocalDate.now().year - 10)..(LocalDate.now().year + 10) // Example range
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePickerDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showStartDatePickerDialog = false
                    datePickerState.selectedDateMillis?.let {
                        val selectedDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        viewModel.onStartDateChange(selectedDate)
                    }
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePickerDialog = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // End Date Picker Dialog
    if (showEndDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = formState.endDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(), // Allow null for end date
            yearRange = (LocalDate.now().year - 10)..(LocalDate.now().year + 10)
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePickerDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showEndDatePickerDialog = false
                    datePickerState.selectedDateMillis?.let {
                        val selectedDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        viewModel.onEndDateChange(selectedDate)
                    } ?: viewModel.onEndDateChange(null) // Allow clearing end date
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePickerDialog = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun FrequencyTypeSelector(selected: HabitFrequency, onSelect: (HabitFrequency) -> Unit) {
    Column {
        HabitFrequency.values().filter { it != HabitFrequency.UNKNOWN }.forEach { freq ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(freq) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (freq == selected),
                    onClick = { onSelect(freq) }
                )
                Spacer(Modifier.width(8.dp))
                Text(text = freq.name.lowercase(Locale.getDefault()).replaceFirstChar { it.titlecase(Locale.getDefault()) })
            }
        }
    }
}

@Composable
fun DaysOfWeekSelector(selectedDays: List<Int>, onSelectionChange: (List<Int>) -> Unit) {
    val days = DayOfWeek.values() // Monday to Sunday
    Text("Repeat on:", style = MaterialTheme.typography.labelLarge)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
        days.forEach { dayOfWeek ->
            val storageValue = if (dayOfWeek == DayOfWeek.SUNDAY) 0 else dayOfWeek.value

            val isSelected = selectedDays.contains(storageValue)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()))
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = {
                        val newList = selectedDays.toMutableList()
                        if (isSelected) newList.remove(storageValue) else newList.add(storageValue)
                        onSelectionChange(newList.sorted())
                    }
                )
            }
        }
    }
}

@Composable
fun DatesOfMonthSelector(selectedDates: List<Int>, onDatesSelected: (List<Int>) -> Unit) {
    val allDates = (1..31).toList()
    Text("Select dates of month:", style = MaterialTheme.typography.labelLarge)
    Spacer(modifier = Modifier.height(8.dp))
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 48.dp),
        modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp), // Constrain height
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(allDates) { dayNum ->
            val isSelected = selectedDates.contains(dayNum)
            OutlinedButton(
                onClick = {
                    val newList = selectedDates.toMutableList()
                    if (isSelected) newList.remove(dayNum) else newList.add(dayNum)
                    onDatesSelected(newList.sorted())
                },
                shape = CircleShape,
                border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                ),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Text(dayNum.toString())
            }
        }
        // Consider adding a "Last Day" toggle if needed, mapping to -1
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecificDatesSelector(
    selectedDates: List<LocalDate>,
    onDatesChange: (List<LocalDate>) -> Unit,
    dateFormatter: DateTimeFormatter
) {
    var showDatePickerDialog by remember { mutableStateOf(false) }
    val zoneId = ZoneId.systemDefault()

    Column {
        Text("Selected Dates:", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(8.dp))

        if (selectedDates.isEmpty()) {
            Text("No dates selected. Add dates for your habit.", style = MaterialTheme.typography.bodyMedium)
        } else {
            // Using simple Column for now, could be FlowRow or LazyVerticalGrid if many dates are expected
            selectedDates.sorted().forEach { date ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                ) {
                    Text(date.format(dateFormatter), modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        onDatesChange(selectedDates.filterNot { it == date })
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Remove date")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { showDatePickerDialog = true },
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Date")
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            Text("Add Date")
        }
    }

    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = Instant.now().toEpochMilli(),
            yearRange = (LocalDate.now().year - 10)..(LocalDate.now().year + 10)
        )
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePickerDialog = false
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDate()
                        if (!selectedDates.contains(selectedDate)) {
                            onDatesChange((selectedDates + selectedDate).sorted())
                        }
                    }
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// TODO: Need DatePickerDialog integration for Start/End dates 