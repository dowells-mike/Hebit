package com.hebit.app.ui.screens.habits

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hebit.app.ui.components.BottomNavItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitListScreen(
    onNavigateBack: () -> Unit,
    onHabitClick: (String) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onTasksClick: () -> Unit = {},
    onGoalsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    // Mock habits data - would come from ViewModel in real app
    val habits = remember {
        mutableStateListOf(
            HabitItem(
                id = "1",
                title = "Morning Workout",
                category = HabitCategory.FITNESS,
                frequency = HabitFrequency.DAILY,
                time = "6:00 AM",
                streak = 5,
                completedToday = false
            ),
            HabitItem(
                id = "2",
                title = "Read 30 minutes",
                category = HabitCategory.EDUCATION,
                frequency = HabitFrequency.DAILY,
                time = "Evening",
                streak = 12,
                completedToday = true
            ),
            HabitItem(
                id = "3",
                title = "Weekly Review",
                category = HabitCategory.PRODUCTIVITY,
                frequency = HabitFrequency.WEEKLY,
                time = null,
                streak = 4,
                completedToday = false
            ),
            HabitItem(
                id = "4",
                title = "Meditation",
                category = HabitCategory.MINDFULNESS,
                frequency = HabitFrequency.DAILY,
                time = "8:00 AM",
                streak = 8,
                completedToday = true
            ),
            HabitItem(
                id = "5",
                title = "Drink Water",
                category = HabitCategory.HEALTH,
                frequency = HabitFrequency.DAILY,
                time = null,
                streak = 9,
                completedToday = false
            ),
            HabitItem(
                id = "6",
                title = "Journal",
                category = HabitCategory.MINDFULNESS,
                frequency = HabitFrequency.DAILY,
                time = "9:30 PM",
                streak = 3,
                completedToday = false
            )
        )
    }
    
    var showAddHabitDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<HabitCategory?>(null) }
    
    // Calculate completed habits
    val completedHabits = habits.count { it.completedToday }
    val totalHabits = habits.size
    val completionRate = if (totalHabits > 0) completedHabits.toFloat() / totalHabits else 0f
    
    val currentDate = remember { LocalDate.now() }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMMM d, yyyy") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Habits") },
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
            FloatingActionButton(onClick = { showAddHabitDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
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
                        selected = false,
                        onClick = onTasksClick
                    )
                    
                    BottomNavItem(
                        icon = Icons.Default.Loop,
                        label = "Habits",
                        selected = true,
                        onClick = { /* Already on habits */ }
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
                .padding(horizontal = 16.dp)
        ) {
            // Category tabs
            CategoriesRow(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )
            
            // Progress card
            ProgressCard(
                date = currentDate.format(dateFormatter),
                completedCount = completedHabits,
                totalCount = totalHabits,
                completionRate = completionRate
            )
            
            // Habit list
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val filteredHabits = if (selectedCategory != null) {
                    habits.filter { it.category == selectedCategory }
                } else {
                    habits
                }
                
                items(filteredHabits, key = { it.id }) { habit ->
                    HabitListItem(
                        habit = habit,
                        onHabitClick = { onHabitClick(habit.id) },
                        onToggleComplete = { habitId ->
                            val index = habits.indexOfFirst { it.id == habitId }
                            if (index != -1) {
                                // Toggle completed status
                                val updatedHabit = habits[index].copy(
                                    completedToday = !habits[index].completedToday,
                                    // Increment streak if completing, don't decrement if uncompleting
                                    streak = if (!habits[index].completedToday) habits[index].streak + 1 else habits[index].streak
                                )
                                habits[index] = updatedHabit
                            }
                        }
                    )
                }
            }
        }
        
        // Add habit dialog
        if (showAddHabitDialog) {
            HabitCreationDialog(
                onDismiss = { showAddHabitDialog = false },
                onHabitCreate = { newHabit ->
                    habits.add(0, newHabit.copy(id = (habits.size + 1).toString()))
                    showAddHabitDialog = false
                }
            )
        }
    }
}

@Composable
fun CategoriesRow(
    selectedCategory: HabitCategory?,
    onCategorySelected: (HabitCategory?) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // "All" option
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("All Habits") }
            )
        }
        
        // Category options
        items(HabitCategory.values()) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(category.title) }
            )
        }
    }
}

