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
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hebit.app.domain.model.Habit
import com.hebit.app.domain.model.Resource
import com.hebit.app.ui.components.BottomNavItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Define enums for UI representation (can be moved later)
enum class HabitFrequencyUI(val title: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    CUSTOM("Custom")
}

enum class HabitCategoryUI(val title: String, val icon: ImageVector, val color: Color) {
    HEALTH("Health", Icons.Default.Favorite, Color(0xFFE91E63)),
    FITNESS("Fitness", Icons.Default.FitnessCenter, Color(0xFF2196F3)),
    MINDFULNESS("Mindfulness", Icons.Default.SelfImprovement, Color(0xFF9C27B0)),
    PRODUCTIVITY("Productivity", Icons.Default.Schedule, Color(0xFF4CAF50)),
    EDUCATION("Education", Icons.Default.School, Color(0xFFFF9800)),
    CREATIVITY("Creativity", Icons.Default.Palette, Color(0xFF795548)),
    SOCIAL("Social", Icons.Default.People, Color(0xFF3F51B5)),
    OTHER("Other", Icons.Default.Circle, Color.Gray) // Default/fallback
}

// Helper function to map icon names to icons (can be expanded)
fun getIconByName(iconName: String?): ImageVector {
    return when (iconName?.lowercase()) {
        null, "", "default_icon" -> Icons.Default.TaskAlt // Default icon for null or empty
        "water_drop" -> Icons.Outlined.WaterDrop
        "book", "menu_book" -> Icons.AutoMirrored.Filled.MenuBook
        "meditation", "self_improvement" -> Icons.Default.SelfImprovement
        "exercise", "fitness_center" -> Icons.Default.FitnessCenter
        "journal", "edit" -> Icons.Default.Edit
        "favorite" -> Icons.Default.Favorite
        "schedule" -> Icons.Default.Schedule
        "school" -> Icons.Default.School
        "palette" -> Icons.Default.Palette
        "people" -> Icons.Default.People
        else -> Icons.Default.TaskAlt // Default icon
    }
}

// Helper function to map frequency string to UI enum
fun getFrequencyUI(frequency: String?): HabitFrequencyUI {
    return when (frequency?.lowercase()) {
        "daily" -> HabitFrequencyUI.DAILY
        "weekly" -> HabitFrequencyUI.WEEKLY
        "monthly" -> HabitFrequencyUI.MONTHLY
        else -> HabitFrequencyUI.CUSTOM // Treat unknown as custom for now
    }
}

