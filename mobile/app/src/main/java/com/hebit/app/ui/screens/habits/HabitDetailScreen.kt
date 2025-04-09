package com.hebit.app.ui.screens.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hebit.app.domain.model.Habit
import com.hebit.app.domain.model.HabitStats
import com.hebit.app.domain.model.Resource
import com.hebit.app.ui.components.BottomNavItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import com.hebit.app.ui.screens.habits.getIconByName
import com.hebit.app.ui.screens.habits.getCategoryUIFromIcon
import com.hebit.app.ui.screens.habits.getFrequencyUI

// Data classes needed for this screen
data class DayInfo(
    val date: LocalDate,
    val isCompleted: Boolean
)

data class NoteItem(
    val id: String,
    val date: LocalDate,
    val content: String
)

@Composable
fun HabitHeader(
    habit: Habit, // Use Habit domain model
    modifier: Modifier = Modifier
) {
    // Use helpers from list screen
    val categoryUI = getCategoryUIFromIcon(habit.iconName)
    val frequencyUI = getFrequencyUI(habit.frequency)
    val icon = getIconByName(habit.iconName)

    // Basic header, styling can be enhanced
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant) // Changed background
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
            Column {
                Text(
                    text = habit.title,
                    style = MaterialTheme.typography.headlineSmall, // Adjusted size
                    fontWeight = FontWeight.Bold
                )
                 Row(verticalAlignment = Alignment.CenterVertically) {
                     Text(
                        text = frequencyUI.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // Display streak
                     Text(
                         text = " • Streak: ${habit.streak}d",
                         style = MaterialTheme.typography.bodyMedium,
                         color = MaterialTheme.colorScheme.onSurfaceVariant
                     )
                 }
            }
        }

        // Description
        if (habit.description.isNotBlank()) {
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
    onStreakClick: (String) -> Unit = {},
    viewModel: HabitViewModel = hiltViewModel()
) {
    // Observe selected habit state from ViewModel
    val selectedHabitState by viewModel.selectedHabitState.collectAsState()
    // Observe habit stats state from ViewModel
    val habitStatsState by viewModel.habitStatsState.collectAsState()

    // Fetch habit details when the screen is composed or habitId changes
    LaunchedEffect(key1 = habitId) {
        viewModel.getHabitById(habitId)
    }

    val currentDate = remember { LocalDate.now() }

    // Calculate week dates and completion status from real data
    @Suppress("UNUSED_VARIABLE")
    val weekDaysWithStatus = remember(selectedHabitState) { // Recompute when state changes
        val days = mutableListOf<DayInfo>()
        val firstDayOfWeek = currentDate.minusDays(currentDate.dayOfWeek.value.toLong() - 1)
        val currentHabit = (selectedHabitState as? Resource.Success<Habit?>)?.data
        val historyMap = currentHabit?.completionHistory?.associateBy {
            it.date.toLocalDate() // Map by LocalDate for easy lookup
        } ?: emptyMap()

        for (i in 0 until 7) {
            val dateIterator = firstDayOfWeek.plusDays(i.toLong())
            // Check history map first for past/present days
            val historyEntry = historyMap[dateIterator]
            val isCompleted = historyEntry?.completed ?: false // Default to false if no entry

            days.add(
                DayInfo(
                    date = dateIterator,
                    isCompleted = isCompleted
                )
            )
        }
        days
    }

    // Mock notes - Replace with actual data fetching later
    // val notes = remember { mutableStateListOf<NoteItem>() } // Start empty, load later
    // TODO: Fetch notes for the habit

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Habit Detail") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
             // Restore BottomAppBar structure
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
                         selected = true, // Mark Habits as selected
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
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Use combined loading state check
            if (selectedHabitState is Resource.Loading || (selectedHabitState is Resource.Success && habitStatsState is Resource.Loading)) {
                 CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (selectedHabitState is Resource.Error) {
                 Text(
                     "Error: ${(selectedHabitState as Resource.Error<Habit?>).message}",
                     color = MaterialTheme.colorScheme.error,
                     modifier = Modifier.align(Alignment.Center)
                 )
            } else if (habitStatsState is Resource.Error) {
                 Text(
                     "Error loading stats: ${(habitStatsState as Resource.Error<HabitStats>).message}",
                     color = MaterialTheme.colorScheme.error,
                     modifier = Modifier.align(Alignment.Center).padding(16.dp)
                 )
            } else if (selectedHabitState is Resource.Success && habitStatsState is Resource.Success) {
                 val habit = (selectedHabitState as Resource.Success<Habit?>).data
                 val stats = (habitStatsState as Resource.Success<HabitStats>).data

                 if (habit != null && stats != null) {
                    // Main content column
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                         HabitHeader(
                             habit = habit,
                             modifier = Modifier.padding(bottom = 16.dp)
                         )

                         Column(
                             modifier = Modifier
                                 .fillMaxWidth()
                                 .padding(horizontal = 16.dp)
                         ) {
                            // ... Weekly View using weekDaysWithStatus ...

                            // Statistics section - Use real data from 'stats'
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 24.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Current Streak (already using habit.streak)
                                StatItem(
                                    icon = Icons.Default.LocalFireDepartment,
                                    title = "Current Streak",
                                    value = habit.streak.toString(),
                                    unit = "days",
                                    modifier = Modifier.weight(1f),
                                    onClick = { onStreakClick(habit.id) }
                                )

                                // Best Streak
                                StatItem(
                                    icon = Icons.Default.EmojiEvents,
                                    title = "Best Streak",
                                    value = stats.longestStreak.toString(), // Use stats data
                                    unit = "days",
                                    modifier = Modifier.weight(1f),
                                    onClick = { onStreakClick(habit.id) }
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
                                    icon = Icons.Default.QueryStats,
                                    title = "Completion Rate",
                                    value = "${(stats.completionRate * 100).toInt()}%", // Use stats data
                                    unit = null,
                                    modifier = Modifier.weight(1f)
                                )

                                // Average Time - Placeholder
                                StatItem(
                                    icon = Icons.Default.Schedule,
                                    title = "Average Time",
                                    value = "--",
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
                                    value = stats.totalCompletions.toString(), // Use stats data
                                    unit = null,
                                    modifier = Modifier.weight(1f)
                                )

                                // Points Earned - Placeholder
                                StatItem(
                                    icon = Icons.Default.Star,
                                    title = "Points Earned",
                                    value = "--",
                                    unit = null,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // ... Notes Section (Placeholder remains) ...
                            // ... Action Buttons ...
                        }
                    }
                 } else {
                     // Handle case where habit or stats are null even on success
                     Text(
                         if (habit == null) "Habit not found." else "Failed to load habit stats.",
                         modifier = Modifier.align(Alignment.Center)
                     )
                 }
            } // End of when statement
        } // End of Box
    }
}

   