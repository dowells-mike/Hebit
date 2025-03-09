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
    onTaskClick: (String) -> Unit = {}
) {
    // Mock tasks data - would come from ViewModel in real app
    val tasks = remember {
        mutableStateListOf(
            TaskItem(id = "1", title = "Complete project plan", completed = false, priority = TaskPriority.HIGH),
            TaskItem(id = "2", title = "Review pull requests", completed = true, priority = TaskPriority.MEDIUM),
            TaskItem(id = "3", title = "Schedule team meeting", completed = false, priority = TaskPriority.LOW),
            TaskItem(id = "4", title = "Update documentation", completed = false, priority = TaskPriority.MEDIUM),
            TaskItem(id = "5", title = "Prepare presentation", completed = false, priority = TaskPriority.HIGH)
        )
    }
    
    var showAddTaskDialog by remember { mutableStateOf(false) }
    
    var viewMode by remember { mutableStateOf(TaskViewMode.LIST) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tasks") },
                actions = {
                    // Search button
                    IconButton(onClick = { /* Open search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    
                    // Filter button
                    IconButton(onClick = { /* Open filter */ }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                    
                    // Menu button
                    IconButton(onClick = { /* Open menu */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddTaskDialog = true }) {
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
            // Search and filter row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search field
                OutlinedTextField(
                    value = "",
                    onValueChange = { },
                    placeholder = { Text("Search tasks...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Filter button
                IconButton(onClick = { /* Show filter options */ }) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // View mode selector (List/Board)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Categories button
                    TextButton(
                        onClick = { onTaskCategoriesClick() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = "Categories",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Categories")
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // View mode selector
                    SegmentedControl(
                        items = listOf("List", "Board"),
                        selectedIndex = if (viewMode == TaskViewMode.LIST) 0 else 1,
                        onItemSelection = { index ->
                            if (index == 0) {
                                viewMode = TaskViewMode.LIST
                            } else {
                                // Navigate to Board view
                                onTaskBoardClick()
                            }
                        }
                    )
                }
            }
            
            // Statistics summary
            TaskStatsCard(
                totalTasks = tasks.size,
                completedTasks = tasks.count { it.completed },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            
            // Task content based on view mode
            if (viewMode == TaskViewMode.LIST) {
                // List view
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onTaskClick(task.id) }
                        ) {
                            TaskItem(
                                task = task,
                                onTaskToggle = { taskId ->
                                    // Find and toggle the task's completed status
                                    val index = tasks.indexOfFirst { it.id == taskId }
                                    if (index != -1) {
                                        tasks[index] = tasks[index].copy(completed = !tasks[index].completed)
                                    }
                                },
                                onTaskDelete = { taskId ->
                                    // Remove the task from the list
                                    tasks.removeIf { it.id == taskId }
                                }
                            )
                        }
                    }
                }
            } else {
                // Board view
                Text(
                    text = "Board View Coming Soon",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
        
        // Task creation screen
        if (showAddTaskDialog) {
            TaskCreationScreen(
                onDismiss = { showAddTaskDialog = false },
                onSaveTask = { taskData ->
                    if (taskData.title.isNotBlank()) {
                        val newTask = TaskItem(
                            id = (tasks.size + 1).toString(),
                            title = taskData.title,
                            completed = false,
                            priority = taskData.priority
                        )
                        tasks.add(0, newTask)
                        showAddTaskDialog = false
                    }
                }
            )
        }
    }
}

@Composable
fun SegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onItemSelection: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.height(36.dp)
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .let {
                            if (isSelected) {
                                it.background(MaterialTheme.colorScheme.primaryContainer)
                            } else {
                                it.background(MaterialTheme.colorScheme.surface)
                            }
                        }
                        .clickable { onItemSelection(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) 
                            MaterialTheme.colorScheme.onPrimaryContainer 
                        else 
                            MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun TaskStatsCard(
    totalTasks: Int,
    completedTasks: Int,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = totalTasks.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(text = "Total", style = MaterialTheme.typography.bodyMedium)
            }
            
            Divider(
                modifier = Modifier
                    .height(36.dp)
                    .width(1.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = completedTasks.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(text = "Completed", style = MaterialTheme.typography.bodyMedium)
            }
            
            Divider(
                modifier = Modifier
                    .height(36.dp)
                    .width(1.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = (totalTasks - completedTasks).toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (totalTasks - completedTasks > 0) 
                        MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                )
                Text(text = "Pending", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskItem(
    task: TaskItem,
    onTaskToggle: (String) -> Unit,
    onTaskDelete: (String) -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onTaskToggle(task.id) }
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
                    .padding(end = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                val color = when (task.priority) {
                    TaskPriority.HIGH -> MaterialTheme.colorScheme.error
                    TaskPriority.MEDIUM -> MaterialTheme.colorScheme.tertiary
                    TaskPriority.LOW -> MaterialTheme.colorScheme.secondary
                }
                Surface(
                    modifier = Modifier.size(12.dp),
                    color = color,
                    shape = MaterialTheme.shapes.small
                ) {}
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Checkbox
            Checkbox(
                checked = task.completed,
                onCheckedChange = { onTaskToggle(task.id) }
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Task title
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None,
                modifier = Modifier.weight(1f)
            )
            
            // Delete button
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
                        onTaskDelete(task.id)
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

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onTaskAdd: (String, TaskPriority) -> Unit
) {
    var taskTitle by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Task") },
        text = {
            Column {
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    label = { Text("Task title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Priority", style = MaterialTheme.typography.bodyMedium)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PriorityOptions.forEach { priority ->
                        FilterChip(
                            selected = selectedPriority == priority,
                            onClick = { selectedPriority = priority },
                            label = { Text(priority.name.capitalize()) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onTaskAdd(taskTitle, selectedPriority) },
                enabled = taskTitle.isNotBlank()
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

// Helper for capitalization
private fun String.capitalize(): String {
    return this.lowercase().replaceFirstChar { it.uppercase() }
}

// Domain models
data class TaskItem(
    val id: String,
    val title: String,
    val completed: Boolean = false,
    val priority: TaskPriority = TaskPriority.MEDIUM
)

enum class TaskPriority {
    LOW, MEDIUM, HIGH
}

enum class TaskViewMode {
    LIST, BOARD
}

val PriorityOptions = listOf(TaskPriority.LOW, TaskPriority.MEDIUM, TaskPriority.HIGH)
