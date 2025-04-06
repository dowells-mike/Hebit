package com.hebit.app.ui.screens.tasks

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.hebit.app.domain.model.TaskCreationData
import com.hebit.app.domain.model.TaskPriority

data class SubTask(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val isCompleted: Boolean = false
)

data class RecurrencePattern(
    val type: RecurrenceType = RecurrenceType.NONE,
    val interval: Int = 1,
    val endDate: LocalDate? = null,
    val daysOfWeek: List<Int> = emptyList() // For weekly recurrence
)

enum class RecurrenceType {
    NONE, DAILY, WEEKLY, MONTHLY, YEARLY
}

data class ReminderSettings(
    val isEnabled: Boolean = false,
    val minutes: Int = 15,
    val time: LocalTime? = null,
    val date: LocalDate? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCreationScreen(
    onDismiss: () -> Unit,
    onSaveTask: (TaskCreationData) -> Unit
) {
    var taskTitle by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(LocalTime.of(14, 0)) }
    var selectedPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var selectedCategory by remember { mutableStateOf("Work") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    
    // Recurrence settings
    var showRecurrenceOptions by remember { mutableStateOf(false) }
    var recurrencePattern by remember { mutableStateOf(RecurrencePattern()) }
    
    // Subtasks
    val subtasks = remember { mutableStateListOf<SubTask>() }
    var showAddSubtask by remember { mutableStateOf(false) }
    var newSubtaskTitle by remember { mutableStateOf("") }
    
    // Reminder settings
    var showReminderOptions by remember { mutableStateOf(false) }
    var reminderSettings by remember { mutableStateOf(ReminderSettings()) }
    
    // Text formatting options
    var isBold by remember { mutableStateOf(false) }
    var isItalic by remember { mutableStateOf(false) }
    var isBulletList by remember { mutableStateOf(false) }
    var isNumberedList by remember { mutableStateOf(false) }
    
    // Available categories
    val categories = listOf("Work", "Personal", "Shopping", "Health", "Education")
    
    // For date picker
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate?.toEpochDay()?.let { it * 24 * 60 * 60 * 1000 } 
            ?: LocalDate.now().toEpochDay() * 24 * 60 * 60 * 1000
    )
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("New Task") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val taskData = TaskCreationData(
                                title = taskTitle,
                                description = taskDescription.ifBlank { null },
                                dueDate = selectedDate,
                                dueTime = selectedTime,
                                priority = selectedPriority,
                                category = selectedCategory,
                                labels = emptyList(),
                                subtasks = subtasks.toList(),
                                recurrencePattern = recurrencePattern,
                                reminderSettings = reminderSettings
                            )
                            onSaveTask(taskData)
                        },
                        enabled = taskTitle.isNotBlank()
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Task title input - required
            OutlinedTextField(
                value = taskTitle,
                onValueChange = { taskTitle = it },
                placeholder = { Text("Task title") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent
                ),
                singleLine = true
            )
            
            Divider()
            
            // Text formatting options
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = { isBold = !isBold },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatBold,
                        contentDescription = "Bold",
                        tint = if (isBold) MaterialTheme.colorScheme.primary else LocalContentColor.current
                    )
                }
                
                IconButton(
                    onClick = { isItalic = !isItalic },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatItalic,
                        contentDescription = "Italic",
                        tint = if (isItalic) MaterialTheme.colorScheme.primary else LocalContentColor.current
                    )
                }
                
                IconButton(
                    onClick = { isBulletList = !isBulletList; if (isBulletList) isNumberedList = false },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatListBulleted,
                        contentDescription = "Bullet List",
                        tint = if (isBulletList) MaterialTheme.colorScheme.primary else LocalContentColor.current
                    )
                }
                
                IconButton(
                    onClick = { isNumberedList = !isNumberedList; if (isNumberedList) isBulletList = false },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatListNumbered,
                        contentDescription = "Numbered List",
                        tint = if (isNumberedList) MaterialTheme.colorScheme.primary else LocalContentColor.current
                    )
                }
            }
            
            Divider()
            
            // Description input - optional
            OutlinedTextField(
                value = taskDescription,
                onValueChange = { taskDescription = it },
                placeholder = { Text("Add description...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent
                ),
                minLines = 3
            )
            
            Divider()
            
            // Date & Time pickers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Date",
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = selectedDate?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) ?: "Set due date",
                    modifier = Modifier.clickable { showDatePicker = true }
                )
                
                Spacer(modifier = Modifier.width(24.dp))
                
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Time",
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = selectedTime?.format(DateTimeFormatter.ofPattern("h:mm a")) ?: "Set time",
                    modifier = Modifier.clickable { showTimePicker = true }
                )
            }
            
            Divider()
            
            // Priority selection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Priority:",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Low priority button
                FilterChip(
                    selected = selectedPriority == TaskPriority.LOW,
                    onClick = { selectedPriority = TaskPriority.LOW },
                    label = { Text("Low") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Flag,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Medium priority button
                FilterChip(
                    selected = selectedPriority == TaskPriority.MEDIUM,
                    onClick = { selectedPriority = TaskPriority.MEDIUM },
                    label = { Text("Medium") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Flag,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // High priority button
                FilterChip(
                    selected = selectedPriority == TaskPriority.HIGH,
                    onClick = { selectedPriority = TaskPriority.HIGH },
                    label = { Text("High") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Flag,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                )
            }
            
            Divider()
            
            // Category selection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .clickable { showCategoryPicker = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Folder,
                    contentDescription = "Category",
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = selectedCategory,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Select"
                )
            }
            
            Divider()
            
            // Recurrence settings
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .clickable { showRecurrenceOptions = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Repeat,
                    contentDescription = "Recurrence",
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Recurrence",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = when(recurrencePattern.type) {
                        RecurrenceType.NONE -> "Not repeating"
                        RecurrenceType.DAILY -> "Daily"
                        RecurrenceType.WEEKLY -> "Weekly"
                        RecurrenceType.MONTHLY -> "Monthly"
                        RecurrenceType.YEARLY -> "Yearly"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Select"
                )
            }
            
            Divider()
            
            // Subtasks section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckBox,
                    contentDescription = "Subtasks",
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Subtasks",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                IconButton(onClick = { showAddSubtask = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Subtask"
                    )
                }
            }
            
            // Display existing subtasks
            if (subtasks.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    subtasks.forEachIndexed { index, subtask ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = subtask.isCompleted,
                                onCheckedChange = { isChecked ->
                                    subtasks[index] = subtask.copy(isCompleted = isChecked)
                                }
                            )
                            
                            Text(
                                text = subtask.title,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp)
                            )
                            
                            IconButton(
                                onClick = {
                                    subtasks.removeAt(index)
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            Divider()
            
            // Reminder settings
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .clickable { showReminderOptions = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Reminder",
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Reminder",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Fix complex expression issue with smart cast
                val reminder = remember(reminderSettings) {
                    if (reminderSettings.isEnabled) {
                        val time = reminderSettings.time
                        if (time != null) {
                            "${time.format(DateTimeFormatter.ofPattern("h:mm a"))}"
                        } else {
                            "${reminderSettings.minutes} minutes before"
                        }
                    } else {
                        "No reminder"
                    }
                }
                
                Text(
                    text = reminder,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Select"
                )
            }
            
            Divider()
            
            // Bottom buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text("Cancel")
                }
                
                Button(
                    onClick = {
                        val taskData = TaskCreationData(
                            title = taskTitle,
                            description = taskDescription.ifBlank { null },
                            dueDate = selectedDate,
                            dueTime = selectedTime,
                            priority = selectedPriority,
                            category = selectedCategory,
                            labels = emptyList(),
                            subtasks = subtasks.toList(),
                            recurrencePattern = recurrencePattern,
                            reminderSettings = reminderSettings
                        )
                        onSaveTask(taskData)
                    },
                    enabled = taskTitle.isNotBlank(),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    Text("Save Task")
                }
            }
        }
    }
    
    // Add Subtask Dialog
    if (showAddSubtask) {
        AlertDialog(
            onDismissRequest = { 
                showAddSubtask = false
                newSubtaskTitle = ""
            },
            title = { Text("Add Subtask") },
            text = {
                OutlinedTextField(
                    value = newSubtaskTitle,
                    onValueChange = { newSubtaskTitle = it },
                    placeholder = { Text("Subtask title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newSubtaskTitle.isNotBlank()) {
                            subtasks.add(SubTask(title = newSubtaskTitle))
                            newSubtaskTitle = ""
                        }
                        showAddSubtask = false
                    },
                    enabled = newSubtaskTitle.isNotBlank()
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showAddSubtask = false
                        newSubtaskTitle = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { 
                Button(
                    onClick = { 
                        // Extract the selected date from state
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                        }
                        showDatePicker = false 
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDatePicker = false 
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = { Text("Select Date") },
                showModeToggle = false,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
    
    // Category Picker Dialog
    if (showCategoryPicker) {
        AlertDialog(
            onDismissRequest = { showCategoryPicker = false },
            title = { Text("Select Category") },
            text = {
                Column {
                    categories.forEach { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCategory = category
                                    showCategoryPicker = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedCategory == category,
                                onClick = {
                                    selectedCategory = category
                                    showCategoryPicker = false
                                }
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = category,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryPicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Recurrence Options Dialog
    if (showRecurrenceOptions) {
        var tempRecurrenceType by remember { mutableStateOf(recurrencePattern.type) }
        var tempInterval by remember { mutableStateOf(recurrencePattern.interval.toString()) }
        
        AlertDialog(
            onDismissRequest = { showRecurrenceOptions = false },
            title = { Text("Set Recurrence") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Recurrence type options
                    Text("Repeat", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Column {
                        RecurrenceType.values().forEach { type ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        tempRecurrenceType = type
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = tempRecurrenceType == type,
                                    onClick = {
                                        tempRecurrenceType = type
                                    }
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = when(type) {
                                        RecurrenceType.NONE -> "Do not repeat"
                                        RecurrenceType.DAILY -> "Daily"
                                        RecurrenceType.WEEKLY -> "Weekly"
                                        RecurrenceType.MONTHLY -> "Monthly"
                                        RecurrenceType.YEARLY -> "Yearly"
                                    },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    
                    // Show interval settings if a recurrence type is selected
                    if (tempRecurrenceType != RecurrenceType.NONE) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Repeat every", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = tempInterval,
                            onValueChange = { 
                                // Only allow numeric input
                                if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                    tempInterval = it
                                }
                            },
                            label = { 
                                Text(
                                    when(tempRecurrenceType) {
                                        RecurrenceType.DAILY -> "days"
                                        RecurrenceType.WEEKLY -> "weeks"
                                        RecurrenceType.MONTHLY -> "months"
                                        RecurrenceType.YEARLY -> "years"
                                        else -> ""
                                    }
                                ) 
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        recurrencePattern = RecurrencePattern(
                            type = tempRecurrenceType,
                            interval = tempInterval.toIntOrNull() ?: 1
                        )
                        showRecurrenceOptions = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRecurrenceOptions = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Reminder Settings Dialog
    if (showReminderOptions) {
        var tempEnabled by remember { mutableStateOf(reminderSettings.isEnabled) }
        var tempMinutes by remember { mutableStateOf(reminderSettings.minutes.toString()) }
        
        AlertDialog(
            onDismissRequest = { showReminderOptions = false },
            title = { Text("Set Reminder") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Enable reminder",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Switch(
                            checked = tempEnabled,
                            onCheckedChange = { tempEnabled = it }
                        )
                    }
                    
                    if (tempEnabled) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = tempMinutes,
                            onValueChange = { 
                                // Only allow numeric input
                                if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                    tempMinutes = it
                                }
                            },
                            label = { Text("Minutes before") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        reminderSettings = ReminderSettings(
                            isEnabled = tempEnabled,
                            minutes = tempMinutes.toIntOrNull() ?: 15
                        )
                        showReminderOptions = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReminderOptions = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Simple FlowRow implementation
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val placeables = measurables.map { measurable ->
            measurable.measure(constraints.copy(minWidth = 0))
        }
        
        var yPosition = 0
        var xPosition = 0
        var rowHeight = 0
        val rowWidths = mutableListOf<Int>()
        val rowHeights = mutableListOf<Int>()
        val itemPositions = mutableListOf<Pair<Int, Int>>()
        
        // Calculate positions
        placeables.forEach { placeable ->
            if (xPosition + placeable.width > constraints.maxWidth) {
                // Move to next row
                rowWidths.add(xPosition)
                rowHeights.add(rowHeight)
                yPosition += rowHeight
                xPosition = 0
                rowHeight = 0
            }
            
            itemPositions.add(Pair(xPosition, yPosition))
            xPosition += placeable.width + 8  // 8dp spacing
            rowHeight = maxOf(rowHeight, placeable.height)
        }
        
        // Add the last row
        if (xPosition > 0) {
            rowWidths.add(xPosition)
            rowHeights.add(rowHeight)
        }
        
        // Set the size of the layout
        val width = rowWidths.maxOfOrNull { it } ?: 0
        val height = yPosition + rowHeight
        
        // Position elements
        layout(width, height) {
            placeables.forEachIndexed { index, placeable ->
                val (x, y) = itemPositions[index]
                placeable.place(x, y)
            }
        }
    }
}
