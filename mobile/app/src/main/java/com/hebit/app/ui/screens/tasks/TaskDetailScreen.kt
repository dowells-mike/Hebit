package com.hebit.app.ui.screens.tasks

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.material3.FilterChip
import com.hebit.app.ui.components.BottomNavItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String,
    onNavigateBack: () -> Unit,
    onHomeClick: () -> Unit = {},
    onTasksClick: () -> Unit = {},
    onHabitsClick: () -> Unit = {},
    onGoalsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    // Mock task data - would come from ViewModel in real app
    val task = remember {
        mutableStateOf(
            TaskDetailItem(
                id = taskId,
                title = "Design System Updates",
                description = "Update the design system components to match the new brand guidelines. Include documentation and examples.",
                status = TaskStatus.IN_PROGRESS,
                dueDate = LocalDate.of(2025, 3, 15),
                category = "Design",
                createdDate = LocalDate.of(2025, 3, 10),
                modifiedDate = LocalDate.of(2025, 3, 12),
                subtasks = mutableStateListOf(
                    SubTask("1", "Update color palette", true, LocalDate.of(2025, 3, 13)),
                    SubTask("2", "Review typography", false, LocalDate.of(2025, 3, 14))
                ),
                comments = mutableStateListOf(
                    Comment("1", "Sarah Chen", "Let's review the typography changes tomorrow.", LocalDate.now().atTime(10, 0)),
                    Comment("2", "Alex Kim", "Updated the due date", LocalDate.now().minusDays(1).atTime(16, 30))
                )
            )
        )
    }
    
    var showCompleteDialog by remember { mutableStateOf(false) }
    var showEditMenu by remember { mutableStateOf(false) }
    var expandedDescription by remember { mutableStateOf(false) }
    var newSubtask by remember { mutableStateOf("") }
    var newComment by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                                // Open edit task
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share") },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                            onClick = { 
                                showEditMenu = false
                                // Open share dialog
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                            onClick = { 
                                showEditMenu = false
                                // Show delete confirmation
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Status indicator
            item {
                Row(
                    modifier = Modifier.padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = true,
                        onClick = { /* Toggle status */ },
                        label = { Text(task.value.status.displayName) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = "Status",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                }
            }
            
            // Task title
            item {
                Text(
                    text = task.value.title,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Due date
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
                        text = "Due ${task.value.dueDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Divider()
            }
            
            // Description
            item {
                Column(modifier = Modifier.padding(vertical = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        IconButton(
                            onClick = { expandedDescription = !expandedDescription },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (expandedDescription) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (expandedDescription) "Collapse" else "Expand",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    if (expandedDescription) {
                        Text(
                            text = task.value.description,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    } else {
                        Text(
                            text = task.value.description,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                
                Divider()
            }
            
            // Category
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Text(
                        text = task.value.category,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Divider()
            }
            
            // Dates
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Created",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Text(
                        text = task.value.createdDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Modified",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Text(
                        text = task.value.modifiedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Divider()
            }
            
            // Subtasks
            item {
                Column(modifier = Modifier.padding(vertical = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Subtasks (${task.value.subtasks.count { it.completed }}/${task.value.subtasks.size})",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        LinearProgressIndicator(
                            progress = { 
                                if (task.value.subtasks.isEmpty()) 0f 
                                else task.value.subtasks.count { it.completed }.toFloat() / task.value.subtasks.size 
                            },
                            modifier = Modifier
                                .width(100.dp)
                                .height(6.dp),
                            strokeCap = StrokeCap.Round
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    task.value.subtasks.forEach { subtask ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = subtask.completed,
                                onCheckedChange = { isChecked -> 
                                    val index = task.value.subtasks.indexOf(subtask)
                                    if (index != -1) {
                                        task.value.subtasks[index] = subtask.copy(completed = isChecked)
                                    }
                                }
                            )
                            
                            Text(
                                text = subtask.title,
                                style = MaterialTheme.typography.bodyMedium,
                                textDecoration = if (subtask.completed) TextDecoration.LineThrough else TextDecoration.None,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Text(
                                text = subtask.date.format(DateTimeFormatter.ofPattern("MMM d")),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    OutlinedTextField(
                        value = newSubtask,
                        onValueChange = { newSubtask = it },
                        placeholder = { Text("Add subtask") },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (newSubtask.isNotBlank()) {
                                        task.value.subtasks.add(
                                            SubTask(
                                                id = (task.value.subtasks.size + 1).toString(),
                                                title = newSubtask,
                                                completed = false,
                                                date = LocalDate.now()
                                            )
                                        )
                                        newSubtask = ""
                                    }
                                },
                                enabled = newSubtask.isNotBlank()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        singleLine = true
                    )
                }
                
                Divider()
            }
            
            // Comments
            item {
                Column(modifier = Modifier.padding(vertical = 16.dp)) {
                    Text(
                        text = "Comments",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    task.value.comments.forEach { comment ->
                        CommentItem(comment = comment)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // User avatar
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "M",
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        OutlinedTextField(
                            value = newComment,
                            onValueChange = { newComment = it },
                            placeholder = { Text("Add comment...") },
                            trailingIcon = {
                                IconButton(onClick = { /* Send comment */ }) {
                                    Icon(Icons.Default.Send, contentDescription = "Send")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        // Bottom action buttons
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Edit button
                OutlinedButton(
                    onClick = { /* Edit task */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit")
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Complete button
                Button(
                    onClick = { showCompleteDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Complete")
                }
            }
        }
        
        // Complete task confirmation dialog
        if (showCompleteDialog) {
            AlertDialog(
                onDismissRequest = { showCompleteDialog = false },
                title = { Text("Complete Task") },
                text = { Text("Mark this task as completed?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // Mark task as completed
                            task.value = task.value.copy(status = TaskStatus.COMPLETED)
                            showCompleteDialog = false
                            // Navigate back after delay
                        }
                    ) {
                        Text("Complete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCompleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CommentItem(comment: Comment) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = comment.author.first().toString(),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column {
                Text(
                    text = comment.author,
                    style = MaterialTheme.typography.titleSmall
                )
                
                Text(
                    text = "${comment.timestamp.toLocalTime().hour}h ago",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Text(
            text = comment.content,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 40.dp, top = 4.dp)
        )
    }
}

// Domain models
data class TaskDetailItem(
    val id: String,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val dueDate: LocalDate,
    val category: String,
    val createdDate: LocalDate,
    val modifiedDate: LocalDate,
    val subtasks: MutableList<SubTask>,
    val comments: MutableList<Comment>
)

enum class TaskStatus(val displayName: String) {
    TODO("To Do"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed")
}

data class SubTask(
    val id: String,
    val title: String,
    val completed: Boolean,
    val date: LocalDate
)

data class Comment(
    val id: String,
    val author: String,
    val content: String,
    val timestamp: java.time.LocalDateTime
)
