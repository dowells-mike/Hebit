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
    viewModel: TaskViewModel = hiltViewModel(),
    onNavigateToCreateCategory: () -> Unit
) {
    var showAddTaskScreen by remember { mutableStateOf(false) }
    var viewMode by remember { mutableStateOf(TaskViewMode.LIST) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Collect tasks from ViewModel
    val tasksState by viewModel.tasksState.collectAsState()
    
    // Load tasks on first composition
    LaunchedEffect(key1 = true) {
        viewModel.loadTasks()
    }
    
    if (showAddTaskScreen) {
        TaskCreationScreen(
            onDismiss = { showAddTaskScreen = false },
            onSaveTask = { taskData ->
                viewModel.createTask(taskData)
                showAddTaskScreen = false
            },
            onNavigateToCreateCategory = onNavigateToCreateCategory
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Tasks") },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { showAddTaskScreen = true }) {
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
                                    TaskItem(
                                        task = task,
                                        onTaskClick = { onTaskClick(task.id) },
                                        onTaskToggle = { viewModel.toggleTaskCompletion(task.id) },
                                        onTaskDelete = { viewModel.deleteTask(task.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onTaskClick: () -> Unit,
    onTaskToggle: () -> Unit,
    onTaskDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTaskClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Priority indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .padding(end = 8.dp)
            ) {
                val color = when (task.priority) {
                    3 -> MaterialTheme.colorScheme.error
                    2 -> MaterialTheme.colorScheme.tertiary
                    1 -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.tertiary
                }
                Surface(
                    modifier = Modifier.size(12.dp),
                    color = color,
                    shape = MaterialTheme.shapes.small
                ) {}
            }
            
            // Checkbox
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onTaskToggle() }
            )
            
            // Task details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                
                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1
                    )
                }
                
                if (task.dueDateTime != null) {
                    Text(
                        text = "Due: ${task.dueDateTime.toLocalDate().format(dateFormatter)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            // Actions
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
    
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Task") },
            text = { Text("Are you sure you want to delete this task?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onTaskDelete()
                        showDeleteConfirm = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Helper for capitalization
private fun String.capitalizeFirst(): String {
    return this.lowercase().replaceFirstChar { it.uppercase() }
}
