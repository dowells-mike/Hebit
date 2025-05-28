package com.hebit.app.ui.screens.habits

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hebit.app.domain.model.Habit
import com.hebit.app.domain.model.HabitFrequency
import com.hebit.app.domain.model.HabitStats
import com.hebit.app.domain.model.Note // Assuming Note domain model exists
import com.hebit.app.domain.model.Resource
import com.hebit.app.ui.components.BottomNavItem // Keep if used
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import com.hebit.app.domain.model.HabitFrequencyType
import com.hebit.app.domain.model.DayOfWeekDomain

// Import helpers from the new utility file
import com.hebit.app.ui.screens.habits.HabitCategoryUI
import com.hebit.app.ui.screens.habits.getIconByName
import com.hebit.app.ui.screens.habits.getCategoryUI
import com.hebit.app.ui.screens.habits.formatHabitFrequency

// Data class for displaying day info in the calendar
data class DayInfo(
    val date: LocalDate,
    val isCompleted: Boolean,
    val isToday: Boolean
)

// Helper functions are now in HabitScreenUtils.kt and imported above.

@Composable
fun HabitHeader(
    habit: Habit,
    modifier: Modifier = Modifier
) {
    val categoryUI = getCategoryUI(habit.category) 
    val frequencyText = formatHabitFrequency(habit.frequency) 
    val icon = getIconByName(habit.icon) 

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = categoryUI.title,
                tint = categoryUI.color,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = frequencyText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = " • Current Streak: ${habit.streakData?.current ?: 0}d", 
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
            // Optional: Edit button if needed in the header
            // IconButton(onClick = { /* TODO: onEditClick */ }) {
            //     Icon(Icons.Default.Edit, contentDescription = "Edit Habit")
            // }
        }

        if (!habit.description.isNullOrBlank()) {
            Text(
                text = habit.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun StatItem(
    icon: ImageVector,
    title: String,
    value: String,
    unit: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null, // Title is descriptive enough
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium, // Adjusted for better fit
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        unit?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun WeekCalendarView(
    habit: Habit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    // Display current week, adjust as needed (e.g., show week of last completion)
    val startOfWeek = today.with(DayOfWeek.MONDAY) // Or Locale specific start
    val days = (0..6).map { startOfWeek.plusDays(it.toLong()) }

    val completionMap = habit.completionHistory
        .associate { it.date to it.completed } // it.date is already LocalDate

    Text(
        "This Week's Progress",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        days.forEach { date ->
            val isCompleted = completionMap[date] ?: false
            val isToday = date.isEqual(today)
            DayCell(date = date, isCompleted = isCompleted, isToday = isToday)
        }
    }
}

@Composable
fun DayCell(date: LocalDate, isCompleted: Boolean, isToday: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(4.dp)
            .then(
                if (isToday) Modifier.border(
                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                    CircleShape
                ) else Modifier
            )
            .padding(4.dp) // Padding inside border
    ) {
        Text(
            text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
        )
        Box(
            modifier = Modifier
                .size(24.dp)
                .padding(top = 4.dp)
                .clip(CircleShape)
                .background(if (isCompleted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "Completed",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    habitId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEditHabit: (String) -> Unit,
    onNavigateToStreakDetail: (String) -> Unit, // For navigating to a dedicated streak screen
    // For bottom navigation
    onHomeClick: () -> Unit = {},
    onTasksClick: () -> Unit = {},
    onHabitsClick: () -> Unit = {}, // Current screen, might not need click action
    onGoalsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    viewModel: HabitViewModel = hiltViewModel()
) {
    val selectedHabitState by viewModel.selectedHabitState.collectAsState()
    val habitStatsState by viewModel.habitStatsState.collectAsState()
    // TODO: Add notesState from ViewModel
    // val notesState by viewModel.notesState.collectAsState() // Example

    LaunchedEffect(key1 = habitId) {
        viewModel.getHabitById(habitId)
        viewModel.loadHabitStats(habitId) // Changed from getHabitStats
        // TODO: viewModel.loadNotesForHabit(habitId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Habit Detail") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEditHabit(habitId) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit Habit")
                    }
                    // Potentially a Delete option in a dropdown menu
                    // OverflowMenu { /* Delete Action */ }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    BottomNavItem(icon = Icons.Default.Home, label = "Home", selected = false, onClick = onHomeClick)
                    BottomNavItem(icon = Icons.Default.CheckCircle, label = "Tasks", selected = false, onClick = onTasksClick)
                    BottomNavItem(icon = Icons.Default.Loop, label = "Habits", selected = true, onClick = onHabitsClick)
                    BottomNavItem(icon = Icons.Default.Flag, label = "Goals", selected = false, onClick = onGoalsClick)
                    BottomNavItem(icon = Icons.Default.Person, label = "Profile", selected = false, onClick = onProfileClick)
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                selectedHabitState is Resource.Loading || (selectedHabitState is Resource.Success && habitStatsState is Resource.Loading) -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                selectedHabitState is Resource.Error -> {
                    Text(
                        "Error: ${(selectedHabitState as Resource.Error<Habit?>).message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                habitStatsState is Resource.Error && selectedHabitState !is Resource.Error -> {
                     // Show habit detail even if stats fail, but indicate stats error
                    Text(
                        "Error loading stats: ${(habitStatsState as Resource.Error<HabitStats?>).message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.TopCenter).padding(16.dp).background(Color.Yellow)
                    )
                }
            }

            // Content Area
            (selectedHabitState as? Resource.Success<Habit?>)?.data?.let { habit ->
                val stats = (habitStatsState as? Resource.Success<HabitStats?>)?.data

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    HabitHeader(
                        habit = habit,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Action Buttons (Track/Skip)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { viewModel.trackHabitCompletion(habit.id) },
                            modifier = Modifier.weight(1f),
                            enabled = habit.streakData?.lastCompleted != LocalDate.now() // Example: Disable if already completed today
                        ) {
                            Icon(Icons.Filled.CheckCircleOutline, contentDescription = "Track")
                            Spacer(Modifier.width(4.dp))
                            Text("Track Completion")
                        }
                        Spacer(Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = { viewModel.skipHabitCompletion(habit.id) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.FactCheck, contentDescription = "Skip") // Or a skip-specific icon
                            Spacer(Modifier.width(4.dp))
                            Text("Skip Today")
                        }
                    }


                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        WeekCalendarView(habit = habit, modifier = Modifier.padding(bottom = 16.dp))

                        Text(
                            "Statistics",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                        )
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    StatItem(
                                        icon = Icons.Default.LocalFireDepartment,
                                        title = "Current Streak",
                                        value = habit.streakData?.current?.toString() ?: "0", // Changed from currentStreak
                                        unit = "days",
                                        modifier = Modifier.weight(1f),
                                        onClick = { onNavigateToStreakDetail(habit.id) }
                                    )
                                    StatItem(
                                        icon = Icons.Default.EmojiEvents,
                                        title = "Best Streak",
                                        value = stats?.longestStreak?.toString() ?: habit.streakData?.longest?.toString() ?: "0", // Changed from longestStreak
                                        unit = "days",
                                        modifier = Modifier.weight(1f),
                                        onClick = { onNavigateToStreakDetail(habit.id) }
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    StatItem(
                                        icon = Icons.Default.QueryStats,
                                        title = "Completion Rate",
                                        value = stats?.let { "${(it.completionRate * 100).toInt()}%" } ?: "--",
                                        unit = null,
                                        modifier = Modifier.weight(1f)
                                    )
                                     StatItem( // Total Completions
                                        icon = Icons.AutoMirrored.Filled.PlaylistAddCheck,
                                        title = "Total Completions",
                                        value = stats?.completedEntries?.toString() ?: "--",
                                        unit = "times",
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                 // Add more StatItems if needed from stats object
                                // e.g. consistency, etc.
                            }
                        }


                        Text(
                            "Notes",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                        )
                        // TODO: Notes Section - replace with actual notes list
                        // Based on notesState
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp) // Placeholder height
                                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Notes section: Implement LazyColumn for notes here.", style = MaterialTheme.typography.bodyMedium)
                            // Example:
                            // if (notesState is Resource.Success) {
                            //    val notesList = (notesState as Resource.Success<List<Note>>).data
                            //    if (notesList.isNullOrEmpty()) { Text("No notes yet.") }
                            //    else { NotesList(notesList) }
                            // } else if (notesState is Resource.Loading) { CircularProgressIndicator() }
                            // else { Text("Error loading notes.") }
                        }
                        Button(
                            onClick = { /* TODO: Navigate to Add/Edit Note Screen */ },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Icon(Icons.Filled.AddComment, contentDescription = "Add Note")
                            Spacer(Modifier.width(4.dp))
                            Text("Add Note")
                        }

                        Spacer(modifier = Modifier.height(24.dp)) // Space before delete

                        OutlinedButton(
                            onClick = { /* TODO: Show confirmation and call viewModel.deleteHabit(habit.id) */ },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Filled.DeleteForever, contentDescription = "Delete Habit")
                            Spacer(Modifier.width(8.dp))
                            Text("Delete Habit")
                        }
                         Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            } ?: run { // Added 'run' for the 'else' part of the 'let'
                 if (selectedHabitState !is Resource.Loading && habitStatsState !is Resource.Loading) {
                    // This case handles when habit is null even after success, or initial state before loading
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "Habit not found or still loading.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } // End of Box
    } // End of Scaffold
}

/* TODO:
@Composable
fun NotesList(notes: List<Note>) {
    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)) { // Limit height
        items(notes) { note ->
            NoteCard(note = note)
            Divider()
        }
    }
}

@Composable
fun NoteCard(note: Note) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Created: ${note.createdAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))}", // Example format
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
*/

// Remember to update HabitViewModel to fetch and manage notes state:
// - Add _notesState: MutableStateFlow<Resource<List<Note>>>
// - Add notesState: StateFlow<Resource<List<Note>>>
// - Add loadNotesForHabit(habitId: String) function
// - Add addNoteForHabit(habitId: String, content: String) function
// - Add deleteNote(noteId: String) function (optional for this screen directly)

// Remember to update navigation for editing habit, streak detail, and adding notes.

   