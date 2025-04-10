package com.hebit.app.ui.screens.goals

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hebit.app.domain.model.Goal
import com.hebit.app.ui.components.BottomNavItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class GoalDetailTab {
    TASKS, HABITS, NOTES
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(
    goalId: String,
    onNavigateBack: () -> Unit,
    onHomeClick: () -> Unit = {},
    onTasksClick: () -> Unit = {},
    onHabitsClick: () -> Unit = {},
    onGoalsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onShareClick: () -> Unit = {}
) {
    // Mock goal data - Replace with ViewModel state
    // Use Goal domain model for mock data structure
    val goal = remember {
        Goal(
            id = goalId,
            title = "Learn Spanish",
            description = "Complete B2 level certification and be able to hold conversations",
            targetDate = LocalDate.now().plusMonths(6),
            progress = 45,
            category = "Education", // Added example category
            isCompleted = false,     // Added isCompleted
            createdAt = LocalDate.now().minusMonths(1), // Added example dates
            updatedAt = LocalDate.now()
        )
    }
    
    // Determine GoalStatus based on Goal properties
    val goalStatus = when {
        goal.isCompleted -> GoalStatus.COMPLETED
        goal.progress > 0 -> GoalStatus.IN_PROGRESS
        else -> GoalStatus.NOT_STARTED
    }
    
    val milestones = remember {
        listOf(
            MilestoneItem(
                id = "1",
                title = "Complete A1 Level",
                description = "Basic vocabulary and grammar fundamentals",
                dueDate = LocalDate.of(2025, 1, 15),
                isCompleted = true
            ),
            MilestoneItem(
                id = "2",
                title = "Complete A2 Level",
                description = "Intermediate grammar and conversation skills",
                dueDate = LocalDate.of(2025, 3, 30),
                isCompleted = false
            ),
            MilestoneItem(
                id = "3",
                title = "Complete B1 Level",
                description = "Advanced grammar and fluent conversations",
                dueDate = LocalDate.of(2025, 6, 15),
                isCompleted = false
            )
        )
    }
    
    val relatedTasks = remember {
        listOf(
            TaskItem(
                id = "1",
                title = "Daily Vocabulary Practice",
                duration = "15 minutes",
                isCompleted = false
            ),
            TaskItem(
                id = "2",
                title = "Complete Chapter 3 Exercises",
                duration = "30 minutes",
                isCompleted = true
            ),
            TaskItem(
                id = "3",
                title = "Watch Spanish Series with Subtitles",
                duration = "45 minutes",
                isCompleted = false
            )
        )
    }
    
    val relatedHabits = remember {
        listOf(
            HabitItem(
                id = "1",
                title = "Duolingo Spanish Practice",
                frequency = "Daily",
                streak = 7
            ),
            HabitItem(
                id = "2",
                title = "Spanish Conversation Group",
                frequency = "Weekly",
                streak = 3
            )
        )
    }
    
    val notes = remember {
        mutableStateListOf(
            NoteItem(
                id = "1",
                content = "Need to focus more on verb conjugations",
                date = LocalDate.now().minusDays(5)
            ),
            NoteItem(
                id = "2",
                content = "Found a great podcast for Spanish listening practice: 'Spanish Pod 101'",
                date = LocalDate.now().minusDays(2)
            )
        )
    }
    
    var selectedTab by remember { mutableStateOf(GoalDetailTab.TASKS) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onShareClick) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Goal"
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
                        selected = false,
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
                        selected = true,
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
        ) {
            // Goal Header
            item {
                GoalDetailHeader(
                    goal = goal,
                    goalStatus = goalStatus,
                    onUpdateClick = { /* Update progress dialog */ },
                    onMilestoneClick = { /* Add milestone dialog */ },
                    onShareClick = onShareClick,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Progress Timeline
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Progress Timeline",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        // Timeline view selector
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Week", "Month", "Year").forEach { period ->
                                TimelineChip(
                                    text = period,
                                    selected = period == "Week",
                                    onClick = { /* Change timeline period */ }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Placeholder for timeline visualization
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = MaterialTheme.shapes.medium
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Weekly Progress Graph",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Milestones
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Milestones",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Text(
                            text = "${milestones.count { it.isCompleted }} of ${milestones.size} completed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Display milestones
                    milestones.forEach { milestone ->
                        MilestoneItem(
                            milestone = milestone,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }
                }
            }
            
            // Tabs
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    TabRow(
                        selectedTabIndex = selectedTab.ordinal,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Tab(
                            selected = selectedTab == GoalDetailTab.TASKS,
                            onClick = { selectedTab = GoalDetailTab.TASKS },
                            text = { Text("Tasks") }
                        )
                        
                        Tab(
                            selected = selectedTab == GoalDetailTab.HABITS,
                            onClick = { selectedTab = GoalDetailTab.HABITS },
                            text = { Text("Habits") }
                        )
                        
                        Tab(
                            selected = selectedTab == GoalDetailTab.NOTES,
                            onClick = { selectedTab = GoalDetailTab.NOTES },
                            text = { Text("Notes") }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Tab content
                    when (selectedTab) {
                        GoalDetailTab.TASKS -> {
                            if (relatedTasks.isEmpty()) {
                                EmptyTabContent(text = "No tasks associated with this goal.")
                            } else {
                                relatedTasks.forEach { task ->
                                    RelatedTaskItem(
                                        task = task,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp)
                                    )
                                }
                                
                                OutlinedButton(
                                    onClick = { /* Add task */ },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add Task")
                                }
                            }
                        }
                        
                        GoalDetailTab.HABITS -> {
                            if (relatedHabits.isEmpty()) {
                                EmptyTabContent(text = "No habits associated with this goal.")
                            } else {
                                relatedHabits.forEach { habit ->
                                    RelatedHabitItem(
                                        habit = habit,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp)
                                    )
                                }
                                
                                OutlinedButton(
                                    onClick = { /* Add habit */ },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add Habit")
                                }
                            }
                        }
                        
                        GoalDetailTab.NOTES -> {
                            if (notes.isEmpty()) {
                                EmptyTabContent(text = "No notes for this goal yet.")
                            } else {
                                notes.forEach { note ->
                                    NoteItem(
                                        note = note,
                                        onDeleteNote = { noteId ->
                                            notes.removeIf { it.id == noteId }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp)
                                    )
                                }
                            }
                            
                            OutlinedButton(
                                onClick = { showAddNoteDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add Note")
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
            
            // Add the bottom padding to avoid being cut off by bottom bar
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        // Add Note Dialog
        if (showAddNoteDialog) {
            AddNoteDialog(
                onDismiss = { showAddNoteDialog = false },
                onAddNote = { content ->
                    if (content.isNotBlank()) {
                        notes.add(
                            0,
                            NoteItem(
                                id = (notes.size + 1).toString(),
                                content = content,
                                date = LocalDate.now()
                            )
                        )
                        showAddNoteDialog = false
                    }
                }
            )
        }
    }
}

@Composable
fun GoalDetailHeader(
    goal: Goal,
    goalStatus: GoalStatus,
    onUpdateClick: () -> Unit,
    onMilestoneClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Tag and progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.wrapContentSize()
                ) {
                    Text(
                        text = goal.category,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
                
                Text(
                    text = "${goal.progress}% Progress",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Title and description
            Text(
                text = goal.title,
                style = MaterialTheme.typography.headlineMedium
            )
            
            if (goal.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = goal.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress bar
            LinearProgressIndicator(
                progress = { goal.progress / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onUpdateClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Update,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Update")
                }
                
                OutlinedButton(
                    onClick = onMilestoneClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddTask,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Milestone")
                }
            }
        }
    }
}

@Composable
fun TimelineChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (selected) 
            MaterialTheme.colorScheme.primaryContainer 
        else 
            MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .border(
                width = 1.dp,
                color = if (selected) 
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.small
            )
            .clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) 
                MaterialTheme.colorScheme.onPrimaryContainer
            else
                MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MilestoneItem(
    milestone: MilestoneItem,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (milestone.isCompleted) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = backgroundColor,
                shape = MaterialTheme.shapes.small
            )
            .clip(MaterialTheme.shapes.small)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.small
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox
        Checkbox(
            checked = milestone.isCompleted,
            onCheckedChange = { /* Toggle milestone completion */ },
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.outline
            )
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = milestone.title,
                style = MaterialTheme.typography.titleSmall
            )
            
            milestone.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = milestone.dueDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Edit/actions icon
        IconButton(onClick = { /* Edit milestone */ }) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit Milestone",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RelatedTaskItem(
    task: TaskItem,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (task.isCompleted) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = backgroundColor,
                shape = MaterialTheme.shapes.small
            )
            .clip(MaterialTheme.shapes.small)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.small
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox
        Checkbox(
            checked = task.isCompleted,
            onCheckedChange = { /* Toggle task completion */ },
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.outline
            )
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleSmall
            )
            
            if (task.duration != null) {
                Text(
                    text = task.duration,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Menu
        IconButton(onClick = { /* Task menu */ }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More Options",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RelatedHabitItem(
    habit: HabitItem,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.small
            )
            .clip(MaterialTheme.shapes.small)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.small
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Habit icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Loop,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = habit.title,
                style = MaterialTheme.typography.titleSmall
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = habit.frequency,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Streak badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${habit.streak} day streak",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
        
        // Complete button
        IconButton(onClick = { /* Complete habit */ }) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Complete Habit",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NoteItem(
    note: NoteItem,
    onDeleteNote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.date.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Note",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Note") },
            text = { Text("Are you sure you want to delete this note?") },
            confirmButton = {
                TextButton(
                    onClick = { 
                        onDeleteNote(note.id)
                        showDeleteDialog = false
                    }
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
fun EmptyTabContent(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AddNoteDialog(
    onDismiss: () -> Unit,
    onAddNote: (String) -> Unit
) {
    var noteText by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Note") },
        text = {
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                placeholder = { Text("Write your note here...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                minLines = 3
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onAddNote(noteText) },
                enabled = noteText.isNotBlank()
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

// Data classes for GoalDetailScreen
data class MilestoneItem(
    val id: String,
    val title: String,
    val description: String? = null,
    val dueDate: LocalDate,
    val isCompleted: Boolean = false
)

data class TaskItem(
    val id: String,
    val title: String,
    val duration: String? = null,
    val isCompleted: Boolean = false
)

data class HabitItem(
    val id: String,
    val title: String,
    val frequency: String,
    val streak: Int = 0
)

data class NoteItem(
    val id: String,
    val content: String,
    val date: LocalDate
)
