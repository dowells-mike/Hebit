package com.hebit.app.ui.screens.habits

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hebit.app.domain.model.Habit
import com.hebit.app.domain.model.HabitStats
import com.hebit.app.domain.model.Resource
import com.hebit.app.ui.components.BottomNavItem
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

// Define DayStatus enum here or in a common place
enum class DayStatus {
    PERFECT, PARTIAL, MISSED, NONE
}

// Mock data classes to be removed/replaced later
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val date: LocalDate
)

data class Suggestion(
    val id: String,
    val title: String,
    val description: String
)

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitStreakScreen(
    habitId: String,
    onNavigateBack: () -> Unit,
    onHomeClick: () -> Unit = {},
    onTasksClick: () -> Unit = {},
    onHabitsClick: () -> Unit = {},
    onGoalsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    // Inject ViewModel
    viewModel: HabitViewModel = hiltViewModel()
) {
    // Observe state from ViewModel
    val selectedHabitState by viewModel.selectedHabitState.collectAsState()
    val habitStatsState by viewModel.habitStatsState.collectAsState()
    // Observe new states
    val performanceInsightsState by viewModel.performanceInsightsState.collectAsState()
    val relatedAchievementsState by viewModel.relatedAchievementsState.collectAsState()
    val suggestionsState by viewModel.suggestionsState.collectAsState()

    // Fetch data when habitId changes
    LaunchedEffect(key1 = habitId) {
        viewModel.getHabitById(habitId) // This will trigger stats loading too
    }

    // Clean up selected state on exit
    DisposableEffect(Unit) {
        onDispose { viewModel.clearSelectedHabit() }
    }

    // Month calendar state
    val today = remember { LocalDate.now() }
    val currentMonth = remember { YearMonth.from(today) }
    var selectedMonth by remember { mutableStateOf(currentMonth) }

    // Calculate calendar data based on selected habit's history
    val calendarData = remember(selectedHabitState, selectedMonth) {
        val habit = (selectedHabitState as? Resource.Success)?.data
        val historyMap = habit?.completionHistory?.associateBy {
            it.date.toLocalDate()
        } ?: emptyMap()

        val daysInMonth = selectedMonth.lengthOfMonth()
        val firstDayOfMonth = selectedMonth.atDay(1)
        val firstDayOfWeekValue = firstDayOfMonth.dayOfWeek.value // 1 (Mon) to 7 (Sun)
        val emptyStartDays = firstDayOfWeekValue - 1

        List(daysInMonth + emptyStartDays) { index ->
            if (index < emptyStartDays) {
                 null // Placeholder for days before the 1st
            } else {
                val dayOfMonth = index - emptyStartDays + 1
                val date = selectedMonth.atDay(dayOfMonth)
                val status = when {
                    // Future days or future months
                    date.isAfter(today) -> DayStatus.NONE
                    // Check history for past/present days
                    historyMap.containsKey(date) -> {
                        if (historyMap[date]?.completed == true) DayStatus.PERFECT
                        else DayStatus.MISSED // Assuming non-completed entry means missed
                        // TODO: Add logic for PARTIAL if needed/possible
                    }
                    // Past days with no history entry
                    else -> DayStatus.MISSED
                }
                 Pair(dayOfMonth, status)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text((selectedHabitState as? Resource.Success)?.data?.title ?: "Streak Analytics") }, // Show title
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Share button - Enable when loaded
                     IconButton(
                         onClick = { /* Share analytics */ },
                         enabled = selectedHabitState is Resource.Success && habitStatsState is Resource.Success
                     ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share"
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
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Handle Loading/Error states for primary data (habit + stats)
             val isLoading = selectedHabitState is Resource.Loading || (selectedHabitState is Resource.Success && habitStatsState is Resource.Loading)
             val hasError = selectedHabitState is Resource.Error || habitStatsState is Resource.Error
             val primaryErrorMessage = (selectedHabitState as? Resource.Error)?.message ?: (habitStatsState as? Resource.Error)?.message

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (hasError) {
                 Text(
                     text = "Error: ${primaryErrorMessage ?: "Unknown error"}",
                     color = MaterialTheme.colorScheme.error,
                     modifier = Modifier.align(Alignment.Center).padding(16.dp)
                 )
            } else if (selectedHabitState is Resource.Success && habitStatsState is Resource.Success) {
                val habit = (selectedHabitState as Resource.Success<Habit?>).data
                val stats = (habitStatsState as Resource.Success<HabitStats>).data

                if (habit != null && stats != null) {
                    // Main content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                    ) {
                        // Current streak card - Use real data
                        Card(
                             modifier = Modifier
                                 .fillMaxWidth()
                                 .padding(vertical = 16.dp)
                        ) {
                             Column(
                                 modifier = Modifier
                                     .fillMaxWidth()
                                     .padding(16.dp),
                                 horizontalAlignment = Alignment.CenterHorizontally
                             ) {
                                 Text(
                                     text = "Current Streak",
                                     style = MaterialTheme.typography.titleMedium
                                 )
                                 Row(verticalAlignment = Alignment.CenterVertically) {
                                     Icon(
                                         imageVector = Icons.Default.LocalFireDepartment,
                                         contentDescription = null,
                                         tint = MaterialTheme.colorScheme.error,
                                         modifier = Modifier.size(48.dp)
                                     )
                                     Text(
                                         text = stats.currentStreak.toString(), // Use stats
                                         style = MaterialTheme.typography.displayLarge,
                                         fontWeight = FontWeight.Bold,
                                         color = MaterialTheme.colorScheme.primary
                                     )
                                     Text("days", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp))
                                 }
                                 Text(
                                     text = "Previous best: ${stats.longestStreak} days", // Use stats
                                     style = MaterialTheme.typography.bodyMedium,
                                     color = MaterialTheme.colorScheme.onSurfaceVariant
                                 )
                             }
                        }

                        // Success rate card - Use real data
                        Card(
                             modifier = Modifier
                                 .fillMaxWidth()
                                 .padding(bottom = 16.dp)
                        ) {
                             Row(
                                 modifier = Modifier
                                     .fillMaxWidth()
                                     .padding(16.dp),
                                 verticalAlignment = Alignment.CenterVertically
                             ) {
                                 Column(modifier = Modifier.weight(2f)) {
                                     Text("Success Rate", style = MaterialTheme.typography.titleMedium)
                                     Text("30-day average", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                     Text(
                                         text = "${(stats.completionRate * 100).toInt()}%", // Use stats
                                         style = MaterialTheme.typography.displayMedium,
                                         fontWeight = FontWeight.Bold
                                     )
                                 }
                                 Box(modifier = Modifier.weight(1f).aspectRatio(1f).padding(8.dp), contentAlignment = Alignment.Center) {
                                     CircularProgressIndicator(
                                         progress = { stats.completionRate }, // Use stats
                                         modifier = Modifier.fillMaxSize(),
                                         strokeWidth = 8.dp
                                     )
                                     Text("${(stats.completionRate * 100).toInt()}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                 }
                             }
                        }

                        // Points card - Placeholder for now
                         Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                             Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                 Column(Modifier.weight(1f)) {
                                     Text("Points", style = MaterialTheme.typography.titleMedium)
                                     Text("--", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                                     Text("-- points to next level", style = MaterialTheme.typography.bodySmall)
                                 }
                                 Badge { Text("-- rewards") }
                             }
                         }

                        // Month calendar
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                             // Month selector
                             Row(
                                 modifier = Modifier
                                     .fillMaxWidth()
                                     .padding(vertical = 8.dp),
                                 horizontalArrangement = Arrangement.SpaceBetween,
                                 verticalAlignment = Alignment.CenterVertically
                             ) {
                                 IconButton(onClick = { selectedMonth = selectedMonth.minusMonths(1) }) {
                                     Icon(
                                         imageVector = Icons.Default.ChevronLeft,
                                         contentDescription = "Previous Month"
                                     )
                                 }
                                 Text(
                                     text = "${selectedMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${selectedMonth.year}",
                                     style = MaterialTheme.typography.titleMedium
                                 )
                                 IconButton(onClick = { if (selectedMonth.isBefore(currentMonth)) selectedMonth = selectedMonth.plusMonths(1) }, enabled = selectedMonth.isBefore(currentMonth)) {
                                     Icon(
                                         imageVector = Icons.Default.ChevronRight,
                                         contentDescription = "Next Month"
                                     )
                                 }
                             }

                             // Days of week header
                             Row(
                                 modifier = Modifier.fillMaxWidth(),
                                 horizontalArrangement = Arrangement.SpaceEvenly
                             ) {
                                 for (dayOfWeek in listOf("M", "T", "W", "T", "F", "S", "S")) {
                                     Text(
                                         text = dayOfWeek,
                                         modifier = Modifier.weight(1f),
                                         textAlign = TextAlign.Center,
                                         style = MaterialTheme.typography.labelMedium
                                     )
                                 }
                             }

                             Spacer(modifier = Modifier.height(4.dp))

                            // Calendar grid
                            Column {
                                val chunkSize = 7
                                calendarData.chunked(chunkSize).forEach { weekData ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        for (dayData in weekData) {
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(1f)
                                                    .padding(2.dp)
                                                    .clip(RectangleShape)
                                                    .background(
                                                        when (dayData?.second) { // Access status from Pair
                                                            DayStatus.PERFECT -> MaterialTheme.colorScheme.primary
                                                            DayStatus.PARTIAL -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                                                            DayStatus.MISSED -> MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                                                            else -> Color.Transparent // NONE or null
                                                        }
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                 if (dayData != null && dayData.second != DayStatus.NONE) {
                                                     Text(
                                                         text = dayData.first.toString(), // Access day from Pair
                                                         color = if (dayData.second == DayStatus.PERFECT) Color.White else MaterialTheme.colorScheme.onBackground,
                                                         style = MaterialTheme.typography.labelSmall
                                                     )
                                                 }
                                            }
                                        }
                                        // Add spacers if row has less than 7 items (last row)
                                        repeat(chunkSize - weekData.size) {
                                             Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                                        }
                                    }
                                }
                            }

                            // Legend
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(RectangleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                                
                                Text(
                                    text = "Perfect",
                                    modifier = Modifier.padding(start = 4.dp, end = 12.dp),
                                    style = MaterialTheme.typography.labelSmall
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(RectangleShape)
                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
                                )
                                
                                Text(
                                    text = "Partial",
                                    modifier = Modifier.padding(start = 4.dp, end = 12.dp),
                                    style = MaterialTheme.typography.labelSmall
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(RectangleShape)
                                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                                )
                                
                                Text(
                                    text = "Missed",
                                    modifier = Modifier.padding(start = 4.dp),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        // Performance analysis - Observe state
                        Text("Performance Analysis", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 8.dp))
                        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(16.dp).heightIn(min = 100.dp)) {
                                when(performanceInsightsState) {
                                    is Resource.Loading -> CircularProgressIndicator()
                                    is Resource.Error -> Text("Could not load insights: ${(performanceInsightsState as Resource.Error).message}", color = MaterialTheme.colorScheme.error)
                                    is Resource.Success -> {
                                        val insights = (performanceInsightsState as Resource.Success).data
                                        if (insights.isNullOrEmpty()) {
                                            Text("No performance insights available yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        } else {
                                            // TODO: Display insights (e.g., list, chart placeholder)
                                            Column {
                                                 insights.forEach { insight ->
                                                      Text("- ${insight.insight}", style = MaterialTheme.typography.bodyMedium)
                                                 }
                                                 // Placeholder for chart
                                                  Box(modifier = Modifier.fillMaxWidth().height(150.dp).background(MaterialTheme.colorScheme.surface).padding(top = 8.dp), contentAlignment = Alignment.Center){
                                                       Text("Performance Chart (TODO)")
                                                  }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Recent achievements - Observe state
                        Text("Recent Achievements", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 8.dp))
                        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                             Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(16.dp).heightIn(min = 60.dp)) {
                                 when(relatedAchievementsState) {
                                      is Resource.Loading -> CircularProgressIndicator()
                                      is Resource.Error -> Text("Could not load achievements: ${(relatedAchievementsState as Resource.Error).message}", color = MaterialTheme.colorScheme.error)
                                      is Resource.Success -> {
                                          val achievements = (relatedAchievementsState as Resource.Success).data
                                          if (achievements.isNullOrEmpty()) {
                                              Text("No related achievements unlocked yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                          } else {
                                              // TODO: Use AchievementCard with HabitAchievement data
                                               Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    achievements.forEach { ach ->
                                                        Text("${ach.title} - Earned: ${ach.earnedDate?.format(DateTimeFormatter.ISO_DATE) ?: "N/A"}")
                                                    }
                                               }
                                          }
                                      }
                                 }
                             }
                        }

                        // Suggestions - Observe state
                        Text("Suggestions", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 8.dp))
                         Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                              Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(16.dp).heightIn(min = 60.dp)) {
                                   when(suggestionsState) {
                                        is Resource.Loading -> CircularProgressIndicator()
                                        is Resource.Error -> Text("Could not load suggestions: ${(suggestionsState as Resource.Error).message}", color = MaterialTheme.colorScheme.error)
                                        is Resource.Success -> {
                                            val suggestions = (suggestionsState as Resource.Success).data
                                            if (suggestions.isNullOrEmpty()) {
                                                Text("No suggestions available right now.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            } else {
                                                 // TODO: Use SuggestionCard with HabitSuggestion data
                                                 Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                      suggestions.forEach { sug ->
                                                           Text("${sug.title}: ${sug.description}")
                                                      }
                                                 }
                                            }
                                        }
                                   }
                               }
                          }

                         Spacer(modifier = Modifier.height(16.dp))

                        // Stats Summary
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    "Streak Statistics",
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Current streak", style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        "${stats.currentStreak} days",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                // ... rest of the code ...
                            }
                        }
                    }
                } else {
                    Text(
                        if (habit == null) "Habit not found." else "Failed to load habit stats.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            } // End of Success state check
        } // End of Box
    } // End of Scaffold
}

// Commented out unused functions 
/*
@Composable
fun AchievementCard(achievement: HabitAchievement) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // Card content
    }
}

@Composable
fun SuggestionCard(suggestion: HabitSuggestion) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // Card content
    }
}
*/
