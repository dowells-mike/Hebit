package com.hebit.app.ui.screens.habits

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hebit.app.domain.model.HabitFrequency
import com.hebit.app.ui.components.ColorPicker
import com.hebit.app.ui.components.IconPicker
import com.hebit.app.ui.screens.habits.viewmodel.CreateEditHabitUiState
import com.hebit.app.ui.screens.habits.viewmodel.CreateEditHabitViewModel
import com.hebit.app.ui.screens.habits.viewmodel.HabitFrequencyType
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import java.util.UUID
import com.hebit.app.ui.screens.habits.getIconByName
import com.hebit.app.ui.screens.habits.viewmodel.HabitReminder
import com.hebit.app.ui.screens.habits.viewmodel.TimeOfDayOptions


val sampleIcons = listOf("fitness_center", "book", "star", "work", "home", "local_fire_department", "settings", "notifications", "link")
val sampleColors = listOf("#FF0000", "#00FF00", "#0000FF", "#FFFF00", "#FF00FF", "#00FFFF", "#FFA500") // Red, Green, Blue, Yellow, Magenta, Cyan, Orange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditHabitScreen(
    habitId: String?, // Null for create, non-null for edit
    onNavigateBack: () -> Unit,
    viewModel: CreateEditHabitViewModel = hiltViewModel()
) {
    // Initialize ViewModel with habitId
    LaunchedEffect(habitId) {
        viewModel.initializeWithHabitId(habitId)
    }

    val uiState by viewModel.uiState
    val context = LocalContext.current

    LaunchedEffect(uiState) {
        if (uiState is CreateEditHabitUiState.Success && (uiState as CreateEditHabitUiState.Success).isSaved) {
            // Could show a toast message here
            // Toast.makeText(context, "Habit saved!", Toast.LENGTH_SHORT).show()
            onNavigateBack() // Navigate back after save
        }
    }

    // Show error toast if there's a save error
    LaunchedEffect(uiState) {
        if (uiState is CreateEditHabitUiState.Success) {
            val errorMessage = (uiState as CreateEditHabitUiState.Success).saveError
            if (errorMessage != null) {
                println("CreateEditHabitScreen: Error saving habit: $errorMessage")
                // TODO: Show error toast or snackbar
                // Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState is CreateEditHabitUiState.Success && (uiState as CreateEditHabitUiState.Success).isEditMode) "Edit Habit"
                        else "Create Habit"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is CreateEditHabitUiState.Loading -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Text("Loading habit details...")
                }
            }
            is CreateEditHabitUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                    Button(onClick = onNavigateBack) { Text("Go Back") }
                }
            }
            is CreateEditHabitUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()), // Make column scrollable
                    horizontalAlignment = Alignment.Start // Align content to start for labels
                ) {
                    OutlinedTextField(
                        value = viewModel.habitName,
                        onValueChange = { viewModel.onHabitNameChanged(it) },
                        label = { Text("Habit Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = viewModel.habitDescription,
                        onValueChange = { viewModel.onHabitDescriptionChanged(it) },
                        label = { Text("Description (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- Icon Selector ---
                    Text("Icon (Optional)", style = MaterialTheme.typography.titleMedium)
                    IconSelector(selectedIconName = viewModel.selectedIconName, onIconSelected = viewModel::onIconSelected)
                    Spacer(modifier = Modifier.height(16.dp))

                    // --- Color Selector ---
                    Text("Color (Optional)", style = MaterialTheme.typography.titleMedium)
                    ColorSelector(selectedColorHex = viewModel.selectedColorHex, onColorSelected = viewModel::onColorSelected)
                    Spacer(modifier = Modifier.height(16.dp))

                    // --- Time of Day Selector ---
                    Text("Time of Day (Optional)", style = MaterialTheme.typography.titleMedium)
                    TimeOfDaySelector(selectedTimeOption = viewModel.selectedTimeOfDay, onTimeOptionSelected = viewModel::onTimeOfDayChanged)
                    Spacer(modifier = Modifier.height(16.dp))

                    // --- Frequency Selection UI --- 
                    Text("Frequency *", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    FrequencyTypeSegmentedButtonSelector(viewModel.frequencySelection.type, viewModel::onFrequencyTypeChanged)
                    Spacer(modifier = Modifier.height(8.dp))
                    FrequencyConfigurator(viewModel)
                    // --- End of Frequency Selection UI ---

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Reminders (Optional)", style = MaterialTheme.typography.titleMedium)
                    ReminderSection(viewModel.reminders, 
                        onAddReminder = { 
                            // In a real app, this would open a time picker dialog
                            viewModel.addReminder(HabitReminder(id = UUID.randomUUID().toString(), time = "10:00", label = "Morning check-in"))
                        },
                        onRemoveReminder = viewModel::removeReminder
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Link to Goal (Optional)", style = MaterialTheme.typography.titleMedium)
                    GoalLinkSection(viewModel.linkedGoalName, 
                        onLinkGoal = {
                             // In a real app, this would open a goal selection screen/dialog
                            viewModel.linkGoal(UUID.randomUUID().toString(), "Achieve Peak Productivity")
                        },
                        onUnlinkGoal = viewModel::unlinkGoal
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Spacer(modifier = Modifier.weight(1f)) // Push buttons to bottom

                    if (state.saveError != null) {
                        Text(state.saveError, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Button(
                        onClick = { viewModel.saveHabit() },
                        enabled = state.canSave,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Habit")
                    }

                    if (state.isEditMode) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { /* TODO: Handle archive habit */ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Archive Habit")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { /* TODO: Handle delete habit */ },
                            // colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer), // Example for destructive action
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Delete Habit")
                        }
                    }
                }
            }
        }
    }
}

// Preview for Create Mode
@Preview(showBackground = true, name = "Create Habit Screen")
@Composable
fun CreateHabitScreenPreview() {
    MaterialTheme {
        // For previews, we'll create a simple composable that simulates the UI without the ViewModel
        CreateEditHabitScreenPreview(habitId = null)
    }
}

// Preview for Edit Mode (simulating loaded state)
@Preview(showBackground = true, name = "Edit Habit Screen (Loaded DAILY)")
@Composable
fun EditHabitScreenPreview_LoadedDaily() {
    MaterialTheme {
        CreateEditHabitScreenPreview(habitId = "editMe")
    }
}

@Preview(showBackground = true, name = "Edit Habit Screen (Loaded WEEKLY)")
@Composable
fun EditHabitScreenPreview_LoadedWeekly() {
    MaterialTheme {
        CreateEditHabitScreenPreview(habitId = "sampleHabitIdForWeekly")
    }
}

@Preview(showBackground = true, name = "Create Habit Screen - Monthly")
@Composable
fun CreateHabitScreenMonthlyPreview() {
    MaterialTheme {
        CreateEditHabitScreenPreview(habitId = null)
    }
}

// Simplified preview composable that doesn't use the actual ViewModel
@Composable
private fun CreateEditHabitScreenPreview(habitId: String?) {
    // This is a simplified preview version that doesn't require ViewModel injection
    // In a real app, you might use a fake/mock ViewModel for previews
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (habitId != null) "Edit Habit Screen Preview" else "Create Habit Screen Preview",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrequencyTypeSegmentedButtonSelector(selectedType: HabitFrequencyType, onTypeSelected: (HabitFrequencyType) -> Unit) {
    val types = HabitFrequencyType.values()
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        types.forEachIndexed { index, type ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = types.size),
                onClick = { onTypeSelected(type) },
                selected = type == selectedType,
                icon = { /* Can add icons later if desired */ }
            ) {
                Text(type.name.lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeOfDaySelector(selectedTimeOption: String?, onTimeOptionSelected: (String) -> Unit) {
    val options = TimeOfDayOptions.getAsList()
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedTimeOption ?: TimeOfDayOptions.ANY_TIME,
            onValueChange = {}, // Not directly editable
            readOnly = true,
            label = { Text("Preferred Time") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor() 
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onTimeOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun IconSelector(selectedIconName: String?, onIconSelected: (String) -> Unit) {
    LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(sampleIcons) { iconName ->
            val isSelected = selectedIconName == iconName
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, CircleShape)
                    .clickable { onIconSelected(iconName) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getIconByName(iconName),
                    contentDescription = iconName,
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ColorSelector(selectedColorHex: String?, onColorSelected: (String) -> Unit) {
    LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(sampleColors) { colorHex ->
            val isSelected = selectedColorHex == colorHex
            val color = Color(android.graphics.Color.parseColor(colorHex))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(2.dp, if (isSelected) MaterialTheme.colorScheme.outlineVariant else Color.Transparent, CircleShape)
                    .clickable { onColorSelected(colorHex) }
            )
        }
    }
}

@Composable
fun FrequencyConfigurator(viewModel: CreateEditHabitViewModel) {
    when (viewModel.frequencySelection.type) {
        HabitFrequencyType.DAILY -> {
            Text("Select days:", style = MaterialTheme.typography.titleSmall)
            DaysOfWeekSelector(selectedDays = viewModel.selectedDaysOfWeek, onDayToggle = viewModel::onDayOfWeekToggled)
        }
        HabitFrequencyType.WEEKLY -> {
            OutlinedTextField(
                value = viewModel.frequencySelection.config.timesPerPeriod?.toString() ?: "",
                onValueChange = { viewModel.onTimesPerPeriodChanged(it) },
                label = { Text("Times per week") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Optionally, select specific days:", style = MaterialTheme.typography.titleSmall)
            DaysOfWeekSelector(selectedDays = viewModel.selectedDaysOfWeek, onDayToggle = viewModel::onDayOfWeekToggled)
        }
        HabitFrequencyType.MONTHLY -> {
            OutlinedTextField(
                value = viewModel.frequencySelection.config.timesPerPeriod?.toString() ?: "",
                onValueChange = { viewModel.onTimesPerPeriodChanged(it) },
                label = { Text("Times per month") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Optionally, select specific dates of the month:", style = MaterialTheme.typography.titleSmall)
            DatesOfMonthSelector(selectedDates = viewModel.selectedDatesOfMonth, onDateToggle = viewModel::onDateOfMonthToggled)
        }
        HabitFrequencyType.SPECIFIC_DATES -> {
            SpecificDatesSelector(viewModel)
        }
    }
}

@Composable
fun DaysOfWeekSelector(selectedDays: List<DayOfWeek>, onDayToggle: (DayOfWeek) -> Unit) {
    LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        items(DayOfWeek.values()) { day ->
            val isSelected = selectedDays.contains(day)
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        shape = MaterialTheme.shapes.small
                    )
                    .background(
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.small
                    )
                    .clickable { onDayToggle(day) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun DatesOfMonthSelector(selectedDates: List<Int>, onDateToggle: (Int) -> Unit) {
    val dates = (1..31).toList()
    // Using a LazyVerticalGrid to display dates in a grid for better space utilization.
    // A simple LazyRow might become too wide.
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 48.dp), // Adjust minSize for desired item width
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 200.dp), // Limit height to prevent excessive scrolling within the main scroll
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(dates) { date ->
            val isSelected = selectedDates.contains(date)
            Box(
                modifier = Modifier
                    .aspectRatio(1f) // Makes items square
                    .border(
                        width = 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        shape = MaterialTheme.shapes.small
                    )
                    .background(
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.small
                    )
                    .clickable { onDateToggle(date) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = date.toString(),
                    textAlign = TextAlign.Center,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
    // TODO: Consider adding a "Last Day of Month" special toggle if required by backend/feature spec.
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecificDatesSelector(viewModel: CreateEditHabitViewModel) {
    var showDatePicker by remember { mutableStateOf(false) }
    val selectedDates = viewModel.selectedSpecificDates

    Column {
        Button(onClick = { showDatePicker = true }) {
            Icon(Icons.Default.DateRange, contentDescription = "Add Specific Date")
            Spacer(Modifier.width(8.dp))
            Text("Add Specific Date")
        }
        Spacer(Modifier.height(8.dp))
        if (selectedDates.isNotEmpty()) {
            Text("Selected Dates:", style = MaterialTheme.typography.titleSmall)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)){
                items(selectedDates.sorted()) { date ->
                    InputChip(
                        selected = false, 
                        onClick = { viewModel.onSpecificDateRemoved(date) }, 
                        label = {Text(date.format(DateTimeFormatter.ISO_LOCAL_DATE))},
                        trailingIcon = { Icon(Icons.Filled.Close, contentDescription = "Remove date") }
                    )
                }
            }
        } else {
            Text("No specific dates selected.")
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    datePickerState.selectedDateMillis?.let {
                        // Correct conversion from millis to LocalDate, considering UTC for epoch day
                        val selectedDate = LocalDate.ofEpochDay(it / (1000 * 60 * 60 * 24))
                        viewModel.onSpecificDateAdded(selectedDate)
                    }
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun ReminderSection(reminders: List<HabitReminder>, onAddReminder: () -> Unit, onRemoveReminder: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (reminders.isEmpty()) {
            Text("No reminders set.", style = MaterialTheme.typography.bodyMedium)
        } else {
            reminders.forEach { reminder ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${reminder.time}${reminder.label?.let { " - $it" } ?: ""}", style = MaterialTheme.typography.bodyLarge)
                    IconButton(onClick = { onRemoveReminder(reminder.id) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Remove Reminder")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onAddReminder, modifier = Modifier.align(Alignment.End)) {
            Icon(Icons.Filled.AddCircleOutline, contentDescription = "Add Reminder")
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add Reminder")
        }
    }
}

@Composable
fun GoalLinkSection(linkedGoalName: String?, onLinkGoal: () -> Unit, onUnlinkGoal: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (linkedGoalName != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Link, contentDescription = "Linked Goal", tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Linked to: ", style = MaterialTheme.typography.bodyLarge)
                Text(linkedGoalName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onUnlinkGoal, modifier = Modifier.align(Alignment.End)) {
                Icon(Icons.Filled.LinkOff, contentDescription = "Unlink Goal")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Unlink Goal")
            }
        } else {
            Text("Not linked to any goal.", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onLinkGoal, modifier = Modifier.align(Alignment.End)) {
                Icon(Icons.Filled.Link, contentDescription = "Link Goal")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Link to Goal")
            }
        }
    }
} 