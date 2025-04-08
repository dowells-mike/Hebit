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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.hebit.app.domain.model.TaskCreationData
import com.hebit.app.domain.model.TaskPriority
import android.app.TimePickerDialog
import android.content.Context
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.hebit.app.domain.model.Resource
import com.hebit.app.ui.screens.tasks.parseRecurrencePattern
import com.hebit.app.ui.screens.tasks.parseSubtasks
import com.hebit.app.ui.screens.tasks.parseReminderSettings

data class RecurrencePattern(
    val type: RecurrenceType = RecurrenceType.NONE,
    val interval: Int = 1,
    val endDate: LocalDate? = null,
    val daysOfWeek: List<Int> = emptyList() // For weekly recurrence
)

data class ReminderSettings(
    val isEnabled: Boolean = false,
    val minutes: Int = 15,
    val time: LocalTime? = null,
    val date: LocalDate? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCreationScreen(
    taskId: String = "",
    isEditMode: Boolean = false,
    onSaveComplete: () -> Unit = {},
    onCancel: () -> Unit = {},
    onDismiss: () -> Unit = {},
    onSaveTask: (TaskCreationData) -> Unit = {},
    viewModel: TaskViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    
    var taskTitle by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(LocalTime.of(14, 0)) }
    var selectedPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var selectedCategory by remember { mutableStateOf("Work") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    
    // Define these state variables before they're used
    var showRecurrenceOptions by remember { mutableStateOf(false) }
    var recurrencePattern by remember { mutableStateOf(RecurrencePattern()) }
    
    // Subtasks
    val subtasks = remember { mutableStateListOf<SubTask>() }
    var showAddSubtask by remember { mutableStateOf(false) }
    var newSubtaskTitle by remember { mutableStateOf("") }
    
    // Reminder settings
    var showReminderOptions by remember { mutableStateOf(false) }
    var reminderSettings by remember { mutableStateOf(ReminderSettings()) }
    
    // Permission states
    var hasNotificationPermission by remember { 
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        ) 
    }
    
    // Load task data if in edit mode
    LaunchedEffect(taskId) {
        if (isEditMode && taskId.isNotBlank()) {
            viewModel.getTaskById(taskId)
        }
    }
    
    // Observe task data
    val taskState by viewModel.selectedTaskState.collectAsState()
    
    // Update UI with task data if in edit mode
    LaunchedEffect(taskState) {
        if (isEditMode && taskState is Resource.Success) {
            val task = (taskState as? Resource.Success)?.data
            if (task != null) {
                taskTitle = task.title
                taskDescription = task.description ?: ""
                selectedDate = task.dueDateTime?.toLocalDate()
                selectedTime = task.dueDateTime?.toLocalTime()
                selectedPriority = when (task.priority) {
                    1 -> TaskPriority.LOW
                    2 -> TaskPriority.MEDIUM
                    3 -> TaskPriority.HIGH
                    else -> TaskPriority.MEDIUM
                }
                selectedCategory = task.category
                
                // Load recurrence pattern if available
                task.metadata["recurrence"]?.let {
                    recurrencePattern = RecurrencePattern(
                        type = parseRecurrencePattern(it) ?: RecurrenceType.NONE
                    )
                }
                
                // Load reminder settings if available
                task.metadata["reminder"]?.let { reminderStr ->
                    val parts = reminderStr.split(",")
                    if (parts.isNotEmpty()) {
                        val minutes = parts[0].toIntOrNull() ?: 15
                        val timeString = parts.getOrNull(1) ?: ""
                        
                        if (timeString.isNotBlank()) {
                            reminderSettings = ReminderSettings(
                                isEnabled = true,
                                minutes = 0,
                                time = try {
                                    LocalTime.parse(timeString, DateTimeFormatter.ofPattern("h:mm a"))
                                } catch (e: Exception) {
                                    null
                                }
                            )
                        } else {
                            reminderSettings = ReminderSettings(
                                isEnabled = true,
                                minutes = minutes
                            )
                        }
                    }
                }
                
                // Load subtasks if available
                task.metadata["subtasks"]?.let {
                    subtasks.clear()
                    subtasks.addAll(parseSubtasks(it))
                }
            }
        }
    }
    
    // Permission launcher
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasNotificationPermission = isGranted
        }
    )
    
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
    
    // Time picker handler
    fun showTimePickerDialog() {
        val currentTime = selectedTime ?: LocalTime.now()
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                selectedTime = LocalTime.of(hourOfDay, minute)
            },
            currentTime.hour,
            currentTime.minute,
            false
        ).show()
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (isEditMode) "Edit Task" else "New Task") },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (isEditMode) {
                            onCancel()
                        } else {
                            onDismiss()
                        }
                    }) {
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
                            
                            if (isEditMode) {
                                viewModel.updateTaskWithData(taskId, taskData)
                                onSaveComplete()
                            } else {
                                onSaveTask(taskData)
                            }
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
                    .clickable { 
                        // Request notification permission if needed before showing reminder options
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            showReminderOptions = true
                        }
                    },
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
    
    // Handle time picker dialog
    LaunchedEffect(showTimePicker) {
        if (showTimePicker) {
            showTimePickerDialog()
            showTimePicker = false
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
            },
            properties = DialogProperties(dismissOnClickOutside = true)
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
        var tempTime by remember { mutableStateOf(reminderSettings.time ?: LocalTime.now()) }
        var showReminderTimePicker by remember { mutableStateOf(false) }
        
        // Function to show time picker
        fun showReminderTimePickerDialog() {
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    tempTime = LocalTime.of(hourOfDay, minute)
                },
                tempTime.hour,
                tempTime.minute,
                false
            ).show()
        }
        
        // Handle reminder time picker
        LaunchedEffect(showReminderTimePicker) {
            if (showReminderTimePicker) {
                showReminderTimePickerDialog()
                showReminderTimePicker = false
            }
        }
        
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
                        
                        // Option type selection
                        Text("Reminder Type", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        var useExactTime by remember { mutableStateOf(reminderSettings.time != null) }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = !useExactTime,
                                onClick = { useExactTime = false }
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = "Minutes before due date",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.clickable { useExactTime = false }
                            )
                        }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = useExactTime,
                                onClick = { useExactTime = true }
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = "At specific time",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.clickable { useExactTime = true }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (useExactTime) {
                            // Specific time selection
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showReminderTimePicker = true }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Time",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = tempTime.format(DateTimeFormatter.ofPattern("h:mm a")),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        } else {
                            // Minutes before selection
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
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        reminderSettings = ReminderSettings(
                            isEnabled = tempEnabled,
                            minutes = tempMinutes.toIntOrNull() ?: 15,
                            time = if (tempEnabled && reminderSettings.time != null) tempTime else null
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

// Move all these duplicate function definitions to a common utility file
// For now, import them from TaskDetailScreen
// Helper function to parse recurrence pattern from metadata string
/*
fun parseRecurrencePattern(recurrenceStr: String): RecurrenceType? {
    val parts = recurrenceStr.split(",")
    if (parts.isNotEmpty()) {
        return try {
            RecurrenceType.valueOf(parts[0])
        } catch (e: Exception) {
            null
        }
    }
    return null
}

// Helper function to parse subtasks from metadata string
fun parseSubtasks(subtasksStr: String): List<SubTask> {
    if (subtasksStr.isBlank()) return emptyList()
    
    return subtasksStr.split(",").mapNotNull { subTaskStr ->
        val parts = subTaskStr.split(":")
        if (parts.size >= 3) {
            SubTask(
                id = parts[0],
                title = parts[1],
                isCompleted = parts[2].toBoolean()
            )
        } else null
    }
}
*/
