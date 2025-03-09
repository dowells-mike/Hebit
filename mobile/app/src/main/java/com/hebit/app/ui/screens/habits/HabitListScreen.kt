package com.hebit.app.ui.screens.habits

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitListScreen(
    onNavigateBack: () -> Unit
) {
    // Mock habits data - would come from ViewModel in real app
    val habits = remember {
        mutableStateListOf(
            HabitItem(
                id = "1",
                title = "Morning Meditation",
                description = "10 minutes mindfulness meditation",
                frequency = HabitFrequency.DAILY,
                streak = 5
            ),
            HabitItem(
                id = "2",
                title = "Read a book",
                description = "Read at least 30 minutes",
                frequency = HabitFrequency.DAILY,
                streak = 3
            ),
            HabitItem(
                id = "3",
                title = "Weekly Review",
                description = "Review goals and progress",
                frequency = HabitFrequency.WEEKLY,
                streak = 2
            ),
            HabitItem(
                id = "4",
                title = "Exercise",
                description = "30 minutes workout",
                frequency = HabitFrequency.DAILY,
                streak = 8
            ),
            HabitItem(
                id = "5",
                title = "Study Language",
                description = "Practice for 20 minutes",
                frequency = HabitFrequency.DAILY,
                streak = 4
            )
        )
    }
    
    var showAddHabitDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Habits") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddHabitDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Habits summary card
            HabitsSummaryCard(
                habits = habits,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            
            // Habit list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(habits, key = { it.id }) { habit ->
                    HabitCard(
                        habit = habit,
                        onMarkComplete = { habitId ->
                            // Mark habit as complete for today
                            val index = habits.indexOfFirst { it.id == habitId }
                            if (index != -1) {
                                val updatedHabit = habits[index].copy(
                                    lastCompleted = LocalDate.now(), 
                                    streak = habits[index].streak + 1
                                )
                                habits[index] = updatedHabit
                            }
                        },
                        onDeleteHabit = { habitId ->
                            // Remove habit from list
                            habits.removeIf { it.id == habitId }
                        }
                    )
                }
            }
        }
        
        // Add habit dialog
        if (showAddHabitDialog) {
            AddHabitDialog(
                onDismiss = { showAddHabitDialog = false },
                onHabitAdd = { title, description, frequency ->
                    if (title.isNotBlank()) {
                        val newHabit = HabitItem(
                            id = (habits.size + 1).toString(),
                            title = title,
                            description = description,
                            frequency = frequency,
                            streak = 0
                        )
                        habits.add(0, newHabit)
                        showAddHabitDialog = false
                    }
                }
            )
        }
    }
}

@Composable
fun HabitsSummaryCard(
    habits: List<HabitItem>,
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
                    text = habits.size.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(text = "Total Habits", style = MaterialTheme.typography.bodyMedium)
            }
            
            Divider(
                modifier = Modifier
                    .height(36.dp)
                    .width(1.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = habits.count { it.frequency == HabitFrequency.DAILY }.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(text = "Daily", style = MaterialTheme.typography.bodyMedium)
            }
            
            Divider(
                modifier = Modifier
                    .height(36.dp)
                    .width(1.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = habits.count { it.frequency == HabitFrequency.WEEKLY }.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Text(text = "Weekly", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitCard(
    habit: HabitItem,
    onMarkComplete: (String) -> Unit,
    onDeleteHabit: (String) -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val isCompletedToday = habit.lastCompleted == LocalDate.now()
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.title,
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    habit.description?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (habit.frequency) {
                                HabitFrequency.DAILY -> Icons.Default.Today
                                HabitFrequency.WEEKLY -> Icons.Default.DateRange
                                HabitFrequency.MONTHLY -> Icons.Default.CalendarMonth
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = habit.frequency.name.lowercase().capitalize(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = "${habit.streak} streak",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Row {
                    // Delete button
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete habit",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        )
                    }
                    
                    // Mark complete button
                    IconButton(
                        onClick = { onMarkComplete(habit.id) },
                        enabled = !isCompletedToday
                    ) {
                        Icon(
                            imageVector = if (isCompletedToday) Icons.Default.CheckCircle else Icons.Default.Circle,
                            contentDescription = "Mark as complete",
                            tint = if (isCompletedToday) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
    
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Habit") },
            text = { Text("Are you sure you want to delete this habit? This will also remove all tracking history.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteHabit(habit.id)
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
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onHabitAdd: (String, String, HabitFrequency) -> Unit
) {
    var habitTitle by remember { mutableStateOf("") }
    var habitDescription by remember { mutableStateOf("") }
    var selectedFrequency by remember { mutableStateOf(HabitFrequency.DAILY) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Habit") },
        text = {
            Column {
                OutlinedTextField(
                    value = habitTitle,
                    onValueChange = { habitTitle = it },
                    label = { Text("Habit name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = habitDescription,
                    onValueChange = { habitDescription = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Frequency", style = MaterialTheme.typography.bodyMedium)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FrequencyOptions.forEach { frequency ->
                        FilterChip(
                            selected = selectedFrequency == frequency,
                            onClick = { selectedFrequency = frequency },
                            label = { Text(frequency.name.lowercase().capitalize()) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onHabitAdd(habitTitle, habitDescription, selectedFrequency) },
                enabled = habitTitle.isNotBlank()
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

// Helper for capitalization
private fun String.capitalize(): String {
    return this.lowercase().replaceFirstChar { it.uppercase() }
}

// Domain models
data class HabitItem(
    val id: String,
    val title: String,
    val description: String? = null,
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val streak: Int = 0,
    val lastCompleted: LocalDate? = null
)

enum class HabitFrequency {
    DAILY, WEEKLY, MONTHLY
}

val FrequencyOptions = listOf(HabitFrequency.DAILY, HabitFrequency.WEEKLY, HabitFrequency.MONTHLY)
