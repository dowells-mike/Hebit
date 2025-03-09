package com.hebit.app.ui.screens.goals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalListScreen(
    onNavigateBack: () -> Unit
) {
    // Mock goals data - would come from ViewModel in real app
    val goals = remember {
        mutableStateListOf(
            GoalItem(
                id = "1",
                title = "Learn Kotlin Compose",
                description = "Complete advanced course and build portfolio project",
                targetDate = LocalDate.now().plusMonths(2),
                progress = 35,
                status = GoalStatus.IN_PROGRESS
            ),
            GoalItem(
                id = "2",
                title = "Run 5K",
                description = "Train for and complete a 5K run",
                targetDate = LocalDate.now().plusMonths(1),
                progress = 60,
                status = GoalStatus.IN_PROGRESS
            ),
            GoalItem(
                id = "3",
                title = "Read 12 Books",
                description = "Read one book per month",
                targetDate = LocalDate.now().plusMonths(12),
                progress = 25,
                status = GoalStatus.IN_PROGRESS
            ),
            GoalItem(
                id = "4",
                title = "Launch Side Project",
                description = "Complete development and launch productivity app",
                targetDate = LocalDate.now().plusMonths(3),
                progress = 15,
                status = GoalStatus.IN_PROGRESS
            ),
            GoalItem(
                id = "5",
                title = "Learn Spanish Basics",
                description = "Complete beginner course and practice conversation",
                targetDate = LocalDate.now().plusMonths(6),
                progress = 10,
                status = GoalStatus.IN_PROGRESS
            )
        )
    }
    
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var expandedGoalId by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Goals") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddGoalDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Goal")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Goals summary
            GoalsSummaryCard(
                goals = goals,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            
            // Goals list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(goals, key = { it.id }) { goal ->
                    GoalCard(
                        goal = goal,
                        isExpanded = expandedGoalId == goal.id,
                        onToggleExpand = { 
                            expandedGoalId = if (expandedGoalId == goal.id) null else goal.id
                        },
                        onUpdateProgress = { goalId, newProgress ->
                            // Update the goal progress
                            val index = goals.indexOfFirst { it.id == goalId }
                            if (index != -1) {
                                // Update progress and status if completed
                                val status = if (newProgress >= 100) 
                                    GoalStatus.COMPLETED 
                                else 
                                    GoalStatus.IN_PROGRESS
                                
                                goals[index] = goals[index].copy(
                                    progress = newProgress,
                                    status = status
                                )
                            }
                        },
                        onDeleteGoal = { goalId ->
                            // Remove goal from list
                            goals.removeIf { it.id == goalId }
                        }
                    )
                }
            }
        }
        
        // Add goal dialog
        if (showAddGoalDialog) {
            AddGoalDialog(
                onDismiss = { showAddGoalDialog = false },
                onGoalAdd = { title, description, targetDateMonths ->
                    if (title.isNotBlank()) {
                        val newGoal = GoalItem(
                            id = (goals.size + 1).toString(),
                            title = title,
                            description = description,
                            targetDate = LocalDate.now().plusMonths(targetDateMonths.toLong()),
                            progress = 0,
                            status = GoalStatus.NOT_STARTED
                        )
                        goals.add(0, newGoal)
                        showAddGoalDialog = false
                    }
                }
            )
        }
    }
}

@Composable
fun GoalsSummaryCard(
    goals: List<GoalItem>,
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
                    text = goals.size.toString(),
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
                    text = goals.count { it.status == GoalStatus.COMPLETED }.toString(),
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
                    text = goals.count { it.status == GoalStatus.IN_PROGRESS }.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Text(text = "In Progress", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalCard(
    goal: GoalItem,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onUpdateProgress: (String, Int) -> Unit,
    onDeleteGoal: (String) -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableStateOf(goal.progress.toFloat()) }
    
    // Update local slider value when external progress changes
    LaunchedEffect(goal.progress) {
        sliderPosition = goal.progress.toFloat()
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onToggleExpand
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = goal.description ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                        overflow = if (isExpanded) TextOverflow.Clip else TextOverflow.Ellipsis
                    )
                }
                
                // Status chip
                val (statusColor, statusText) = when (goal.status) {
                    GoalStatus.NOT_STARTED -> Pair(MaterialTheme.colorScheme.outline, "Not Started")
                    GoalStatus.IN_PROGRESS -> Pair(MaterialTheme.colorScheme.tertiary, "In Progress")
                    GoalStatus.COMPLETED -> Pair(MaterialTheme.colorScheme.primary, "Completed")
                }
                
                SuggestionChip(
                    onClick = { },
                    label = { Text(statusText) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = statusColor.copy(alpha = 0.1f),
                        labelColor = statusColor
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress bar
            LinearProgressIndicator(
                progress = { goal.progress / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = when {
                    goal.progress < 30 -> MaterialTheme.colorScheme.error
                    goal.progress < 70 -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Text(
                text = "${goal.progress}% Complete",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.End)
            )
            
            // Target date
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = "Target: ${goal.targetDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Expand/collapse icon
                IconButton(onClick = onToggleExpand) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Expanded section
            if (isExpanded) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Progress adjustment slider
                Text(
                    text = "Update Progress",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Slider(
                    value = sliderPosition,
                    onValueChange = { sliderPosition = it },
                    valueRange = 0f..100f,
                    steps = 100,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${sliderPosition.toInt()}%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Button(
                        onClick = {
                            onUpdateProgress(goal.id, sliderPosition.toInt())
                        },
                        enabled = sliderPosition.toInt() != goal.progress
                    ) {
                        Text("Update")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Delete button
                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete Goal")
                }
            }
        }
    }
    
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Goal") },
            text = { Text("Are you sure you want to delete this goal? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteGoal(goal.id)
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
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onGoalAdd: (String, String, Int) -> Unit
) {
    var goalTitle by remember { mutableStateOf("") }
    var goalDescription by remember { mutableStateOf("") }
    var targetMonths by remember { mutableStateOf("3") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Goal") },
        text = {
            Column {
                OutlinedTextField(
                    value = goalTitle,
                    onValueChange = { goalTitle = it },
                    label = { Text("Goal title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = goalDescription,
                    onValueChange = { goalDescription = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Target completion in (months):", style = MaterialTheme.typography.bodyMedium)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("1", "3", "6", "12").forEach { month ->
                        FilterChip(
                            selected = targetMonths == month,
                            onClick = { targetMonths = month },
                            label = { Text("$month month${if (month != "1") "s" else ""}") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    val months = targetMonths.toIntOrNull() ?: 3
                    onGoalAdd(goalTitle, goalDescription, months) 
                },
                enabled = goalTitle.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Domain models
data class GoalItem(
    val id: String,
    val title: String,
    val description: String? = null,
    val targetDate: LocalDate,
    val progress: Int = 0,
    val status: GoalStatus = GoalStatus.NOT_STARTED
)

enum class GoalStatus {
    NOT_STARTED, IN_PROGRESS, COMPLETED
}
