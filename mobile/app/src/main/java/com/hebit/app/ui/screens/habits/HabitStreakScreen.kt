package com.hebit.app.ui.screens.habits

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.hebit.app.ui.components.BottomNavItem
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitStreakScreen(
    habitId: String,
    onNavigateBack: () -> Unit,
    onHomeClick: () -> Unit = {},
    onTasksClick: () -> Unit = {},
    onHabitsClick: () -> Unit = {},
    onGoalsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    // Mock data - would come from ViewModel in real app
    val currentStreak = remember { 28 }
    val previousBest = remember { 32 }
    val successRate = remember { 85 }
    val points = remember { 2450 }
    val pointsToNextLevel = remember { 50 }
    val rewards = remember { 2 }
    
    // Month calendar data
    val currentMonth = remember { YearMonth.now() }
    var selectedMonth by remember { mutableStateOf(currentMonth) }
    
    // Calendar completion data (mock)
    val calendarData = remember {
        val daysInMonth = selectedMonth.lengthOfMonth()
        val currentDay = LocalDate.now().dayOfMonth
        
        List(daysInMonth) { day ->
            // Only mark days up to today in the current month
            if (selectedMonth == currentMonth && day + 1 > currentDay) {
                DayStatus.NONE
            } else {
                // Random completion for the mock
                when ((0..10).random()) {
                    in 0..6 -> DayStatus.PERFECT // 70% perfect
                    in 7..8 -> DayStatus.PARTIAL // 20% partial
                    else -> DayStatus.MISSED // 10% missed
                }
            }
        }
    }
    
    // Performance data (mock)
    val bestDay = remember { "Monday" }
    val bestDaySuccess = remember { 92 }
    val peakTime = remember { "Morning" }
    val peakTimeWindow = remember { "6-8 AM" }
    
    // Achievements
    val achievements = remember {
        listOf(
            Achievement(
                id = "1",
                title = "30-Day Streak",
                description = "Personal Best Achievement",
                date = LocalDate.now().minusDays(5)
            )
        )
    }
    
    // Suggestions
    val suggestions = remember {
        listOf(
            Suggestion(
                id = "1",
                title = "Morning Routine Optimization",
                description = "Your success rate is 20% higher when you start before 7 AM."
            )
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Streak Analytics") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share analytics */ }) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Current streak
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
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        
                        Text(
                            text = "$currentStreak",
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Text(
                            text = "days",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    
                    Text(
                        text = "Previous best: $previousBest days",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Success rate
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
                    Column(
                        modifier = Modifier.weight(2f)
                    ) {
                        Text(
                            text = "Success Rate",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Text(
                            text = "30-day average",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Text(
                            text = "$successRate%",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { successRate / 100f },
                            modifier = Modifier.fillMaxSize(),
                            strokeWidth = 8.dp
                        )
                        
                        Text(
                            text = "$successRate%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Points
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Points",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Text(
                            text = "$points",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = "$pointsToNextLevel points to next level",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Badge(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = "$rewards rewards",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            // Month calendar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                // Month selector with navigation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { 
                            selectedMonth = selectedMonth.minusMonths(1)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous Month"
                        )
                    }
                    
                    Text(
                        text = "${selectedMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${selectedMonth.year}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    IconButton(
                        onClick = { 
                            if (selectedMonth.isBefore(currentMonth)) {
                                selectedMonth = selectedMonth.plusMonths(1)
                            }
                        },
                        enabled = selectedMonth.isBefore(currentMonth)
                    ) {
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
                
                // Calendar grid (simplified version)
                // In a real app, you'd calculate the actual grid layout with correct day spacing
                val weeks = (calendarData.size + 6) / 7 // Ceiling division for number of weeks
                
                for (weekIndex in 0 until weeks) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (dayOfWeek in 0 until 7) {
                            val dayIndex = weekIndex * 7 + dayOfWeek
                            
                            if (dayIndex < calendarData.size) {
                                val status = calendarData[dayIndex]
                                val dayOfMonth = dayIndex + 1
                                
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                        .clip(RectangleShape)
                                        .background(
                                            when (status) {
                                                DayStatus.PERFECT -> MaterialTheme.colorScheme.primary
                                                DayStatus.PARTIAL -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                                                DayStatus.MISSED -> MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                                                DayStatus.NONE -> Color.Transparent
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (status != DayStatus.NONE) {
                                        Text(
                                            text = "$dayOfMonth",
                                            color = if (status == DayStatus.PERFECT) Color.White else Color.Black,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            } else {
                                // Empty space for days beyond month end
                                Spacer(modifier = Modifier.weight(1f))
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
            
            // Performance analysis
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Performance Analysis",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    // Placeholder for performance chart
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Performance Chart",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Best performing days/times
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Best day: $bestDay",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Text(
                                text = "Peak time: $peakTime",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "$bestDaySuccess% success",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Text(
                                text = "$peakTimeWindow",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
            
            // Recent achievements
            Text(
                text = "Recent Achievements",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            if (achievements.isEmpty()) {
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
                            text = "No achievements yet. Keep going!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                achievements.forEach { achievement ->
                    AchievementCard(
                        achievement = achievement,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                }
            }
            
            // Suggestions
            Text(
                text = "Suggestions",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            if (suggestions.isEmpty()) {
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
                            text = "No suggestions yet. Keep tracking!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                suggestions.forEach { suggestion ->
                    SuggestionCard(
                        suggestion = suggestion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@SuppressLint("NewApi")
@Composable
fun AchievementCard(
    achievement: Achievement,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Achievement icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.titleMedium
                )
                
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = achievement.date.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun SuggestionCard(
    suggestion: Suggestion,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = suggestion.title,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Text(
                text = suggestion.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Button(
                onClick = { /* Apply suggestion */ },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Apply Suggestion")
            }
        }
    }
}

// Helper classes
enum class DayStatus {
    PERFECT, PARTIAL, MISSED, NONE
}

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
