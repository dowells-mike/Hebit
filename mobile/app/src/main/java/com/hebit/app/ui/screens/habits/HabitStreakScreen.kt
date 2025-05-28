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
import com.hebit.app.domain.model.HabitAchievement
import com.hebit.app.domain.model.HabitPerformanceInsight
import com.hebit.app.domain.model.HabitStats
import com.hebit.app.domain.model.HabitSuggestion
import com.hebit.app.domain.model.Resource
import com.hebit.app.ui.components.BottomNavItem
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

// DayStatus enum for calendar visualization
enum class DayStatus {
    COMPLETED, // Simplified from PERFECT
    SKIPPED,   // If we can determine skipped days from HabitCompletionRecord
    MISSED,
    NONE       // For future dates or empty cells
}

// Mock data classes (to be replaced by actual domain models from Achievement feature later)
// data class Achievement( ... ) - Keep commented or remove if not immediately used for UI structure
// data class Suggestion( ... ) - Keep commented or remove

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitStreakScreen(
    habitId: String,
    onNavigateBack: () -> Unit,
    // Navigation lambdas for bottom bar
    onHomeClick: () -> Unit = {},
    onTasksClick: () -> Unit = {},
    onHabitsClick: () -> Unit = {},
    onGoalsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    viewModel: HabitViewModel = hiltViewModel()
) {
    val selectedHabitState by viewModel.selectedHabitState.collectAsState()
    val habitStatsState by viewModel.habitStatsState.collectAsState()

    // Placeholder states - these will need actual implementation in ViewModel and Repository
    val performanceInsightsState by viewModel.performanceInsightsState.collectAsState()
    val relatedAchievementsState by viewModel.relatedAchievementsState.collectAsState()
    val suggestionsState by viewModel.suggestionsState.collectAsState()

    LaunchedEffect(key1 = habitId) {
        viewModel.getHabitById(habitId) // This should also trigger a call to loadHabitStats in the VM
        // viewModel.loadPerformanceInsights(habitId) // Example: if these are separate calls
        // viewModel.loadRelatedAchievements(habitId)
        // viewModel.loadSuggestionsForHabit(habitId)
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearSelectedHabit() }
    }

    val today = remember { LocalDate.now() }
    var selectedMonth by remember { mutableStateOf(YearMonth.from(today)) }
    val currentMonth = YearMonth.from(today)

    val calendarData = remember(selectedHabitState, selectedMonth) {
        val habit = (selectedHabitState as? Resource.Success<Habit?>)?.data
        val historyMap = habit?.completionHistory?.associateBy {
            it.date // Already LocalDate from HabitCompletionRecord
        } ?: emptyMap()

        val daysInMonth = selectedMonth.lengthOfMonth()
        val firstDayOfMonth = selectedMonth.atDay(1)
        // Adjust for Locale, e.g. using WeekFields.firstDayOfWeek
        val firstDayOfWeekValue = firstDayOfMonth.dayOfWeek.value // Monday=1, Sunday=7
        val emptyStartDays = (firstDayOfWeekValue - 1 + 7) % 7 // Ensure it's always positive for Monday start

        List(daysInMonth + emptyStartDays) { index ->
            if (index < emptyStartDays) {
                null // Placeholder for days before the 1st of the month
            } else {
                val dayOfMonth = index - emptyStartDays + 1
                val date = selectedMonth.atDay(dayOfMonth)
                val record = historyMap[date]

                val status = when {
                    date.isAfter(today) -> DayStatus.NONE // Future dates
                    record != null -> {
                        if (record.completed) DayStatus.COMPLETED
                        else if (!record.skipReason.isNullOrBlank()) DayStatus.SKIPPED
                        else DayStatus.MISSED
                    }
                    date.isBefore(habit?.startDate ?: LocalDate.MIN) -> DayStatus.NONE // Before habit started
                    else -> DayStatus.MISSED // Past day with no record, assuming not a skip day unless backend logic implies otherwise
                }
                 Pair(dayOfMonth, status)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text((selectedHabitState as? Resource.Success<Habit?>)?.data?.title ?: "Streak Analytics") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                     IconButton(
                        onClick = { /* TODO: Implement Share Analytics */ },
                         enabled = selectedHabitState is Resource.Success && habitStatsState is Resource.Success
                     ) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
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
             val isLoading = selectedHabitState is Resource.Loading || (selectedHabitState is Resource.Success && habitStatsState is Resource.Loading)
            val primaryError = selectedHabitState as? Resource.Error ?: habitStatsState as? Resource.Error

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (primaryError != null) {
                 Text(
                    text = "Error: ${primaryError.message ?: "Could not load habit details."}",
                     color = MaterialTheme.colorScheme.error,
                     modifier = Modifier.align(Alignment.Center).padding(16.dp)
                 )
            } else if (selectedHabitState is Resource.Success && habitStatsState is Resource.Success) {
                val habit = (selectedHabitState as Resource.Success<Habit?>).data
                val stats = (habitStatsState as Resource.Success<HabitStats?>).data // Assuming HabitStats can be nullable if not found

                if (habit != null && stats != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                    ) {
                        // Current Streak Display
                        Card(
                             modifier = Modifier
                                 .fillMaxWidth()
                                 .padding(vertical = 16.dp)
                        ) {
                             Column(
                                modifier = Modifier.padding(16.dp),
                                 horizontalAlignment = Alignment.CenterHorizontally
                             ) {
                                Text("Current Streak", style = MaterialTheme.typography.titleMedium)
                                 Row(verticalAlignment = Alignment.CenterVertically) {
                                     Icon(
                                        imageVector = Icons.Default.LocalFireDepartment, // Or a custom streak icon
                                        contentDescription = "Current Streak",
                                        tint = if ((habit.streakData?.current ?: 0) > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                         modifier = Modifier.size(48.dp)
                                     )
                                     Text(
                                        text = habit.streakData?.current?.toString() ?: "0",
                                         style = MaterialTheme.typography.displayLarge,
                                         fontWeight = FontWeight.Bold,
                                        color = if ((habit.streakData?.current ?: 0) > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                     )
                                    Text(" days", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp))
                                 }
                                 Text(
                                    text = "Previous best: ${habit.streakData?.longest ?: 0} days",
                                     style = MaterialTheme.typography.bodyMedium,
                                     color = MaterialTheme.colorScheme.onSurfaceVariant
                                 )
                             }
                        }

                        // Success Rate Card (using stats from HabitStats)
                        Card(
                             modifier = Modifier
                                 .fillMaxWidth()
                                 .padding(bottom = 16.dp)
                        ) {
                             Row(
                                modifier = Modifier.padding(16.dp),
                                 verticalAlignment = Alignment.CenterVertically
                             ) {
                                 Column(modifier = Modifier.weight(2f)) {
                                     Text("Success Rate", style = MaterialTheme.typography.titleMedium)
                                    // TODO: Determine how to calculate 30-day average or if overall is fine
                                    Text("Overall", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                     Text(
                                        text = "${(stats.completionRate * 100).toInt()}%",
                                         style = MaterialTheme.typography.displayMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary // Or primary
                                     )
                                 }
                                Box(
                                    modifier = Modifier.weight(1f).aspectRatio(1f).padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                     CircularProgressIndicator(
                                        progress = { stats.completionRate },
                                         modifier = Modifier.fillMaxSize(),
                                        strokeWidth = 8.dp,
                                        color = MaterialTheme.colorScheme.secondary, // Or primary
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                     )
                                    Text("${(stats.completionRate * 100).toInt()}%", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                 }
                             }
                        }

                        // Monthly Calendar View
                        Text("Monthly Progress", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 8.dp))
                         Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                 horizontalArrangement = Arrangement.SpaceBetween,
                                 verticalAlignment = Alignment.CenterVertically
                             ) {
                                 IconButton(onClick = { selectedMonth = selectedMonth.minusMonths(1) }) {
                                        Icon(Icons.Default.ChevronLeft, "Previous Month")
                                 }
                                 Text(
                                     text = "${selectedMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${selectedMonth.year}",
                                     style = MaterialTheme.typography.titleMedium
                                 )
                                    IconButton(
                                        onClick = { selectedMonth = selectedMonth.plusMonths(1) },
                                        enabled = selectedMonth.isBefore(currentMonth) // Can't go to future months beyond current
                                    ) {
                                        Icon(Icons.Default.ChevronRight, "Next Month")
                                    }
                                }
                             Row(
                                 modifier = Modifier.fillMaxWidth(),
                                 horizontalArrangement = Arrangement.SpaceEvenly
                             ) {
                                    // Use Locale to get first day of week and short names
                                    val daysOfWeek = выворотДняНедели().map { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()) }
                                    daysOfWeek.forEach { dayLabel ->
                                        Text(text = dayLabel, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                             Spacer(modifier = Modifier.height(4.dp))
                            Column {
                                val chunkSize = 7
                                calendarData.chunked(chunkSize).forEach { weekData ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                            weekData.forEach { dayData ->
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(1f)
                                                    .padding(2.dp)
                                                        .clip(CircleShape) // Changed to CircleShape for better look
                                                    .background(
                                                            when (dayData?.second) {
                                                                DayStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                                                                DayStatus.SKIPPED -> MaterialTheme.colorScheme.tertiaryContainer
                                                                DayStatus.MISSED -> MaterialTheme.colorScheme.errorContainer
                                                                else -> Color.Transparent
                                                        }
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                 if (dayData != null && dayData.second != DayStatus.NONE) {
                                                     Text(
                                                            text = dayData.first.toString(),
                                                            color = when (dayData.second) {
                                                                DayStatus.COMPLETED -> MaterialTheme.colorScheme.onPrimary
                                                                DayStatus.SKIPPED -> MaterialTheme.colorScheme.onTertiaryContainer
                                                                DayStatus.MISSED -> MaterialTheme.colorScheme.onErrorContainer
                                                                else -> MaterialTheme.colorScheme.onSurface
                                                            },
                                                         style = MaterialTheme.typography.labelSmall
                                                     )
                                                 }
                                            }
                                        }
                                            // Fill remaining space in the row if weekData.size < chunkSize
                                            for (i in 0 until (chunkSize - weekData.size)) {
                                                Spacer(Modifier.weight(1f).aspectRatio(1f))
                                            }
                                        }
                                    }
                                }
                            // Legend
                            Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp, start = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                    LegendItem(color = MaterialTheme.colorScheme.primary, text = "Completed")
                                    LegendItem(color = MaterialTheme.colorScheme.tertiaryContainer, text = "Skipped")
                                    LegendItem(color = MaterialTheme.colorScheme.errorContainer, text = "Missed")
                                }
                            }
                        }
                        
                        // Placeholder sections for Performance, Achievements, Suggestions remain similar
                        // but ensure they use the correct state objects and handle loading/error/empty states.

                        // Performance Analysis Card (Placeholder)
                        PerformanceAnalysisCard(performanceInsightsState)

                        // Related Achievements Card (Placeholder)
                        RelatedAchievementsCard(relatedAchievementsState)

                        // Suggestions Card (Placeholder)
                        SuggestionsCard(suggestionsState)

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                } else {
                    // Handle case where habit or stats is null after success (e.g. habit deleted, stats not found)
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(
                            if (habit == null) "Habit data not available." else "Could not load statistics for this habit.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } // End of main content loading check
        } // End of Box padding
    } // End of Scaffold
}

@Composable
fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
        Text(text = text, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(start = 4.dp))
    }
}

