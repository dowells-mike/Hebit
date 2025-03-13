package com.hebit.app.ui.screens.habits

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    habitId: String,
    onNavigateBack: () -> Unit,
    onHomeClick: () -> Unit = {},
    onTasksClick: () -> Unit = {},
    onHabitsClick: () -> Unit = {},
    onGoalsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onStreakClick: () -> Unit = {}
) {
    // Mock habit data - would come from ViewModel in real app
    val habit = remember {
        mutableStateOf(
            HabitItem(
                id = habitId,
                title = "Morning Workout",
                category = HabitCategory.FITNESS,
                frequency = HabitFrequency.DAILY,
                time = "6:00 AM",
                streak = 5,
                completedToday = false,
                description = "30 minutes of strength training followed by a 10-minute stretch routine."
            )
        )
    }
    
    // For the week view
    val currentDate = remember { LocalDate.now() }
    val weekDays = remember {
        val days = mutableListOf<DayInfo>()
        var dayIterator = currentDate.minusDays(currentDate.dayOfWeek.value.toLong() - 1)
        
        // Generate week days (Mon-Sun)
        for (i in 0 until 7) {
            val isCompleted = if (dayIterator.isBefore(currentDate)) {
                // Random completion status for past days (for the mock)
                listOf(true, true, false, true).random()
            } else if (dayIterator.isEqual(currentDate)) {
                habit.value.completedToday
            } else {
                false
            }
            
            days.add(
                DayInfo(
                    date = dayIterator,
                    isCompleted = isCompleted
                )
            )
            dayIterator = dayIterator.plusDays(1)
        }
        days
    }
    
    // For the notes section
    val notes = remember {
        mutableStateListOf(
            NoteItem(
                id = "1",
                date = LocalDate.now().minusDays(1),
                content = "Increased weights for bench press. Feeling stronger!"
            ),
            NoteItem(
                id = "2",
                date = LocalDate.now().minusDays(3),
                content = "Added 10 minutes of HIIT to my routine."
            )
        )
    }
    
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showEditMenu by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share habit */ showShareDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share"
                        )
                    }
                    
                    IconButton(onClick = { showEditMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options"
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showEditMenu,
                        onDismissRequest = { showEditMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Habit") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = { 
                                showEditMenu = false
                                // Open edit dialog
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
                        selected = false,
                        onClick = onTasksClick
                    )
                    
                    BottomNavItem(
                        icon = Icons.Default.Loop,
                        label = "Habits",
                        selected = true,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Habit header section
            HabitHeader(
                habit = habit.value,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // This Week section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "This Week",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = currentDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Days of week with completion status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weekDays.forEach { dayInfo ->
                        DayIndicator(
                            dayName = dayInfo.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                            isCompleted = dayInfo.isCompleted,
                            isToday = dayInfo.date.isEqual(currentDate)
                        )
                    }
                }
                
                // Statistics section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Current Streak
                    StatItem(
                        icon = Icons.Default.LocalFireDepartment,
                        title = "Current Streak",
                        value = "5",
                        unit = "days",
                        modifier = Modifier.weight(1f),
                        onClick = onStreakClick
                    )
                    
                    // Best Streak
                    StatItem(
                        icon = Icons.Default.EmojiEvents,
                        title = "Best Streak",
                        value = "12",
                        unit = "days",
                        modifier = Modifier.weight(1f),
                        onClick = onStreakClick
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Completion Rate
                    StatItem(
                        icon = Icons.Default.ShowChart,
                        title = "Completion Rate",
                        value = "85%",
                        unit = null,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Average Time
                    StatItem(
                        icon = Icons.Default.Schedule,
                        title = "Average Time",
                        value = "6:30",
                        unit = null,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Total Completions
                    StatItem(
                        icon = Icons.Default.CheckCircle,
                        title = "Total Completions",
                        value = "45",
                        unit = null,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Points Earned
                    StatItem(
                        icon = Icons.Default.Star,
                        title = "Points Earned",
                        value = "250",
                        unit = null,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Notes section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Notes",
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    IconButton(onClick = { showAddNoteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add note"
                        )
                    }
                }
                
                // Notes list
                if (notes.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No notes yet. Tap + to add one.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    notes.forEach { note ->
                        NoteCard(
                            note = note,
                            onDeleteNote = { noteId ->
                                notes.removeIf { it.id == noteId }
                            },
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { /* Skip for today */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Skip")
                    }
                    
                    Button(
                        onClick = { 
                            habit.value = habit.value.copy(
                                completedToday = true,
                                streak = habit.value.streak + 1
                            )
                        },
                        enabled = !habit.value.completedToday,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Complete")
                    }
                }
            }
        }
        
        // Add note dialog
        if (showAddNoteDialog) {
            AddNoteDialog(
                onDismiss = { showAddNoteDialog = false },
                onAddNote = { content ->
                    if (content.isNotBlank()) {
                        notes.add(
                            0,
                            NoteItem(
                                id = (notes.size + 1).toString(),
                                date = LocalDate.now(),
                                content = content
                            )
                        )
                        showAddNoteDialog = false
                    }
                }
            )
        }
        
        // Share dialog
        if (showShareDialog) {
            AlertDialog(
                onDismissRequest = { showShareDialog = false },
                title = { Text("Share Habit") },
                text = { 
                    Text(
                        "Share your '${habit.value.title}' streak progress (${habit.value.streak} days) with friends!"
                    ) 
                },
                confirmButton = {
                    TextButton(onClick = { 
                        showShareDialog = false
                        // Implement actual sharing
                    }) {
                        Text("Share")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showShareDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun HabitHeader(
    habit: HabitItem,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Icon and category row
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = habit.category.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = habit.category.title,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${habit.streak} days",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
            
            // Habit title
            Text(
                text = habit.title,
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            
            // Frequency and time
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when(habit.frequency) {
                        HabitFrequency.DAILY -> Icons.Default.Today
                        HabitFrequency.WEEKLY -> Icons.Default.DateRange
                        HabitFrequency.MONTHLY -> Icons.Default.CalendarToday
                        HabitFrequency.CUSTOM -> Icons.Default.Schedule
                    },
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = habit.frequency.title,
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                habit.time?.let {
                    Text(
                        text = " • $it",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Description
            habit.description?.let {
                Text(
                    text = it,
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun DayIndicator(
    dayName: String,
    isCompleted: Boolean,
    isToday: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Day name (Mon, Tue, etc.)
        Text(
            text = dayName,
            style = MaterialTheme.typography.labelMedium,
            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Day indicator circle
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    if (isCompleted) 
                        MaterialTheme.colorScheme.primary 
                    else if (isToday)
                        MaterialTheme.colorScheme.surfaceVariant
                    else 
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            } else if (isToday) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    unit: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
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
            style = MaterialTheme.typography.titleLarge,
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
fun NoteCard(
    note: NoteItem,
    onDeleteNote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
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
                    text = note.date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                IconButton(
                    onClick = { onDeleteNote(note.id) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
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

// Helper classes
data class DayInfo(
    val date: LocalDate,
    val isCompleted: Boolean
)

data class NoteItem(
    val id: String,
    val date: LocalDate,
    val content: String
)
