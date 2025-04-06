package com.hebit.app.ui.screens.tasks

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hebit.app.domain.model.TaskPriority
import com.hebit.app.ui.components.BottomNavItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Define PriorityOptions using the imported TaskPriority enum
private val PriorityOptions = arrayOf(TaskPriority.LOW, TaskPriority.MEDIUM, TaskPriority.HIGH)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskBoardScreen(
    onNavigateBack: () -> Unit,
    onTaskClick: (String) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onTasksClick: () -> Unit = {},
    onHabitsClick: () -> Unit = {},
    onGoalsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    // Mock data for task board - would come from ViewModel in real app
    val columns = remember { 
        mutableStateListOf(
            TaskColumn(
                id = "1",
                title = "To Do",
                taskCount = 4,
                tasks = mutableStateListOf(
                    BoardTask(
                        id = "1",
                        title = "Design System Update",
                        dueDate = LocalDate.now().plusDays(10),
                        priority = TaskPriority.HIGH,
                        assignees = listOf("MK", "SC")
                    ),
                    BoardTask(
                        id = "2",
                        title = "Create user flow diagrams",
                        dueDate = LocalDate.now().plusDays(5),
                        priority = TaskPriority.MEDIUM,
                        assignees = listOf("MK")
                    ),
                    BoardTask(
                        id = "3",
                        title = "Review analytics dashboard",
                        dueDate = LocalDate.now().plusDays(7),
                        priority = TaskPriority.LOW,
                        assignees = listOf("SC")
                    ),
                    BoardTask(
                        id = "4",
                        title = "Finalize onboarding screens",
                        dueDate = LocalDate.now().plusDays(3),
                        priority = TaskPriority.MEDIUM,
                        assignees = listOf("MK", "SC")
                    )
                )
            ),
            TaskColumn(
                id = "2",
                title = "In Progress",
                taskCount = 2,
                tasks = mutableStateListOf(
                    BoardTask(
                        id = "5",
                        title = "User testing sessions",
                        dueDate = LocalDate.now().plusDays(2),
                        priority = TaskPriority.HIGH,
                        assignees = listOf("MK")
                    ),
                    BoardTask(
                        id = "6",
                        title = "Refactor authentication flow",
                        dueDate = LocalDate.now().plusDays(4),
                        priority = TaskPriority.MEDIUM,
                        assignees = listOf("SC")
                    )
                )
            ),
            TaskColumn(
                id = "3",
                title = "Done",
                taskCount = 3,
                tasks = mutableStateListOf(
                    BoardTask(
                        id = "7",
                        title = "Create wireframes",
                        dueDate = LocalDate.now().minusDays(5),
                        priority = TaskPriority.HIGH,
                        assignees = listOf("MK")
                    ),
                    BoardTask(
                        id = "8",
                        title = "Stakeholder review",
                        dueDate = LocalDate.now().minusDays(2),
                        priority = TaskPriority.HIGH,
                        assignees = listOf("MK", "SC")
                    ),
                    BoardTask(
                        id = "9",
                        title = "Initial project setup",
                        dueDate = LocalDate.now().minusDays(10),
                        priority = TaskPriority.MEDIUM,
                        assignees = listOf("SC")
                    )
                )
            )
        )
    }
    
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddColumnDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Project Board") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Search button
                    IconButton(onClick = { /* Open search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    
                    // Filter button
                    IconButton(onClick = { /* Open filter */ }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                    
                    // User avatar and dropdown menu
                    IconButton(onClick = { /* Show user options */ }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "User")
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
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Add Task") },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                onClick = { showAddTaskDialog = true }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Board content - horizontal scrolling columns
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Display all columns
                columns.forEach { column ->
                    TaskColumnCard(
                        column = column,
                        onTaskClick = onTaskClick
                    )
                }
                
                // Add column button
                Card(
                    modifier = Modifier
                        .width(300.dp)
                        .height(IntrinsicSize.Min)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .clickable { showAddColumnDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Add Column",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
        
        // Add task dialog
        if (showAddTaskDialog) {
            AddBoardTaskDialog(
                onDismiss = { showAddTaskDialog = false },
                onTaskAdd = { title, columnId, priority, assignees ->
                    // Would add task to specified column in real app
                    showAddTaskDialog = false
                },
                columns = columns
            )
        }
        
        // Add column dialog
        if (showAddColumnDialog) {
            AddColumnDialog(
                onDismiss = { showAddColumnDialog = false },
                onColumnAdd = { title ->
                    // Would add column to board in real app
                    showAddColumnDialog = false
                }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TaskColumnCard(
    column: TaskColumn,
    onTaskClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .width(300.dp)
            .heightIn(min = 300.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Column header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = column.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Task count badge
                    Surface(
                        modifier = Modifier.size(24.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = column.taskCount.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
                
                // Column options menu
                IconButton(
                    onClick = { /* Column options */ },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Divider()
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Tasks list
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                column.tasks.forEach { task ->
                    BoardTaskCard(
                        task = task,
                        onClick = { onTaskClick(task.id) }
                    )
                }
                
                // Add task button
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add task",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = "Add Task",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BoardTaskCard(
    task: BoardTask,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(12.dp)
        ) {
            // Rest of the card content
            Column {
                // Task title
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Due date and priority
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Due date
                    Text(
                        text = task.dueDate.format(DateTimeFormatter.ofPattern("MMM d")),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Priority indicator
                    val priorityColor = when (task.priority) {
                        TaskPriority.HIGH -> MaterialTheme.colorScheme.error
                        TaskPriority.MEDIUM -> MaterialTheme.colorScheme.tertiary
                        TaskPriority.LOW -> MaterialTheme.colorScheme.secondary
                    }
                    
                    Text(
                        text = when (task.priority) {
                            TaskPriority.HIGH -> "High Priority"
                            TaskPriority.MEDIUM -> "Medium"
                            TaskPriority.LOW -> "Low"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = priorityColor
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Assignees
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    task.assignees.forEachIndexed { index, initials ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when (index % 3) {
                                        0 -> MaterialTheme.colorScheme.primaryContainer
                                        1 -> MaterialTheme.colorScheme.secondaryContainer
                                        else -> MaterialTheme.colorScheme.tertiaryContainer
                                    }
                                )
                                .offset(x = if (index > 0) ((-8).dp * index) else 0.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.labelSmall,
                                color = when (index % 3) {
                                    0 -> MaterialTheme.colorScheme.onPrimaryContainer
                                    1 -> MaterialTheme.colorScheme.onSecondaryContainer
                                    else -> MaterialTheme.colorScheme.onTertiaryContainer
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBoardTaskDialog(
    onDismiss: () -> Unit,
    onTaskAdd: (String, String, TaskPriority, List<String>) -> Unit,
    columns: List<TaskColumn>
) {
    var taskTitle by remember { mutableStateOf("") }
    var selectedColumnId by remember { mutableStateOf(columns.firstOrNull()?.id ?: "") }
    var selectedPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var assigneeInputValue by remember { mutableStateOf("") }
    var assignees by remember { mutableStateOf(listOf<String>()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Task") },
        text = {
            Column {
                // Task title
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    label = { Text("Task Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Column selection
                Text("Column", style = MaterialTheme.typography.bodyMedium)
                
                Spacer(modifier = Modifier.height(4.dp))
                
                if (columns.isNotEmpty()) {
                    // Column dropdown
                    ExposedDropdownMenuBox(
                        expanded = false,
                        onExpandedChange = { /* Handle dropdown */ }
                    ) {
                        OutlinedTextField(
                            value = columns.find { it.id == selectedColumnId }?.title ?: "",
                            onValueChange = { },
                            readOnly = true,
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        
                        /* Dropdown implementation omitted for simplicity */
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Priority selection
                Text("Priority", style = MaterialTheme.typography.bodyMedium)
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PriorityOptions.forEach { priority ->
                        FilterChip(
                            selected = selectedPriority == priority,
                            onClick = { selectedPriority = priority },
                            label = { Text(priority.name.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Assignees
                Text("Assignees", style = MaterialTheme.typography.bodyMedium)
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Assignee input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = assigneeInputValue,
                        onValueChange = { assigneeInputValue = it },
                        label = { Text("Add Assignee") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Add assignee button
                    IconButton(
                        onClick = {
                            if (assigneeInputValue.isNotBlank()) {
                                assignees = assignees + assigneeInputValue
                                assigneeInputValue = ""
                            }
                        },
                        enabled = assigneeInputValue.isNotBlank()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
                
                // Assignee chips
                if (assignees.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        assignees.forEach { assignee ->
                            AssigneeChip(
                                name = assignee,
                                onRemove = {
                                    assignees = assignees.filter { it != assignee }
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onTaskAdd(taskTitle, selectedColumnId, selectedPriority, assignees)
                },
                enabled = taskTitle.isNotBlank() && selectedColumnId.isNotBlank()
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
fun AssigneeChip(
    name: String,
    onRemove: () -> Unit
) {
    InputChip(
        selected = true,
        onClick = { /* No action */ },
        label = { Text(name) },
        trailingIcon = {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(18.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    )
}

@Composable
fun AddColumnDialog(
    onDismiss: () -> Unit,
    onColumnAdd: (String) -> Unit
) {
    var columnTitle by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Column") },
        text = {
            OutlinedTextField(
                value = columnTitle,
                onValueChange = { columnTitle = it },
                label = { Text("Column Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onColumnAdd(columnTitle) },
                enabled = columnTitle.isNotBlank()
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

// Helper for capitalization - updated version
private fun String.capitalizeFirst(): String {
    return this.lowercase().replaceFirstChar { it.uppercase() }
}

// Domain models
data class TaskColumn(
    val id: String,
    val title: String,
    val taskCount: Int,
    val tasks: MutableList<BoardTask>
)

data class BoardTask(
    val id: String,
    val title: String,
    val dueDate: LocalDate,
    val priority: TaskPriority,
    val assignees: List<String>
)