// Helper function to get days of the week starting from Locale's first day
fun выворотДняНедели(): List<java.time.DayOfWeek> {
    val firstDay = java.time.temporal.WeekFields.of(Locale.getDefault()).firstDayOfWeek
    return (0L until 7L).map { firstDay.plus(it) }
}

// Placeholder Composable for Performance Analysis
@Composable
fun PerformanceAnalysisCard(performanceInsightsState: Resource<List<HabitPerformanceInsight>>) {
    Text("Performance Analysis", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(16.dp).heightIn(min = 100.dp)) {
            when (performanceInsightsState) {
                                    is Resource.Loading -> CircularProgressIndicator()
                is Resource.Error -> Text("Insights: ${performanceInsightsState.message}", color = MaterialTheme.colorScheme.error)
                                    is Resource.Success -> {
                    val insights = performanceInsightsState.data
                                        if (insights.isNullOrEmpty()) {
                        Text("No performance insights yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        } else {
                                            Column {
                            insights.forEach { insight -> Text("- ${insight.insight}", style = MaterialTheme.typography.bodyMedium) }
                            // Chart placeholder
                            Box(modifier = Modifier.fillMaxWidth().height(150.dp).background(MaterialTheme.colorScheme.surfaceVariant).padding(top = 8.dp), contentAlignment = Alignment.Center){
                                Text("Performance Chart (Placeholder)")
                            }
                                                  }
                                            }
                                        }
                                    }
                                }
                            }
                        }

