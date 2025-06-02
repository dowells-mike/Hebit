package com.hebit.app.ui.screens.habits

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hebit.app.domain.model.Habit // Your main domain model
import com.hebit.app.domain.model.Resource
import com.hebit.app.ui.components.BottomNavItem // Assuming this exists
import java.time.LocalDate
import java.time.format.DateTimeFormatter
// Removed as they are imported via HabitScreenUtils.kt or directly from domain.model
// import com.hebit.app.domain.model.HabitFrequencyType 
// import com.hebit.app.domain.model.DayOfWeekDomain

// Import helpers from the new utility file

// Helper functions are now in HabitScreenUtils.kt and imported above.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitListScreen(
    onNavigateToCreateHabit: () -> Unit, // For FAB navigation
    onHabitClick: (String) -> Unit,      // For navigating to habit detail
    // For bottom navigation (assuming these are passed from a main NavHost)
    onHomeClick: () -> Unit,
    onTasksClick: () -> Unit,
    onGoalsClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: HabitViewModel = hiltViewModel()
) {
    val habitsResource by viewModel.habitsState.collectAsState() // All habits for the list
    val todayHabitsResource by viewModel.todayHabitsState.collectAsState() // Habits filtered for today by ViewModel

    var selectedCategoryFilter by remember { mutableStateOf<HabitCategoryUI?>(null) }

    // Progress card calculation based on today's habits from ViewModel
    val (completedTodayCount, totalTodayHabits, todayCompletionRate) = remember(todayHabitsResource) {
        when (todayHabitsResource) {
            is Resource.Success -> {
                val habitList = (todayHabitsResource as Resource.Success<List<Habit>>).data ?: emptyList()
                val completed = habitList.count {
                    // A simple check: if lastCompleted on streakData is today.
                    // For more complex "is completed for its current active period", ViewModel might need to provide more direct state.
                    it.streakData?.lastCompleted?.toLocalDate() == LocalDate.now()
                }
                val total = habitList.size
                val rate = if (total > 0) completed.toFloat() / total else 0f
                Triple(completed, total, rate)
            }
            else -> Triple(0, 0, 0f) // Loading or Error
        }
    }

    val currentDate = remember { LocalDate.now() }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMMM d, yyyy") }

    // Example of how to collect UI events for Snackbar, if needed in this screen
    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is HabitViewModel.UiEvent.ShowSnackbar -> {
                    // Show snackbar here if you have a SnackbarHostState
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Habits") },
                actions = {
                    IconButton(onClick = { /* TODO: Implement filter/sort */ }) {
                        Icon(Icons.Filled.FilterList, contentDescription = "Filter or Sort Habits")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreateHabit) {
                Icon(Icons.Filled.Add, contentDescription = "Add new habit")
            }
        },
        bottomBar = {
            BottomAppBar { // Standard BottomAppBar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavItem(icon = Icons.Default.Home, label = "Home", selected = false, onClick = onHomeClick)
                    BottomNavItem(icon = Icons.Default.CheckCircle, label = "Tasks", selected = false, onClick = onTasksClick)
                    BottomNavItem(icon = Icons.Default.Loop, label = "Habits", selected = true, onClick = { /* Current Screen */ })
                    BottomNavItem(icon = Icons.Default.Flag, label = "Goals", selected = false, onClick = onGoalsClick)
                    BottomNavItem(icon = Icons.Default.Person, label = "Profile", selected = false, onClick = onProfileClick)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from scaffold
                .padding(horizontal = 16.dp)
        ) {
            // Optional: Date display or summary
            Text(
                text = currentDate.format(dateFormatter),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            ProgressCard( // Assuming ProgressCard is defined elsewhere
                completedCount = completedTodayCount,
                totalCount = totalTodayHabits,
                completionRate = todayCompletionRate,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            CategoriesRow( // Assuming CategoriesRow is defined elsewhere
                selectedCategory = selectedCategoryFilter,
                onCategorySelected = { category ->
                    selectedCategoryFilter = if (selectedCategoryFilter == category) null else category
                },
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Habit list
            when (habitsResource) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is Resource.Success -> {
                    val allHabitsList = (habitsResource as Resource.Success<List<Habit>>).data ?: emptyList()
                    val habitsToDisplay = if (selectedCategoryFilter != null) {
                        allHabitsList.filter { getCategoryUI(it.category) == selectedCategoryFilter }
                    } else {
                        allHabitsList
                    }

                    if (habitsToDisplay.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                if (selectedCategoryFilter != null && allHabitsList.isNotEmpty()) "No habits in this category."
                                else "No habits yet. Tap '+' to add one!",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(bottom = 16.dp), // Padding for FAB
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(habitsToDisplay, key = { habit -> habit.id }) { habit ->
                                HabitItem(
                                    habit = habit,
                                    onHabitClick = { onHabitClick(habit.id) },
                                    onCompleteClick = {
                                        // Pass the inverse of current completedToday state, or let ViewModel decide
                                        // For simplicity, let's tell ViewModel to toggle or handle logic.
                                        // The onCompleteClick lambda in HabitItem is now parameterless.
                                        // The ViewModel's trackHabitCompletion can decide what 'completed' value to send.
                                        // Let's assume trackHabitCompletion in ViewModel will handle the toggle.
                                        // It knows the current state or can fetch it.
                                        // For a simple toggle for UI initiated action:
                                        viewModel.trackHabitCompletion(habit.id, !(habit.completedToday ?: false))
                                    }
                                )
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "Error: ${(habitsResource as Resource.Error<List<Habit>>).message}",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HabitItem(
    habit: Habit,
    onHabitClick: () -> Unit,
    onCompleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Use the direct 'completedToday' field from the Habit domain model
    val isCompletedToday = habit.completedToday == true

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onHabitClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f) // Allow text to take space
            ) {
                Icon(
                    imageVector = getIconByName(habit.icon),
                    contentDescription = habit.title,
                    tint = habit.color?.let { Color(android.graphics.Color.parseColor(it)) } ?: LocalContentColor.current,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            habit.color?.let { Color(android.graphics.Color.parseColor(it)).copy(alpha = 0.1f) }
                                ?: MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                        )
                        .padding(8.dp)

                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) { // Allow title to take available space
                    Text(
                        text = habit.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatHabitFrequency(habit),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if ((habit.streakData?.current ?: 0) > 0) {
                        Text(
                            text = "Streak: ${habit.streakData?.current} day(s)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp)) // Space before the button

            IconButton(
                onClick = onCompleteClick,
                modifier = Modifier.size(48.dp) // Ensure good touch target size
            ) {
                Icon(
                    imageVector = if (isCompletedToday) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = if (isCompletedToday) "Mark as incomplete" else "Mark as complete",
                    tint = if (isCompletedToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

// Placeholder for CategoriesRow - you might have your own implementation
@Composable
fun CategoriesRow(
    selectedCategory: HabitCategoryUI?,
    onCategorySelected: (HabitCategoryUI) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(HabitCategoryUI.values()) { category ->
            SuggestionChip(
                onClick = { onCategorySelected(category) },
                label = { Text(category.title) },
                icon = { Icon(category.icon, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) },
                shape = CircleShape,
                border = if (selectedCategory == category) BorderStroke(1.5.dp, category.color) else null,
                colors = if (selectedCategory == category) {
                    SuggestionChipDefaults.suggestionChipColors()
                } else {
                    SuggestionChipDefaults.suggestionChipColors()
                }
            )
        }
    }
}

// Placeholder for ProgressCard - you might have your own implementation
@Composable
fun ProgressCard(
    modifier: Modifier = Modifier,
    // date: String, // Removed as it's part of the screen now
    completedCount: Int,
    totalCount: Int,
    completionRate: Float
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Today's Progress", // Changed from date
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("$completedCount / $totalCount habits completed")
                Text("${(completionRate * 100).toInt()}%")
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { completionRate }, // Corrected lambda for progress
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// Dummy Streak Color Logic - to be refined in Theme
object HebitTheme {
    @Composable
    fun getStreakColor(streak: Int): Color {
        return when {
            streak >= 100 -> Color(0xFFD4AF37) // Gold
            streak >= 50 -> Color(0xFFC0C0C0) // Silver
            streak >= 25 -> Color(0xFFCD7F32) // Bronze
            streak >= 7 -> MaterialTheme.colorScheme.primary
            streak > 0 -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }
    }
}