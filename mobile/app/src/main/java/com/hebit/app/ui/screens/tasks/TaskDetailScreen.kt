package com.hebit.app.ui.screens.tasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.model.Task
import com.hebit.app.ui.components.BottomNavItem
import com.hebit.app.ui.screens.tasks.SubTask
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.compose.material3.HorizontalDivider
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.FilterChip
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar
import com.hebit.app.ui.screens.tasks.RecurrenceType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String,
    onNavigateBack: () -> Unit,
    onHomeClick: () -> Unit = {},
    onTasksClick: () -> Unit = {},
    onHabitsClick: () -> Unit = {},
    onGoalsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onEditTask: (String) -> Unit = {},
    viewModel: TaskViewModel = hiltViewModel()
) {
    LaunchedEffect(key1 = taskId) {
        viewModel.getTaskById(taskId)
    }
    
    val taskState by viewModel.selectedTaskState.collectAsState()
    
    var showEditMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var expandedDescription by remember { mutableStateOf(false) }
    var showAddSubtaskDialog by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }
    var newSubtaskTitle by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showEditMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    
                    // Edit menu dropdown
                    DropdownMenu(
                        expanded = showEditMenu,
                        onDismissRequest = { showEditMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Task") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = { 
                                showEditMenu = false
                                onEditTask(taskId)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Add Subtask") },
                            leadingIcon = { Icon(Icons.Default.AddTask, contentDescription = null) },
                            onClick = { 
                                showEditMenu = false
                                showAddSubtaskDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Set Reminder") },
                            leadingIcon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                            onClick = { 
                                showEditMenu = false
                                showReminderDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                            onClick = { 
                                showEditMenu = false
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    BottomNavItem(
                        icon = Icons.Default.Home,
                        label = "Home",
                        selected = false,
                        onClick = onHomeClick
                    )
                    
                    BottomNavItem(
                        icon = Icons.Default.CheckCircle,
                        label = "Tasks",
                        selected = true,
                        onClick = onTasksClick
                    )
                    
                    BottomNavItem(
                        icon = Icons.Default.Loop,
                        label = "Habits",
                        selected = false,
                        onClick = onHabitsClick
                    )
                    
                    BottomNavItem(
                        icon = Icons.Default.Flag,
                        label = "Goals",
                        selected = false,
                        onClick = onGoalsClick
                    )
                    
                    BottomNavItem(
                        icon = Icons.Default.Person,
                        label = "Profile",
                        selected = false,
                        onClick = onProfileClick
                    )
                }
            }
        }
    ) { paddingValues ->
        when (taskState) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is Resource.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Error loading task",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (taskState as Resource.Error).message ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.getTaskById(taskId) }) {
                            Text("Retry")
                        }
                    }
                }
            }
            
            is Resource.Success -> {
                val task = (taskState as Resource.Success<Task?>).data
                if (task == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Task not found")
                    }
                } else {
                    // Parse subtasks from metadata
                    val subtasks = remember(task.metadata["subtasks"]) {
                        val subtasksStr = task.metadata["subtasks"] ?: ""
                        parseSubtasks(subtasksStr)
                    }
                    
                    // Parse recurrence pattern from metadata
                    val recurrencePattern = remember(task.metadata["recurrence"]) {
                        val recurrenceStr = task.metadata["recurrence"]
                        if (recurrenceStr != null) parseRecurrencePattern(recurrenceStr) else null
                    }
                    
                    // Parse reminder settings from metadata
                    val reminderSettings = remember(task.metadata["reminder"]) {
                        val reminderStr = task.metadata["reminder"]
                        if (reminderStr != null) parseReminderSettings(reminderStr) else null
                    }
                    
                    TaskDetailContent(
                        task = task,
                        subtasks = subtasks,
                        recurrencePattern = recurrencePattern,
                        reminderSettings = reminderSettings,
                        expandedDescription = expandedDescription,
                        onToggleDescription = { expandedDescription = !expandedDescription },
                        onToggleComplete = { 
                            viewModel.toggleTaskCompletion(task.id)
                        },
                        onUpdateProgress = { progress ->
                            val updatedTask = task.copy(progress = progress)
                            viewModel.updateTask(updatedTask)
                        },
                        onSubtaskToggle = { index, isCompleted ->
                            val updatedSubtasks = subtasks.toMutableList()
                            updatedSubtasks[index] = updatedSubtasks[index].copy(isCompleted = isCompleted)
                            
                            // Compute new progress based on subtasks completion
                            val completedCount = updatedSubtasks.count { subtask -> subtask.isCompleted }
                            val newProgress = if (updatedSubtasks.isNotEmpty()) {
                                (completedCount * 100) / updatedSubtasks.size
                            } else {
                                task.progress
                            }
                            
                            val subtasksStr = updatedSubtasks.joinToString(",") { subtask -> 
                                "${subtask.id}:${subtask.title}:${subtask.isCompleted}" 
                            }
                            
                            val updatedMetadata = task.metadata.toMutableMap()
                            updatedMetadata["subtasks"] = subtasksStr
                            
                            val updatedTask = task.copy(
                                progress = newProgress,
                                metadata = updatedMetadata
                            )
                            viewModel.updateTask(updatedTask)
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                    
                    // Add subtask dialog
                    if (showAddSubtaskDialog) {
                        AddSubtaskDialog(
                            onDismiss = { 
                                showAddSubtaskDialog = false
                                newSubtaskTitle = ""
                            },
                            onAdd = { subtaskTitle ->
                                val newSubtask = SubTask(
                                    title = subtaskTitle,
                                    isCompleted = false
                                )
                                
                                val updatedSubtasks = subtasks.toMutableList().apply {
                                    add(newSubtask)
                                }
                                
                                val subtasksStr = updatedSubtasks.joinToString(",") { subtask -> 
                                    "${subtask.id}:${subtask.title}:${subtask.isCompleted}" 
                                }
                                
                                val updatedMetadata = task.metadata.toMutableMap()
                                updatedMetadata["subtasks"] = subtasksStr
                                
                                val updatedTask = task.copy(metadata = updatedMetadata)
                                viewModel.updateTask(updatedTask)
                                
                                showAddSubtaskDialog = false
                                newSubtaskTitle = ""
                            },
                            subtaskTitle = newSubtaskTitle,
                            onSubtaskTitleChange = { value -> newSubtaskTitle = value }
                        )
                    }

                    // Add Reminder dialog
                    if (showReminderDialog) {
                        ReminderDialog(
                            onDismiss = { showReminderDialog = false },
                            onSetReminder = { minutes, timeString ->
                                val reminderStr = if (timeString.isNotBlank()) {
                                    "$minutes,$timeString"
                                } else {
                                    "$minutes,"
                                }
                                
                                val updatedMetadata = task.metadata.toMutableMap()
                                updatedMetadata["reminder"] = reminderStr
                                
                                val updatedTask = task.copy(metadata = updatedMetadata)
                                viewModel.updateTask(updatedTask)
                                
                                showReminderDialog = false
                            },
                            initialMinutes = task.metadata["reminder"]?.split(",")?.get(0)?.toIntOrNull() ?: 15,
                            initialTimeString = task.metadata["reminder"]?.split(",")?.getOrNull(1) ?: ""
                        )
                    }
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Task") },
            text = { Text("Are you sure you want to delete this task? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTask(taskId)
                        showDeleteDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TaskDetailContent(
    task: Task,
    subtasks: List<SubTask>,
    recurrencePattern: RecurrenceType? = null,
    reminderSettings: String? = null,
    expandedDescription: Boolean,
    onToggleDescription: () -> Unit,
    onToggleComplete: () -> Unit,
    onUpdateProgress: (Int) -> Unit,
    onSubtaskToggle: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    val hasSubtasks = subtasks.isNotEmpty()
    
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp)
    ) {
        // Task status
        item {
            Row(
                modifier = Modifier.padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Task completion toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Checkbox(
                        checked = task.isCompleted,
                        onCheckedChange = { onToggleComplete() }
                    )
                    Text(
                        text = if (task.isCompleted) "Completed" else "Mark as complete",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Priority indicator
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = when (task.priority) {
                                    3 -> MaterialTheme.colorScheme.error
                                    2 -> MaterialTheme.colorScheme.tertiary
                                    1 -> MaterialTheme.colorScheme.secondary
                                    else -> MaterialTheme.colorScheme.tertiary
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (task.priority) {
                                3 -> "H"
                                2 -> "M"
                                1 -> "L"
                                else -> "M"
                            },
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
        
        // Task title
        item {
            Text(
                text = task.title,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp),
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
            )
            
            // Due date
            if (task.dueDateTime != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = "Due date",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Due: ${task.dueDateTime.toLocalDate().format(dateFormatter)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (task.dueDateTime.toLocalTime() != LocalTime.MIDNIGHT) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "at ${task.dueDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("h:mm a"))}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        // Category
        if (task.category.isNotBlank()) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = "Category",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = task.category,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
        
        // Recurrence pattern (if any)
        if (recurrencePattern != null) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = "Recurrence",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Text(
                            text = when(recurrencePattern) {
                                RecurrenceType.DAILY -> "Repeats Daily"
                                RecurrenceType.WEEKLY -> "Repeats Weekly"
                                RecurrenceType.MONTHLY -> "Repeats Monthly"
                                RecurrenceType.YEARLY -> "Repeats Yearly"
                                else -> "Does not repeat"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
        
        // Reminder (if any)
        if (reminderSettings != null) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Reminder",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = reminderSettings,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
        
        // Progress (automated if has subtasks)
        if (!task.isCompleted) {
            item {
                Column(
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Progress: ${task.progress}%",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Text(
                            text = "${task.progress}/100",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LinearProgressIndicator(
                        progress = { task.progress / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        strokeCap = StrokeCap.Round
                    )
                    
                    // Only show manual progress buttons if no subtasks
                    if (!hasSubtasks) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Progress buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            val progressOptions = listOf(0, 25, 50, 75, 100)
                            progressOptions.forEach { progress ->
                                OutlinedButton(
                                    onClick = { onUpdateProgress(progress) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp),
                                    enabled = progress != task.progress
                                ) {
                                    Text(text = "$progress%")
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Subtasks section (if any)
        if (hasSubtasks) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Subtasks (${subtasks.count { it.isCompleted }}/${subtasks.size})",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        
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
                                        onSubtaskToggle(index, isChecked)
                                    }
                                )
                                
                                Text(
                                    text = subtask.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textDecoration = if (subtask.isCompleted) 
                                        TextDecoration.LineThrough 
                                    else 
                                        TextDecoration.None,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                            
                            if (index < subtasks.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier
                                        .padding(start = 40.dp)
                                        .padding(vertical = 4.dp),
                                    thickness = 0.5.dp
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Description
        if (task.description.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Description",
                                style = MaterialTheme.typography.titleMedium
                            )
                            
                            IconButton(onClick = onToggleDescription) {
                                Icon(
                                    imageVector = if (expandedDescription) 
                                        Icons.Default.ExpandLess 
                                    else 
                                        Icons.Default.ExpandMore,
                                    contentDescription = if (expandedDescription) 
                                        "Show less" 
                                    else 
                                        "Show more"
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = if (expandedDescription) Int.MAX_VALUE else 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
        
        // Creation and modification info
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Task Information",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Created",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = task.createdAt.toLocalDate().format(dateFormatter),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Last Updated",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = task.updatedAt.toLocalDate().format(dateFormatter),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Task ID",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = task.id,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddSubtaskDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit,
    subtaskTitle: String,
    onSubtaskTitleChange: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Subtask") },
        text = {
            OutlinedTextField(
                value = subtaskTitle,
                onValueChange = onSubtaskTitleChange,
                placeholder = { Text("Subtask title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { onAdd(subtaskTitle) },
                enabled = subtaskTitle.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ReminderDialog(
    onDismiss: () -> Unit,
    onSetReminder: (Int, String) -> Unit,
    initialMinutes: Int = 15,
    initialTimeString: String = ""
) {
    var minutes by remember { mutableStateOf(initialMinutes) }
    var timeString by remember { mutableStateOf(initialTimeString) }
    var useSpecificTime by remember { mutableStateOf(initialTimeString.isNotEmpty()) }
    
    val context = LocalContext.current
    val currentTime = remember { Calendar.getInstance() }
    val hour = currentTime.get(Calendar.HOUR_OF_DAY)
    val minute = currentTime.get(Calendar.MINUTE)
    
    val showTimePicker = { 
        val timePickerDialog = TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                val formattedHour = if (selectedHour > 12) selectedHour - 12 else if (selectedHour == 0) 12 else selectedHour
                val amPm = if (selectedHour >= 12) "PM" else "AM"
                timeString = String.format("%d:%02d %s", formattedHour, selectedMinute, amPm)
            },
            hour,
            minute,
            false
        )
        timePickerDialog.show()
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Reminder") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "When do you want to be reminded?",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Option to select minutes before
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { useSpecificTime = false }
                        .padding(vertical = 8.dp)
                ) {
                    RadioButton(
                        selected = !useSpecificTime,
                        onClick = { useSpecificTime = false }
                    )
                    
                    Text(
                        text = "Minutes before due time",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                
                // Minutes before selection
                if (!useSpecificTime) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 32.dp, top = 8.dp, bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val minutesOptions = listOf(5, 15, 30, 60, 120, 1440) // last one is 1 day
                        minutesOptions.chunked(3).forEach { rowOptions ->
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                rowOptions.forEach { option ->
                                    FilterChip(
                                        selected = minutes == option,
                                        onClick = { minutes = option },
                                        label = { 
                                            Text(
                                                text = when(option) {
                                                    1440 -> "1 day"
                                                    60 -> "1 hour"
                                                    120 -> "2 hours"
                                                    else -> "$option min"
                                                }
                                            ) 
                                        },
                                        modifier = Modifier
                                            .padding(end = 8.dp)
                                            .weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Option to select specific time
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { useSpecificTime = true }
                        .padding(vertical = 8.dp)
                ) {
                    RadioButton(
                        selected = useSpecificTime,
                        onClick = { useSpecificTime = true }
                    )
                    
                    Text(
                        text = "At specific time",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                
                // Specific time selection
                if (useSpecificTime) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 32.dp, top = 8.dp, bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { showTimePicker() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Select time",
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = if (timeString.isNotEmpty()) timeString else "Select time"
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (useSpecificTime) {
                        onSetReminder(0, timeString)
                    } else {
                        onSetReminder(minutes, "")
                    }
                },
                enabled = !useSpecificTime || timeString.isNotEmpty()
            ) {
                Text("Set Reminder")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Utility functions moved to TaskUtils.kt
