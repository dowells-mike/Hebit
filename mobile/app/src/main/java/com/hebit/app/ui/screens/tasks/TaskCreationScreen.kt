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
import androidx.compose.foundation.lazy.LazyRow
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
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.hebit.app.domain.model.Resource
import com.hebit.app.ui.screens.tasks.parseRecurrencePattern
import com.hebit.app.ui.screens.tasks.parseSubtasks
import com.hebit.app.ui.screens.tasks.parseReminderSettings
import com.hebit.app.ui.screens.categories.CategoryViewModel
import com.hebit.app.domain.model.Category

// Imports for ical4j
import net.fortuna.ical4j.model.Recur
import net.fortuna.ical4j.model.DateList
import net.fortuna.ical4j.model.DateTime // For parsing UNTIL if it includes time
import java.time.ZoneId // For converting ical4j Date to LocalDate
import net.fortuna.ical4j.model.WeekDay // For BYDAY parsing
// Required for Recur.Builder and Frequency
import net.fortuna.ical4j.model.Recur.Builder as RecurBuilder
import net.fortuna.ical4j.model.parameter.Value
import net.fortuna.ical4j.model.property.RRule
import java.util.Date // For converting LocalDate to java.util.Date for UNTIL

// Import new recurrence models and utils
import com.hebit.app.domain.model.RecurrencePattern
import com.hebit.app.domain.model.RecurrenceType
import com.hebit.app.util.generateRRuleString
import com.hebit.app.util.formatRecurrencePattern
import com.hebit.app.util.parseRRuleStringToPattern
import com.hebit.app.domain.model.Reminder
import com.hebit.app.domain.model.ReminderType
import java.time.Instant
import java.time.LocalDateTime
import java.util.Calendar

data class ReminderSettings(
    val isEnabled: Boolean = false,
    val minutes: Int = 15,
    val time: LocalTime? = null,
    val date: LocalDate? = null
)

