package com.hebit.app.ui.screens.dashboard

import android.os.Build
import androidx.annotation.RequiresApi
import com.hebit.app.ui.components.BottomNavItem
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onTasksClick: () -> Unit,
    onHabitsClick: () -> Unit,
    onGoalsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onQuickActionsClick: () -> Unit = {},
    onProgressStatsClick: () -> Unit = {}
) {
    val today = LocalDate.now()
    val formattedDate = today.format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault()))
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Today's Overview") },
                actions = {
                    IconButton(onClick = { onProgressStatsClick() }) {
                        Icon(Icons.Default.BarChart, contentDescription = "Stats")
                    }
                    
                    IconButton(onClick = { onQuickActionsClick() }) {
                        Icon(Icons.Default.Speed, contentDescription = "Quick Actions")
                    }
                    
                    IconButton(onClick = { /* Open notifications */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                    
                    IconButton(onClick = { onSettingsClick() }) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
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
                        selected = true,
                        onClick = { /* Already on home */ }
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
                        selected = false,
                        onClick = onGoalsClick
                    )
                    
                    BottomNavItem(
                        icon = Icons.Default.Person,
                        label = "Profile",
                        selected = false,
                        onClick = onSettingsClick
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Add new item */ },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date and Weather Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = "Weather",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "23°",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            
            // Priority Tasks Section
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Priority Tasks",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    TextButton(onClick = { onTasksClick() }) {
                        Text("See All")
                    }
                }
                
                // Horizontal scrolling task cards
                PriorityTasksList(onTaskClick = { /* Open task details */ })
            }
            
            // Today's Habits Section
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Habits",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Text(
                        text = "3/5 Completed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                
                // Horizontal scrolling habit cards
                TodayHabitsList(onHabitClick = { /* Open habit details */ })
            }
            
            // Active Goals Section
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Active Goals",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Goal cards
                ActiveGoalsList(onGoalClick = { /* Open goal details */ })
            }
            
            // Quick Links Section
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Quick Links",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    QuickLinkCard(
                        title = "Progress Stats",
                        icon = Icons.Default.BarChart,
                        description = "View your productivity metrics",
                        onClick = onProgressStatsClick,
                        modifier = Modifier.weight(1f)
                    )
                    
                    QuickLinkCard(
                        title = "Quick Actions",
                        icon = Icons.Default.Speed,
                        description = "Access common tasks faster",
                        onClick = onQuickActionsClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Extra space for FAB
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// Using common BottomNavItem from components package

@Composable
fun PriorityTasksList(onTaskClick: (String) -> Unit) {
    val tasks = listOf(
        Task("Client Meeting Preparation", "Work", "2:00 PM", 70),
        Task("Gym Workout", "Personal", "5:30 PM", 0),
        Task("Project Deadline", "Work", "11:59 PM", 90)
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        tasks.forEach { task ->
            TaskCard(task = task, onClick = { onTaskClick(task.title) })
        }
    }
}

@Composable
fun TaskCard(task: Task, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(140.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.category,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                if (task.category == "Work") {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = "Priority",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Text(
                text = "Due ${task.dueTime}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            
            if (task.progress > 0) {
                Column {
                    LinearProgressIndicator(
                        progress = { task.progress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TodayHabitsList(onHabitClick: (String) -> Unit) {
    val habits = listOf(
        Habit("Drink Water", Icons.Outlined.WaterDrop, true),
        Habit("Read", Icons.Default.MenuBook, true),
        Habit("Meditate", Icons.Default.SelfImprovement, false),
        Habit("Exercise", Icons.Default.FitnessCenter, true),
        Habit("Journal", Icons.Default.Edit, false)
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        habits.forEach { habit ->
            HabitCard(habit = habit, onClick = { onHabitClick(habit.title) })
        }
    }
}

@Composable
fun HabitCard(habit: Habit, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(100.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (habit.completed) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f) 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = habit.icon,
                contentDescription = habit.title,
                tint = if (habit.completed) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = habit.title,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Progress indicator
            if (habit.completed) {
                Box(
                    modifier = Modifier
                        .size(4.dp, 2.dp)
                        .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                )
            }
        }
    }
}

@Composable
fun ActiveGoalsList(onGoalClick: (String) -> Unit) {
    val goals = listOf(
        Goal("Learn Spanish", 75, "Feb 28, 2025", "Education")
    )
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        goals.forEach { goal ->
            GoalCard(goal = goal, onClick = { onGoalClick(goal.title) })
        }
    }
}

@Composable
fun GoalCard(goal: Goal, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick
    ) {
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
                    text = goal.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "${goal.progress}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { goal.progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Due ${goal.dueDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                
                Text(
                    text = goal.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun QuickLinkCard(
    title: String, 
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(140.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// Data classes for UI models
data class Task(
    val title: String,
    val category: String,
    val dueTime: String,
    val progress: Int
)

data class Habit(
    val title: String,
    val icon: ImageVector,
    val completed: Boolean
)

data class Goal(
    val title: String,
    val progress: Int,
    val dueDate: String,
    val category: String
)