@Composable
fun ProgressCard(
    date: String,
    completedCount: Int,
    totalCount: Int,
    completionRate: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Date and Progress header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.titleMedium
                )
                
                Text(
                    text = "Overall Progress",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress indicator and fraction
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { completionRate },
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 8.dp
                    )
                    
                    Text(
                        text = "${(completionRate * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "$completedCount/$totalCount",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "habits completed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Motivational message
            Text(
                text = if (completionRate >= 0.7) {
                    "Keep going! You're doing great today!"
                } else if (completionRate >= 0.3) {
                    "Good progress! Keep up the momentum."
                } else {
                    "Let's start building those habits today!"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun HabitListItem(
    habit: HabitItem,
    onHabitClick: () -> Unit,
    onToggleComplete: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onHabitClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(habit.category.color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = habit.category.icon,
                contentDescription = habit.category.title,
                tint = habit.category.color
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Habit info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = habit.title,
                style = MaterialTheme.typography.titleMedium
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                habit.time?.let {
                    Text(
                        text = " • $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Streak",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = "Streak: ${habit.streak}d",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Completion indicator and progress
        Column(
            horizontalAlignment = Alignment.End
        ) {
            // Completion indicator
            IconButton(
                onClick = { onToggleComplete(habit.id) },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (habit.completedToday) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (habit.completedToday) "Completed" else "Mark as complete",
                    tint = if (habit.completedToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Progress indicator for the habit (simplified version)
            LinearProgressIndicator(
                progress = { habit.streak.toFloat() / 10f }, // Simplified progress calculation
                modifier = Modifier
                    .width(80.dp)
                    .padding(top = 4.dp),
                color = habit.category.color
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitCreationDialog(
    onDismiss: () -> Unit,
    onHabitCreate: (HabitItem) -> Unit
) {
    var habitName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(HabitCategory.HEALTH) }
    var selectedFrequency by remember { mutableStateOf(HabitFrequency.DAILY) }
    var habitTime by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Habit") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // Habit name
                OutlinedTextField(
                    value = habitName,
                    onValueChange = { habitName = it },
                    label = { Text("Habit name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Category selector chips
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleSmall
                )
                
                LazyRow(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(HabitCategory.values()) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category.title) },
                            leadingIcon = {
                                Icon(
                                    imageVector = category.icon,
                                    contentDescription = null,
                                    tint = category.color
                                )
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Frequency selector
                Text(
                    text = "Frequency",
                    style = MaterialTheme.typography.titleSmall
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    HabitFrequency.values().forEach { frequency ->
                        Button(
                            onClick = { selectedFrequency = frequency },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedFrequency == frequency) 
                                    MaterialTheme.colorScheme.primaryContainer
                                else 
                                    MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = frequency.title,
                                color = if (selectedFrequency == frequency)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Time preference
                Text(
                    text = "Time Preference (Optional)",
                    style = MaterialTheme.typography.titleSmall
                )
                
                OutlinedTextField(
                    value = habitTime,
                    onValueChange = { habitTime = it },
                    label = { Text("e.g., Morning, 8:00 AM") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Time"
                        )
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (habitName.isNotBlank()) {
                        val newHabit = HabitItem(
                            id = "temp",
                            title = habitName,
                            category = selectedCategory,
                            frequency = selectedFrequency,
                            time = if (habitTime.isBlank()) null else habitTime,
                            streak = 0,
                            completedToday = false
                        )
                        onHabitCreate(newHabit)
                    }
                },
                enabled = habitName.isNotBlank()
            ) {
                Text("Save Habit")
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
data class HabitItem(
    val id: String,
    val title: String,
    val category: HabitCategory,
    val frequency: HabitFrequency,
    val time: String? = null,
    val streak: Int = 0,
    val completedToday: Boolean = false,
    val description: String? = null
)

enum class HabitFrequency(val title: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    CUSTOM("Custom")
}

enum class HabitCategory(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val color: Color) {
    HEALTH("Health", Icons.Default.Favorite, Color(0xFFE91E63)),
    FITNESS("Fitness", Icons.Default.FitnessCenter, Color(0xFF2196F3)),
    MINDFULNESS("Mindfulness", Icons.Default.SelfImprovement, Color(0xFF9C27B0)),
    PRODUCTIVITY("Productivity", Icons.Default.Schedule, Color(0xFF4CAF50)),
    EDUCATION("Education", Icons.Default.School, Color(0xFFFF9800)),
    CREATIVITY("Creativity", Icons.Default.Palette, Color(0xFF795548)),
    SOCIAL("Social", Icons.Default.People, Color(0xFF3F51B5))
}