// Function to format the reminder summary
fun formatRemindersSummary(reminders: List<Reminder>, taskDueDate: LocalDate?): String {
    if (reminders.isEmpty()) return "No reminder"
    if (reminders.size == 1) {
        val reminder = reminders.first()
        return when (reminder.type) {
            ReminderType.RELATIVE -> {
                val offset = reminder.offsetMinutes?.let { Math.abs(it) } ?: 0
                when {
                    offset == 0 -> "At time of due date"
                    offset < 60 -> "$offset minutes before"
                    offset % 60 == 0 -> {
                        val hours = offset / 60
                        if (hours == 1) "1 hour before" else "$hours hours before"
                    }
                    else -> "$offset minutes before" // e.g. 75 minutes
                }
            }
            ReminderType.ABSOLUTE -> {
                reminder.absoluteDateTime?.format(DateTimeFormatter.ofPattern("MMM d, h:mm a")) ?: "On specific date"
            }
        }
    }
    return "${reminders.size} reminders set"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCreationScreen(
    taskId: String = "",
    isEditMode: Boolean = false,
    onSaveComplete: () -> Unit = {},
    onCancel: () -> Unit = {},
    onDismiss: () -> Unit = {},
    onSaveTask: (TaskCreationData) -> Unit = {},
    viewModel: TaskViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    onNavigateToCreateCategory: () -> Unit
) {
    val context = LocalContext.current
    
    var taskTitle by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(LocalTime.of(14, 0)) }
    var selectedPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    
    // State for categories
    val categoriesResource by categoryViewModel.categoriesState.collectAsState()
    var availableCategories by remember { mutableStateOf<List<Category>>(emptyList()) }
    // Initialize selectedCategoryName from existing task if in edit mode, else a default.
    // selectedCategoryObject will be derived.
    var selectedCategoryName by remember { 
        mutableStateOf(if (isEditMode) "" else "Uncategorized") // Default to Uncategorized for new tasks
    }
    var selectedCategoryObject by remember { mutableStateOf<Category?>(null) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    
    // Define these state variables before they're used
    var showRecurrenceOptions by remember { mutableStateOf(false) }
    var recurrencePattern by remember { mutableStateOf(RecurrencePattern()) }
    
    // Subtasks - using the existing mutableStateListOf
    val currentSubtasks = remember { mutableStateListOf<SubTask>() }
    var newSubtaskTitle by remember { mutableStateOf("") }
    
    // Reminder states
    var showReminderDialog by remember { mutableStateOf(false) }
    var reminders by remember { mutableStateOf<List<Reminder>>(emptyList()) }
    var showAddEditReminderDialog by remember { mutableStateOf(false) } // New state for Add/Edit dialog
    var editingReminder by remember { mutableStateOf<Reminder?>(null) } // To hold reminder being edited, or null for new
    
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
                taskDescription = task.description ?: "" // Ensure not null
                selectedDate = task.dueDateTime?.toLocalDate()
                selectedTime = task.dueDateTime?.toLocalTime()
                selectedPriority = when (task.priority) {
                    1 -> TaskPriority.LOW
                    2 -> TaskPriority.MEDIUM
                    3 -> TaskPriority.HIGH
                    else -> TaskPriority.MEDIUM
                }
                selectedCategoryName = task.category ?: "Uncategorized"
                // selectedCategoryObject will be updated by another LaunchedEffect based on availableCategories
                
                recurrencePattern = parseRRuleStringToPattern(task.recurrenceRuleString)
                
                // Load reminders from the new field
                reminders = task.reminders ?: emptyList()

                // Load subtasks from metadata
                val subtasksString = task.metadata["subtasks"] as? String
                currentSubtasks.clear()
                currentSubtasks.addAll(parseSubtasks(subtasksString)) // parseSubtasks is expected to handle null input gracefully
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
    
    // Update availableCategories and selectedCategoryObject when categoriesResource changes
    LaunchedEffect(categoriesResource, selectedCategoryName, isEditMode) { // Added isEditMode to dependencies
        Log.d("TaskCreationScreen", "Categories LaunchedEffect triggered. categoriesResource: $categoriesResource")
        if (categoriesResource is Resource.Success) {
            val cats = (categoriesResource as Resource.Success<List<Category>>).data
            Log.d("TaskCreationScreen", "Categories Resource.Success. Data: ${cats?.size ?: "null"} categories")
            availableCategories = cats ?: emptyList()

            if (isEditMode) {
                if (selectedCategoryName.isNotEmpty()) { // Task data has a category name
                    val foundCat = availableCategories.find { it.name == selectedCategoryName }
                    if (foundCat != null) {
                        selectedCategoryObject = foundCat
                    } else {
                        // Category name from task not found in available list (e.g., deleted)
                        selectedCategoryObject = null
                        selectedCategoryName = "Uncategorized" // Or keep the old name and show an error/warning
                    }
                } else {
                    // Task data has no category name, treat as Uncategorized
                    selectedCategoryObject = null
                    selectedCategoryName = "Uncategorized"
                }
            } else { // Not in edit mode (new task)
                if (selectedCategoryName != "Uncategorized" && selectedCategoryObject == null) {
                    // User might have picked a category, then it got deleted before save - try to re-find
                    val foundCat = availableCategories.find { it.name == selectedCategoryName }
                    if (foundCat != null) {
                        selectedCategoryObject = foundCat
                    } else {
                        selectedCategoryObject = null
                        selectedCategoryName = "Uncategorized"
                    }
                } else if (selectedCategoryObject != null) {
                    // User has picked a category, ensure name is consistent
                    selectedCategoryName = selectedCategoryObject!!.name
                } else {
                    // New task, no interaction yet, remains Uncategorized
                    selectedCategoryName = "Uncategorized"
                    selectedCategoryObject = null
                }
            }
        } else if (categoriesResource is Resource.Error) {
            Log.e("TaskCreationScreen", "Categories Resource.Error: ${(categoriesResource as Resource.Error).message}")
            availableCategories = emptyList()
            selectedCategoryObject = null
            // selectedCategoryName will retain its current value (e.g., from edit mode, or "Uncategorized")
            // Or, force to "Uncategorized" if an error occurs during new task creation:
            if (!isEditMode) {
                 selectedCategoryName = "Uncategorized"
            }
        } else if (categoriesResource is Resource.Loading) {
            Log.d("TaskCreationScreen", "Categories Resource.Loading")
            // Potentially do nothing or set a loading state for availableCategories if needed elsewhere
        }
        Log.d("TaskCreationScreen", "End of Categories LaunchedEffect. availableCategories: ${availableCategories.size}")
    }
    
    // For date picker
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate?.toEpochDay()?.let { it * 24 * 60 * 60 * 1000 } 
            ?: (LocalDate.now().toEpochDay() * 24 * 60 * 60 * 1000)
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
    
    // Observe category suggestions from ViewModel
    val categorySuggestions by viewModel.categorySuggestions.collectAsState()
    var showSuggestions by remember { mutableStateOf(false) }
    
    // Add this effect to reload categories when the category picker is opened
    LaunchedEffect(showCategoryPicker) {
        if (showCategoryPicker) {
            Log.d("TaskCreationScreen", "Category picker opened - explicitly reloading categories")
            categoryViewModel.loadCategories()
        }
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
                            val rruleStringValue = generateRRuleString(recurrencePattern, selectedDate)
                            val taskData = TaskCreationData(
                                title = taskTitle,
                                description = taskDescription.ifBlank { null },
                                dueDate = selectedDate,
                                dueTime = selectedTime,
                                priority = selectedPriority,
                                category = if (selectedCategoryName == "Uncategorized") null else selectedCategoryName,
                                labels = emptyList(),
                                subtasks = currentSubtasks.toList(),
                                reminders = reminders,
                                rruleString = rruleStringValue,
                                recurrenceStartDate = if (rruleStringValue != null) selectedDate else null
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
                onValueChange = { 
                    if (it.length <= 255) { // Add character limit
                    taskTitle = it
                    }
                    
                    // Add this to trigger category suggestions when title changes
                    viewModel.suggestCategories(taskTitle, taskDescription)
                    if (taskTitle.length >= 3) {
                        showSuggestions = true
                    }
                },
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
                    modifier = Modifier.clickable(enabled = selectedDate != null) { 
                        if (selectedDate != null) showTimePicker = true 
                    }
                )

                if (selectedDate != null && selectedTime != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { selectedTime = null }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear time")
                    }
                }
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
                    tint = selectedCategoryObject?.color?.let { 
                        try { Color(android.graphics.Color.parseColor(it)) } 
                        catch (e: IllegalArgumentException) { MaterialTheme.colorScheme.primary }
                    } ?: MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = selectedCategoryName, // Display selected category name
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Select"
                )
            }
            
            // Show category suggestions if available and title has enough characters
            if (showSuggestions && categorySuggestions.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = "Suggested Categories",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                        
                        categorySuggestions.take(3).forEach { suggestion ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedCategoryName = suggestion.category 
                                        selectedCategoryObject = availableCategories.find { it.name == suggestion.category }
                                        showSuggestions = false
                                    }
                                    .padding(horizontal = 8.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Label,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = suggestion.category,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                
                                Spacer(modifier = Modifier.weight(1f))
                                
                                // Show confidence as percentage
                                Text(
                                    text = "${(suggestion.confidence * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                        
                        TextButton(
                            onClick = { showSuggestions = false },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Dismiss")
                        }
                    }
                }
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
                    text = formatRecurrencePattern(recurrencePattern), // Use the new formatter
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Select"
                )
            }
            
            Divider()
            
            // Reminder section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .clickable {
                        // Request notification permission if needed before showing reminder options
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            showReminderDialog = true
                        }
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                            Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Reminders",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Reminders", 
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.weight(1f))
                val remindersSummary = formatRemindersSummary(reminders, selectedDate) 
                Text(
                    text = remindersSummary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (reminders.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Select Reminders"
                )
            }
            Divider()
            
            // Subtasks section
            Text(
                "Subtasks",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            
            // Display existing subtasks
            if (currentSubtasks.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    currentSubtasks.forEachIndexed { index, subtask ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = subtask.isCompleted,
                                onCheckedChange = { isChecked ->
                                    currentSubtasks[index] = subtask.copy(isCompleted = isChecked)
                                }
                            )
                            Text(
                                text = subtask.title,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp)
                            )
                            IconButton(
                                onClick = { currentSubtasks.removeAt(index) },
                                modifier = Modifier.size(24.dp)
                            ) {
                        Icon(
                                    imageVector = Icons.Default.RemoveCircleOutline, // Changed icon
                                    contentDescription = "Remove Subtask",
                                    modifier = Modifier.size(20.dp) // Adjusted size
                                )
                            }
                        }
                        if (index < currentSubtasks.size - 1) {
                            HorizontalDivider(modifier = Modifier.padding(start = 40.dp))
                        }
                    }
                }
            }

            // Input field to add new subtask
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newSubtaskTitle,
                    onValueChange = { newSubtaskTitle = it },
                    label = { Text("New subtask...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    // leadingIcon = { Icon(Icons.Default.PlaylistAddCheck, contentDescription = null) }
                )
                Button(
                    onClick = {
                        if (newSubtaskTitle.isNotBlank()) {
                            currentSubtasks.add(SubTask(title = newSubtaskTitle))
                            newSubtaskTitle = "" // Clear input field
                }
                    },
                    enabled = newSubtaskTitle.isNotBlank(),
                    modifier = Modifier.height(56.dp) // Match OutlinedTextField height
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Subtask")
                }
            }
            // End of Subtasks Section ---
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            
            // Recurrence Options Dialog
            if (showRecurrenceOptions) {
                var tempRecurrenceType by remember { mutableStateOf(recurrencePattern.type) }
                var tempInterval by remember { mutableStateOf(recurrencePattern.interval.toString()) }
                var tempEndDate by remember { mutableStateOf(recurrencePattern.endDate) }
                var showEndDatePicker by remember { mutableStateOf(false) }
                var tempDaysOfWeek by remember { mutableStateOf(recurrencePattern.daysOfWeek.toMutableStateList()) }

                // Date validator for the recurrence end date picker state
                val recurrenceEndDateValidator = { utcDateMillis: Long ->
                    val selectedInstant = java.time.Instant.ofEpochMilli(utcDateMillis)
                    val selectedLocalDate = selectedInstant.atZone(ZoneId.systemDefault()).toLocalDate()
                    // Use task's main selectedDate as the earliest possible start for recurrence end date
                    // or today if selectedDate is null.
                    val taskStartDate = selectedDate ?: LocalDate.now() 
                    selectedLocalDate.isAfter(taskStartDate.minusDays(1)) // Allow recurrence to end on the same day it starts
                }

                val recurrenceEndDatePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = tempEndDate?.toEpochDay()?.let { it * 24 * 60 * 60 * 1000L },
                    yearRange = IntRange(LocalDate.now().year, LocalDate.now().year + 100), // Optional: restrict year range
                    selectableDates = object : SelectableDates {
                        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                            return recurrenceEndDateValidator(utcTimeMillis)
                        }
                        // You might also want to override isSelectableYear if needed, though yearRange often suffices
                    }
                )
        
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

                            // End Date Picker
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Ends", style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showEndDatePicker = true }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.EventBusy, contentDescription = "End Date", tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = tempEndDate?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) ?: "Never",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            // Days of Week Picker (only for Weekly recurrence)
                            if (tempRecurrenceType == RecurrenceType.WEEKLY) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Repeat on", style = MaterialTheme.typography.bodyLarge)
                                Spacer(modifier = Modifier.height(8.dp))
                                // Changed from Row to LazyRow for horizontal scrolling
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp) // Adds a small space between chips
                                ) {
                                    val days = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")
                                    items(days.size) { index -> // Use items(count) for LazyRow
                                        val dayLabel = days[index]
                                        val dayNumber = index + 1 // 1 for Monday, ..., 7 for Sunday
                                        val isSelected = tempDaysOfWeek.contains(dayNumber)
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                if (isSelected) {
                                                    tempDaysOfWeek.remove(dayNumber)
                                                } else {
                                                    tempDaysOfWeek.add(dayNumber)
                                                }
                                            },
                                            label = { Text(dayLabel) },
                                            modifier = Modifier.padding(horizontal = 2.dp) 
                                        )
                                    }
                                }
                            }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                                // Validate that at least one day is selected for weekly recurrence
                                if (tempRecurrenceType == RecurrenceType.WEEKLY && tempDaysOfWeek.isEmpty()) {
                                    // Optionally show a toast or error message to the user
                                    // For now, just defaulting to not saving or reverting type to NONE
                                    // This behavior might need refinement (e.g. prevent dialog close)
                                    Log.w("TaskCreationScreen", "Weekly recurrence selected but no days chosen.")
                                    // Potentially set tempRecurrenceType = RecurrenceType.NONE here or handle error
                                }
                        recurrencePattern = RecurrencePattern(
                            type = tempRecurrenceType,
                                    interval = tempInterval.toIntOrNull() ?: 1,
                                    endDate = tempEndDate,
                                    daysOfWeek = tempDaysOfWeek.toList().sorted() // Save sorted list
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
                // Moved Date Picker Dialog for Recurrence End Date INSIDE the if (showRecurrenceOptions) block
                if (showEndDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showEndDatePicker = false },
                        confirmButton = {
                            Button(
                                onClick = {
                                    recurrenceEndDatePickerState.selectedDateMillis?.let { millis ->
                                        tempEndDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000L)) // Added L for Long
                                    }
                                    showEndDatePicker = false
                                }
                            ) {
                                Text("OK")
                            }
                        },
                        dismissButton = {
                            Row {
                                TextButton(
                                    onClick = { 
                                        tempEndDate = null // Clear the end date
                                        showEndDatePicker = false 
                                    }
                                ) {
                                    Text("Clear (Never)")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                TextButton(
                                    onClick = { showEndDatePicker = false }
                                ) {
                                    Text("Cancel")
                                }
                            }
                        }
                    ) {
                        DatePicker(
                            state = recurrenceEndDatePickerState
                        )
                    }
                }
            }

            // Bottom buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = {
                        if (isEditMode) onCancel() else onDismiss()
                    },
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
                            description = taskDescription,
                            dueDate = selectedDate,
                            dueTime = selectedTime,
                            priority = selectedPriority,
                            category = if (selectedCategoryObject?.name == "Uncategorized") null else selectedCategoryObject?.name,
                            labels = emptyList(), 
                            subtasks = currentSubtasks.toList(),
                            rruleString = if (recurrencePattern.type != RecurrenceType.NONE) generateRRuleString(recurrencePattern, selectedDate) else null,
                            recurrenceStartDate = if (recurrencePattern.type != RecurrenceType.NONE) selectedDate else null,
                            reminders = reminders
                        )
                        if (isEditMode) {
                            Log.d("TaskCreationScreen", "Updating task ID: $taskId with data: Title - ${taskData.title}, RRULE - ${taskData.rruleString}, Reminders: ${taskData.reminders?.size}")
                            viewModel.updateTaskWithData(taskId, taskData)
                        } else {
                            Log.d("TaskCreationScreen", "Creating new task with data: Title - ${taskData.title}, RRULE - ${taskData.rruleString}, Reminders: ${taskData.reminders?.size}")
                            onSaveTask(taskData)
                        }
                        onDismiss() // Close the dialog/screen
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
                Row {
                    TextButton(
                        onClick = { 
                            selectedDate = null // Clear the date
                            selectedTime = null // Also clear the time
                            showDatePicker = false 
                        }
                    ) {
                        Text("Clear")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = { 
                        showDatePicker = false 
                    }
                ) {
                    Text("Cancel")
                    }
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
    
    // Category Picker Dialog/Dropdown
    if (showCategoryPicker) {
        AlertDialog(
            onDismissRequest = { showCategoryPicker = false },
            title = { Text("Select Category") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) { 
                    if (categoriesResource is Resource.Loading) {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    } else { // Handles both empty and non-empty states for categories
                        LazyColumn {
                            items(availableCategories) { category ->
                                ListItem(
                                    headlineContent = { Text(category.name) },
                                    modifier = Modifier.clickable {
                                        selectedCategoryName = category.name
                                        selectedCategoryObject = category
                                    showCategoryPicker = false
                                    },
                                    leadingContent = {
                                        Box(
                                modifier = Modifier
                                                .size(24.dp)
                                                .background(
                                                    color = try {
                                                        Color(android.graphics.Color.parseColor(category.color))
                                                    } catch (e: IllegalArgumentException) {
                                                        MaterialTheme.colorScheme.primary // Fallback color
                                                    },
                                                    shape = CircleShape
                                                )
                                        )
                                    },
                                    trailingContent = {
                                        if (selectedCategoryObject?.id == category.id) {
                                            Icon(Icons.Filled.Check, contentDescription = "Selected")
                                        }
                                    }
                                )
                            }
                            // Always show "Create New Category" at the end of the list
                            item {
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                                ListItem(
                                    headlineContent = { Text("Create New Category...") },
                                    leadingContent = { Icon(Icons.Filled.Add, contentDescription = "Create New Category") },
                                    modifier = Modifier.clickable {
                                    showCategoryPicker = false
                                        onNavigateToCreateCategory()
                                        Log.d("TaskCreationScreen", "Create New Category list item clicked - navigating")
                                }
                            )
                            }
                        }
                        if (availableCategories.isEmpty()) {
                            Text(
                                 text = "No categories yet. Click above to create one.",
                                 style = MaterialTheme.typography.bodySmall,
                                 textAlign = TextAlign.Center,
                                 modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryPicker = false }) {
                    Text("Done")
                }
            },
            dismissButton = {
                // Optional: Only show cancel if you want a different action from "Done" when no selection is made.
                // Otherwise, "Done" can also act as dismiss.
                TextButton(onClick = { showCategoryPicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showReminderDialog) {
        ReminderListDialog(
            reminders = reminders,
            onAddReminder = {
                editingReminder = null 
                showAddEditReminderDialog = true
                // showReminderDialog = false // Keep ReminderListDialog open in the background for now
            },
            onDeleteReminder = { reminderToDelete ->
                reminders = reminders.filterNot { it == reminderToDelete }
            },
            onDismiss = { showReminderDialog = false }
        )
    }

    if (showAddEditReminderDialog) {
        AddEditReminderDialog(
            initialReminder = editingReminder, // Pass current reminder for editing, or null for new
            taskDueDate = selectedDate, // Pass the task's due date for context
            onSave = { reminder ->
                if (editingReminder == null) { // Adding new reminder
                    reminders = reminders + reminder
                } else { // Updating existing reminder
                    reminders = reminders.map { if (it == editingReminder) reminder else it }
                }
                showAddEditReminderDialog = false
                editingReminder = null // Reset editing state
            },
            onDismiss = {
                showAddEditReminderDialog = false
                editingReminder = null // Reset editing state
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

// Utility functions have been moved to TaskUtils.kt to avoid duplicates

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderListDialog(
    reminders: List<Reminder>,
    onAddReminder: () -> Unit,
    onDeleteReminder: (Reminder) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Reminders") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (reminders.isEmpty()) {
                    Text(
                        text = "No reminders set. Tap \"Add Reminder\" to create one.",
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(reminders) { reminder ->
                            // TODO: Create formatReminderItem(reminder: Reminder) for better display
                            val reminderText = when (reminder.type) {
                                ReminderType.RELATIVE -> "${reminder.offsetMinutes?.let { Math.abs(it) }} minutes before"
                                ReminderType.ABSOLUTE -> reminder.absoluteDateTime?.format(DateTimeFormatter.ofPattern("MMM d, h:mm a")) ?: "Specific time"
                            }
                            ListItem(
                                headlineContent = { Text(reminderText) },
                                trailingContent = {
                                    IconButton(onClick = { onDeleteReminder(reminder) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Reminder")
                                    }
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onAddReminder) {
                Text("Add Reminder")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditReminderDialog(
    initialReminder: Reminder? = null, // Pass existing reminder for editing, null for new
    taskDueDate: LocalDate?, // Needed for context if setting absolute reminder relative to due date initially
    onSave: (Reminder) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedType by remember { mutableStateOf(initialReminder?.type ?: ReminderType.RELATIVE) }
    
    // For RELATIVE type
    var offsetMinutesString by remember { 
        mutableStateOf(initialReminder?.offsetMinutes?.let { Math.abs(it) }?.toString() ?: "15") 
    }

    // For ABSOLUTE type
    val initialAbsoluteDateTime = initialReminder?.absoluteDateTime ?: taskDueDate?.atTime(LocalTime.now().hour, LocalTime.now().minute)
    var absoluteDate by remember { mutableStateOf(initialAbsoluteDateTime?.toLocalDate()) }
    var absoluteTime by remember { mutableStateOf(initialAbsoluteDateTime?.toLocalTime()) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // DatePickerState for the ABSOLUTE reminder date
    val absoluteDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = absoluteDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialReminder == null) "Add Reminder" else "Edit Reminder") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // Type Selector (Relative vs Absolute)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    listOf(ReminderType.RELATIVE, ReminderType.ABSOLUTE).forEach { type ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { selectedType = type }
                        ) {
                            RadioButton(
                                selected = selectedType == type,
                                onClick = { selectedType = type }
                            )
                            Text(type.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))

                when (selectedType) {
                    ReminderType.RELATIVE -> {
                        OutlinedTextField(
                            value = offsetMinutesString,
                            onValueChange = { value -> offsetMinutesString = value.filter { it.isDigit() } },
                            label = { Text("Minutes Before Due Time") },
                            placeholder = { Text("e.g., 15") },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Schedule, "Offset")}
                        )
                        Text(
                            text = "Reminder will trigger this many minutes before the task's due date/time.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    ReminderType.ABSOLUTE -> {
                        // Date Picker
                        Text("Specific Date & Time", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom=8.dp))
                        Button(
                            onClick = { showDatePicker = true }, 
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Event, contentDescription = "Date", modifier = Modifier.padding(end=8.dp))
                            Text(absoluteDate?.format(DateTimeFormatter.ofPattern("MMM d, yyyy")) ?: "Select Date")
                        }
                        if (showDatePicker) {
                            DatePickerDialog(
                                onDismissRequest = { showDatePicker = false },
                                confirmButton = {
                                    TextButton(onClick = { 
                                        absoluteDatePickerState.selectedDateMillis?.let { millis ->
                                            absoluteDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                                        }
                                        showDatePicker = false 
                                    }) { Text("OK") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                                }
                            ) {
                                DatePicker(state = absoluteDatePickerState)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        // Time Picker
                        Button(
                            onClick = { showTimePicker = true }, 
                            modifier = Modifier.fillMaxWidth(),
                            enabled = absoluteDate != null
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = "Time", modifier = Modifier.padding(end=8.dp))
                            Text(absoluteTime?.format(DateTimeFormatter.ofPattern("h:mm a")) ?: "Select Time")
                        }
                        if (showTimePicker && absoluteDate != null) {
                            TimePickerDialog(
                                context = context,
                                initialTime = absoluteTime ?: LocalTime.now(),
                                onTimeSelected = { time -> absoluteTime = time },
                                onDismiss = { showTimePicker = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val reminder = when (selectedType) {
                        ReminderType.RELATIVE -> {
                            val offset = offsetMinutesString.toIntOrNull() ?: 15
                            Reminder(type = ReminderType.RELATIVE, offsetMinutes = -offset) // Negative for "before"
                        }
                        ReminderType.ABSOLUTE -> {
                            if (absoluteDate != null && absoluteTime != null) {
                                Reminder(type = ReminderType.ABSOLUTE, absoluteDateTime = LocalDateTime.of(absoluteDate, absoluteTime))
                            } else null // Invalid absolute reminder if date or time is missing
                        }
                    }
                    reminder?.let { onSave(it) }
                    onDismiss()
                },
                // Enable save only if data is valid
                enabled = when(selectedType) {
                    ReminderType.RELATIVE -> offsetMinutesString.isNotBlank() && offsetMinutesString.toIntOrNull() != null
                    ReminderType.ABSOLUTE -> absoluteDate != null && absoluteTime != null
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun TimePickerDialog(
    context: Context,
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, initialTime.hour)
        set(Calendar.MINUTE, initialTime.minute)
    }

    val timePickerDialog = android.app.TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            onTimeSelected(LocalTime.of(hourOfDay, minute))
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false // Use 24-hour format or not, false for AM/PM
    )
    
    timePickerDialog.setOnDismissListener { 
        onDismiss()
    }
    // Ensure the dialog is shown. 
    // We need to manage its visibility from where it's called (using showTimePicker state).
    // This composable's role is to configure and provide the dialog instance.
    // A LaunchedEffect in the caller can show it.
    // However, for simplicity in this direct usage, we might just show it directly if this composable is invoked.
    // Let's reconsider: the AddEditReminderDialog already has `if (showTimePicker ...)`
    // So this composable should just be the dialog itself.
    // We need to make sure it's shown when this composable enters the composition.
    LaunchedEffect(Unit) { // Show when composable enters the composition
        timePickerDialog.show()
    }
}
