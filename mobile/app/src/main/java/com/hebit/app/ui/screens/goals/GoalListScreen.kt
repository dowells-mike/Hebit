package com.hebit.app.ui.screens.goals

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.hebit.app.ui.components.BottomNavItem
import java.time.Duration
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

enum class GoalViewMode {
    LIST_VIEW, TIMELINE
}

enum class GoalCategory {
    ALL, PERSONAL, CAREER, HEALTH, EDUCATION, FINANCIAL
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalListScreen(
    onNavigateBack: () -> Unit,
    onGoalClick: (String) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onTasksClick: () -> Unit = {},
    onHabitsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
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
    
    // State for view mode and selected category
    var viewMode by remember { mutableStateOf(GoalViewMode.LIST_VIEW) }
    var selectedCategory by remember { mutableStateOf(GoalCategory.ALL) }

    // Filter goals based on selected category
    val filteredGoals = when (selectedCategory) {
        GoalCategory.ALL -> goals
        // In a real app, goals would have a category property
        // For mock data, we'll distribute them across categories
        GoalCategory.PERSONAL -> goals.filter { it.id.toInt() % 5 == 1 }
        GoalCategory.CAREER -> goals.filter { it.id.toInt() % 5 == 2 }
        GoalCategory.HEALTH -> goals.filter { it.id.toInt() % 5 == 3 }
        GoalCategory.EDUCATION -> goals.filter { it.id.toInt() % 5 == 4 }
        GoalCategory.FINANCIAL -> goals.filter { it.id.toInt() % 5 == 0 }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Goals") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Search functionality */ }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Goals"
                        )
                    }
                    IconButton(onClick = { /* Filter functionality */ }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter Goals"
                        )
                    }
                    IconButton(onClick = { /* More options */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options"
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
                        onClick = {}
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
            // View mode switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            shape = MaterialTheme.shapes.small
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clickable { viewMode = GoalViewMode.LIST_VIEW }
                            .background(
                                if (viewMode == GoalViewMode.LIST_VIEW)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    Color.Transparent
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "List View",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (viewMode == GoalViewMode.LIST_VIEW)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .clickable { viewMode = GoalViewMode.TIMELINE }
                            .background(
                                if (viewMode == GoalViewMode.TIMELINE)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    Color.Transparent
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Timeline",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (viewMode == GoalViewMode.TIMELINE)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            
            // Category selector
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(GoalCategory.values()) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { 
                            Text(
                                text = when(category) {
                                    GoalCategory.ALL -> "All Goals"
                                    GoalCategory.PERSONAL -> "Personal"
                                    GoalCategory.CAREER -> "Career"
                                    GoalCategory.HEALTH -> "Health"
                                    GoalCategory.EDUCATION -> "Education"
                                    GoalCategory.FINANCIAL -> "Financial"
                                }
                            )
                        },
                        leadingIcon = if (selectedCategory == category) {
                            {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null
                    )
                }
            }
            
            if (viewMode == GoalViewMode.LIST_VIEW) {
                // List view - goals list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredGoals, key = { it.id }) { goal ->
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
            } else {
                // Timeline view
                TimelineView(
                    goals = filteredGoals,
                    onGoalClick = onGoalClick,
                    modifier = Modifier.fillMaxSize()
                )
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

@SuppressLint("NewApi")
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TimelineView(
    goals: List<GoalItem>,
    onGoalClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Sort goals by target date
    val sortedGoals = remember(goals) {
        goals.sortedBy { it.targetDate }
    }
    
    // Group goals by months
    val goalsByMonth = remember(sortedGoals) {
        sortedGoals.groupBy { YearMonth.from(it.targetDate) }
    }
    
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        goalsByMonth.forEach { (yearMonth, goalsInMonth) ->
            item {
                Text(
                    text = yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
                )
                
                // Timeline for this month
                Column {
                    goalsInMonth.forEach { goal ->
                        TimelineGoalItem(
                            goal = goal,
                            onClick = { onGoalClick(goal.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TimelineGoalItem(
    goal: GoalItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when (goal.status) {
        GoalStatus.NOT_STARTED -> MaterialTheme.colorScheme.outline
        GoalStatus.IN_PROGRESS -> MaterialTheme.colorScheme.tertiary
        GoalStatus.COMPLETED -> MaterialTheme.colorScheme.primary
    }
    
    Row(
        modifier = modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.Top
    ) {
        // Timeline dot and line
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(if (goal == goal) 24.dp else 80.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            // Vertical line
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(2.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
            
            // Circle marker
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(statusColor)
                    .align(Alignment.TopCenter),
                contentAlignment = Alignment.Center
            ) {
                if (goal.status == GoalStatus.COMPLETED) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Goal content
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Header with date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = goal.targetDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Status chip
                    val statusText = when (goal.status) {
                        GoalStatus.NOT_STARTED -> "Not Started"
                        GoalStatus.IN_PROGRESS -> "In Progress"
                        GoalStatus.COMPLETED -> "Completed"
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
                
                // Goal title
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleMedium
                )
                
                // Description if available
                goal.description?.let { description ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
            }
        }
    }
}
