package com.hebit.app.ui.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import com.hebit.app.ui.components.BottomNavItem
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hebit.app.domain.model.Resource
import com.hebit.app.domain.model.Task
import com.hebit.app.domain.model.TaskCreationData
import com.hebit.app.domain.model.TaskPriority
import com.hebit.app.domain.model.TaskViewMode
import java.time.format.DateTimeFormatter
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.hebit.app.ui.navigation.Routes
import androidx.compose.foundation.border
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.hebit.app.ui.screens.tasks.getSubtaskProgressCounts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    onNavigateBack: () -> Unit,
    onHomeClick: () -> Unit = {},
    onHabitsClick: () -> Unit = {},
    onGoalsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onTaskCategoriesClick: () -> Unit = {},
    onTaskBoardClick: () -> Unit = {},
    onTaskClick: (String) -> Unit = {},
    onCreateTaskClick: () -> Unit = {},
    viewModel: TaskViewModel = hiltViewModel(),
    onNavigateToCreateCategory: () -> Unit,
    navController: NavController? = null // Optional NavController parameter
) {
    // Collect tasks from ViewModel
    val tasksState by viewModel.tasksState.collectAsState()
    
    // Load tasks on first composition
    LaunchedEffect(key1 = true) {
        viewModel.loadTasks()
        
        // Check if we have a new task from TaskCreationScreen - simpler approach
        navController?.currentBackStackEntry?.savedStateHandle?.get<TaskCreationData>("new_task_data")?.let { taskData ->
            Log.d("TaskListScreen", "Received task data from creation screen")
            viewModel.createTask(taskData)
            // Important: Remove the data from SavedStateHandle after processing to prevent re-processing
            navController.currentBackStackEntry?.savedStateHandle?.remove<TaskCreationData>("new_task_data")
        }
    }
    
    var searchQuery by remember { mutableStateOf("") }
    var showMoreMenu by remember { mutableStateOf(false) } // State for the dropdown menu
    var taskForDeletionDialog by remember { mutableStateOf<Task?>(null) }
    
    // To store swipe states of items, keyed by task ID
    val swipeStates = remember { mutableMapOf<String, SwipeToDismissBoxState>() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tasks") },
                actions = {
                    IconButton(onClick = { /* TODO: Implement search functionality */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    Box { // Box to anchor the DropdownMenu
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Manage Categories") },
                                onClick = {
                                    showMoreMenu = false
                                    navController?.navigate(Routes.CATEGORY_LIST)
                                    Log.d("TaskListScreen", "Manage Categories clicked - navigating to CATEGORY_LIST")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("View Task Board") },
                                onClick = {
                                    showMoreMenu = false
                                    onTaskBoardClick()
                                }
                            )
                            // Add other menu items here if needed
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onCreateTaskClick() }) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
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
                        onClick = { /* Already on tasks */ }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search tasks...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true
            )
            
            // Task list
            when (tasksState) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                is Resource.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Error loading tasks",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = (tasksState as Resource.Error).message ?: "Unknown error",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadTasks() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                
                is Resource.Success -> {
                    val tasks = (tasksState as Resource.Success<List<Task>>).data ?: emptyList()
                    val filteredTasks = tasks.filter {
                        searchQuery.isEmpty() || 
                        it.title.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
                    }
                    
                    if (filteredTasks.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (searchQuery.isNotEmpty()) {
                                    Text(
                                        text = "No tasks match your search",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                } else {
                                    Text(
                                        text = "No tasks yet",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Tap + to create your first task",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredTasks, key = { task -> task.id }) { task ->
                                val dismissState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = { proposedTargetValue ->
                                        if (proposedTargetValue == SwipeToDismissBoxValue.EndToStart) {
                                            // Trying to swipe to "delete" position
                                            taskForDeletionDialog = task // Set the task for the dialog
                                            true // Allow settling at EndToStart to show dialog & background
                                        } else if (proposedTargetValue == SwipeToDismissBoxValue.Settled) {
                                            // Trying to swipe back to "settled" position
                                            true // Allow settling back to normal (e.g., user swipes it back)
                                        } else {
                                            // Reject other transitions (e.g., StartToEnd, which we don't enable anyway)
                                            false
                                        }
                                    },
                                    positionalThreshold = { it * 0.25f }
                                )

                                // Store the state in the map and clean up on dispose
                                DisposableEffect(task.id) {
                                    swipeStates[task.id] = dismissState
                                    onDispose {
                                        swipeStates.remove(task.id)
                                    }
                                }

                                SwipeToDismissBox(
                                    state = dismissState,
                                    enableDismissFromStartToEnd = false, // Disable swipe right
                                    enableDismissFromEndToStart = true,  // Enable swipe left
                                    backgroundContent = {
                                        val color = when(dismissState.targetValue) {
                                            SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                                            else -> Color.Transparent
                                        }
                                        val icon = when(dismissState.targetValue) {
                                            SwipeToDismissBoxValue.EndToStart -> Icons.Outlined.Delete
                                            else -> null // No icon for other states or if not swiped enough
                                        }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(color)
                                                .padding(horizontal = 20.dp),
                                            contentAlignment = Alignment.CenterEnd // Align icon to the end (left swipe)
                                        ) {
                                            if (icon != null) {
                                                Icon(
                                                    icon,
                                                    contentDescription = "Delete Task",
                                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    TaskItem(
                                        task = task,
                                        onTaskClick = { onTaskClick(task.id) },
                                        onTaskToggle = { viewModel.toggleTaskCompletion(task.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog logic using the new variable name
    val currentTaskToDelete = taskForDeletionDialog
    if (currentTaskToDelete != null) {
        AlertDialog(
            onDismissRequest = { // Dialog dismissed (e.g., back press or click outside)
                coroutineScope.launch {
                    swipeStates[currentTaskToDelete.id]?.reset()
                }
                taskForDeletionDialog = null
            },
            title = { Text("Delete Task?") },
            text = { Text("Are you sure you want to permanently delete \"${currentTaskToDelete.title}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTask(currentTaskToDelete.id)
                        taskForDeletionDialog = null
                        // No need to reset swipe state, item will be removed
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        swipeStates[currentTaskToDelete.id]?.reset()
                    }
                    taskForDeletionDialog = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TaskItem(
    task: Task,
    onTaskClick: () -> Unit,
    onTaskToggle: () -> Unit
) {
    val today = java.time.LocalDate.now()
    val tomorrow = today.plusDays(1)

    // Get subtask progress
    val subtaskProgress = getSubtaskProgressCounts(task.metadata["subtasks"])

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTaskClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onTaskToggle() },
                modifier = Modifier.padding(end = 10.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) { // Row for title and subtask count
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                    )
                    if (subtaskProgress.total > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${subtaskProgress.completed}/${subtaskProgress.total})",
                            style = MaterialTheme.typography.bodySmall, // Or titleSmall if preferred
                            color = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                var showSpacer = false

                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        color = (if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    showSpacer = true
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = if (task.description.isNotBlank()) 4.dp else 2.dp)) {
                    task.dueDateTime?.let {
                        val dateText = when (it.toLocalDate()) {
                            today -> "Today"
                            tomorrow -> "Tomorrow"
                            else -> it.format(DateTimeFormatter.ofPattern("MMM d"))
                        }
                        val timeText = it.format(DateTimeFormatter.ofPattern("h:mm a"))
                        Text(
                            text = "$dateText, $timeText",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        showSpacer = true
                    }

                    task.category?.let { categoryName ->
                        if (categoryName.isNotBlank() && categoryName.lowercase() != "uncategorized") {
                            if (task.dueDateTime != null) {
                                Spacer(Modifier.width(8.dp))
                            }
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                border = BorderStroke(1.dp, if (task.isCompleted) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline),
                                modifier = Modifier
                            ) {
                                Text(
                                    text = categoryName,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            val priorityColor = when (task.priority) {
                3 -> MaterialTheme.colorScheme.error
                2 -> MaterialTheme.colorScheme.tertiary
                1 -> MaterialTheme.colorScheme.secondary
                else -> Color.Transparent
            }
            if (task.priority > 0) {
                 Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(if (task.isCompleted) priorityColor.copy(alpha = 0.5f) else priorityColor, shape = androidx.compose.foundation.shape.CircleShape)
                )
            } else {
                Spacer(modifier = Modifier.size(10.dp))
            }
        }
    }
}

// Helper for capitalization
private fun String.capitalizeFirst(): String {
    return this.lowercase().replaceFirstChar { it.uppercase() }
}