// Placeholder Composable for Related Achievements
@Composable
fun RelatedAchievementsCard(relatedAchievementsState: Resource<List<HabitAchievement>>) {
    Text("Related Achievements", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 8.dp))
                        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                             Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(16.dp).heightIn(min = 60.dp)) {
            when (relatedAchievementsState) {
                                      is Resource.Loading -> CircularProgressIndicator()
                is Resource.Error -> Text("Achievements: ${relatedAchievementsState.message}", color = MaterialTheme.colorScheme.error)
                                      is Resource.Success -> {
                    val achievements = relatedAchievementsState.data
                                          if (achievements.isNullOrEmpty()) {
                        Text("No related achievements unlocked.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                          } else {
                                               Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            achievements.forEach { ach -> Text("${ach.title} - Earned: ${ach.earnedDate?.format(DateTimeFormatter.ISO_LOCAL_DATE) ?: "Pending"}") }
                                                    }
                                               }
                                          }
                                      }
                                 }
                             }
                        }

// Placeholder Composable for Suggestions
@Composable
fun SuggestionsCard(suggestionsState: Resource<List<HabitSuggestion>>) {
    Text("Suggestions For You", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 8.dp))
                         Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                              Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(16.dp).heightIn(min = 60.dp)) {
            when (suggestionsState) {
                                        is Resource.Loading -> CircularProgressIndicator()
                is Resource.Error -> Text("Suggestions: ${suggestionsState.message}", color = MaterialTheme.colorScheme.error)
                                        is Resource.Success -> {
                    val suggestions = suggestionsState.data
                                            if (suggestions.isNullOrEmpty()) {
                        Text("No suggestions at the moment.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            } else {
                                                 Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            suggestions.forEach { sug -> Text("${sug.title}: ${sug.description}") }
                                                      }
                                                 }
                                            }
                                        }
                                   }
                               }
                          }

// Ensure HabitViewModel has: _performanceInsightsState, _relatedAchievementsState, _suggestionsState
// and functions to load data for them e.g. loadPerformanceInsights(habitId: String) etc.
// These would typically call new methods in HabitRepository.