// Helper function to map icon name to category UI (simple mapping for now)
fun getCategoryUIFromIcon(iconName: String?): HabitCategoryUI {
     return when (iconName?.lowercase()) {
        null, "", "default_icon" -> HabitCategoryUI.OTHER
        "water_drop", "favorite" -> HabitCategoryUI.HEALTH
        "exercise", "fitness_center" -> HabitCategoryUI.FITNESS
        "meditation", "self_improvement" -> HabitCategoryUI.MINDFULNESS
        "schedule" -> HabitCategoryUI.PRODUCTIVITY
        "book", "menu_book", "school" -> HabitCategoryUI.EDUCATION
        "palette" -> HabitCategoryUI.CREATIVITY
        "journal", "edit" -> HabitCategoryUI.MINDFULNESS // Or create specific
        "people" -> HabitCategoryUI.SOCIAL
        else -> HabitCategoryUI.OTHER
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitListScreen(
    onNavigateBack: () -> Unit,
    onHabitClick: (String) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onTasksClick: () -> Unit = {},
    onGoalsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    // Inject ViewModel
    viewModel: HabitViewModel = hiltViewModel()
) {
    // Remove mock data
    // val habits = remember { ... }

    // Observe state from ViewModel
    val habitsState by viewModel.habitsState.collectAsState()

    var showAddHabitDialog by remember { mutableStateOf(false) }
    // Use HabitCategoryUI for filtering state
    var selectedCategoryFilter by remember { mutableStateOf<HabitCategoryUI?>(null) }

    // Calculate progress based on ViewModel data (when successful)
    val (completedHabits, totalHabits, completionRate) = remember(habitsState) {
        if (habitsState is Resource.Success) {
            val habitList = (habitsState as Resource.Success<List<Habit>>).data ?: emptyList()
            val completed = habitList.count { it.completedToday }
            val total = habitList.size
            val rate = if (total > 0) completed.toFloat() / total else 0f
            Triple(completed, total, rate)
        } else {
            Triple(0, 0, 0f)
        }
    }

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
            // Category tabs - Use HabitCategoryUI
            CategoriesRow(
                selectedCategory = selectedCategoryFilter,
                onCategorySelected = { selectedCategoryFilter = it }
            )

            // Progress card - Use calculated values
            ProgressCard(
                date = currentDate.format(dateFormatter),
                completedCount = completedHabits,
                totalCount = totalHabits,
                completionRate = completionRate
            )

            // Habit list - Observe ViewModel state
            Box(modifier = Modifier.fillMaxSize()) { // Use Box for alignment
                when (habitsState) {
                    is Resource.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is Resource.Success -> {
                        val habitList = (habitsState as Resource.Success<List<Habit>>).data ?: emptyList()
                        if (habitList.isEmpty()) {
                             Text(
                                 "No habits found. Add one using the '+' button.",
                                 modifier = Modifier.align(Alignment.Center),
                                 textAlign = TextAlign.Center
                             )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val filteredHabits = if (selectedCategoryFilter != null) {
                                    habitList.filter { getCategoryUIFromIcon(it.iconName) == selectedCategoryFilter }
                                } else {
                                    habitList
                                }

                                items(filteredHabits, key = { it.id }) { habit ->
                                    HabitListItem(
                                        habit = habit, // Use Habit domain model
                                        onHabitClick = { onHabitClick(habit.id) },
                                        onToggleComplete = { habitId, currentState ->
                                            // Call ViewModel function
                                            viewModel.toggleHabitCompletion(habitId, currentState)
                                        }
                                    )
                                }
                            }
                        }
                    }
                    is Resource.Error -> {
                        Text(
                            "Error: ${(habitsState as Resource.Error<List<Habit>>).message}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }

        // Add habit dialog - Pass ViewModel
        if (showAddHabitDialog) {
            HabitCreationDialog(
                viewModel = viewModel, // Pass the viewmodel
                onDismiss = { showAddHabitDialog = false }
                // onHabitCreate removed, call viewModel directly
            )
        }
    }
}

@Composable
fun CategoriesRow(
    // Use HabitCategoryUI for state
    selectedCategory: HabitCategoryUI?,
    onCategorySelected: (HabitCategoryUI?) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // \"All\" option
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("All Habits") }
            )
        }

        // Category options - Use HabitCategoryUI
        items(HabitCategoryUI.values()) { category ->
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
    habit: Habit, // Use Habit domain model
    onHabitClick: () -> Unit,
    onToggleComplete: (String, Boolean) -> Unit // Pass current state
) {
    // Map icon name and frequency from Habit model
    val categoryUI = getCategoryUIFromIcon(habit.iconName)
    val frequencyUI = getFrequencyUI(habit.frequency)
    val icon = getIconByName(habit.iconName) // Use helper

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onHabitClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category icon based on mapping
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(categoryUI.color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon, // Use resolved icon
                contentDescription = categoryUI.title,
                tint = categoryUI.color
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Habit info - Use fields from Habit model
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
                    // Display frequency from mapped enum
                    text = frequencyUI.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Optionally display time if available/needed from model
                // habit.time?.let { ... } // Habit model doesn't have time field directly
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Streak",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.error // Or dynamic based on streak > 0
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = "Streak: ${habit.streak}d", // Use streak from Habit model
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Completion indicator and progress
        Column(
            horizontalAlignment = Alignment.End
        ) {
            // Completion indicator - Use completedToday from Habit model
            IconButton(
                // Call lambda with id and *current* completion state
                onClick = { onToggleComplete(habit.id, habit.completedToday) },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (habit.completedToday) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (habit.completedToday) "Completed" else "Mark as complete",
                    tint = if (habit.completedToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Progress indicator - Use streak from Habit model (adjust logic as needed)
            LinearProgressIndicator(
                progress = { habit.streak.toFloat().coerceAtMost(30f) / 30f }, // Example: progress based on streak up to 30 days
                modifier = Modifier
                    .width(80.dp)
                    .padding(top = 4.dp),
                color = categoryUI.color // Use color from mapped category
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitCreationDialog(
    viewModel: HabitViewModel, // Inject ViewModel
    onDismiss: () -> Unit
    // onHabitCreate removed, call viewModel directly
) {
    var habitName by remember { mutableStateOf("") }
    // Use UI enums for dialog state
    var selectedCategory by remember { mutableStateOf(HabitCategoryUI.HEALTH) }
    var selectedFrequency by remember { mutableStateOf(HabitFrequencyUI.DAILY) }
    // Add description state
    var habitDescription by remember { mutableStateOf("") }
    // Time is not part of the simplified Habit model or basic create call, handle later if needed
    // var habitTime by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Habit") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp) // Added spacing
            ) {
                // Habit name
                OutlinedTextField(
                    value = habitName,
                    onValueChange = { habitName = it },
                    label = { Text("Habit name*") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                 // Habit description (Optional)
                OutlinedTextField(
                    value = habitDescription,
                    onValueChange = { habitDescription = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                // Category selector chips - Use UI enum
                Text(
                    text = "Category*",
                    style = MaterialTheme.typography.titleSmall
                )

                LazyRow(
                    modifier = Modifier.padding(vertical = 0.dp), // Reduced padding
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(HabitCategoryUI.values()) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category.title) },
                            leadingIcon = {
                                Icon(
                                    imageVector = category.icon,
                                    contentDescription = null,
                                    // Use category color for icon tint
                                    tint = category.color
                                )
                            }
                        )
                    }
                }


                // Frequency selector - Use UI enum
                Text(
                    text = "Frequency*",
                    style = MaterialTheme.typography.titleSmall
                )

                // Use Row with selectable Text or Buttons
                 Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp) // Added spacing
                ) {
                    HabitFrequencyUI.values().filter { it != HabitFrequencyUI.CUSTOM }.forEach { frequency -> // Exclude Custom for now
                        FilterChip(
                             selected = selectedFrequency == frequency,
                             onClick = { selectedFrequency = frequency },
                             label = { Text(frequency.title) }
                         )
                    }
                }


                // Time preference removed for now, add back if needed
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (habitName.isNotBlank()) {
                        // Map UI selections back to strings for ViewModel
                        val frequencyString = selectedFrequency.name.lowercase() // e.g., "daily"
                        // Map selected category icon name (or title) for the backend
                        // This mapping might need refinement based on what backend expects
                        val iconName = when(selectedCategory) {
                            HabitCategoryUI.HEALTH -> "favorite"
                            HabitCategoryUI.FITNESS -> "fitness_center"
                            HabitCategoryUI.MINDFULNESS -> "self_improvement"
                            HabitCategoryUI.PRODUCTIVITY -> "schedule"
                            HabitCategoryUI.EDUCATION -> "school"
                            HabitCategoryUI.CREATIVITY -> "palette"
                            HabitCategoryUI.SOCIAL -> "people"
                            HabitCategoryUI.OTHER -> "circle" // Default/fallback
                        }

                        viewModel.createHabit(
                            title = habitName,
                            description = habitDescription, // Pass description
                            iconName = iconName, // Pass mapped icon name
                            frequency = frequencyString // Pass mapped frequency string
                        )
                        onDismiss() // Close dialog after calling create
                    }
                },
                enabled = habitName.isNotBlank() // Enable only if name is entered
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

// Remove local data classes HabitItem, HabitFrequency, HabitCategory
// data class HabitItem(...)
// enum class HabitFrequency(...)
// enum class HabitCategory(...)
